package edu.msu.keifcame.russianwordoftheday;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.SimpleCursorTreeAdapter;

public class FavoriteWordsFragment extends WordListFragment {
   @Override
   public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
      View v = inflater.inflate( R.layout.favorite_words_fragment, null );
      mWordsList = (ExpandableListView) v.findViewById( R.id.favoriteWordsList );
      return v;
   }
   
   @Override
   public void onResume() {
      super.onResume();
      
      super.onResume();
      mDBHelper = new DatabaseHelper( getActivity() );
      
      final DatabaseHelper db = mDBHelper;

      final SimpleCursorTreeAdapter adapter = new SimpleCursorTreeAdapter( getActivity(), db.getFavoriteWordsCursor(),
            R.layout.recent_list_element,
            R.layout.recent_list_element,
            new String[] { DatabaseHelper.COLUMN_WORDS, DatabaseHelper.COLUMN_PART_OF_SPEECH },
            new int[] { R.id.russianWord, R.id.partOfSpeech },
            R.layout.recent_list_element_child, 
            new String[] { DatabaseHelper.COLUMN_DEFINITION }, 
            new int [] { R.id.text1 } ) {

         @Override
         protected Cursor getChildrenCursor( Cursor groupCursor ) {
            String s = groupCursor.getString( 1 );
            return db.getRecentWordDefinition( s );
         }
      };
      
      ( (ExpandableListView) mWordsList ).setAdapter( adapter );
   }
}
