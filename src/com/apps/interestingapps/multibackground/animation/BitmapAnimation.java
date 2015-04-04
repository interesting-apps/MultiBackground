package com.apps.interestingapps.multibackground.animation;

import android.graphics.Bitmap;
import android.view.SurfaceHolder;

public interface BitmapAnimation {

	public void draw(SurfaceHolder surfaceHolder,
			Bitmap oldBitmap,
			Bitmap nextBitmap);
}
