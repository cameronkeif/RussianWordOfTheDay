package edu.msu.keifcame.russianwordoftheday;

import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
   ListView mBlockedWordsList;
   DatabaseHelper mDBHelper;
   
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	
      mBlockedWordsList = (ListView) findViewById( R.id.blockedWordsList );
	}
	
	@Override
	public void onResume() {
	   super.onResume();
	   mDBHelper = new DatabaseHelper( this );
	   
       final DatabaseHelper db = mDBHelper;

       final CursorAdapter adapter = new CursorAdapter( this, db.getBlockedWordsCursor(), CursorAdapter.NO_SELECTION ) {
        
        @Override
        public View newView( Context context, Cursor cursor, ViewGroup parent ) {
           View v = getLayoutInflater().inflate( R.layout.blocked_list_element, null );
           bindView( v, context, cursor );
           return v;
        }
        
        @Override
        public void bindView( View v, final Context context, final Cursor cursor ) {
           final String blockedWord = cursor.getString( DatabaseHelper.BLOCKED_WORD_INDEX );

           final RelativeLayout layout = (RelativeLayout) v;
           TextView blockedWordTextView = (TextView) layout.findViewById( R.id.blockedWord );
           blockedWordTextView.setText( blockedWord );
           
           ImageButton removeButton = (ImageButton) layout.findViewById( R.id.removeButton );
           removeButton.setOnClickListener( new OnClickListener() {
              
              @Override
              public void onClick( View v ) {
                 AlertDialog.Builder builder = new AlertDialog.Builder( context );
                 builder.setTitle( getString( R.string.remove_blocked_word_dialog_title_1 ) + " " 
                 + blockedWord + " " + getString( R.string.remove_blocked_word_dialog_title_2 ) );
                 
                 builder.setPositiveButton( android.R.string.yes, new DialogInterface.OnClickListener() {
                  
                  @Override
                  public void onClick( DialogInterface dialog, int which ) {
                     db.removeWord( blockedWord );
                     MainActivity.this.updateList();
                  }
               } );
                 builder.setNegativeButton( android.R.string.no, new DialogInterface.OnClickListener() {
                  
                  @Override
                  public void onClick( DialogInterface dialog, int which ) {
                     // Do nothing, just close dialog
                  }
               } );
                 builder.show();
              }
           } );
              
        }
     };
     
     mBlockedWordsList.setAdapter( adapter );
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	private void updateList() {
	   ( (CursorAdapter) mBlockedWordsList.getAdapter() ).swapCursor( mDBHelper.getBlockedWordsCursor() );
	   updateWidgets();
	}
	
	private void updateWidgets() {
	   Intent intent = new Intent(this, RussianWOTDWidgetProvider.class);
	   intent.setAction("android.appwidget.action.APPWIDGET_UPDATE"); 
	   // Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID, 
	   // since it seems the onUpdate() is only fired on that: 
	   int ids[] = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), RussianWOTDWidgetProvider.class));
	   intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,ids); 
	   sendBroadcast(intent); 
	}
}
