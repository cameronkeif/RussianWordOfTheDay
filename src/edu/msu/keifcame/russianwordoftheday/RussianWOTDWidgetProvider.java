package edu.msu.keifcame.russianwordoftheday;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;

public class RussianWOTDWidgetProvider extends AppWidgetProvider {
	String mRussianWord    = "";
	String mDefinition     = "";
	String mPartsOfSpeech  = "";
	
	Random mIndexGenerator = new Random();
	
	private static final String REFRESH_CLICKED = "refreshButtonClick";
	
	// Initialize to be out of range
	private static int sLastWordNumber= 3000;
	
	public RussianWOTDWidgetProvider() {
	}

	 public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		final int N = appWidgetIds.length;

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i=0; i<N; i++) {
           int appWidgetId = appWidgetIds[i];
            
           // Update text views
           RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
           
           updateViews( context, views );
           
           views.setOnClickPendingIntent(R.id.imageViewRefresh, getPendingSelfIntent(context, REFRESH_CLICKED, appWidgetId));
           
           // Tell the AppWidgetManager to perform an update on the current app widget
           appWidgetManager.updateAppWidget(appWidgetId, views);
        }
	 }
	 
	 private void updateViews( Context context, RemoteViews views ) {
	    int wordNumber = mIndexGenerator.nextInt( 1999 );
	    
	    // Make sure we don't end up with the same number as last time!
	    while ( wordNumber == sLastWordNumber ) {
	       wordNumber = mIndexGenerator.nextInt( 1999 );
	    }
	    
	    sLastWordNumber = wordNumber;
	    
	    parseXML( context, wordNumber );
        
        views.setTextViewText( R.id.russianWord, mRussianWord );
        views.setTextViewText( R.id.englishDefinition, mDefinition );
        views.setTextViewText( R.id.partOfSpeech, mPartsOfSpeech );
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
	       
	    if ( REFRESH_CLICKED.equals( intent.getAction() ) ) {
           AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
           RemoteViews views = new RemoteViews( context.getPackageName(), R.layout.widget_layout );
           
           int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
           
	       views.setViewVisibility( R.id.progressBar, View.VISIBLE );
	       appWidgetManager.updateAppWidget( widgetId, views );
	       
	       updateViews( context, views );
	       views.setViewVisibility( R.id.progressBar, View.GONE );

	       appWidgetManager.updateAppWidget( widgetId, views );
	    }
	 }
	 
	 protected PendingIntent getPendingSelfIntent(Context context, String action, int appWidgetId) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        return PendingIntent.getBroadcast(context, appWidgetId, intent, 0);
    }
}
