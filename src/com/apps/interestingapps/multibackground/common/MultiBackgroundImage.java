package com.apps.interestingapps.multibackground.common;

import android.database.Cursor;

/**
 * Class to represent a particular image in the MultiBackground database
 */
public class MultiBackgroundImage implements Comparable<MultiBackgroundImage> {

	private long _id;
	private int imageNumber;
	private String path;
	private int imageViewIndexNumber;

	public MultiBackgroundImage(long _id, int imageNumber, String path) {
		super();
		this._id = _id;
		this.imageNumber = imageNumber;
		this.path = path;
	}

	public long get_id() {
		return _id;
	}

	public void set_id(long _id) {
		this._id = _id;
	}

	public int getImageNumber() {
		return imageNumber;
	}

	public void setImageNumber(int imageNumber) {
		this.imageNumber = imageNumber;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
	public int getImageViewIndexNumber() {
		return imageViewIndexNumber;
	}

	public void setImageViewIndexNumber(int imageViewNumber) {
		this.imageViewIndexNumber = imageViewNumber;
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

		return this.getImageNumber() - rhs.getImageNumber();
	}

	/**
	 * Returns a new object of MultiBackgroundImage using the values provided by
	 * the cursor
	 *
	 * @param cursor
	 * @return
	 */
	public static MultiBackgroundImage newInstance(Cursor cursor) {
		long cursor_id = cursor.getLong(cursor
				.getColumnIndex(MultiBackgroundConstants.ID_COLUMN));
		int cursorImageNumber = cursor.getInt(cursor
				.getColumnIndex(MultiBackgroundConstants.IMAGE_NUMBER_COLUMN));
		String cursorPath = cursor.getString(cursor
				.getColumnIndex(MultiBackgroundConstants.PATH_COLUMN));
		return new MultiBackgroundImage(cursor_id, cursorImageNumber,
				cursorPath);
	}

	/**
	 * Return a String representation of the image
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer("");
		sb.append("MultiBackgroundImage: _id = ").append(_id).append(
				" imageNumber = ").append(imageNumber).append(" path = ")
				.append(path);
		return sb.toString();
	}

}
