package com.apps.interestingapps.multibackground.listeners;

import android.view.DragEvent;
import android.view.View;
import android.view.View.OnDragListener;
import android.widget.ImageView;

import com.apps.interestingapps.multibackground.R;
import com.apps.interestingapps.multibackground.SetWallpaperActivity;

public class DragToDeleteListener implements OnDragListener {

//	private static final String TAG = "DragToDeleteListener";
	private SetWallpaperActivity setWallpaperActivity;

	public DragToDeleteListener(SetWallpaperActivity setWallpaperActivity) {
		this.setWallpaperActivity = setWallpaperActivity;
	}

	public boolean onDrag(View view, DragEvent event) {
		int action = event.getAction();
		switch (action) {
		case DragEvent.ACTION_DRAG_STARTED:
			// do nothing
			break;
		case DragEvent.ACTION_DRAG_LOCATION:
			break;
		case DragEvent.ACTION_DRAG_ENTERED:
			setWallpaperActivity
					.changeDeleteImageView(R.drawable.open_delete_bin);
			break;
		case DragEvent.ACTION_DRAG_EXITED:
			setWallpaperActivity.changeDeleteImageView(R.drawable.delete_bin);
			break;
		case DragEvent.ACTION_DROP:
			ImageView sourceImageView = (ImageView)event.getLocalState();
			setWallpaperActivity.deleteImage(sourceImageView);
			setWallpaperActivity.changeDeleteImageView(R.drawable.delete_bin);
			setWallpaperActivity.changeDeleteImageViewVisibilty(View.INVISIBLE);
			break;
		case DragEvent.ACTION_DRAG_ENDED:
			setWallpaperActivity.changeDeleteImageViewVisibilty(View.INVISIBLE);
		default:
			break;
		}
		return true;
	}

}
