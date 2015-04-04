package com.apps.interestingapps.multibackground.common;

import android.database.Cursor;

import com.apps.interestingapps.multibackground.animation.AnimationGroup;

public class AnimationGroupDetails implements Comparable<AnimationGroupDetails> {

	private int animationGroupId;
	private AnimationGroup animationGroup;
	private String animationGroupDisplayName;
	private String animationGroupDescription;

	public AnimationGroupDetails(int animationGroupId,
			AnimationGroup animationGroup,
			String animationGroupDisplayName,
			String animationGroupDescription) {
		this.animationGroupId = animationGroupId;
		this.animationGroup = animationGroup;
		this.animationGroupDisplayName = animationGroupDisplayName;
		this.animationGroupDescription = animationGroupDescription;
	}

	public int getAnimationGroupId() {
		return animationGroupId;
	}

	public void setAnimationGroupId(int animationGroupId) {
		this.animationGroupId = animationGroupId;
	}

	public AnimationGroup getAnimationGroup() {
		return animationGroup;
	}

	public void setAnimationGroup(AnimationGroup animationGroup) {
		this.animationGroup = animationGroup;
	}

	public String getAnimationGroupDescription() {
		return animationGroupDescription;
	}

	public void setAnimationGroupDescription(String animationDescription) {
		this.animationGroupDescription = animationDescription;
	}

	public String getAnimationGroupDisplayName() {
		return animationGroupDisplayName;
	}

	public void setAnimationGroupDisplayName(String animationDisplayName) {
		this.animationGroupDisplayName = animationDisplayName;
	}

	public static AnimationGroupDetails newInstance(Cursor cursor) {
		int animationGroupId = cursor
				.getInt(cursor
						.getColumnIndex(MultiBackgroundConstants.ANIMATION_GROUP_ID_COLUMN));
		AnimationGroup animationGroup = AnimationGroup
				.fromString(cursor
						.getString(cursor
								.getColumnIndex(MultiBackgroundConstants.ANIMATION_GROUP_NAME_COLUMN)));
		String animationGroupDisplayName = cursor
				.getString(cursor
						.getColumnIndex(MultiBackgroundConstants.ANIMATION_GROUP_DISPLAY_NAME_COLUMN));
		String animationDescription = cursor
				.getString(cursor
						.getColumnIndex(MultiBackgroundConstants.ANIMATION_GROUP_DESCRIPTION_COLUMN));
		return new AnimationGroupDetails(animationGroupId, animationGroup,
				animationGroupDisplayName, animationDescription);
	}

	/**
	 * Method to sort the AnimationDetails objects first based on AnimationGroup
	 * and then based on AnimationType so that they could be shown in groups
	 * together.
	 */
	public int compareTo(AnimationGroupDetails another) {
		return this.getAnimationGroupDisplayName().compareTo(
				another.getAnimationGroupDisplayName());
	}
}
