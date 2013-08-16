package edu.msu.keifcame.russianwordoftheday;

import java.util.Random;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
	
	private static final String BASE_WIKTIONARY_URL = "http://en.wiktionary.org/wiki/";
	
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
	    
        db.close();
        
	    mRussianWord   = WordFragment.getRussianWord();
	    mDefinition    = WordFragment.getEnglishDefinition();
	    mPartsOfSpeech = WordFragment.getPartOfSpeech();
	    
	    if ( mRussianWord.isEmpty() || mDefinition.isEmpty() || mPartsOfSpeech.isEmpty() ||
	         mRussianWord == null   || mDefinition == null   || mPartsOfSpeech == null ) {
	       WordFragment.updateWord( mIndexGenerator.nextInt( WordFragment.NUMBER_OF_WORDS ), context );
	       
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
	       String wordToBlock = "";
           
           DatabaseHelper db = new DatabaseHelper( context );
           if ( !db.wordBlocked( wordToBlock ) ) {
              db.addBlockedWord( wordToBlock );
           }
           db.close();
           
           refreshViews( context, widgetId );
	    } else if ( SEARCH_CLICKED.equals( intent.getAction() ) ) {
	       String wordToSearchFor = "";
	       
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
	 
	 private void showAllBlockedWarning( RemoteViews views, Context context ) {
	    views.setTextViewText( R.id.russianWord, context.getString( R.string.all_words_blocked ) );
        views.setTextColor( R.id.russianWord, context.getResources().getColor( R.color.warning_red ) );

        views.setTextViewText( R.id.englishDefinition, context.getString( R.string.all_words_blocked_explanation ) );
        views.setTextColor( R.id.englishDefinition, context.getResources().getColor( R.color.warning_red ) );
        views.setTextViewText( R.id.partOfSpeech, "" );
	 }
}
