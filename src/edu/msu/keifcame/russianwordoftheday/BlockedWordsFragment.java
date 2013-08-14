package edu.msu.keifcame.russianwordoftheday;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class BlockedWordsFragment extends WordListFragment {
   public BlockedWordsFragment() {
      
   }

   @Override
   public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
      View v = inflater.inflate( R.layout.blocked_words_fragment, null );
      mWordsList = (ListView) v.findViewById( R.id.blockedWordsList );
      return v;
   }
   
   @Override
   public void onResume() {
      super.onResume();
      mDBHelper = new DatabaseHelper( getActivity() );
      
      final DatabaseHelper db = mDBHelper;

      final CursorAdapter adapter = new CursorAdapter( getActivity(), db.getRecentWordsCursor(), CursorAdapter.NO_SELECTION ) {
       
       @Override
       public View newView( Context context, Cursor cursor, ViewGroup parent ) {
          View v = getActivity().getLayoutInflater().inflate( R.layout.blocked_list_element, null );
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
                    BlockedWordsFragment.this.updateList();
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
    
    mWordsList.setAdapter( adapter );
   }
}
