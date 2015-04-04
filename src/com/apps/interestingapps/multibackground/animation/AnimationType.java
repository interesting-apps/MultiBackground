package com.apps.interestingapps.multibackground.animation;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;

public enum AnimationType {

	CIRCLE_WITH_INCREASING_RADIUS("CIRCLE_WITH_INCREASING_RADIUS"),

	SQUARE_WITH_INCREASING_DIMENSIONS("SQUARE_WITH_INCREASING_DIMENSIONS"),

	ROTATING_CANVAS("ROTATING_CANVAS"),

	INWARD_TWO_WAY_TRIANGLE("INWARD_TWO_WAY_TRIANGLE"),

	NONE("NONE"),

	ALTERNATE_TILES_SHOWING_FROM_TOP_TO_BOTTOM(
			"ALTERNATE_TILES_SHOWING_FROM_TOP_TO_BOTTOM"),

	FULL_PAGE_FOLDING("FULL_PAGE_FOLDING");

	private String animationTypeName;
	private static final String TAG = "AnimationType";
	private static Map<String, AnimationType> nameToAnimationTypeMap = null;
	private static AnimationType defaultAnimationType = NONE;

	private AnimationType(String animationName) {
		this.animationTypeName = animationName;
	}

	public String getAnimationTypeName() {
		return animationTypeName;
	}

	@Override
	public String toString() {
		return animationTypeName;
	}

	public static AnimationType fromString(String text) {
		if (nameToAnimationTypeMap == null) {
			Log.d(TAG,
					"Name to Animation Type map is null. Initialize with all the values");
			nameToAnimationTypeMap = new HashMap<String, AnimationType>();
			for (AnimationType animationType : AnimationType.values()) {
				nameToAnimationTypeMap.put(
						animationType.getAnimationTypeName(), animationType);
			}
		}

		if (text != null) {
			return nameToAnimationTypeMap.get(text);
		} else {
			return defaultAnimationType;
		}

	}
}
