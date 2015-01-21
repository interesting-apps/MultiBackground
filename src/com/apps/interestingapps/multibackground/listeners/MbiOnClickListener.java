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
import com.apps.interestingapps.multibackground.common.MultiBackgroundImage.ImageSize;
import com.apps.interestingapps.multibackground.common.MultiBackgroundUtilities;

public class MbiOnClickListener implements OnClickListener {

	private SetWallpaperActivity setWallpaperActivity;
	private String path;
	private ImageView currentImageView;
	private int width, height;
	private static final String TAG = "MbiClickListener";
	private RadioGroup radioGroup;
	private MultiBackgroundImage clickedMbi, previousMbi;

	public MbiOnClickListener(SetWallpaperActivity setWallpaperActivity,
			MultiBackgroundImage clickedMbi,
			MultiBackgroundImage previousMbi,
			ImageView currentImageView,
			String path,
			int width,
			int height,
			RadioGroup radioGroup) {
		this.setWallpaperActivity = setWallpaperActivity;
		this.clickedMbi = clickedMbi;
		this.previousMbi = previousMbi;
		this.path = path;
		this.currentImageView = currentImageView;
		this.width = width;
		this.height = height;
		this.radioGroup = radioGroup;
	}

	public void onClick(View v) {
		if (clickedMbi.isDeletedImage()) {
			Log.w(TAG, "Unable to load image from the given path." + path
					+ " Loading the default image.");
			Bitmap bitmap = MultiBackgroundUtilities.scaleDownImageAndDecode(
					setWallpaperActivity.getResources(),
					R.drawable.default_wallpaper, width, height);
			currentImageView.setImageBitmap(bitmap);
			radioGroup.setVisibility(View.INVISIBLE);
			setWallpaperActivity.hideCropButtonsAndRectangle();
			Log.i(TAG, "Image source deleted.");
		} else {

			switch (clickedMbi.getImageSize()) {
			case COVER_FULL_SCREEN:
				radioGroup.check(R.id.radio_cover_full_screen);
				setWallpaperActivity.hideCropButtonsAndRectangle();
				break;
			case BEST_FIT:
				radioGroup.check(R.id.radio_best_fit);
				setWallpaperActivity.hideCropButtonsAndRectangle();
				break;
			case CROP_IMAGE:
				radioGroup.check(R.id.radio_crop_image);
				break;
			}

			Bitmap bitmap = null;
			try {
				bitmap = MultiBackgroundUtilities.scaleDownImageAndDecode(path,
						width, height, clickedMbi.getImageSize());
			} catch (Exception e) {
				Log.d(TAG, "Unable to create bitmap for the given path due to "
						+ e);
			}
			int[] scaledWidthHeight = { bitmap.getWidth(), bitmap.getHeight() };
			currentImageView.setImageBitmap(bitmap);
			currentImageView.setVisibility(View.VISIBLE);
			radioGroup.setVisibility(View.VISIBLE);
			if (clickedMbi.getImageSize() == ImageSize.CROP_IMAGE) {
				setWallpaperActivity.showCropButtonsAndRectangle(clickedMbi.get_id(),
						scaledWidthHeight);
			}
		}
		ImageView previousClickedImage = setWallpaperActivity
				.getPreviousClickedImageView();
		if (previousClickedImage != null) {
			previousClickedImage.setBackgroundResource(0);
		}
		v.setBackgroundResource(R.drawable.border);
		setWallpaperActivity.setPreviousClickedImageView((ImageView) v);
		setWallpaperActivity.setPreviousSelectedMbi(previousMbi);
		setWallpaperActivity.setCurrentSelectedMbi(clickedMbi);
	}

}
