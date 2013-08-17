package edu.msu.keifcame.russianwordoftheday;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.view.Menu;
import android.widget.TextView;

public class FragmentTabs extends FragmentActivity {
   private FragmentTabHost mTabHost;
   
   @SuppressWarnings ( "deprecation")
   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
      
      mTabHost = (FragmentTabHost)findViewById(android.R.id.tabhost);
      mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);
      
      mTabHost.addTab(mTabHost.newTabSpec("Current").setIndicator("Current"),
            WordFragment.class, null);
      mTabHost.addTab(mTabHost.newTabSpec("Blocked").setIndicator("Blocked"),
            BlockedWordsFragment.class, null);
      mTabHost.addTab(mTabHost.newTabSpec("Recent").setIndicator("Recent"),
            RecentWordsFragment.class, null);
      
      for ( int i = 0; i < mTabHost.getTabWidget().getChildCount(); i++) {
         mTabHost.getTabWidget().getChildAt( i ).setBackgroundDrawable( getResources().getDrawable( R.drawable.tab_selector ) );
         TextView tv = (TextView) mTabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
         tv.setTextColor( getResources().getColor( R.color.off_white ) );
      }
   }
	
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      // Inflate the menu; this adds items to the action bar if it is present.
      getMenuInflater().inflate(R.menu.activity_main, menu);
      return true;
   }
}
