package com.apps.interestingapps.multibackground.listeners;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

import com.apps.interestingapps.multibackground.AnimateTransitionActivity;
import com.apps.interestingapps.multibackground.R;
import com.apps.interestingapps.multibackground.SetWallpaperActivity;
import com.apps.interestingapps.multibackground.common.MultiBackgroundConstants;

public class AnimateTransitionOnClickListener implements OnClickListener {

	private Context context;
	private SetWallpaperActivity setWallpaperActivity;

	public AnimateTransitionOnClickListener(Context context,
			SetWallpaperActivity setWallpaperActivity) {
		this.context = context;
		this.setWallpaperActivity = setWallpaperActivity;
	}

	public void onClick(View v) {
		Intent intent = new Intent(context, AnimateTransitionActivity.class);
		intent.setAction(context.getResources().getString(
				R.string.animate_transition_activity_filter_action));
		intent.addCategory(context.getResources().getString(
				R.string.animate_transition_activity_filter_category));
		setWallpaperActivity.startActivityForResult(intent,
				MultiBackgroundConstants.ANIMATE_TRANSITION_ACTIVITY);
	}

}
