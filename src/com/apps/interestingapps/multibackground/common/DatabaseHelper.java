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

/**
 * Class to handle database creation and updates
 */
public class DatabaseHelper extends SQLiteOpenHelper {

	private static DatabaseHelper databaseHelper;
	private Context context;
	private SQLiteDatabase database;
	private static final String TAG = "DatabaseHelper";
	private static final int TEMP_UINQUE_IMAGE_NUMBER = MultiBackgroundConstants.MAX_IMAGES + 2;
	private volatile boolean isDatabaseUpdated = false;
	private static int openConnections = 0;

	private String[] allColumns = { MultiBackgroundConstants.ID_COLUMN,
			MultiBackgroundConstants.IMAGE_NUMBER_COLUMN,
			MultiBackgroundConstants.PATH_COLUMN };

	private DatabaseHelper(Context context) {
		super(context, MultiBackgroundConstants.DATABASE_NAME, null,
				MultiBackgroundConstants.DATABASE_VERSION);
		this.context = context;
	}

	public static DatabaseHelper initializeDatabase(Context contextForDatabase) {
		DatabaseHelper databaseHelper = DatabaseHelper
				.getInstance(contextForDatabase);
		try {
			databaseHelper.createDataBase();
			databaseHelper.openDatabase();
			Log.i(TAG, "Database opened");
		} catch (IOException e) {
			Log.i(TAG, "Error occurred while opening database.");
			e.printStackTrace();
		}
		return databaseHelper;
	}

