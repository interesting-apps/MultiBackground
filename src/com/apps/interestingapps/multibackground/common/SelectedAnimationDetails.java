package com.apps.interestingapps.multibackground.common;

import android.database.Cursor;

public class SelectedAnimationDetails {

	private int animationId;
	private String animationName;

	public SelectedAnimationDetails(int animationId, String animationName) {
		super();
		this.animationId = animationId;
		this.animationName = animationName;
	}

	public int getAnimationId() {
		return animationId;
	}

	public void setAnimationId(int animationId) {
		this.animationId = animationId;
	}

	public String getAnimationName() {
		return animationName;
	}

	public void setAnimationName(String animationName) {
		this.animationName = animationName;
	}

	public static SelectedAnimationDetails newInstance(Cursor cursor) {
		int animationId = cursor.getInt(cursor
				.getColumnIndex(MultiBackgroundConstants.ANIMATION_ID_COLUMN));
		String animationName = cursor
				.getString(cursor
						.getColumnIndex(MultiBackgroundConstants.ANIMATION_NAME_COLUMN));
		return new SelectedAnimationDetails(animationId, animationName);
	}
}
