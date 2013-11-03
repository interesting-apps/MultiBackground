package com.apps.interestingapps.multibackground.listeners;

import com.apps.interestingapps.multibackground.SetWallpaperActivity;

import android.content.ClipData;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnLongClickListener;

/**
 * Class to implement the long click listener for MulitBackground images. This
 * uses the OnDragListener to implement drag and drop
 * 
 * TODO: Find an alternative for APIs less than 11
 */
public class MbiLongClickListener implements OnLongClickListener {

	private SetWallpaperActivity setWallpaperActivity;
	
	public MbiLongClickListener(SetWallpaperActivity setWallpaperActivity) {
		this.setWallpaperActivity = setWallpaperActivity;
	}
	public boolean onLongClick(View view) {
		ClipData clipData = ClipData.newPlainText("", "");
		DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
		view.startDrag(clipData, shadowBuilder, view, 0);
		
		setWallpaperActivity.changeDeleteImageViewVisibilty(View.VISIBLE);
		return true;
	}
}
