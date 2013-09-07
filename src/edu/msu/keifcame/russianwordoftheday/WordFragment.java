package edu.msu.keifcame.russianwordoftheday;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

public class WordFragment extends Fragment {
   public static final String LAST_UPDATED_TIME  = "LAST_UPDATED_TIME";
   public static final String RUSSIAN_WORD       = "RUSSIAN_WORD";
   public static final String ENGLISH_DEFINITION = "ENGLISH_DEFINITION";
   public static final String PART_OF_SPEECH     = "PART_OF_SPEECH";
   
   private static final String BASE_WIKTIONARY_URL = "http://en.wiktionary.org/wiki/";
   
   private static String sRussianWord;
   private static String sEnglishDefinition;
   private static String sPartOfSpeech;
   
   public  static final int NUMBER_OF_WORDS = 1999;
   
   Random mIndexGenerator = new Random();
   DatabaseHelper mDatabaseHelper;
   
   public WordFragment() {
   }

   @Override
   public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
      View v = inflater.inflate( R.layout.word_fragment, null );
      mDatabaseHelper = new DatabaseHelper( getActivity() );
      
      SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences( getActivity() );

      sRussianWord = preferences.getString( RUSSIAN_WORD, "");
      sEnglishDefinition = preferences.getString( ENGLISH_DEFINITION, "" );
      sPartOfSpeech = preferences.getString( PART_OF_SPEECH, "" );
      
      long lastUpdatedTime = preferences.getLong( LAST_UPDATED_TIME, 0 );
      long currentTime = System.currentTimeMillis();
      if ( currentTime - lastUpdatedTime >= 86400000 ) {
         // update
         attemptUpdateWord();
      }

      if ( mDatabaseHelper.getNumberOfBlockedWords() < NUMBER_OF_WORDS ) {
      
         ( (TextView) v.findViewById( R.id.russianWord ) ).setText( sRussianWord );
         ( (TextView) v.findViewById( R.id.englishDefinition ) ).setText( sEnglishDefinition );
         ( (TextView) v.findViewById( R.id.partOfSpeech ) ).setText( sPartOfSpeech );
      
      } else {
         showAllBlockedWarning( v, getActivity() );
      }
      
      v.findViewById( R.id.imageViewBlock ).setOnClickListener( new OnClickListener() {
         
         @Override
         public void onClick( View v ) {
            onClickBlock( v );
         }
      } );
      
      v.findViewById( R.id.imageViewSearch ).setOnClickListener( new OnClickListener() {
         
         @Override
         public void onClick( View v ) {
            onClickSearch( v );
         }
      } );
      
      v.findViewById( R.id.imageViewRefresh ).setOnClickListener( new OnClickListener() {
         
         @Override
         public void onClick( View v ) {
            onClickRefresh( v );
         }
      } );
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
      db.close();
      
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
   
   private void updateWidgets() {
      Intent intent = new Intent(getActivity(), RussianWOTDWidgetProvider.class);
      intent.setAction("android.appwidget.action.APPWIDGET_UPDATE"); 
      int ids[] = AppWidgetManager.getInstance(getActivity().getApplication()).getAppWidgetIds(new ComponentName(getActivity().getApplication(), RussianWOTDWidgetProvider.class));
      intent.putExtra( AppWidgetManager.EXTRA_APPWIDGET_IDS, ids ); 
      getActivity().sendBroadcast(intent); 
   }
   
   public void onClickBlock( View v ) {
      if ( mDatabaseHelper.getNumberOfBlockedWords() >= NUMBER_OF_WORDS ) {
         return;
      }
      
      String wordToBlock = ( (TextView) getView().findViewById( R.id.russianWord ) ).getText().toString();
      if ( !mDatabaseHelper.wordBlocked( wordToBlock ) ) {
         mDatabaseHelper.addBlockedWord( wordToBlock );
      }
      
      if ( mDatabaseHelper.getNumberOfBlockedWords() >= NUMBER_OF_WORDS ) {
         showAllBlockedWarning( getView(), getActivity() );
      } else {
         onClickRefresh( v );
      }
   }
   
   public void onClickRefresh ( View v ) {
      if ( mDatabaseHelper.getNumberOfBlockedWords() >= NUMBER_OF_WORDS ) {
         return;
      }
      
      attemptUpdateWord();
      ( (TextView) getView().findViewById( R.id.russianWord ) ).setText( sRussianWord );
      ( (TextView) getView().findViewById( R.id.englishDefinition ) ).setText( sEnglishDefinition );
      ( (TextView) getView().findViewById( R.id.partOfSpeech ) ).setText( sPartOfSpeech );
      updateWidgets();
   }
   
   public void onClickSearch( View v ) {
      if ( mDatabaseHelper.getNumberOfBlockedWords() >= NUMBER_OF_WORDS ) {
         return;
      }
      
      Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(BASE_WIKTIONARY_URL + sRussianWord ));
      browserIntent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
      getActivity().startActivity(browserIntent); 
   }
   
   public void attemptUpdateWord() {
      DatabaseHelper db = new DatabaseHelper( getActivity() );

      if ( db.getNumberOfBlockedWords() >= NUMBER_OF_WORDS ) {
         showAllBlockedWarning( getView(), getActivity() );
         return;
      } else {
         ( (TextView) getActivity().findViewById( R.id.russianWord ) ).setTextColor( getResources().getColor( R.color.blue ) );
         ( (TextView) getActivity().findViewById( R.id.englishDefinition ) ).setTextColor( getResources().getColor( R.color.blue ) );
      
      
         int wordNumber = mIndexGenerator.nextInt( NUMBER_OF_WORDS );
         
         // Keep parsing the XML for a new word, until we get one that is not blocked.
         do {
            wordNumber = mIndexGenerator.nextInt( NUMBER_OF_WORDS );
             
            WordFragment.updateWord( wordNumber, getActivity() );
         } while ( sRussianWord == "" || db.wordBlocked( sRussianWord ) );
         db.close();
      }
   }
}
