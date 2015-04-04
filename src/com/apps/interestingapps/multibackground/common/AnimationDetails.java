package com.apps.interestingapps.multibackground.common;

import java.util.Map;

import android.database.Cursor;

import com.apps.interestingapps.multibackground.animation.AnimationType;

public class AnimationDetails implements Comparable<AnimationDetails> {

	private int animationId;
	private AnimationType animationType;
	private AnimationGroupDetails animationGroupDetails;
	private String animationDisplayName;
	private String animationDescription;

	public AnimationDetails(int animationId,
			AnimationType animationType,
			AnimationGroupDetails animationGroupDetails,
			String animationDisplayName,
			String animationDescription) {
		this.animationId = animationId;
		this.animationType = animationType;
		this.animationGroupDetails = animationGroupDetails;
		this.animationDisplayName = animationDisplayName;
		this.animationDescription = animationDescription;
	}

	public int getAnimationId() {
		return animationId;
	}

	public void setAnimationId(int animationId) {
		this.animationId = animationId;
	}

	public AnimationType getAnimationType() {
		return animationType;
	}

	public void setAnimationType(AnimationType animationType) {
		this.animationType = animationType;
	}

	public AnimationGroupDetails getAnimationGroupDetails() {
		return animationGroupDetails;
	}

	public void
			setAnimationGroupDetails(AnimationGroupDetails animationGroupDetails) {
		this.animationGroupDetails = animationGroupDetails;
	}

	public String getAnimationDisplayName() {
		return animationDisplayName;
	}

	public void setAnimationDisplayName(String animationDisplayName) {
		this.animationDisplayName = animationDisplayName;
	}

	public String getAnimationDescription() {
		return animationDescription;
	}

	public void setAnimationDescription(String animationDescription) {
		this.animationDescription = animationDescription;
	}

	public static AnimationDetails newInstance(Cursor cursor,
			Map<Integer, AnimationGroupDetails> animationGroupIdToObjectMap) {
		int animationId = cursor.getInt(cursor
				.getColumnIndex(MultiBackgroundConstants.ANIMATION_ID_COLUMN));
		AnimationType animationType = AnimationType
				.fromString(cursor
						.getString(cursor
								.getColumnIndex(MultiBackgroundConstants.ANIMATION_NAME_COLUMN)));
		int animationGroupId = cursor
				.getInt(cursor
						.getColumnIndex(MultiBackgroundConstants.ANIMATION_GROUP_ID_COLUMN));
		String animationDisplayName = cursor
				.getString(cursor
						.getColumnIndex(MultiBackgroundConstants.ANIMATION_DISPLAY_NAME_COLUMN));
		String animationDescription = cursor
				.getString(cursor
						.getColumnIndex(MultiBackgroundConstants.ANIMATION_DESCRIPTION_COLUMN));
		return new AnimationDetails(animationId, animationType,
				animationGroupIdToObjectMap.get(animationGroupId),
				animationDisplayName, animationDescription);
	}

	/**
	 * Method to sort the AnimationDetails objects first based on AnimationGroup
	 * and then based on AnimationType so that they could be shown in groups
	 * together.
	 */
	public int compareTo(AnimationDetails another) {
		if (this.getAnimationGroupDetails().getAnimationGroupId() != another
				.getAnimationGroupDetails().getAnimationGroupId()) {
			String thisAnimationGroupDisplayName = this
					.getAnimationGroupDetails().getAnimationGroupDisplayName();
			String anotherAnimationGroupDisplayName = another
					.getAnimationGroupDetails().getAnimationGroupDisplayName();
			return thisAnimationGroupDisplayName
					.compareTo(anotherAnimationGroupDisplayName);
		}
		return this.getAnimationDisplayName().compareTo(
				another.getAnimationDisplayName());
	}
}
