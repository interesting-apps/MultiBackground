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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

import com.apps.interestingapps.multibackground.common.AddImageClickListener;
import com.apps.interestingapps.multibackground.common.DatabaseHelper;
import com.apps.interestingapps.multibackground.common.MultiBackgroundConstants;
import com.apps.interestingapps.multibackground.common.MultiBackgroundImage;

/**
 * Class to Set the live wallpaper and select images to be used in the live
 * wallpaper
 */
public class SetWallpaperActivity extends Activity {

	private DatabaseHelper databaseHelper;
	private List<ImageView> imageViewsList = new ArrayList<ImageView>();
	private LinearLayout linearLayoutInsideHsv;
	private static final String TAG = "SetWallpaperActivity";
	private ImageView plusImageView;
	private int imageIdCounter = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		initializeDatabase();

		linearLayoutInsideHsv = (LinearLayout) findViewById(R.id.linearLayoutInsideHsv);
		plusImageView = (ImageView) findViewById(R.id.plusImageView);
		plusImageView.setOnClickListener(new AddImageClickListener(this));
		/*
		 * Get all the images and load them in the activity
		 */
		List<MultiBackgroundImage> allImages = databaseHelper.getAllImages();
		for (MultiBackgroundImage image : allImages) {
			addImageToHorizontalLayout(image.getPath());
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (databaseHelper != null) {
			databaseHelper.closeDatabase();
			databaseHelper.close();
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

	private void addPlusImageView() {
		if (plusImageView == null) {
			/*
			 * Add plus image to the Horizontal Scroll view
			 */
			plusImageView = new ImageView(getApplicationContext());
			plusImageView
					.setBackgroundResource(R.drawable.green_circle_plus_image);
			plusImageView.setOnClickListener(new AddImageClickListener(this));
			plusImageView.setId(++imageIdCounter);
			RelativeLayout.LayoutParams plusImageLayoutParams = new RelativeLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			plusImageLayoutParams.addRule(RelativeLayout.ALIGN_RIGHT);
			plusImageView.setLayoutParams(plusImageLayoutParams);

			Log.i(TAG, "Plus image view Id is: " + plusImageView.getId());
			linearLayoutInsideHsv.addView(plusImageView);
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
		Log.i(TAG, "In OnActivityResult");
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == MultiBackgroundConstants.SELECT_PICTURE_ACTIVITY) {
				Uri selectedImageUri = data.getData();
				addNewImage(selectedImageUri);
			}
		}
	}

	private String getPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(uri, projection, null, null, null);
		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	/**
	 * Updates the list of current Images in database and adds the provided
	 * image to the Horizontal Scroll View
	 *
	 * @param imagePath
	 */
	private void addImageToHorizontalLayout(String imagePath) {
		ImageView iv = new ImageView(getApplicationContext());
		File imageFile = new File(imagePath);
		if (imageFile.exists()) {
			iv.setImageURI(Uri.fromFile(imageFile));
		} else {
			Log.i(TAG,
					"Unable to load image from the given path. Loading the default image:");
			iv.setBackgroundResource(R.drawable.image_not_found);
		}
		iv.setId(++imageIdCounter);
		Log.i(TAG, "New Image Id is: " + imageIdCounter);

		// setImagePosition(iv);
		linearLayoutInsideHsv.addView(iv);
		imageViewsList.add(iv);
	}

	/**
	 * Sets the current Image's position to the left of Plus Image view and sets
	 * the previous image's position to be left of the current image. This
	 * method assumes that all the images will be added to the left of the Plus
	 * image, and that the first image will move further away from plus image
	 * towards left as new images are added.
	 *
	 * @param currentImageView
	 */
	private void setImagePosition(ImageView currentImageView) {
		if (currentImageView == null) {
			return;
		}

		if (imageViewsList.size() != 0) {
			/*
			 * Make the previous image to be on the left of the current image.
			 */
			ImageView previousImageView = imageViewsList.get(imageViewsList
					.size() - 1);
			RelativeLayout.LayoutParams previousImageLayoutParams = new RelativeLayout.LayoutParams(
					previousImageView.getLayoutParams());
			previousImageLayoutParams.addRule(RelativeLayout.LEFT_OF,
					currentImageView.getId());
			previousImageView.setLayoutParams(previousImageLayoutParams);
		}

		/*
		 * Add the current image to be on left of Plus image View
		 */
		RelativeLayout.LayoutParams currentImageLayoutParams = new RelativeLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		currentImageLayoutParams.addRule(RelativeLayout.LEFT_OF, plusImageView
				.getId());
		currentImageView.setLayoutParams(currentImageLayoutParams);

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
		int nextImageNumber = imageViewsList.size() + 1;
		MultiBackgroundImage newMbi = null;
		try {
			newMbi = databaseHelper.addMultiBackgroundImage(nextImageNumber,
					imagePath);
			if (newMbi == null) {
				Toast.makeText(getApplicationContext(),
						"Could not add a new Image.", Toast.LENGTH_SHORT)
						.show();
			} else {
				addImageToHorizontalLayout(imagePath);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(getApplicationContext(), "Unable to add Image.",
					Toast.LENGTH_SHORT).show();
		}

	}
}