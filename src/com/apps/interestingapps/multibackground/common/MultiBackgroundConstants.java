package com.apps.interestingapps.multibackground.common;

public class MultiBackgroundConstants {

	public static final String ID_COLUMN = "_id";
	public static final String NEXT_IMAGE_NUMBER_COLUMN = "next_image_number";
	public static final String PATH_COLUMN = "path";
	public static final String IMAGE_PATH_TABLE = "image_path";
	public static final String IMAGE_SIZE_COLUMN = "image_size";
	public static final String DATABASE_NAME = "multi_background_1.db";
	// public static final String DATABASE_NAME =
	// "multi_background_1_version5.db";
	public static final int DATABASE_VERSION = 6;

	public static final String IMAGE_CROP_TABLE = "image_crop";
	public static final String IMAGE_ID_COLUMN = "image_id";
	public static final String CROP_LEFT_COLUMN = "crop_left";
	public static final String CROP_TOP_COLUMN = "crop_top";
	public static final String CROP_LENGTH_COLUMN = "crop_length";
	public static final String CROP_HEIGHT_COLUMN = "crop_height";
	public static final String IMAGE_OFFSET_LEFT_COLUMN = "image_offset_left";
	public static final String IMAGE_OFFSET_TOP_COLUMN = "image_offset_top";

	public static final String CROP_BUTTON_DIMENSIONS_TABLE = "crop_button_dimensions";
	public static final String CROP_BUTTON_LENGTH_COLUMN = "crop_button_length";
	public static final String CROP_BUTTON_HEIGHT_COLUMN = "crop_button_height";

	public static final String DB_PATH = "/data/data/com.apps.interestingapps.multibackground/databases/";
	public static final int SELECT_PICTURE_ACTIVITY = 1;
	public static final int MAX_IMAGES = 15;

	public static final int TEMP_UINQUE_IMAGE_NUMBER = MultiBackgroundConstants.MAX_IMAGES + 2;
	public static final int DEFAULT_NEXT_IMAGE_NUMBER = -1;
	public static final String APP_PACKAGE = "com.apps.interestingapps.multibackground";
	public static final String PREFERENCES_FILE_NAME = APP_PACKAGE
			+ ".preferences";

	public static final String CREATE_IMAGE_CROP_TABLE_QUERY = "CREATE TABLE image_crop("
			+ " _id INTEGER primary key autoincrement ,"
			+ " image_id INTEGER NOT NULL UNIQUE, "
			+ "crop_left INTEGER NOT NULL, "
			+ "crop_top INTEGER NOT NULL, "
			+ "crop_length INTEGER NOT NULL, "
			+ "crop_height INTEGER NOT NULL, "
			+ "image_offset_left INTEGER NOT NULL,"
			+ "image_offset_top INTEGER NOT NULL,"
			+ "FOREIGN KEY(image_id) REFERENCES image_path(_id) ON DELETE CASCADE)";

	public static final String CREATE_CROP_BUTTON_DIMENSIONS_TABLE_QUERY = "CREATE TABLE crop_button_dimensions(crop_button_length INTEGER, crop_button_height INTEGER)";

	public static final String ENABLE_FOREIGN_KEY_QUERY = "PRAGMA foreign_keys=ON;";
}
