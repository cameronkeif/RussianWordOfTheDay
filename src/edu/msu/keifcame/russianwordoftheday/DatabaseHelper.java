package edu.msu.keifcame.russianwordoftheday;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
   private static final String DATABASE_NAME       = "blocked.words.db";
   
   public static final String TABLE_NAME_BLOCKED   = "BlockedWords";
   public static final String COLUMN_WORDS         = "WORDS";
   public static final String COLUMN_ID            = "_id";
   
   public static final String TABLE_NAME_RECENT     = "RecentWords";
   public static final String COLUMN_DEFINITION     = "Defintion";
   public static final String COLUMN_PART_OF_SPEECH = "PartOfSpeech";
   
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
                     COLUMN_DEFINITION + " VARCHAR, " +
                     COLUMN_PART_OF_SPEECH + " VARCHAR);" );
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
   
   public void addRecentWord( String word, String definition, String partOfSpeech ) {
      SQLiteDatabase db = this.getWritableDatabase();
   
      ContentValues values = new ContentValues();
      values.put( COLUMN_WORDS, word );
      values.put( COLUMN_DEFINITION, definition );
      values.put( COLUMN_PART_OF_SPEECH, partOfSpeech );
   
      // Inserting Row
      db.insert(TABLE_NAME_RECENT, null, values);
      db.close();
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
      
      return db.rawQuery("select * from " + TABLE_NAME_RECENT + " order by " + COLUMN_ID + " desc limit 100", null ); 
   }
   
   public Cursor getMostRecentWordCursor() {
      SQLiteDatabase db = this.getReadableDatabase();
      
      return db.rawQuery("select * from " + TABLE_NAME_RECENT + " order by " + COLUMN_ID + " desc limit 1", null ); 
   }
   
   public Cursor getRecentWordDefinition( String word ) {
      SQLiteDatabase db = this.getReadableDatabase();
      
      return db.rawQuery("select * from " + TABLE_NAME_RECENT + " where " + COLUMN_WORDS + " = ?", new String[] { word }); 
   }
   
   public void removeWord( String word ) {
      SQLiteDatabase db = this.getReadableDatabase();
      
      db.delete( TABLE_NAME_BLOCKED, COLUMN_WORDS + "='" + word + "'", null );
   }
}
