package com.apps.interestingapps.multibackground.common;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.apps.interestingapps.multibackground.SetWallpaperActivity;

/**
 * Class to handle database creation and updates
 */
public class DatabaseHelper extends SQLiteOpenHelper {

	private static DatabaseHelper databaseHelper;
	private Context context;
	private SQLiteDatabase database;
	private static final String TAG = "DatabaseHelper";

	private String[] allColumns = { MultiBackgroundConstants.ID_COLUMN,
			MultiBackgroundConstants.IMAGE_NUMBER_COLUMN,
			MultiBackgroundConstants.PATH_COLUMN };

	private DatabaseHelper(Context context) {
		super(context, MultiBackgroundConstants.DATABASE_NAME, null,
				MultiBackgroundConstants.DATABASE_VERSION);
		this.context = context;
	}

	public static DatabaseHelper getInstance(Context context) {
		if (context == null) {
			throw new IllegalArgumentException("Context is null");
		}
		if (databaseHelper == null) {
			databaseHelper = new DatabaseHelper(context);
		}
		return databaseHelper;
	}

	/**
	 * Method called when database is created
	 */
	@Override
	public void onCreate(SQLiteDatabase database) {

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	/**
	 * Method to create a database. The database is copied from assests folder
	 * if it doesn't exists already
	 *
	 * @throws IOException
	 */
	public void createDataBase() throws IOException {
		boolean dbExist = checkDataBase();
		SQLiteDatabase tempDatabase = null;
		if (!dbExist) {
			try {
				Log.i(TAG,
						"Database file does not exists");
				tempDatabase = getReadableDatabase();
				copyDataBase();
				tempDatabase.close();
			} catch (IOException e) {
				if (tempDatabase != null) {
					tempDatabase.close();
				}
				if (database != null) {
					closeDatabase();
				}
				throw new Error("Error copying database", e);
			}
		}
	}

	/**
	 * Checks if database file exists, and it can be opened
	 *
	 * @return true if the database can be opened
	 */
	private boolean checkDataBase() {
		SQLiteDatabase checkDB = null;
		boolean dbExist = true;
		try {
			String myPath = MultiBackgroundConstants.DB_PATH
					+ MultiBackgroundConstants.DATABASE_NAME;
			checkDB = SQLiteDatabase.openDatabase(myPath, null,
					SQLiteDatabase.OPEN_READONLY);
		} catch (Exception e) {
			// Some error occurred. Override the existing database to avoid
			// errors
			dbExist = false;
		}
		if (checkDB != null) {
			String query = "SELECT * FROM SQLITE_MASTER";
			dbExist = false;
			try {
				Cursor cursor = checkDB.rawQuery(query, null);
				while (cursor.moveToNext()) {
					String tableName = cursor.getString(cursor
							.getColumnIndex("name"));
					if (tableName
							.equals(MultiBackgroundConstants.IMAGE_PATH_TABLE)) {
						dbExist = true;
						break;
					}
				}
				cursor.close();
				checkDB.close();
			} catch (Exception e) {
				if (checkDB != null) {
					checkDB.close();
				}
			}
		} else {
			dbExist = false;
		}
		Log.i(context.getClass().getName(), "DB exists: " + dbExist);
		return dbExist;
	}

	/**
	 * Copy the database file from assests to database folder of the app
	 *
	 * @throws IOException
	 */
	private void copyDataBase() throws IOException {
		// Open your local db as the input stream
		InputStream myInput = context.getAssets().open(
				MultiBackgroundConstants.DATABASE_NAME);
		// Path to the just created empty db
		String outFileName = MultiBackgroundConstants.DB_PATH
				+ MultiBackgroundConstants.DATABASE_NAME;
		;
		// Open the empty db as the output stream
		OutputStream myOutput = new FileOutputStream(outFileName);
		// transfer bytes from the inputfile to the outputfile
		byte[] buffer = new byte[1024];
		int length;
		while ((length = myInput.read(buffer)) > 0) {
			myOutput.write(buffer, 0, length);
		}
		// Close the streams
		myOutput.flush();
		myOutput.close();
		myInput.close();
		Log.i(context.getClass().getName(), "Successfully copied the file");
	}

	/**
	 * Open a connection to database in read/write mode
	 *
	 * @throws SQLException
	 */
	public void openDatabase() throws SQLException {
		// Open the database
		String myPath = MultiBackgroundConstants.DB_PATH
				+ MultiBackgroundConstants.DATABASE_NAME;
		database = SQLiteDatabase.openDatabase(myPath, null,
				SQLiteDatabase.OPEN_READWRITE);

	}

	/**
	 * Close the database if its open
	 */
	public void closeDatabase() {
		if (database != null) {
			database.close();
			database = null;
		}
	}

	public String getImagePath(int imageNumber) {
		String pathToImage = null;
		Cursor recordCursor = database.query(
				MultiBackgroundConstants.IMAGE_PATH_TABLE, allColumns,
				MultiBackgroundConstants.IMAGE_NUMBER_COLUMN + "= ?",
				new String[] { Integer.toString(imageNumber) }, null, null,
				null);
		if (recordCursor.moveToFirst()) {
			pathToImage = recordCursor.getString(recordCursor
					.getColumnIndex(MultiBackgroundConstants.PATH_COLUMN));
		}
		recordCursor.close();
		return pathToImage;
	}

	/**
	 * @return All the values that are currently present in the database
	 */
	public List<MultiBackgroundImage> getAllImages() {
		List<MultiBackgroundImage> allImages = new ArrayList<MultiBackgroundImage>();
		Cursor allValuesCursor = database.query(
				MultiBackgroundConstants.IMAGE_PATH_TABLE, allColumns, null,
				null, null, null, null);
		while (allValuesCursor.moveToNext()) {
			allImages.add(MultiBackgroundImage.newInstance(allValuesCursor));
		}
		allValuesCursor.close();
		return allImages;
	}

	public MultiBackgroundImage addMultiBackgroundImage(int imageNumber,
			String path) {
		boolean invalidInput = false;
		if (imageNumber > MultiBackgroundConstants.MAX_IMAGES) {
			Log.e(TAG, imageNumber
					+ " images are not supported. Only "
					+ MultiBackgroundConstants.MAX_IMAGES + " images are supported");
			invalidInput = true;
		}

		if (path == null || path.length() == 0) {
			Log.e(TAG, "Invalid path");
			invalidInput = true;
		}

		if (invalidInput) {
			return null;
		}

		ContentValues values = new ContentValues();
		values.put(MultiBackgroundConstants.IMAGE_NUMBER_COLUMN, imageNumber);
		values.put(MultiBackgroundConstants.PATH_COLUMN, path);
		long insertId = database.insert(
				MultiBackgroundConstants.IMAGE_PATH_TABLE, null, values);
		Log.i(TAG, "Successfully created a new MBI  with id "
				+ insertId);
		if (insertId < 0) {
			return null;
		}
		MultiBackgroundImage newMbi = new MultiBackgroundImage(insertId,
				imageNumber, path);
		return newMbi;
	}
}