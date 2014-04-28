package com.apps.interestingapps.multibackground.common;

import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.util.Log;

/**
 * Class to represent a particular image in the MultiBackground database
 */
public class MultiBackgroundImage implements Comparable<MultiBackgroundImage> {

	public static enum ImageSize {
		COVER_FULL_SCREEN("cover_full_screen"),

		BEST_FIT("best_fit");

		private String imageSize;

		private ImageSize(String imageSize) {
			this.imageSize = imageSize;
		}

		public String toString() {
			return imageSize;
		}
	};

	private int _id;
	private int nextImageNumber;
	private boolean isDeletedImage;

	private String path;
	private ImageSize imageSize;
	private double aspectRatio = -1.0;
	private static final String TAG = "MultiBackgroundImage";

	public MultiBackgroundImage(int _id,
			int imageNumber,
			String path,
			ImageSize imageSize) {
		super();
		this._id = _id;
		this.nextImageNumber = imageNumber;
		this.path = path;
		this.imageSize = imageSize;
	}

	public int get_id() {
		return _id;
	}

	public void set_id(int _id) {
		this._id = _id;
	}

	public int getNextImageNumber() {
		return nextImageNumber;
	}

	public void setNextImageNumber(int imageNumber) {
		this.nextImageNumber = imageNumber;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public ImageSize getImageSize() {
		return imageSize;
	}

	public void setImageSize(ImageSize imageSize) {
		this.imageSize = imageSize;
	}

	public double getAspectRatio() {
		if (aspectRatio < 0) {
			aspectRatio = MultiBackgroundUtilities.getImageAspectRatio(path);
		}
		return aspectRatio;
	}

	/**
	 * An image which has image number less than some other image is considered
	 * to be an image less than the other image
	 */
	public int compareTo(MultiBackgroundImage rhs) {
		/*
		 * Give preference to this object if rhs null
		 */
		if (rhs == null) {
			return 1;
		}

		return this.getNextImageNumber() - rhs.getNextImageNumber();
	}

	/**
	 * Returns a new object of MultiBackgroundImage using the values provided by
	 * the cursor
	 * 
	 * @param cursor
	 * @return
	 */
	public static MultiBackgroundImage newInstance(Cursor cursor) {
		int cursor_id = cursor.getInt(cursor
				.getColumnIndex(MultiBackgroundConstants.ID_COLUMN));
		int cursorImageNumber = cursor
				.getInt(cursor
						.getColumnIndex(MultiBackgroundConstants.NEXT_IMAGE_NUMBER_COLUMN));
		String cursorPath = cursor.getString(cursor
				.getColumnIndex(MultiBackgroundConstants.PATH_COLUMN));
		ImageSize imageSize = ImageSize
				.valueOf(cursor
						.getString(
								cursor.getColumnIndex(MultiBackgroundConstants.IMAGE_SIZE_COLUMN))
						.toUpperCase());
		return new MultiBackgroundImage(cursor_id, cursorImageNumber,
				cursorPath, imageSize);
	}

	/**
	 * Return a String representation of the image
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer("");
		sb.append("MultiBackgroundImage: _id = ").append(_id).append(
				" nextImageNumber = ").append(nextImageNumber).append(
				" path = ").append(path).append(" image_size = ").append(
				imageSize.toString());
		return sb.toString();
	}

	public boolean equals(Object targetObject) {
		if (targetObject instanceof MultiBackgroundImage) {
			return ((MultiBackgroundImage) targetObject).get_id() == _id;
		} else {
			return false;
		}
	}
	
	public boolean isDeletedImage() {
		return isDeletedImage;
	}

	public void setDeletedImage(boolean isDeletedImage) {
		this.isDeletedImage = isDeletedImage;
	}
}
