package edu.msu.keifcame.russianwordoftheday;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.SimpleCursorTreeAdapter;

public class RecentWordsFragment extends WordListFragment {
   @Override
   public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
      View v = inflater.inflate( R.layout.recent_words_fragment, null );
      mWordsList = (ExpandableListView) v.findViewById( R.id.recentWordsList );
      return v;
   }
   
   @Override
   public void onResume() {
      super.onResume();
      
      super.onResume();
      mDBHelper = new DatabaseHelper( getActivity() );
      
      final DatabaseHelper db = mDBHelper;

      final SimpleCursorTreeAdapter adapter = new SimpleCursorTreeAdapter( getActivity(), db.getRecentWordsCursor(),
            android.R.layout.simple_expandable_list_item_1,
            android.R.layout.simple_expandable_list_item_1,
            new String[] { DatabaseHelper.COLUMN_WORDS },
            new int[] { android.R.id.text1 },
            android.R.layout.simple_expandable_list_item_1, 
            new String[] { DatabaseHelper.COLUMN_DEFINITION }, 
            new int [] { android.R.id.text1 } ) {

         @Override
         protected Cursor getChildrenCursor( Cursor groupCursor ) {
            String s = groupCursor.getString( 1 );
            return db.getRecentWordDefinition( s );
         }
      };
      
      ( (ExpandableListView) mWordsList ).setAdapter( adapter );
   }
}
