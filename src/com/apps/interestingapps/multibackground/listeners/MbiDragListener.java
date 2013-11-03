package com.apps.interestingapps.multibackground.listeners;

import android.util.Log;
import android.view.Display;
import android.view.DragEvent;
import android.view.View;
import android.view.View.OnDragListener;
import android.widget.ImageView;

import com.apps.interestingapps.multibackground.SetWallpaperActivity;

/**
 * Class to implement Drag listener on a MuliBackgorund Image. Assuming the view
 * being dragged as source view and the current view that recieves the drag
 * events as the target view
 */
public class MbiDragListener implements OnDragListener {

	private static final String TAG = "MbiDragListener";
	private SetWallpaperActivity setWallpaperActivity;

	public MbiDragListener(SetWallpaperActivity setWallpaperActivity) {
		this.setWallpaperActivity = setWallpaperActivity;
	}

	@SuppressWarnings("deprecation")
	public boolean onDrag(View view, DragEvent event) {
		int action = event.getAction();
		ImageView targetView = (ImageView) view;
		ImageView sourceView = (ImageView) event.getLocalState();
		
		Display display = setWallpaperActivity.getWindowManager()
				.getDefaultDisplay();
		int screenWidth = display.getWidth();
		int halfScreenWidth = screenWidth / 2;
		
		switch (action) {
		/*
		 * For now implementing change in positions of views once the view is
		 * dropped at the desired positions.
		 * 
		 * TODO: Update the code so that there is a live update of position of
		 * views as the source image is dragged through them
		 * 
		 * TODO: Update the image number column with the new index of images
		 */
		case DragEvent.ACTION_DRAG_STARTED:
			// do nothing
			break;
		case DragEvent.ACTION_DRAG_LOCATION:
//			int dragX = (int)event.getX();
//			int targetViewX = (int)targetView.getX();
//			int distanceOfTargetViewFromLeft = targetViewX % screenWidth ;
//			int distanceDragXFromLeft = dragX + distanceOfTargetViewFromLeft;
//			Log.i(TAG, "Drag X = " + dragX + " Distance from left:" + distanceDragXFromLeft);
//			int diffX = 0;
//			if(distanceDragXFromLeft < halfScreenWidth) {
//				diffX = halfScreenWidth - distanceDragXFromLeft; 
//			} else {
//				diffX = distanceDragXFromLeft - screenWidth;
//			}
//			
//			setWallpaperActivity.scrollHorizontalScrollView(-1 * diffX);
//			break;
		case DragEvent.ACTION_DRAG_ENTERED:
			float sourceViewX = sourceView.getX();
			float targetViewX = targetView.getX();

			int diffX = (int) (sourceViewX - targetViewX);
			
			int targetViewXFromLeftCorner = (int) targetViewX % screenWidth;
			/*
			 * If the sourceView is at the left end of the screen, then scroll
			 * the list a little left, else scroll the list a little right.
			 */
			if (targetViewXFromLeftCorner < halfScreenWidth) {
				diffX = halfScreenWidth;
			} else if (targetViewXFromLeftCorner + targetView.getWidth() > screenWidth) {
				diffX = -halfScreenWidth;
			} else {
				Log.d(TAG, "TargetView X: " + targetViewX);
			}

			/*
			 * Scroll the view in the opposite direction of where the drag is
			 * being made
			 */
			setWallpaperActivity.scrollHorizontalScrollView(-1 * diffX);
			break;
		case DragEvent.ACTION_DRAG_EXITED:

			break;
		case DragEvent.ACTION_DROP:

			setWallpaperActivity.updateImagePosition(sourceView, targetView);
			break;
		case DragEvent.ACTION_DRAG_ENDED:

		default:
			break;
		}
		return true;
	}
}
