package com.apps.interestingapps.multibackground.listeners;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RadioGroup;

import com.apps.interestingapps.multibackground.R;
import com.apps.interestingapps.multibackground.SetWallpaperActivity;
import com.apps.interestingapps.multibackground.common.MultiBackgroundImage;
import com.apps.interestingapps.multibackground.common.MultiBackgroundUtilities;

public class MbiOnClickListener implements OnClickListener {

	private SetWallpaperActivity setWallpaperActivity;
	private String path;
	private ImageView currentImageView;
	private int width, height;
	private static final String TAG = "MbiClickListener";
	private RadioGroup radioGroup;
	private MultiBackgroundImage mbi;

	public MbiOnClickListener(SetWallpaperActivity setWallpaperActivity,
			MultiBackgroundImage mbi,
			ImageView currentImageView,
			String path,
			int width,
			int height,
			RadioGroup radioGroup) {
		this.setWallpaperActivity = setWallpaperActivity;
		this.mbi = mbi;
		this.path = path;
		this.currentImageView = currentImageView;
		this.width = width;
		this.height = height;
		this.radioGroup = radioGroup;
	}

	public void onClick(View v) {
		currentImageView.setVisibility(View.VISIBLE);
		radioGroup.setVisibility(View.VISIBLE);

		
		switch(mbi.getImageSize()) {
		case COVER_FULL_SCREEN:
			radioGroup.check(R.id.radio_cover_full_screen);
			break;
		case BEST_FIT:
			radioGroup.check(R.id.radio_best_fit);
			break;
		}
		setWallpaperActivity.setCurrentSelectedMbi(mbi);
		
		Bitmap bitmap = null;
		try {
			bitmap = MultiBackgroundUtilities.scaleDownImageAndDecode(path,
					width, height, mbi.getImageSize());
		} catch (Exception e) {
			Log.d(TAG, "Unable to create bitmap for the given path due to " + e);
		}
		if (bitmap == null) {
			Log.w(TAG,
					"Unable to load image from the given path. Loading the default image:");
			bitmap = MultiBackgroundUtilities.scaleDownImageAndDecode(
					setWallpaperActivity.getResources(),
					R.drawable.image_not_found, width, height);
		}
		currentImageView.setImageBitmap(bitmap);
	}

}
