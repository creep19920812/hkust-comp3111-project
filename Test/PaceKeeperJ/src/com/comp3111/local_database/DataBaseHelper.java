package com.comp3111.local_database;

import static com.comp3111.local_database.DataBaseConstants.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class DataBaseHelper extends SQLiteOpenHelper {

	// The Android's default system path of your application database.
	private static String DB_PATH = Environment.getExternalStorageDirectory()
			.toString() + "/.PaceKeeper/Database/";
    private static final int DATABASE_VERSION = 20;
	private static String DB_NAME = "pacekeeper.db";
	private SQLiteDatabase myDataBase;
	private final static String TAG = "SQLiteHelper";
	
	public DataBaseHelper(Context context) {
		super(context, DB_NAME, null, DATABASE_VERSION);
	}
	
	public void openDataBase() throws SQLException {
		String myPath = DB_PATH + DB_NAME;
		Log.i(TAG, "Fetching from " + myPath);
		// test database existence or version
		try {
			myDataBase = SQLiteDatabase.openDatabase(myPath, null,
					SQLiteDatabase.OPEN_READONLY);
			Log.d(TAG, "Database EXISTS");
			// if version is old, DB exits then delete
			/*
			 * if(myDataBase.getVersion() <
			 * myContext.getResources().getInteger(R.integer.databaseVersion)){
			 * Log.d(TAG,"Database version is old, replacing");
			 * myDataBase.close(); File file = new File(myPath); boolean deleted
			 * = file.delete(); copyDbToSDCard(); }else{ myDataBase.close(); }
			 */
		} catch (SQLException se) {
			// DB not exits code to copy database
	////	//	copyDbToSDCard();
		}
		// Open the database
		myDataBase = SQLiteDatabase.openDatabase(myPath, null,
				SQLiteDatabase.OPEN_READONLY);

	}

	public Cursor getSingleRoomInfo(String SQLITE_TABLE, String Room_number)
			throws SQLException {
		String parameters[] = { Room_number };
		// parameterized query
		Cursor mCursor = myDataBase.rawQuery("SELECT * FROM " + SQLITE_TABLE
				+ " WHERE ROOM_ID=?", parameters);
		return mCursor;
	}

	public String[] getBuildingInfo() throws SQLException {
		String[] to_return = null;
		// parameterized query
		Cursor mCursor = myDataBase.rawQuery("SELECT * FROM building", null);

		// fetch result or toast error
		if (mCursor.getCount() > 0) {
			to_return = new String[mCursor.getCount()];
			Integer i = 0;
			while (mCursor.moveToNext()) {
				to_return[i] = mCursor.getString(1);
				i++;
			}
		}
		mCursor.close();
		return to_return;
	}

	public String[] getLiftInfo() throws SQLException {
		String[] to_return = null;
		// parameterized query
		Cursor mCursor = myDataBase.rawQuery("SELECT * FROM lift", null);

		// fetch result or toast error
		if (mCursor.getCount() > 0) {
			to_return = new String[mCursor.getCount()];
			Integer i = 0;
			while (mCursor.moveToNext()) {
				to_return[i] = mCursor.getString(1);
				i++;
			}
		}
		mCursor.close();
		return to_return;
	}

	/*
	 * public ArrayList<Room_Info> getRoomInfo(String SQLITE_TABLE, String
	 * Room_number) throws SQLException { ArrayList<Room_Info> orderDetailList =
	 * new ArrayList<Room_Info>(); Cursor mCursor = myDataBase.query(true,
	 * SQLITE_TABLE, new String[] { KEY_ROWID, KEY_COMPANY, KEY_ORDER, KEY_SEQ,
	 * KEY_ITEM, KEY_DESCRIPTION, KEY_QUANTITY, KEY_PRICE,}, KEY_COMPANY + "=?"
	 * + " and " + KEY_ORDER + "=?", new String[] {company,orderNumber}, null,
	 * null, KEY_ITEM , null);
	 * 
	 * 
	 * if (mCursor.moveToFirst()) { do { OrderDetail orderDetail = new
	 * OrderDetail();
	 * orderDetail.setItem(mCursor.getString(mCursor.getColumnIndexOrThrow
	 * (KEY_ITEM)));
	 * orderDetail.setDescription(mCursor.getString(mCursor.getColumnIndexOrThrow
	 * (KEY_DESCRIPTION)));
	 * orderDetail.setQuantity(mCursor.getString(mCursor.getColumnIndexOrThrow
	 * (KEY_QUANTITY)));
	 * orderDetail.setPrice(mCursor.getString(mCursor.getColumnIndexOrThrow
	 * (KEY_PRICE))); orderDetailList.add(orderDetail); } while
	 * (mCursor.moveToNext()); } if (mCursor != null && !mCursor.isClosed()) {
	 * mCursor.close(); } ArrayList<Room_Info> orderDetailList = new
	 * ArrayList<Room_Info>(); Room_Info orderDetail = new Room_Info();
	 * orderDetailList.add(orderDetail); return orderDetailList; }
	 */

	@Override
	public void onCreate(SQLiteDatabase db) {

		String PROFILE_TABLE = "CREATE TABLE " + PRO_TABLE + " ( " 
		+ PID		+ " INTEGER PRIMARY KEY AUTOINCREMENT," 
		+ P_NAME 	+ " TEXT," 
		+ P_EMAIL	+ " TEXT," 
		+ P_AGE 	+ " INTEGER," 
		+ P_JOG 	+ " TEXT," 
		+ P_WALK	+ " TEXT," 
		+ P_SPRINT 	+ " TEXT" + ");";
		db.execSQL(PROFILE_TABLE);

		db.execSQL("INSERT INTO "+PRO_TABLE+"  Values "+
		"(null, '', '', '0', '', '', '');");

	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		Log.e(TAG, "SQLiteHelper on Open!");
		super.onOpen(db);
	}

	

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + PRO_TABLE);
		onCreate(db);
	}

	
	// FUNCTION

	public void add_achievement(String name) { // acheivement constructor
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(ACH, name);
		values.put(ISS, 0);
		values.put(REC, "0");

		db.insert(ACH_TABLE, null, values);
		db.close(); // Closing database connection
	}

	public void update_achievement(int ID, String record) { // update record
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(ISS, 1);
		values.put(REC, record);

		db.update(ACH_TABLE, values, AID + " = " + ID, null);
		db.close(); // Closing database connection
	}

	public String get_achievement_record(int id) {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(ACH, new String[] { AID, ISS, REC }, AID + "="
				+ id, null, null, null, null);
		if (cursor != null)
			cursor.moveToFirst();

		return cursor.getString(3);

	}


	public void update_profile_data(ContentValues value) {
		SQLiteDatabase db = this.getWritableDatabase();

		db.update(PRO_TABLE, value, PID + " = " + "1", null);
		db.close(); // Closing database connection
	}



	@Override
	public synchronized void close() {
		if (myDataBase != null)
			myDataBase.close();
		super.close();
	}

}

/**
 * http://www.androidhive.info/2011/11/android-sqlite-database-tutorial/
 */
