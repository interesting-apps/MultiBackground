package com.apps.interestingapps.multibackground.common;

import android.database.Cursor;

public class MultiBackgroundLocalImage {

	private int imageId;
	private String localImagePath;
	private int isImageOnExternalStorage;

	public MultiBackgroundLocalImage(int imageId,
			String localImagePath,
			int isImageOnExternalStorage) {
		this.imageId = imageId;
		this.localImagePath = localImagePath;
		this.isImageOnExternalStorage = isImageOnExternalStorage;
	}

	public int getImageId() {
		return imageId;
	}

	public void setImageId(int imageId) {
		this.imageId = imageId;
	}

	public String getLocalImagePath() {
		return localImagePath;
	}

	public void setLocalImagePath(String localImagePath) {
		this.localImagePath = localImagePath;
	}

	public int isImageOnExternalStorage() {
		return isImageOnExternalStorage;
	}

	public void setImageOnExternalStorage(int isImageOnExternalStorage) {
		this.isImageOnExternalStorage = isImageOnExternalStorage;
	}

	/**
	 * Returns a new object of MultiBackgroundLocalImage using the values
	 * provided by the cursor
	 *
	 * @param cursor
	 * @return
	 */
	public static MultiBackgroundLocalImage newInstance(Cursor cursor) {
		int cursorImageId = cursor.getInt(cursor
				.getColumnIndex(MultiBackgroundConstants.IMAGE_ID_COLUMN));
		String localImagePath = cursor.getString(cursor
				.getColumnIndex(MultiBackgroundConstants.LOCAL_PATH_COLUMN));
		int isImageOnExternalStorage = cursor
				.getInt(cursor
						.getColumnIndex(MultiBackgroundConstants.IMAGE_ON_EXTERNAL_STORAGE_COLUMN));
		return new MultiBackgroundLocalImage(cursorImageId, localImagePath,
				isImageOnExternalStorage);
	}
}
