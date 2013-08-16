package edu.msu.keifcame.russianwordoftheday;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class WordFragment extends Fragment {
   private static final String LAST_UPDATED_TIME  = "LAST_UPDATED_TIME";
   private static final String RUSSIAN_WORD       = "RUSSIAN_WORD";
   private static final String ENGLISH_DEFINITION = "ENGLISH_DEFINITION";
   private static final String PART_OF_SPEECH     = "PART_OF_SPEECH";
   
   private static String sRussianWord;
   private static String sEnglishDefinition;
   private static String sPartOfSpeech;
   
   public  static final int NUMBER_OF_WORDS = 1999;
   
   Random mIndexGenerator = new Random();
   
   public WordFragment() {
   }

   @Override
   public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
      View v = inflater.inflate( R.layout.word_fragment, null );
      
      SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences( getActivity() );
      long lastUpdatedTime = preferences.getLong( LAST_UPDATED_TIME, 0 );
      
      sRussianWord = preferences.getString( RUSSIAN_WORD, "");
      sEnglishDefinition = preferences.getString( ENGLISH_DEFINITION, "" );
      sPartOfSpeech = preferences.getString( PART_OF_SPEECH, "" );
      
      long currentTime = System.currentTimeMillis();
      if ( currentTime - lastUpdatedTime >= 86400000 ) {
         // update
         DatabaseHelper db = new DatabaseHelper( getActivity() );
         if ( db.getNumberOfBlockedWords() >= NUMBER_OF_WORDS ) {
            showAllBlockedWarning( v, getActivity() );
         } else {
            ( (TextView) v.findViewById( R.id.russianWord ) ).setTextColor( getResources().getColor( R.color.blue ) );
            ( (TextView) v.findViewById( R.id.englishDefinition ) ).setTextColor( getResources().getColor( R.color.blue ) );
         
         
            int wordNumber = mIndexGenerator.nextInt( NUMBER_OF_WORDS );
            
            // Keep parsing the XML for a new word, until we get one that is not blocked.
            do {
               wordNumber = mIndexGenerator.nextInt( NUMBER_OF_WORDS );
                
               WordFragment.updateWord( wordNumber, getActivity() );
            } while ( sRussianWord == "" || db.wordBlocked( sRussianWord ) );
            
            db.close();
            lastUpdatedTime = currentTime;
         }
      }
      
      ( (TextView) v.findViewById( R.id.russianWord ) ).setText( sRussianWord );
      ( (TextView) v.findViewById( R.id.englishDefinition ) ).setText( sEnglishDefinition );
      ( (TextView) v.findViewById( R.id.partOfSpeech ) ).setText( sPartOfSpeech );
      
      SharedPreferences.Editor editor = preferences.edit();
      
      editor.putString( PART_OF_SPEECH, sPartOfSpeech );
      editor.putString( RUSSIAN_WORD, sRussianWord );
      editor.putString( ENGLISH_DEFINITION, sEnglishDefinition );
      editor.commit();
      return v;
   }
   
   public static void updateWord( int wordNumber, Context context ) {
      try {
         InputStream istr = context.getAssets().open("definitions.xml");
         XmlPullParserFactory factory = XmlPullParserFactory.newInstance(); 
         factory.setNamespaceAware(true); 
         XmlPullParser xrp = factory.newPullParser(); 
         xrp.setInput(istr, "UTF-8"); 
           
         int eventType = xrp.getEventType();
         int index = 0;
         while (eventType != XmlPullParser.END_DOCUMENT && index <= wordNumber ) {
            if(eventType == XmlPullParser.START_TAG) {
               sEnglishDefinition = xrp.getAttributeValue( null, "definition" );
               sPartOfSpeech = xrp.getAttributeValue( null, "parts" );
               sRussianWord = xrp.getAttributeValue( null, "word" );
               index++;
            }
            eventType = xrp.next();
         }
         
      } catch ( IOException e ) {
         e.printStackTrace();
      } catch ( XmlPullParserException e ) {
         e.printStackTrace();
      }
      
      DatabaseHelper db = new DatabaseHelper( context );
      db.addRecentWord( sRussianWord, sEnglishDefinition, sPartOfSpeech );
      
      SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences( context );
      SharedPreferences.Editor editor = preferences.edit();
      
      long lastUpdatedTime = System.currentTimeMillis();
      
      editor.putLong(   LAST_UPDATED_TIME, lastUpdatedTime );
      editor.putString( PART_OF_SPEECH, sPartOfSpeech );
      editor.putString( RUSSIAN_WORD, sRussianWord );
      editor.putString( ENGLISH_DEFINITION, sEnglishDefinition );
      editor.commit();
   }
   
   public static String getRussianWord() {
      return sRussianWord;
   }
   
   public static String getEnglishDefinition() {
      return sEnglishDefinition;
   }
   
   public static String getPartOfSpeech() {
      return sPartOfSpeech;
   }
   
   private void showAllBlockedWarning( View v, Context context ) {
      TextView russianWord = (TextView) v.findViewById( R.id.russianWord );
      TextView englishDefinition = (TextView) v.findViewById( R.id.englishDefinition );
      TextView partOfSpeech = (TextView) v.findViewById( R.id.partOfSpeech );
      
      russianWord.setText( context.getString( R.string.all_words_blocked ) );
      russianWord.setTextColor( context.getResources().getColor( R.color.warning_red ) );

      englishDefinition.setText( context.getString( R.string.all_words_blocked_explanation ) );
      englishDefinition.setTextColor( context.getResources().getColor( R.color.warning_red ) );
      
      partOfSpeech.setText( "" );
   }
}
