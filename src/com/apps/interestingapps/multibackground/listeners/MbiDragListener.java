package com.apps.interestingapps.multibackground.listeners;

import android.content.Context;
import android.view.Display;
import android.view.DragEvent;
import android.view.View;
import android.view.View.OnDragListener;
import android.view.WindowManager;
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

	public boolean onDrag(View view, DragEvent event) {
		int action = event.getAction();
		ImageView targetView = (ImageView) view;
		ImageView sourceView = (ImageView) event.getLocalState();
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

			break;
		case DragEvent.ACTION_DRAG_ENTERED:
			float sourceViewX = sourceView.getX();
			float targetViewX = targetView.getX();

			int diffX = (int) (sourceViewX - targetViewX);
			if (diffX == 0) {
				WindowManager wm = (WindowManager) setWallpaperActivity
						.getApplicationContext().getSystemService(
								Context.WINDOW_SERVICE);
				Display display = wm.getDefaultDisplay();
				int screenX = display.getWidth();
				/*
				 * If the sourceView is at the left end of the screen, the scoll
				 * the list a little left, else scroll the list a little right.
				 */
				if (targetViewX <= screenX / 2) {
					diffX = screenX / 2;
				} else {
					diffX = -screenX / 2;
				}
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
