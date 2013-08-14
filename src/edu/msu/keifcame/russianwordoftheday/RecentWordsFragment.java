package edu.msu.keifcame.russianwordoftheday;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class RecentWordsFragment extends WordListFragment {
   @Override
   public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
      View v = inflater.inflate( R.layout.recent_words_fragment, null );
      mWordsList = (ListView) v.findViewById( R.id.recentWordsList );
      return v;
   }
}
