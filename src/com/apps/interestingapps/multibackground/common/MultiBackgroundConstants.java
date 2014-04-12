package com.apps.interestingapps.multibackground.common;

public class MultiBackgroundConstants {

	public static final String ID_COLUMN = "_id";
	public static final String NEXT_IMAGE_NUMBER_COLUMN = "next_image_number";
//	public static final String IMAGE_NUMBER_COLUMN = "IMAGE_NUMBER";
	public static final String PATH_COLUMN = "path";
	public static final String IMAGE_PATH_TABLE = "image_path";
	public static final String IMAGE_SIZE_COLUMN = "image_size";
	public static final String DATABASE_NAME = "multi_background_1.db";
	public static final int DATABASE_VERSION = 5;

	public static final String DB_PATH = "/data/data/com.apps.interestingapps.multibackground/databases/";
	public static final int SELECT_PICTURE_ACTIVITY = 1;
	public static final int MAX_IMAGES = 15;
	
	public static final int TEMP_UINQUE_IMAGE_NUMBER = MultiBackgroundConstants.MAX_IMAGES + 2;
	public static final int DEFAULT_NEXT_IMAGE_NUMBER = -1;
}
