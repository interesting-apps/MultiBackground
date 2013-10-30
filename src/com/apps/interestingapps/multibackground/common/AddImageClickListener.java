package com.apps.interestingapps.multibackground.common;

import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

import com.apps.interestingapps.multibackground.SetWallpaperActivity;

public class AddImageClickListener implements OnClickListener{

	private SetWallpaperActivity setWallpaperActivity;
	public AddImageClickListener(SetWallpaperActivity setWallpaperActivity) {
		this.setWallpaperActivity = setWallpaperActivity;
	}
	
	public void onClick(View v) {
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		setWallpaperActivity.startActivityForResult(Intent.createChooser(intent, "Select Picture"),
				MultiBackgroundConstants.SELECT_PICTURE_ACTIVITY);
	}	
}
