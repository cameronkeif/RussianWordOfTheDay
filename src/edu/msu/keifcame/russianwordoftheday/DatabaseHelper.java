package edu.msu.keifcame.russianwordoftheday;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
   private static final String DATABASE_NAME = "blocked.words.db";
   
   private static final String TABLE_NAME_BLOCKED   = "BlockedWords";
   private static final String COLUMN_WORDS         = "WORDS";
   private static final String COLUMN_ID            = "_id";
   
   private static final String TABLE_NAME_RECENT    = "RecentWords";
   private static final String COLUMN_DEFINITION    = "Defintion";
   
   public static final int     BLOCKED_WORD_INDEX = 1;
   public DatabaseHelper( Context context) {
      super( context, DATABASE_NAME, null, 33 );
   }

   @Override
   public void onCreate( SQLiteDatabase db ) {
      try {
         db.execSQL( "CREATE TABLE " + TABLE_NAME_BLOCKED + " (" + COLUMN_ID + " INT PRIMARY KEY, "+ COLUMN_WORDS + " VARCHAR);" );
      } catch ( Exception e ) {
         e.printStackTrace();
      }
      try {
         db.execSQL( "CREATE TABLE " + TABLE_NAME_RECENT + " (" + COLUMN_ID + " INT PRIMARY KEY, "+ COLUMN_WORDS + " VARCHAR, " +
                     COLUMN_DEFINITION + " VARCHAR);" );
      } catch ( Exception e ) {
         e.printStackTrace();
      }
   }

   @Override
   public void onUpgrade( SQLiteDatabase db, int oldVersion, int newVersion ) {
      db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_BLOCKED);
      onCreate(db);
   }
   
   public void addBlockedWord( String word) {
      SQLiteDatabase db = this.getWritableDatabase();
   
      ContentValues values = new ContentValues();
      values.put(COLUMN_WORDS, word );
   
      // Inserting Row
      db.insert(TABLE_NAME_BLOCKED, null, values);
      db.close();
   }
   
   public void addRecentWord( String word, String definition ) {
      SQLiteDatabase db = this.getWritableDatabase();
   
      if ( getRecentWordsCursor().getCount() > 100 ) {
         removeOldestWordFromRecentTable();
      }
      
      ContentValues values = new ContentValues();
      values.put( COLUMN_WORDS, word );
      values.put( COLUMN_DEFINITION, definition );
   
      // Inserting Row
      db.insert(TABLE_NAME_RECENT, null, values);
      db.close();
   }
   
   private void removeOldestWordFromRecentTable() {
      SQLiteDatabase db = getWritableDatabase();
      
      db.rawQuery("select * from " + TABLE_NAME_RECENT + " where " + COLUMN_ID + "=", new String[] { "(SELECT " + COLUMN_ID +" FROM " + TABLE_NAME_RECENT + " order by " + COLUMN_ID + " limit 1)" }); 
   }
   
   public boolean wordBlocked( String word ) {
      SQLiteDatabase db = this.getReadableDatabase();
      
      Cursor cursor = db.rawQuery("select * from " + TABLE_NAME_BLOCKED + " where " + COLUMN_WORDS + " = ?", new String[] { word }); 
      return cursor.getCount() > 0;
   }
   
   public int getNumberOfBlockedWords() {
      return getBlockedWordsCursor().getCount();
   }
   
   public Cursor getBlockedWordsCursor() {
      SQLiteDatabase db = this.getReadableDatabase();
      
      return db.rawQuery("select * from " + TABLE_NAME_BLOCKED, null ); 
   }
   
   public Cursor getRecentWordsCursor() {
      SQLiteDatabase db = this.getReadableDatabase();
      
      return db.rawQuery("select * from " + TABLE_NAME_RECENT + " order by " + COLUMN_ID + " desc", null ); 
   }
   
   public void removeWord( String word ) {
      SQLiteDatabase db = this.getReadableDatabase();
      
      db.delete( TABLE_NAME_BLOCKED, COLUMN_WORDS + "='" + word + "'", null );
   }
}
