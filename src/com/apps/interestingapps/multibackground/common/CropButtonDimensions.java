package com.apps.interestingapps.multibackground.common;

import android.database.Cursor;

public class CropButtonDimensions {

	private int cropButtonLength;
	private int cropButtonHeight;

	public CropButtonDimensions(int cropButtonLength, int cropButtonHeight) {
		this.cropButtonLength = cropButtonLength;
		this.cropButtonHeight = cropButtonHeight;
	}

	@Override
	public String toString() {
		return "CropButtonDimensions [cropButtonLength=" + cropButtonLength
				+ ", cropButtonHeight=" + cropButtonHeight + "]";
	}

	public int getCropButtonLength() {
		return cropButtonLength;
	}

	public void setCropButtonLength(int cropButtonLength) {
		this.cropButtonLength = cropButtonLength;
	}

	public int getCropButtonHeight() {
		return cropButtonHeight;
	}

	public void setCropButtonHeight(int cropButtonHeight) {
		this.cropButtonHeight = cropButtonHeight;
	}

	/**
	 * Returns a new object of {@link CropButtonDimensions} using the values
	 * provided by the cursor
	 *
	 * @param cursor
	 * @return
	 */
	public static CropButtonDimensions newInstance(Cursor cursor) {
		int cropButtonLength = cursor
				.getInt(cursor
						.getColumnIndex(MultiBackgroundConstants.CROP_BUTTON_LENGTH_COLUMN));
		int cropButtonHeight = cursor
				.getInt(cursor
						.getColumnIndex(MultiBackgroundConstants.CROP_BUTTON_HEIGHT_COLUMN));

		return new CropButtonDimensions(cropButtonLength, cropButtonHeight);
	}
}
