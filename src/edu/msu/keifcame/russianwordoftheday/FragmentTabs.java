package edu.msu.keifcame.russianwordoftheday;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.view.Menu;

public class FragmentTabs extends FragmentActivity {
   private FragmentTabHost mTabHost;
   
   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
      
      mTabHost = (FragmentTabHost)findViewById(android.R.id.tabhost);
      mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);
      
      mTabHost.addTab(mTabHost.newTabSpec("asdf").setIndicator("asdf"),
            WordFragment.class, null);
      mTabHost.addTab(mTabHost.newTabSpec("simple").setIndicator("Simple"),
            BlockedWordsFragment.class, null);
      mTabHost.addTab(mTabHost.newTabSpec("contacts").setIndicator("Contacts"),
            RecentWordsFragment.class, null);
   }
	
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      // Inflate the menu; this adds items to the action bar if it is present.
      getMenuInflater().inflate(R.menu.activity_main, menu);
      return true;
   }
}
