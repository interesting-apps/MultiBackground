package com.apps.interestingapps.multibackground.listeners;

import android.content.ClipData;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;

import com.apps.interestingapps.multibackground.SetWallpaperActivity;

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
		DragShadowBuilder shadowBuilder = new MyDragShadowBuilder(
				setWallpaperActivity.getResources(), view);
		view.startDrag(clipData, shadowBuilder, view, 0);

		setWallpaperActivity.changeDeleteImageViewVisibilty(View.VISIBLE);
		return true;
	}

	private static class MyDragShadowBuilder extends View.DragShadowBuilder {

		// The drag shadow image, defined as a drawable thing
		private static Drawable shadow;
		private View v;
		private Resources res;

		// Defines the constructor for myDragShadowBuilder
		public MyDragShadowBuilder(Resources res, View v) {

			// Stores the View parameter passed to myDragShadowBuilder.
			super(v);
			this.v = v;
			this.res = res;

			// Creates a draggable image that will fill the Canvas provided by
			// the system.
			ImageView iv = (ImageView) v;

			shadow = new BitmapDrawable(res,
					((BitmapDrawable) iv.getDrawable()).getBitmap());
		}

		// Defines a callback that sends the drag shadow dimensions and touch
		// point back to the
		// system.
		@Override
		public void onProvideShadowMetrics(Point size, Point touch) {

			// Sets the width of the shadow to half the width of the original
			// View
			int width = v.getWidth();

			// Sets the height of the shadow to half the height of the original
			// View
			int height = v.getHeight();

			// The drag shadow is a ColorDrawable. This sets its dimensions to
			// be the same as the
			// Canvas that the system will provide. As a result, the drag shadow
			// will fill the
			// Canvas.
			shadow.setBounds(0, 0, width, height);
			shadow.setAlpha(100);

			// Sets the size parameter's width and height values. These get back
			// to the system
			// through the size parameter.
			size.set(width, height);

			// Sets the touch point's position to be in the middle of the drag
			// shadow
			touch.set(width / 2, height / 2);
		}

		// Defines a callback that draws the drag shadow in a Canvas that the
		// system constructs
		// from the dimensions passed in onProvideShadowMetrics().
		@Override
		public void onDrawShadow(Canvas canvas) {

			// Draws the ColorDrawable in the Canvas passed in from the system.

			shadow.draw(canvas);
		}
	}
}
