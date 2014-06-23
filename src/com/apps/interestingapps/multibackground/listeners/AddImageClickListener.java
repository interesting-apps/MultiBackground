package com.apps.interestingapps.multibackground.listeners;

import android.content.Intent;
import android.os.Build;
import android.view.View;
import android.view.View.OnClickListener;

import com.apps.interestingapps.multibackground.SetWallpaperActivity;
import com.apps.interestingapps.multibackground.common.MultiBackgroundConstants;

public class AddImageClickListener implements OnClickListener {

	private SetWallpaperActivity setWallpaperActivity;

	public AddImageClickListener(SetWallpaperActivity setWallpaperActivity) {
		this.setWallpaperActivity = setWallpaperActivity;
	}

	public void onClick(View v) {
//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//			// ACTION_OPEN_DOCUMENT is the intent to choose a file via the
//			// system's file
//			// browser.
//			Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
//
//			// Filter to only show results that can be "opened", such as a
//			// file (as opposed to a list of contacts or timezones)
//			intent.addCategory(Intent.CATEGORY_OPENABLE);
//
//			// Filter to show only images, using the image MIME data type.
//			// If one wanted to search for ogg vorbis files, the type would be
//			// "audio/ogg".
//			// To search for all documents available via installed storage
//			// providers,
//			// it would be "*/*".
//			intent.setType("image/*");
//
//			setWallpaperActivity.startActivityForResult(intent,
//					MultiBackgroundConstants.SELECT_PICTURE_ACTIVITY);
//		} else {
//			Intent intent = new Intent(Intent.ACTION_PICK);
//			intent.setType("image/*");
//			setWallpaperActivity.startActivityForResult(intent,
//					MultiBackgroundConstants.SELECT_PICTURE_ACTIVITY);
//		}
		Intent i = new Intent(Intent.ACTION_PICK,
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		setWallpaperActivity.startActivityForResult(i,
				MultiBackgroundConstants.SELECT_PICTURE_ACTIVITY);
	}
}