	public static DatabaseHelper getInstance(Context context) {
		if (context == null) {
			throw new IllegalArgumentException("Context is null");
		}
		if (databaseHelper == null) {
			databaseHelper = new DatabaseHelper(context);
		}
		synchronized (databaseHelper) {
			openConnections++;
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

	public boolean isDatabaseUpdated() {
		return isDatabaseUpdated;
	}

	public void setDatabaseUpdated(boolean isDatabaseUpdated) {
		this.isDatabaseUpdated = isDatabaseUpdated;
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
				Log.i(TAG, "Database file does not exists");
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
			synchronized (databaseHelper) {
				if (openConnections > 0) {
					openConnections--;
					if (openConnections == 0) {
						database.close();
						databaseHelper.close();
						database = null;
					}
				}
			}
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
				null, null, null, MultiBackgroundConstants.IMAGE_NUMBER_COLUMN);
		while (allValuesCursor.moveToNext()) {
			allImages.add(MultiBackgroundImage.newInstance(allValuesCursor));
		}
		allValuesCursor.close();
		return allImages;
	}

	/**
	 * Adds a {@link MultiBackgroundImage} to the database
	 *
	 * @param imageNumber
	 * @param path
	 * @return The {@link MultiBackgroundImage} object having the above data
	 */
	public MultiBackgroundImage addMultiBackgroundImage(int imageNumber,
			String path) {
		boolean invalidInput = false;
		if (imageNumber > MultiBackgroundConstants.MAX_IMAGES) {
			Log.e(TAG, imageNumber + " images are not supported. Only "
					+ MultiBackgroundConstants.MAX_IMAGES
					+ " images are supported");
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
		if (insertId < 0) {
			return null;
		}
		isDatabaseUpdated = true;
		MultiBackgroundImage newMbi = new MultiBackgroundImage(insertId,
				imageNumber, path);
		Log.i(TAG, "Successfully created a new MBI: " + newMbi);
		return newMbi;
	}

	/**
	 * Updates those rows in database whose image number lies within the range
	 * given by rangeOfImageNumbers. Also updates the image number of the source
	 * and target images as well
	 *
	 * The updates are done as follows:
	 *
	 * 1. Update the image number of source view's row to -1 (since all the
	 * image numbers have to unique)
	 *
	 * 2. Update the image numbers of all the rows by 1 or -1 (depending on drag
	 * was made from right to left or left to right respectively)
	 *
	 * 3. Update the image number of target view's row by 1 or -1 as above.
	 *
	 * 4. Update the image number of source view's row equal to the index given
	 * by rangeOfImageNumbers[1] or targetViewIndex
	 *
	 * @param rangeOfImageNumbers
	 * @return True if positions of images were updated correctly. False
	 *         otherwise
	 */
	public boolean updateRowWithinImageNumberRange(int[] rangeOfImageNumbers) {
		int sourceViewIndex = rangeOfImageNumbers[0];
		int targetViewIndex = rangeOfImageNumbers[1];

		int delta = calculateDeltaToChangeRowNumbers(sourceViewIndex,
				targetViewIndex);
		if (delta == 0) {
			return false;
		}
		/*
		 * TODO: Do the following operation in a transaction, so that if any
		 * operation fails, everything is rolledback.
		 */
		/*
		 * Update the image number of source view's row to some constant which
		 * is not expected to occur (since all the image numbers have to unique)
		 */
		if (updateImageNumber(sourceViewIndex, TEMP_UINQUE_IMAGE_NUMBER) != 1) {
			Log.e(TAG, "Could not update the image number of source image");
			return false;
		}
		/*
		 * Update the image numbers of all the rows by 1 or -1 (depending on
		 * drag was made from right to left or left to right respectively)
		 */
		if (!changeImageNumbersWithinRangeByDelta(rangeOfImageNumbers, delta)) {
			Log.e(TAG,
					"Could not update the image numbers of rows within the given range");
			return false;
		}

		/*
		 * Update the image number of target view's row by 1 or -1 as above.
		 */
		if (updateImageNumber(targetViewIndex, targetViewIndex + delta) != 1) {
			Log.e(TAG, "Could not update the image number of target image");
			return false;
		}

		/*
		 * Update the image number of source view's row equal to the index given
		 * by rangeOfImageNumbers[1] or targetViewIndex
		 */
		if (updateImageNumber(TEMP_UINQUE_IMAGE_NUMBER, targetViewIndex) != 1) {
			Log.e(TAG,
					"Could not update the image number of source image to traget image's image number");
			return false;
		}
		Log.d(TAG, "Successfully updated the row numbers of all the images");
		isDatabaseUpdated = true;
		return true;
	}

	/**
	 * Returns a cursor with all those rows whose image number lies withing the
	 * given range.
	 *
	 * NOTE: Close the cursor once its used.
	 *
	 * @param rangeOfImageNumbers
	 * @return
	 */
	public Cursor getRowsWithinImageNumberRange(int[] rangeOfImageNumbers) {
		if (rangeOfImageNumbers == null || rangeOfImageNumbers.length != 2) {
			throw new IllegalArgumentException(
					"Wrong input for range of Image Numbers");
		}

		int sourceViewIndex = rangeOfImageNumbers[0];
		int targetViewIndex = rangeOfImageNumbers[1];

		if (sourceViewIndex == targetViewIndex) {
			Log.w(TAG,
					"Source and Target views are same. No operation needs to be done.");
			return null;
		}
		/*
		 * We have to decrease the index of all the images, if sourceViewIndex
		 * is less than targetViewIndex which means that sourceView moved from
		 * left to right If sourceView moved from right to left, we will have to
		 * increase the index of all the images
		 */
		int smallerIndex, biggerIndex;
		String orderBy = MultiBackgroundConstants.IMAGE_NUMBER_COLUMN;
		if (sourceViewIndex < targetViewIndex) {
			smallerIndex = sourceViewIndex;
			biggerIndex = targetViewIndex;
		} else {
			smallerIndex = targetViewIndex;
			biggerIndex = sourceViewIndex;
			orderBy += " DESC";
		}
		/*
		 * Create the query like: SELECT * FROM <table-name> WHERE
		 * IMAGE_NUMBER_COLUMN > smallerIndex AND IMAGE_NUMBER_COLUMN <
		 * biggerIndex
		 */
		Cursor rangeValueCursor = database.query(
				MultiBackgroundConstants.IMAGE_PATH_TABLE, allColumns,
				MultiBackgroundConstants.IMAGE_NUMBER_COLUMN + " > ? AND "
						+ MultiBackgroundConstants.IMAGE_NUMBER_COLUMN
						+ " <  ?", new String[] {
						Integer.toString(smallerIndex),
						Integer.toString(biggerIndex) }, null, null, orderBy,
				null);
		return rangeValueCursor;
	}

	/**
	 * Return the value of Delta that should added or subtracted from the image
	 * numbers that lie within the range of given source and target indices
	 *
	 * @param sourceViewIndex
	 * @param targetViewIndex
	 * @return
	 */
	private int calculateDeltaToChangeRowNumbers(int sourceViewIndex,
			int targetViewIndex) {
		if (sourceViewIndex == targetViewIndex) {
			return 0;
		}
		return sourceViewIndex < targetViewIndex ? -1 : 1;
	}

	/**
	 * Updates the image number of a row using its current image number
	 *
	 * @param currentImageNumber
	 * @param newImageNumber
	 * @return
	 */
	private int updateImageNumber(int currentImageNumber, int newImageNumber) {
		ContentValues values = new ContentValues();
		values.put(MultiBackgroundConstants.IMAGE_NUMBER_COLUMN, newImageNumber);
		int rowsAffected = database.update(
				MultiBackgroundConstants.IMAGE_PATH_TABLE, values,
				MultiBackgroundConstants.IMAGE_NUMBER_COLUMN + "=?",
				new String[] { Integer.toString(currentImageNumber) });
		if (rowsAffected < 1) {
			Log.e(TAG, "Unable to locate a row with image number: "
					+ currentImageNumber);
		} else {
			Log.d(TAG, "Updated the image number from :" + currentImageNumber
					+ " to " + newImageNumber);
			isDatabaseUpdated = true;
		}
		return rowsAffected;
	}

	/**
	 * Returns a cursor to access the row in database that has the given image
	 * number
	 *
	 * NOTE: Close the cursor once its used.
	 *
	 * @param imageNumber
	 * @return
	 */
	private Cursor getRowByImageNumber(int imageNumber) {
		Cursor valueCursor = database.query(
				MultiBackgroundConstants.IMAGE_PATH_TABLE, allColumns,
				MultiBackgroundConstants.IMAGE_NUMBER_COLUMN + " = ? ",
				new String[] { Integer.toString(imageNumber) }, null, null,
				null, null);
		return valueCursor;
	}

	/**
	 * Method to update the image numbers of the rows that lie within the given
	 * range exclusive of the given range image numbers
	 *
	 * @param rangeOfImageNumbers
	 * @param delta
	 * @return
	 */
	private boolean
			changeImageNumbersWithinRangeByDelta(int[] rangeOfImageNumbers,
					int delta) {
		Cursor rangeValueCursor = getRowsWithinImageNumberRange(rangeOfImageNumbers);
		while (rangeValueCursor.moveToNext()) {
			int currentImageNumber = rangeValueCursor
					.getInt(rangeValueCursor
							.getColumnIndex(MultiBackgroundConstants.IMAGE_NUMBER_COLUMN));
			int newImageNumber = currentImageNumber + delta;
			if (updateImageNumber(currentImageNumber, newImageNumber) != 1) {
				Log.e(TAG,
						"Could not update the image number of intermediate images");
				return false;
			}
			isDatabaseUpdated = true;
		}
		if (rangeValueCursor != null) {
			rangeValueCursor.close();
		}
		return true;
	}

	/**
	 * Method to delete a image from database using its image number. The method
	 * also updates the image numbers of remaining rows by decreasing it by 1
	 *
	 * @param imageNumber
	 * @return
	 */
	public boolean deleteMultibackgroundImage(int imageNumber) {
		int[] rangeOfImageNumbers = { imageNumber, TEMP_UINQUE_IMAGE_NUMBER };
		/*
		 * Delete the desired row first, so that the next image's image number
		 * can be reduced by 1
		 */
		int numOfRowsAffected = database.delete(
				MultiBackgroundConstants.IMAGE_PATH_TABLE,
				MultiBackgroundConstants.IMAGE_NUMBER_COLUMN + "=?",
				new String[] { Integer.toString(imageNumber) });
		if (numOfRowsAffected < 1) {
			Log.e(TAG, "Unable to delete the row with imageNumber: "
					+ imageNumber);
			return false;
		}
		isDatabaseUpdated = true;
		/*
		 * Since a row is deleted, then we have to reduce the image numbers of
		 * all the rows that have image number greater than the image number of
		 * the deleted images
		 */
		if (!changeImageNumbersWithinRangeByDelta(rangeOfImageNumbers, -1)) {
			Log.e(TAG, "Unable to update the rows within the given range ");
			return false;
		}
		return true;
	}
}