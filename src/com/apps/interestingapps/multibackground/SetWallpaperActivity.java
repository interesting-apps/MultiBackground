package com.apps.interestingapps.multibackground;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.apps.interestingapps.multibackground.common.DatabaseHelper;
import com.apps.interestingapps.multibackground.common.MultiBackgroundConstants;
import com.apps.interestingapps.multibackground.common.MultiBackgroundImage;
import com.apps.interestingapps.multibackground.listeners.AddImageClickListener;
import com.apps.interestingapps.multibackground.listeners.MbiDragListener;
import com.apps.interestingapps.multibackground.listeners.MbiLongClickListener;

/**
 * Class to Set the live wallpaper and select images to be used in the live
 * wallpaper
 */
public class SetWallpaperActivity extends Activity {

	private DatabaseHelper databaseHelper;
	private List<ImageView> imageViewList;
	private LinearLayout linearLayoutInsideHsv;
	private static final String TAG = "SetWallpaperActivity";
	private ImageView plusImageView;
	private HorizontalScrollView hsv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		initializeDatabase();
		imageViewList = new ArrayList<ImageView>();
		linearLayoutInsideHsv = (LinearLayout) findViewById(R.id.linearLayoutInsideHsv);
		plusImageView = (ImageView) findViewById(R.id.plusImageView);
		plusImageView.setOnClickListener(new AddImageClickListener(this));
		getAllImages();
		 hsv = (HorizontalScrollView)findViewById(R.id.horizontalScrollView);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (databaseHelper != null) {
			databaseHelper.closeDatabase();
			databaseHelper.close();
		}
	}

	/**
	 * Clears the Horizontal Scroll View and list of image views and adds all
	 * the images back to Horizontal Scroll View
	 */
	private void getAllImages() {
		/*
		 * Get all the images and load them in the activity
		 */
		linearLayoutInsideHsv.removeAllViews();
		imageViewList.clear();
		List<MultiBackgroundImage> allImages = databaseHelper.getAllImages();
		for (MultiBackgroundImage image : allImages) {
			addImageToHorizontalLayout(image);
		}
	}

	private void initializeDatabase() {
		databaseHelper = DatabaseHelper.getInstance(this);
		try {
			databaseHelper.createDataBase();
			databaseHelper.openDatabase();
			Log.i(this.getLocalClassName(), "Database opened");
		} catch (IOException e) {
			Log.i(TAG, "Error occurred while opening database.");
			e.printStackTrace();
		}
	}

	public void onClick(View view) {
		Intent intent = new Intent(
				WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER);
		startActivity(intent);
	}

	/**
	 * Method to perform operation once an activity has been started
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == MultiBackgroundConstants.SELECT_PICTURE_ACTIVITY) {
				Uri selectedImageUri = data.getData();
				addNewImage(selectedImageUri);
			}
		}
	}

	private String getPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = getContentResolver().query(uri, projection, null, null,
				null);
		// managedQuery(uri, projection, null, null, null);
		if (cursor != null && cursor.moveToFirst()) {
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			return cursor.getString(column_index);
		} else {
			Log.i(TAG, "Path not recognized");
			return "";
		}

	}

	/**
	 * Updates the list of current Images in database and adds the provided
	 * image to the Horizontal Scroll View at the end
	 *
	 * @param imagePath
	 */
	private void addImageToHorizontalLayout(MultiBackgroundImage mbi) {
		ImageView iv = new ImageView(getApplicationContext());
		File imageFile = new File(mbi.getPath());
		if (imageFile.exists()) {
			iv.setImageURI(Uri.fromFile(imageFile));
		} else {
			Log.w(TAG,
					"Unable to load image from the given path. Loading the default image:");
			iv.setBackgroundResource(R.drawable.image_not_found);
		}
		iv.setOnDragListener(new MbiDragListener(this));
		iv.setOnLongClickListener(new MbiLongClickListener());

		linearLayoutInsideHsv.addView(iv);
		imageViewList.add(iv);
	}

	/**
	 * Method to add an image to the database and current list of Image Views
	 *
	 * @param imageUri
	 */
	public void addNewImage(Uri imageUri) {
		String imagePath = getPath(imageUri);
		addNewImage(imagePath);
	}

	public void addNewImage(String imagePath) {
		int nextImageNumber = imageViewList.size();
		MultiBackgroundImage newMbi = null;
		try {
			newMbi = databaseHelper.addMultiBackgroundImage(nextImageNumber,
					imagePath);
			if (newMbi == null) {
				Toast.makeText(getApplicationContext(),
						"Could not add a new Image.", Toast.LENGTH_SHORT)
						.show();
			} else {
				addImageToHorizontalLayout(newMbi);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(getApplicationContext(), "Unable to add Image.",
					Toast.LENGTH_SHORT).show();
		}
	}

	public void updateImagePosition(ImageView sourceView, ImageView targetView) {
		int[] sourceTargetViewIndices = getIndexOfDragMovement(sourceView,
				targetView);
		if (sourceTargetViewIndices[0] == -1
				|| sourceTargetViewIndices[1] == -1) {
			Log.w(TAG,
					"Couldn't find the desired views. Drag is not a valid drag.");
			return;
		}

		if (sourceTargetViewIndices[0] == sourceTargetViewIndices[1]) {
			Log.w(TAG,
					"The image and source views the are same. So no need to update any positions");
			return;
		}

		if (databaseHelper
				.updateRowWithinImageNumberRange(sourceTargetViewIndices)) {
			getAllImages();
		}

	}

	/**
	 * Method to find the indices where the drag started and where it ended in
	 * the imageViewList. We will have to update the image numbers of all the
	 * images in between
	 *
	 * @param sourceView
	 *            The image that is being dragged
	 * @param targetView
	 *            The image on which the source view is dropped
	 * @return An array of 2 integer. The first element gives the index of
	 *         source view and the second gives the index of target view. It
	 *         returns an index of -1 for a view it couldn't find in the
	 *         imageViewList
	 */
	private int[] getIndexOfDragMovement(ImageView sourceView,
			ImageView targetView) {
		int[] indices = new int[2];
		indices[0] = -1;
		indices[1] = -1;
		for (int i = 0; i < imageViewList.size(); i++) {
			ImageView currentView = imageViewList.get(i);
			if (currentView.equals(sourceView)) {
				indices[0] = i;
			}

			if (currentView.equals(targetView)) {
				indices[1] = i;
			}

			if (indices[0] > -1 && indices[1] > -1) {
				Log.d(TAG, "Found the source View index as: " + indices[0]
						+ " and targetView index as: " + indices[1]);
				break;
			}
		}
		return indices;
	}
	
	public void scrollHorizontalScrollView(int scrollBy) {
		hsv.smoothScrollBy(scrollBy, 0);
	}
}