package com.apps.interestingapps.multibackground.common;

import android.database.Cursor;

/**
 * Class to represent a particular image in the MultiBackground database
 */
public class MultiBackgroundImage implements Comparable<MultiBackgroundImage> {

	private int _id;
	private int nextImageNumber;
	private String path;

	public MultiBackgroundImage(int _id, int imageNumber, String path) {
		super();
		this._id = _id;
		this.nextImageNumber = imageNumber;
		this.path = path;
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
		int cursorImageNumber = cursor.getInt(cursor
				.getColumnIndex(MultiBackgroundConstants.NEXT_IMAGE_NUMBER_COLUMN));
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
				" imageNumber = ").append(nextImageNumber).append(" path = ")
				.append(path);
		return sb.toString();
	}

}
