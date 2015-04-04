package com.apps.interestingapps.multibackground.animation;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;

public enum AnimationGroup {

	SHAPE_ANIMATION("SHAPE_ANIMATION"),

	PAGE_ANIMATION("PAGE_ANIMATION");

	private String animationGroupName;
	private static final String TAG = "AnimationGroup";
	private static Map<String, AnimationGroup> nameToAnimationGroupMap = null;
	private static AnimationGroup defaultAnimationGroup = SHAPE_ANIMATION;

	private AnimationGroup(String animationGroupName) {
		this.animationGroupName = animationGroupName;
	}

	public String getAnimationGroupName() {
		return animationGroupName;
	}

	@Override
	public String toString() {
		return animationGroupName;
	}

	public static AnimationGroup fromString(String text) {
		if (nameToAnimationGroupMap == null) {
			Log.d(TAG,
					"Name to Animation Group map is null. Initialize with all the values");
			nameToAnimationGroupMap = new HashMap<String, AnimationGroup>();
			for (AnimationGroup animationGroup : AnimationGroup.values()) {
				nameToAnimationGroupMap.put(animationGroup
						.getAnimationGroupName(), animationGroup);
			}
		}
		if (text != null) {
			return nameToAnimationGroupMap.get(text);
		} else {
			return defaultAnimationGroup;
		}
	}
}
