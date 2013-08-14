package edu.msu.keifcame.russianwordoftheday;

import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.widget.ListView;

public class WordListFragment extends Fragment {
   ListView mWordsList;
   DatabaseHelper mDBHelper;
   
   public WordListFragment() {
   }

   protected void updateList() {
      ( (CursorAdapter) mWordsList.getAdapter() ).swapCursor( mDBHelper.getBlockedWordsCursor() );
   }
}
