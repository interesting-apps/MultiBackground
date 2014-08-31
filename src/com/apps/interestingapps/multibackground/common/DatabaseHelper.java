package com.apps.interestingapps.multibackground.common;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

/**
 * Class to handle database creation and updates
 */
public class DatabaseHelper extends SQLiteOpenHelper {

	private static DatabaseHelper databaseHelper;
	private Context context;
	private SQLiteDatabase database;
	private static final String TAG = "DatabaseHelper";
	private volatile boolean isDatabaseUpdated = false;
	private static int openConnections = 0;

	private String[] imagePathAllColumns = {
			MultiBackgroundConstants.ID_COLUMN,
			MultiBackgroundConstants.NEXT_IMAGE_NUMBER_COLUMN,
			MultiBackgroundConstants.PATH_COLUMN,
			MultiBackgroundConstants.IMAGE_SIZE_COLUMN };

	private String[] imageCropAllColumns = {
			MultiBackgroundConstants.ID_COLUMN,
			MultiBackgroundConstants.IMAGE_ID_COLUMN,
			MultiBackgroundConstants.CROP_LEFT_COLUMN,
			MultiBackgroundConstants.CROP_TOP_COLUMN,
			MultiBackgroundConstants.CROP_LENGTH_COLUMN,
			MultiBackgroundConstants.CROP_HEIGHT_COLUMN,
			MultiBackgroundConstants.IMAGE_OFFSET_LEFT_COLUMN,
			MultiBackgroundConstants.IMAGE_OFFSET_TOP_COLUMN };

	private String[] cropButtonDimensionsAllColumns = {
			MultiBackgroundConstants.CROP_BUTTON_LENGTH_COLUMN,
			MultiBackgroundConstants.CROP_BUTTON_HEIGHT_COLUMN };

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

	public synchronized boolean isDatabaseUpdated() {
		return isDatabaseUpdated;
	}

