package com.apps.interestingapps.multibackground.listeners;

import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

import com.apps.interestingapps.multibackground.SetWallpaperActivity;
import com.apps.interestingapps.multibackground.common.MultiBackgroundConstants;

public class AddImageClickListener implements OnClickListener{

	private SetWallpaperActivity setWallpaperActivity;
	public AddImageClickListener(SetWallpaperActivity setWallpaperActivity) {
		this.setWallpaperActivity = setWallpaperActivity;
	}
	
	public void onClick(View v) {
		Intent intent = new Intent(Intent.ACTION_PICK);
	    intent.setType("image/*");
	    setWallpaperActivity.startActivityForResult(intent, MultiBackgroundConstants.SELECT_PICTURE_ACTIVITY);
	}	
}
