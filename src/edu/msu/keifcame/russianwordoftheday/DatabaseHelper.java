package edu.msu.keifcame.russianwordoftheday;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
   private static final String TABLE_NAME = "BlockedWords";
   private static final String COLUMN_WORDS = "WORDS";
   private static final String COLUMN_ID    = "_id";
   private static final String DATABASE_NAME = "blocked.words.db";
   
   public static final int     BLOCKED_WORD_INDEX = 1;
   public DatabaseHelper( Context context) {
      super( context, DATABASE_NAME, null, 33 );
   }

   @Override
   public void onCreate( SQLiteDatabase db ) {
      try {
         db.execSQL( "CREATE TABLE " + TABLE_NAME + " (" + COLUMN_ID + " INT PRIMARY KEY, "+ COLUMN_WORDS + " VARCHAR);" );
      } catch ( Exception e ) {
         e.printStackTrace();
      }
   }

   @Override
   public void onUpgrade( SQLiteDatabase db, int oldVersion, int newVersion ) {
      db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
      onCreate(db);
   }
   
   public void addWord( String word) {
      SQLiteDatabase db = this.getWritableDatabase();
   
      ContentValues values = new ContentValues();
      values.put(COLUMN_WORDS, word );
   
      // Inserting Row
      db.insert(TABLE_NAME, null, values);
      db.close();
   }
   
   public boolean wordBlocked( String word ) {
      SQLiteDatabase db = this.getReadableDatabase();
      
      Cursor cursor = db.rawQuery("select * from " + TABLE_NAME + " where " + COLUMN_WORDS + " = ?", new String[] { word }); 
      return cursor.getCount() > 0;
   }
   
   public int getNumberOfBlockedWords() {
      return getBlockedWordsCursor().getCount();
   }
   
   public Cursor getBlockedWordsCursor() {
      SQLiteDatabase db = this.getReadableDatabase();
      
      return db.rawQuery("select * from " + TABLE_NAME, null ); 
   }
   
   public void removeWord( String word ) {
      SQLiteDatabase db = this.getReadableDatabase();
      
      db.delete( TABLE_NAME, COLUMN_WORDS + "='" + word + "'", null );
   }
}
