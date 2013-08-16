package edu.msu.keifcame.russianwordoftheday;

import java.util.Random;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.RemoteViews;

public class RussianWOTDWidgetProvider extends AppWidgetProvider {
	String mRussianWord    = "";
	String mDefinition     = "";
	String mPartsOfSpeech  = "";
	
	Random mIndexGenerator = new Random();
	
	private static final String REFRESH_CLICKED = "refreshButtonClick";
	private static final String BLOCK_CLICKED   = "blockButonClick";
	private static final String SEARCH_CLICKED  = "searchButtonClick";
	
	public RussianWOTDWidgetProvider() {
	}

	 public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		final int N = appWidgetIds.length;
		
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
	    DatabaseHelper db = new DatabaseHelper( context );
        if ( db.getNumberOfBlockedWords() >= WordFragment.NUMBER_OF_WORDS ) {
           showAllBlockedWarning( views, context );
           return;
        }
	    
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences( context );
        long lastUpdatedTime = preferences.getLong( WordFragment.LAST_UPDATED_TIME, 0 );
        long currentTime = System.currentTimeMillis();
        
        if ( currentTime - lastUpdatedTime >= 86400000 ) {
           if ( db.getNumberOfBlockedWords() >= WordFragment.NUMBER_OF_WORDS ) {
              showAllBlockedWarning( views, context );
           } else {
              views.setTextColor( R.id.russianWord, context.getResources().getColor( R.color.off_white ) );
              views.setTextColor( R.id.englishDefinition, context.getResources().getColor( R.color.off_white ) );
           
              int wordNumber = mIndexGenerator.nextInt( WordFragment.NUMBER_OF_WORDS );
              
              // Keep parsing the XML for a new word, until we get one that is not blocked.
              do {
                 wordNumber = mIndexGenerator.nextInt( WordFragment.NUMBER_OF_WORDS );
                  
                 WordFragment.updateWord( wordNumber, context );
                 
                 mRussianWord   = WordFragment.getRussianWord();
                 mDefinition    = WordFragment.getEnglishDefinition();
                 mPartsOfSpeech = WordFragment.getPartOfSpeech();
              } while ( mRussianWord == "" || db.wordBlocked( mRussianWord ) );
              
              db.close();
           }
        } else {
           mRussianWord   = WordFragment.getRussianWord();
           mDefinition    = WordFragment.getEnglishDefinition();
           mPartsOfSpeech = WordFragment.getPartOfSpeech();
        }
	    
        views.setTextViewText( R.id.russianWord, mRussianWord );
        views.setTextViewText( R.id.englishDefinition, mDefinition );
        views.setTextViewText( R.id.partOfSpeech, mPartsOfSpeech );
	 }
	 
	 @Override
	 public void onReceive( Context context, Intent intent ) {
	    super.onReceive( context, intent );
	    int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
	    
	    if ( REFRESH_CLICKED.equals( intent.getAction() ) ) {
           refreshViews( context, widgetId );
	    } else if ( BLOCK_CLICKED.equals( intent.getAction() ) ) {
           refreshViews( context, widgetId );
	    } else if ( SEARCH_CLICKED.equals( intent.getAction() ) ) {
	       
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
	 
	 private void showAllBlockedWarning( RemoteViews views, Context context ) {
	    views.setTextViewText( R.id.russianWord, context.getString( R.string.all_words_blocked ) );
        views.setTextColor( R.id.russianWord, context.getResources().getColor( R.color.warning_red ) );

        views.setTextViewText( R.id.englishDefinition, context.getString( R.string.all_words_blocked_explanation ) );
        views.setTextColor( R.id.englishDefinition, context.getResources().getColor( R.color.warning_red ) );
        views.setTextViewText( R.id.partOfSpeech, "" );
	 }
}
