package edu.msu.keifcame.russianwordoftheday;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.RemoteViews;

public class RussianWOTDWidgetProvider extends AppWidgetProvider {
	String mRussianWord   = "";
	String mDefinition     = "";
	String mPartsOfSpeech = "";
	
	public RussianWOTDWidgetProvider() {
	}

	 public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		 final int N = appWidgetIds.length;

		 Random rand = new Random();
		 int wordNumber = rand.nextInt( 1000 );
		 
        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i=0; i<N; i++) {
            int appWidgetId = appWidgetIds[i];
            
           // Update text views
           RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
           
           parseXML( context, wordNumber );
           
           views.setTextViewText( R.id.russianWord, mRussianWord );
           views.setTextViewText( R.id.englishDefinition, mDefinition );
           views.setTextViewText( R.id.partOfSpeech, mPartsOfSpeech );
   
           // Tell the AppWidgetManager to perform an update on the current app widget
           appWidgetManager.updateAppWidget(appWidgetId, views);
        }
	 }
	 
	 /**
      * Parse the definitions xml file
      * @param wordNumber [0, 999] represents the line of the xml file
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
		              System.out.println("Start tag "+xrp.getName());
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
}
