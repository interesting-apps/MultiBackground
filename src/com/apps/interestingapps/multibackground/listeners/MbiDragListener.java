package com.apps.interestingapps.multibackground.listeners;

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

	private SetWallpaperActivity setWallpaperActivity;
	private static final int THRESHOLD = 50;
	private static long previousAutoScrollTime = 0;
	/*
	 * Time in milliseconds
	 */
	private static final long TIME_BETWEEN_AUTO_SCROLLS = 500;
//	private static final String TAG = "MbiDragListener";

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
		int hsvScrollX = setWallpaperActivity.getHorizontalScrollView()
				.getScrollX();
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
			int dragX = (int) event.getX();
			int exactDragPosition = (int) (targetView.getX() + dragX);
			int currentPositionOnScreen = Math.abs(exactDragPosition
					- hsvScrollX);

			int diffX = 0;

			if (currentPositionOnScreen < THRESHOLD) {
				diffX = halfScreenWidth;
			} else if (currentPositionOnScreen > (screenWidth - THRESHOLD)) {
				diffX = -halfScreenWidth;
			}
			long currentTimeMillis = System.currentTimeMillis();
			if (diffX != 0) {
				if ((currentTimeMillis - previousAutoScrollTime) > TIME_BETWEEN_AUTO_SCROLLS) {
					setWallpaperActivity.scrollHorizontalScrollView(-1 * diffX);
					previousAutoScrollTime = System.currentTimeMillis();
				} 
			}
			break;
		case DragEvent.ACTION_DRAG_ENTERED:
			break;
		case DragEvent.ACTION_DRAG_EXITED:
			break;
		case DragEvent.ACTION_DROP:
			setWallpaperActivity.updateImagePosition(sourceView, targetView);
			break;
		case DragEvent.ACTION_DRAG_ENDED:
			break;
		default:
			break;
		}
		return true;
	}
}
