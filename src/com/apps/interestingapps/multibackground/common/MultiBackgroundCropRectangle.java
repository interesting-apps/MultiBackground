package com.apps.interestingapps.multibackground.common;

import android.database.Cursor;

public class MultiBackgroundCropRectangle {

	private int _id;
	private int imageId;
	private int cropLeft;
	private int cropTop;
	private int cropLength;
	private int cropHeight;
	private int imageOffsetLeft;
	private int imageOffsetTop;

	public MultiBackgroundCropRectangle(int _id,
			int imageId,
			int cropLeft,
			int cropTop,
			int cropLength,
			int cropHeight,
			int imageOffsetLeft,
			int imageOffsetTop) {
		this._id = _id;
		this.imageId = imageId;
		this.cropLeft = cropLeft;
		this.cropTop = cropTop;
		this.cropLength = cropLength;
		this.cropHeight = cropHeight;
		this.imageOffsetLeft = imageOffsetLeft;
		this.imageOffsetTop = imageOffsetTop;
	}

	public int get_id() {
		return _id;
	}

	public void set_id(int _id) {
		this._id = _id;
	}

	public int getImageId() {
		return imageId;
	}

	public void setImageId(int imageId) {
		this.imageId = imageId;
	}

	public int getCropLeft() {
		return cropLeft;
	}

	public void setCropLeft(int cropLeft) {
		this.cropLeft = cropLeft;
	}

	public int getCropTop() {
		return cropTop;
	}

	public void setCropTop(int cropTop) {
		this.cropTop = cropTop;
	}

	public int getCropLength() {
		return cropLength;
	}

	public void setCropLength(int cropLength) {
		this.cropLength = cropLength;
	}

	public int getCropHeight() {
		return cropHeight;
	}

	public void setCropHeight(int cropHeight) {
		this.cropHeight = cropHeight;
	}

	public int getImageOffsetLeft() {
		return imageOffsetLeft;
	}

	public void setImageOffsetLeft(int imageOffsetLeft) {
		this.imageOffsetLeft = imageOffsetLeft;
	}

	public int getImageOffsetTop() {
		return imageOffsetTop;
	}

	public void setImageOffsetTop(int imageOffsetTop) {
		this.imageOffsetTop = imageOffsetTop;
	}

	@Override
	public String toString() {
		return "MultiBackgroundCropRectangle [_id=" + _id + ", image_id="
				+ imageId + ", cropLeft=" + cropLeft + ", cropTop=" + cropTop
				+ ", cropLength=" + cropLength + ", cropHeight=" + cropHeight
				+ ", imageOffsetLeft=" + imageOffsetLeft + ", imageOffsetTop="
				+ imageOffsetTop + "]";
	}

	/**
	 * Returns a new object of {@link MultiBackgroundCropRectangle} using the
	 * values provided by the cursor
	 *
	 * @param cursor
	 * @return
	 */
	public static MultiBackgroundCropRectangle newInstance(Cursor cursor) {
		int _id = cursor.getInt(cursor
				.getColumnIndex(MultiBackgroundConstants.ID_COLUMN));
		int imageId = cursor.getInt(cursor
				.getColumnIndex(MultiBackgroundConstants.IMAGE_ID_COLUMN));
		int cropLeft = cursor.getInt(cursor
				.getColumnIndex(MultiBackgroundConstants.CROP_LEFT_COLUMN));
		int cropTop = cursor.getInt(cursor
				.getColumnIndex(MultiBackgroundConstants.CROP_TOP_COLUMN));
		int cropLength = cursor.getInt(cursor
				.getColumnIndex(MultiBackgroundConstants.CROP_LENGTH_COLUMN));
		int cropHeight = cursor.getInt(cursor
				.getColumnIndex(MultiBackgroundConstants.CROP_HEIGHT_COLUMN));
		int imageOffsetLeft = cursor
				.getInt(cursor
						.getColumnIndex(MultiBackgroundConstants.IMAGE_OFFSET_LEFT_COLUMN));
		int imageOffsetTop = cursor
				.getInt(cursor
						.getColumnIndex(MultiBackgroundConstants.IMAGE_OFFSET_TOP_COLUMN));
		return new MultiBackgroundCropRectangle(_id, imageId, cropLeft,
				cropTop, cropLength, cropHeight, imageOffsetLeft,
				imageOffsetTop);
	}

}
