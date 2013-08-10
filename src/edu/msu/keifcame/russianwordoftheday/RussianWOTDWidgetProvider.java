package edu.msu.keifcame.russianwordoftheday;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Random;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.RemoteViews;

public class RussianWOTDWidgetProvider extends AppWidgetProvider {
	String mRussianWord    = "наступить";
	String mDefinition     = "to begin; act on the offensive";
	String mPartsOfSpeech  = "verb";
	
	Random mIndexGenerator = new Random();
	
	private static final String REFRESH_CLICKED = "refreshButtonClick";
	private static final String BLOCK_CLICKED   = "blockButonClick";
	private static final String SEARCH_CLICKED  = "searchButtonClick";
	private static final int    NUMBER_OF_WORDS = 1999;
	
	private static final String BASE_WIKTIONARY_URL = "http://en.wiktionary.org/wiki/";
	
	private static final boolean PICK = true;
	
	private static Hashtable<Integer, String> sLastShownWord;
	public RussianWOTDWidgetProvider() {
	}

	 public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		final int N = appWidgetIds.length;

		if ( sLastShownWord == null ) {
		   sLastShownWord = new Hashtable<Integer, String>();
		}
		
        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i=0; i<N; i++) {
           int appWidgetId = appWidgetIds[i];
            
           // Update text views
           RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
           
           updateViews( context, views, appWidgetId );
           
           views.setOnClickPendingIntent(R.id.imageViewRefresh, getPendingSelfIntent(context, REFRESH_CLICKED, appWidgetId));
           views.setOnClickPendingIntent(R.id.imageViewBlock, getPendingSelfIntent(context, BLOCK_CLICKED, appWidgetId));
           views.setOnClickPendingIntent(R.id.imageViewSearch, getPendingSelfIntent(context, SEARCH_CLICKED, appWidgetId));
           
           // Tell the AppWidgetManager to perform an update on the current app widget
           appWidgetManager.updateAppWidget(appWidgetId, views);
        }
	 }
	 
	 private void updateViews( Context context, RemoteViews views, int widgetId ) {
	    if ( !PICK ) {
	        views.setTextViewText( R.id.russianWord, mRussianWord );
	        views.setTextViewText( R.id.englishDefinition, mDefinition );
	        views.setTextViewText( R.id.partOfSpeech, mPartsOfSpeech );
	       return;
	    }
	    DatabaseHelper db = new DatabaseHelper( context );
	    if ( db.getNumberOfBlockedWords() >= NUMBER_OF_WORDS ) {
           views.setViewVisibility( R.id.allWordsBlockedWarning, View.VISIBLE );
           return;
        }
	    
	    int wordNumber = mIndexGenerator.nextInt( NUMBER_OF_WORDS );
	    
	    // Keep parsing the XML for a new word, until we get one that is not blocked.
	    do {
	       wordNumber = mIndexGenerator.nextInt( NUMBER_OF_WORDS );
	        
	       parseXML( context, wordNumber );
	    } while ( mRussianWord == "" || db.wordBlocked( mRussianWord ) );
	    db.close();
	    
        views.setTextViewText( R.id.russianWord, mRussianWord );
        views.setTextViewText( R.id.englishDefinition, mDefinition );
        views.setTextViewText( R.id.partOfSpeech, mPartsOfSpeech );
        
        if ( sLastShownWord == null ) {
           sLastShownWord = new Hashtable<Integer, String>();
        }
        sLastShownWord.put( widgetId, mRussianWord );
	 }
	 
	 /**
      * Parse the definitions xml file
      * @param wordNumber [0, 1998] represents the line of the xml file
      */
	 private void parseXML( Context context, int wordNumber ) {
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
		         mDefinition = xrp.getAttributeValue( null, "definition" );
		         mPartsOfSpeech = xrp.getAttributeValue( null, "parts" );
		         mRussianWord = xrp.getAttributeValue( null, "word" );
		         index++;
		      }
		      eventType = xrp.next();
		   }
		   
	    } catch ( IOException e ) {
	       e.printStackTrace();
	    } catch ( XmlPullParserException e ) {
	       e.printStackTrace();
		}
	 }
	 
	 @Override
	 public void onReceive( Context context, Intent intent ) {
	    super.onReceive( context, intent );
	    int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
	    
	    if ( REFRESH_CLICKED.equals( intent.getAction() ) ) {
           refreshViews( context, widgetId );
	    } else if ( BLOCK_CLICKED.equals( intent.getAction() ) ) {
	       String wordToBlock = "";
           
           if ( sLastShownWord != null ) {
              wordToBlock = sLastShownWord.get( widgetId );
           }
           
           DatabaseHelper db = new DatabaseHelper( context );
           if ( !db.wordBlocked( wordToBlock ) ) {
              db.addWord( wordToBlock );
           }
           db.close();
           
           refreshViews( context, widgetId );
	    } else if ( SEARCH_CLICKED.equals( intent.getAction() ) ) {
	       String wordToSearchFor = "";
	       if ( sLastShownWord != null ) {
              wordToSearchFor = sLastShownWord.get( widgetId );
           }
	       
	       Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(BASE_WIKTIONARY_URL + wordToSearchFor ));
	       browserIntent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
	       context.startActivity(browserIntent); 
	    }
	 }
	 
	 protected PendingIntent getPendingSelfIntent(Context context, String action, int appWidgetId) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        return PendingIntent.getBroadcast(context, appWidgetId, intent, 0);
    }
	 
	 private void refreshViews( Context context, int widgetId) {
	    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        RemoteViews views = new RemoteViews( context.getPackageName(), R.layout.widget_layout );
        
        views.setViewVisibility( R.id.progressBar, View.VISIBLE );
        appWidgetManager.updateAppWidget( widgetId, views );
        
        updateViews( context, views, widgetId );
        views.setViewVisibility( R.id.progressBar, View.GONE );

        appWidgetManager.updateAppWidget( widgetId, views );
	 }
}