	public synchronized void setDatabaseUpdated(boolean isDatabaseUpdated) {
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
			checkDB = getReadableDatabase();
			// checkDB = SQLiteDatabase.openDatabase(myPath, null,
			// SQLiteDatabase.OPEN_READONLY);
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
							.equalsIgnoreCase(MultiBackgroundConstants.IMAGE_PATH_TABLE)) {
						dbExist = true;
						break;
					}
				}
				cursor.close();
				checkDB.close();
			} catch (Exception e) {
				e.printStackTrace();
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
		database.execSQL(MultiBackgroundConstants.ENABLE_FOREIGN_KEY_QUERY);
		upgradeToLatestDatabaseVersion(database);
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

	public String getImagePath(int imageId) {
		String pathToImage = null;
		Cursor recordCursor = database.query(
				MultiBackgroundConstants.IMAGE_PATH_TABLE, imagePathAllColumns,
				MultiBackgroundConstants.ID_COLUMN + "= ?",
				new String[] { Integer.toString(imageId) }, null, null, null);
		if (recordCursor.moveToFirst()) {
			pathToImage = recordCursor.getString(recordCursor
					.getColumnIndex(MultiBackgroundConstants.PATH_COLUMN));
		}
		if (recordCursor != null) {
			recordCursor.close();
		}
		return pathToImage;
	}

	/**
	 * @return All the values that are currently present in the database
	 */
	@SuppressLint("UseSparseArrays")
	public List<MultiBackgroundImage> getAllImages() {
		Cursor allValuesCursor = getAllRows();
		/*
		 * Create a map of images with nextImageNumber as the key and the
		 * corresponding MultiBackgroundImage object that contains that number
		 */
		Map<Integer, MultiBackgroundImage> nextImageNumberToMbiMap = new HashMap<Integer, MultiBackgroundImage>();
		while (allValuesCursor.moveToNext()) {
			MultiBackgroundImage newMbi = MultiBackgroundImage
					.newInstance(allValuesCursor);
			nextImageNumberToMbiMap.put(newMbi.getNextImageNumber(), newMbi);
		}
		Log.i(TAG, "total rows: " + allValuesCursor.getCount());
		allValuesCursor.close();

		return MultiBackgroundUtilities
				.getImagesFromMap(nextImageNumberToMbiMap);
	}

	public Cursor getAllRows() {
		return database.query(MultiBackgroundConstants.IMAGE_PATH_TABLE,
				imagePathAllColumns, null, null, null, null, null);
	}

	/**
	 * Adds a {@link MultiBackgroundImage} to the database
	 *
	 * Performs the operation in 2 steps:
	 *
	 * 1. Adds a new row to the database with next Image number = -1
	 *
	 * 2. Updates the row that previously had -1 in next Image number with the
	 * ID of the newly added row
	 *
	 * @param imageNumber
	 * @param path
	 * @return The {@link MultiBackgroundImage} object having the above data
	 */
	public MultiBackgroundImage addMultiBackgroundImage(String path) {
		boolean invalidInput = false;
		Cursor allValuesCursor = getAllRows();
		if (allValuesCursor != null
				&& allValuesCursor.getCount() >= MultiBackgroundConstants.MAX_IMAGES) {
			Log.e(TAG, "Only " + MultiBackgroundConstants.MAX_IMAGES
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
		MultiBackgroundImage newMbi = null;
		Cursor previousImageCursor = null;

		database.beginTransaction();
		boolean isTransactionSuccessful = false;
		try {
			/*
			 * Add the new image path to database
			 */
			ContentValues values = new ContentValues();
			values.put(MultiBackgroundConstants.NEXT_IMAGE_NUMBER_COLUMN,
					MultiBackgroundConstants.DEFAULT_NEXT_IMAGE_NUMBER);
			values.put(MultiBackgroundConstants.PATH_COLUMN, path);
			values.put(MultiBackgroundConstants.IMAGE_SIZE_COLUMN,
					MultiBackgroundImage.ImageSize.BEST_FIT.toString());
			int insertId = (int) database.insert(
					MultiBackgroundConstants.IMAGE_PATH_TABLE, null, values);
			if (insertId < 0) {
				return null;
			}
			isDatabaseUpdated = true;
			newMbi = new MultiBackgroundImage(insertId,
					MultiBackgroundConstants.DEFAULT_NEXT_IMAGE_NUMBER, path,
					MultiBackgroundImage.ImageSize.BEST_FIT);
			Log.i(TAG, "Successfully created a new MBI: " + newMbi);

			/*
			 * Update previous row that had nextImageNumber -1 to point to newly
			 * added row in database
			 */
			previousImageCursor = getRowsByNextImageNumber(MultiBackgroundConstants.DEFAULT_NEXT_IMAGE_NUMBER);
			while (previousImageCursor.moveToNext()) {
				int currentId = previousImageCursor.getInt(previousImageCursor
						.getColumnIndex(MultiBackgroundConstants.ID_COLUMN));
				if (currentId == insertId) {
					continue;
				}
				updateNextImageNumber(currentId, insertId);
			}
			database.setTransactionSuccessful();
			isTransactionSuccessful = true;
		} catch (Exception e) {
			isDatabaseUpdated = false;
		} finally {
			if (!isTransactionSuccessful) {
				isDatabaseUpdated = false;
			}
			if (previousImageCursor != null) {
				previousImageCursor.close();
			}
			database.endTransaction();
		}
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
	public boolean reorderImages(MultiBackgroundImage sourceMbi,
			MultiBackgroundImage targetMbi,
			int[] sourceTargetViewIndices) {
		int sourceIndex = sourceTargetViewIndices[0];
		int targetIndex = sourceTargetViewIndices[1];
		if (sourceIndex == targetIndex) {
			return false;
		}
		if (sourceIndex < 0 || targetIndex < 0) {
			return false;
		}

		/*
		 * 1. Update nextImageNumber of source's previous row to source's
		 * nextImageNumber
		 */
		/*
		 * Find the previous row of the sourceMbi
		 */
		database.beginTransaction();
		boolean isTransactionSuccessful = false;
		try {
			int _idOfPreviousRowOfSourceMbi = getIdWithNextImageNumber(sourceMbi
					.get_id());
			if (_idOfPreviousRowOfSourceMbi != -1) {
				if (updateNextImageNumber(_idOfPreviousRowOfSourceMbi,
						sourceMbi.getNextImageNumber()) < 1) {
					return false;
				}
				isDatabaseUpdated = true;
			}
			if (sourceIndex < targetIndex) {
				/*
				 * 1. Update source's nextImageNumber to target's
				 * nextImageNumber
				 *
				 * 2. Update the nextImageNumber of target row to source id
				 */
				if (updateNextImageNumber(sourceMbi.get_id(), targetMbi
						.getNextImageNumber()) < 1) {
					return false;
				}
				isDatabaseUpdated = true;
				if (updateNextImageNumber(targetMbi.get_id(), sourceMbi
						.get_id()) < 1) {
					return false;
				}
			} else {
				/*
				 * 1. Update nextImageNumber of target's previous row to source
				 * id
				 *
				 * 2. Update the nextImageNumber of source to target id
				 */
				int _idOfTargetsPreviousRow = getIdWithNextImageNumber(targetMbi
						.get_id());
				if (_idOfTargetsPreviousRow != -1) {
					if (updateNextImageNumber(_idOfTargetsPreviousRow,
							sourceMbi.get_id()) < 1) {
						return false;
					}
					isDatabaseUpdated = true;
				}
				if (updateNextImageNumber(sourceMbi.get_id(), targetMbi
						.get_id()) < 1) {
					return false;
				}
			}
			database.setTransactionSuccessful();
			isTransactionSuccessful = true;
		} catch (Exception e) {
			isDatabaseUpdated = false;
		} finally {
			if (!isTransactionSuccessful) {
				isDatabaseUpdated = false;
			}
			database.endTransaction();
		}
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
		String orderBy = MultiBackgroundConstants.NEXT_IMAGE_NUMBER_COLUMN;
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
				MultiBackgroundConstants.IMAGE_PATH_TABLE, imagePathAllColumns,
				MultiBackgroundConstants.NEXT_IMAGE_NUMBER_COLUMN + " > ? AND "
						+ MultiBackgroundConstants.NEXT_IMAGE_NUMBER_COLUMN
						+ " <  ?", new String[] {
						Integer.toString(smallerIndex),
						Integer.toString(biggerIndex) }, null, null, orderBy,
				null);
		return rangeValueCursor;
	}

	/**
	 * Updates the next image number for a row given by _id
	 *
	 * @param currentImageNumber
	 * @param nextImageNumber
	 * @return
	 */
	private int updateNextImageNumber(int _id, int nextImageNumber) {
		ContentValues values = new ContentValues();
		values.put(MultiBackgroundConstants.NEXT_IMAGE_NUMBER_COLUMN,
				nextImageNumber);
		int rowsAffected = database.update(
				MultiBackgroundConstants.IMAGE_PATH_TABLE, values,
				MultiBackgroundConstants.ID_COLUMN + "=?",
				new String[] { Integer.toString(_id) });
		if (rowsAffected < 1) {
			Log.e(TAG, "Unable to locate a row with id: " + _id);
		} else {
			Log.d(TAG, "Updated the next image number for row with id:" + _id
					+ " to " + nextImageNumber);
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
	 * @param nextImageNumber
	 * @return
	 */
	private Cursor getRowsByNextImageNumber(int nextImageNumber) {
		Cursor valueCursor = database.query(
				MultiBackgroundConstants.IMAGE_PATH_TABLE, imagePathAllColumns,
				MultiBackgroundConstants.NEXT_IMAGE_NUMBER_COLUMN + " = ? ",
				new String[] { Integer.toString(nextImageNumber) }, null, null,
				null, null);
		return valueCursor;
	}

	public int getIdWithNextImageNumber(int nextImageNumber) {
		Cursor valueCursor = database.query(
				MultiBackgroundConstants.IMAGE_PATH_TABLE, imagePathAllColumns,
				MultiBackgroundConstants.NEXT_IMAGE_NUMBER_COLUMN + " = ? ",
				new String[] { Integer.toString(nextImageNumber) }, null, null,
				null, null);
		int result = -1;
		if (valueCursor.moveToNext()) {
			result = valueCursor.getInt(valueCursor
					.getColumnIndex(MultiBackgroundConstants.ID_COLUMN));
		}
		if (valueCursor != null) {
			valueCursor.close();
		}
		return result;
	}

	/**
	 * Method to delete a image from database using its image number. The method
	 * also updates the image numbers of remaining rows by decreasing it by 1
	 *
	 * @param imageNumber
	 * @return
	 */
	public boolean deleteMultibackgroundImage(MultiBackgroundImage mbiToDelete) {
		int previousRowId = getIdWithNextImageNumber(mbiToDelete.get_id());
		database.beginTransaction();
		boolean isTransactionSuccessful = false;
		try {
			if (previousRowId >= 0) {
				/*
				 * Update the previous row's next image number with next image
				 * number of the row to be deleted
				 */
				if (updateNextImageNumber(previousRowId, mbiToDelete
						.getNextImageNumber()) != 1) {
					return false;
				}
			}

			/*
			 * Delete the desired row using its id
			 */
			int numOfRowsAffected = database.delete(
					MultiBackgroundConstants.IMAGE_PATH_TABLE,
					MultiBackgroundConstants.ID_COLUMN + "=?",
					new String[] { Integer.toString(mbiToDelete.get_id()) });
			if (numOfRowsAffected < 1) {
				Log.e(TAG, "Unable to delete the row with id: "
						+ mbiToDelete.get_id());
				return false;
			}
			isDatabaseUpdated = true;
			database.setTransactionSuccessful();
			isTransactionSuccessful = true;
		} catch (Exception e) {
			isDatabaseUpdated = false;
		} finally {
			if (!isTransactionSuccessful) {
				isDatabaseUpdated = false;
			}
			database.endTransaction();
		}
		return true;
	}

	public int
			updateImageSize(int _id, MultiBackgroundImage.ImageSize imageSize) {
		ContentValues values = new ContentValues();
		values.put(MultiBackgroundConstants.IMAGE_SIZE_COLUMN, imageSize
				.toString());
		int rowsAffected = database.update(
				MultiBackgroundConstants.IMAGE_PATH_TABLE, values,
				MultiBackgroundConstants.ID_COLUMN + "=?",
				new String[] { Integer.toString(_id) });
		if (rowsAffected < 1) {
			Log.e(TAG, "Unable to locate a row with id: " + _id);
		} else {
			Log.d(TAG, "Updated the ImageSize for row with id:" + _id + " to "
					+ imageSize);
			isDatabaseUpdated = true;
		}
		return rowsAffected;
	}

	public MultiBackgroundCropRectangle addCropImageData(int imageId,
			int left,
			int top,
			int length,
			int height,
			int imageOffsetLeft,
			int imageOffsetTop) {

		MultiBackgroundCropRectangle newCropRectangle = null;
		database.beginTransaction();
		boolean isTransactionSuccessful = false;
		try {
			ContentValues values = new ContentValues();
			values.put(MultiBackgroundConstants.IMAGE_ID_COLUMN, imageId);
			values.put(MultiBackgroundConstants.CROP_LEFT_COLUMN, left);
			values.put(MultiBackgroundConstants.CROP_TOP_COLUMN, top);
			values.put(MultiBackgroundConstants.CROP_LENGTH_COLUMN, length);
			values.put(MultiBackgroundConstants.CROP_HEIGHT_COLUMN, height);
			values.put(MultiBackgroundConstants.IMAGE_OFFSET_LEFT_COLUMN,
					imageOffsetLeft);
			values.put(MultiBackgroundConstants.IMAGE_OFFSET_TOP_COLUMN,
					imageOffsetTop);
			int insertId = (int) database.insert(
					MultiBackgroundConstants.IMAGE_CROP_TABLE, null, values);
			if (insertId < 0) {
				return null;
			}
			isDatabaseUpdated = true;
			database.setTransactionSuccessful();
			newCropRectangle = new MultiBackgroundCropRectangle(insertId,
					imageId, left, top, length, height, imageOffsetLeft,
					imageOffsetTop);
			Log.i(TAG, "Successfully created a new MBCR: " + newCropRectangle);
			isTransactionSuccessful = true;
		} catch (Exception e) {
			isDatabaseUpdated = false;
			Log.e(TAG, "Unable to add crop rectangle details due to: " + e);
		} finally {
			if (!isTransactionSuccessful) {
				isDatabaseUpdated = false;
			}
			database.endTransaction();
		}
		return newCropRectangle;
	}

	public int updateCropRectangleDetails(int imageId,
			int left,
			int top,
			int length,
			int height) {
		ContentValues values = new ContentValues();
		values.put(MultiBackgroundConstants.CROP_LEFT_COLUMN, left);
		values.put(MultiBackgroundConstants.CROP_TOP_COLUMN, top);
		values.put(MultiBackgroundConstants.CROP_LENGTH_COLUMN, length);
		values.put(MultiBackgroundConstants.CROP_HEIGHT_COLUMN, height);
		int rowsAffected = database.update(
				MultiBackgroundConstants.IMAGE_CROP_TABLE, values,
				MultiBackgroundConstants.IMAGE_ID_COLUMN + "=?",
				new String[] { Integer.toString(imageId) });
		if (rowsAffected < 1) {
			Log.e(TAG, "Unable to locate a row with image_id: " + imageId);
		} else {
			isDatabaseUpdated = true;
		}
		return rowsAffected;
	}

	/**
	 * This method assumes that only the locale and image_path table exists with
	 * columns as per version 5 database. It checks for existence of other
	 * tables. If any table doesn't exist, this method would add that table.
	 * Similarly if in future some columns are changed or added, the check can
	 * be placed here. This method is a workaround for the problem that due to
	 * some reason, onUpgrade method is not getting called.
	 *
	 * @param db
	 *            A writable database.
	 */
	private void upgradeToLatestDatabaseVersion(SQLiteDatabase db) {
		String query = "SELECT * FROM SQLITE_MASTER";
		Cursor cursor = db.rawQuery(query, null);
		boolean imageCropTableExists = false;
		boolean cropButtonTableExists = false;
		while (cursor.moveToNext()) {
			String tableName = cursor.getString(cursor.getColumnIndex("name"));
			if (tableName
					.equalsIgnoreCase(MultiBackgroundConstants.IMAGE_CROP_TABLE)) {
				imageCropTableExists = true;
			}
			if (tableName
					.equalsIgnoreCase(MultiBackgroundConstants.CROP_BUTTON_DIMENSIONS_TABLE)) {
				cropButtonTableExists = true;
			}
		}
		cursor.close();
		try {
			if (!imageCropTableExists) {
				db.execSQL(MultiBackgroundConstants.CREATE_IMAGE_CROP_TABLE_QUERY);
				Log.i(TAG, "Created Image Crop table");
			}
			if (!cropButtonTableExists) {
				db.execSQL(MultiBackgroundConstants.CREATE_CROP_BUTTON_DIMENSIONS_TABLE_QUERY);
				Log.i(TAG, "Created Crop button dimensions table");
			}
		} catch (Exception e) {
			Log.e(TAG, "Unable to create new tables");
			Toast.makeText(
					context,
					"Error occurred while upgrading database. Creating a new one.",
					Toast.LENGTH_LONG).show();
			context.deleteDatabase(MultiBackgroundConstants.DATABASE_NAME);
			try {
				createDataBase();
				openDatabase();
			} catch (IOException e1) {
				e1.printStackTrace();
				Toast.makeText(
						context,
						"Error occurred recreating new database. Please go to settings and Clear App's data to resolve issue.",
						Toast.LENGTH_LONG).show();
			}

		}
	}

	public MultiBackgroundCropRectangle getCropRectangle(int imageId) {
		MultiBackgroundCropRectangle cropRectangle = null;
		Cursor recordCursor = database.query(
				MultiBackgroundConstants.IMAGE_CROP_TABLE, imageCropAllColumns,
				MultiBackgroundConstants.IMAGE_ID_COLUMN + "= ?",
				new String[] { Integer.toString(imageId) }, null, null, null);
		if (recordCursor.moveToFirst()) {
			cropRectangle = MultiBackgroundCropRectangle
					.newInstance(recordCursor);
		}
		if (recordCursor != null) {
			recordCursor.close();
		}
		return cropRectangle;
	}

	public CropButtonDimensions addCropButtonDimensions(int cropButtonLength,
			int cropButtonHeight) {
		CropButtonDimensions cropButtonDimensions = null;
		database.beginTransaction();
		try {
			ContentValues values = new ContentValues();
			values.put(MultiBackgroundConstants.CROP_BUTTON_LENGTH_COLUMN,
					cropButtonLength);
			values.put(MultiBackgroundConstants.CROP_BUTTON_HEIGHT_COLUMN,
					cropButtonHeight);
			int insertId = (int) database.insert(
					MultiBackgroundConstants.CROP_BUTTON_DIMENSIONS_TABLE,
					null, values);
			if (insertId < 0) {
				return null;
			}
			database.setTransactionSuccessful();
			cropButtonDimensions = new CropButtonDimensions(cropButtonLength,
					cropButtonHeight);
			Log.i(TAG, "Successfully updated new CropButtonDimensions: "
					+ cropButtonDimensions);
		} catch (Exception e) {
			Log.e(TAG, "Unable to update new CropButtonDimensions: " + e);
		} finally {
			database.endTransaction();
		}
		return cropButtonDimensions;
	}

	public CropButtonDimensions getCropButtonDimensions() {
		CropButtonDimensions cropButtonDimensions = null;
		Cursor recordCursor = database.query(
				MultiBackgroundConstants.CROP_BUTTON_DIMENSIONS_TABLE,
				cropButtonDimensionsAllColumns, null, null, null, null, null);
		if (recordCursor.moveToFirst()) {
			cropButtonDimensions = CropButtonDimensions
					.newInstance(recordCursor);
		}
		if (recordCursor != null) {
			recordCursor.close();
		}
		return cropButtonDimensions;
	}

	/**
	 * Method to delete data in cropButtonDimensions table so that only one row
	 * can be present in the table.
	 *
	 * @return
	 */
	public boolean deleteCropButtonDimensions() {
		database.beginTransaction();
		try {
			/*
			 * Delete the desired row using its id
			 */
			database.delete(
					MultiBackgroundConstants.CROP_BUTTON_DIMENSIONS_TABLE,
					null, null);
			database.setTransactionSuccessful();
		} catch (Exception e) {

		} finally {
			database.endTransaction();
		}
		return true;
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

}