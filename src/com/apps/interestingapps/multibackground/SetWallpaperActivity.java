package com.apps.interestingapps.multibackground;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.apps.interestingapps.multibackground.common.DatabaseHelper;
import com.apps.interestingapps.multibackground.common.MultiBackgroundConstants;
import com.apps.interestingapps.multibackground.common.MultiBackgroundImage;
import com.apps.interestingapps.multibackground.common.MultiBackgroundUtilities;
import com.apps.interestingapps.multibackground.listeners.AddImageClickListener;
import com.apps.interestingapps.multibackground.listeners.DragToDeleteListener;
import com.apps.interestingapps.multibackground.listeners.MbiDragListener;
import com.apps.interestingapps.multibackground.listeners.MbiLongClickListener;
import com.google.ads.AdRequest;
import com.google.ads.AdView;

/**
 * Class to Set the live wallpaper and select images to be used in the live
 * wallpaper
 */
public class SetWallpaperActivity extends Activity {

	private DatabaseHelper databaseHelper;
	private List<ImageView> imageViewList;
	private List<MultiBackgroundImage> mbiList;
	private LinearLayout linearLayoutInsideHsv;
	private static final String TAG = "SetWallpaperActivity";
	private ImageView plusImageView;
	private HorizontalScrollView hsv;
	private ImageView deleteImageView;
	private int screenWidth, screenHeight, quarterScreenWidth,
			quarterScreenHeight;

	private AdView adview;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		Display display = getWindowManager().getDefaultDisplay();
		screenWidth = display.getWidth();
		screenHeight = display.getHeight();
		quarterScreenWidth = screenWidth / 4;
		quarterScreenHeight = screenHeight / 4;
		databaseHelper = DatabaseHelper.initializeDatabase(this);
		initializeStaticViews();
		imageViewList = new ArrayList<ImageView>();
		mbiList = new ArrayList<MultiBackgroundImage>();
		getAllImages();

		adview = (AdView) findViewById(R.id.adView);
		AdRequest re = new AdRequest();
		// re.addTestDevice(AdRequest.TEST_EMULATOR);
		// re.addTestDevice("134A570CAEC830760EF3144B1EED15A5");
		adview.loadAd(re);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (databaseHelper != null) {
			databaseHelper.closeDatabase();
		}
	}

	private void initializeStaticViews() {
		linearLayoutInsideHsv = (LinearLayout) findViewById(R.id.linearLayoutInsideHsv);
		plusImageView = (ImageView) findViewById(R.id.plusImageView);
		plusImageView.setOnClickListener(new AddImageClickListener(this));
		hsv = (HorizontalScrollView) findViewById(R.id.horizontalScrollView);
		deleteImageView = (ImageView) findViewById(R.id.deleteImageView);
		deleteImageView.setOnDragListener(new DragToDeleteListener(this));
	}

	/**
	 * Clears the Horizontal Scroll View and list of image views and adds all
	 * the images back to Horizontal Scroll View
	 */
	private void getAllImages() {
		/*
		 * Get all the images and load them in the activity
		 */
		removeAllImageViews();
		List<MultiBackgroundImage> allImages = databaseHelper.getAllImages();
		for (MultiBackgroundImage image : allImages) {
			addImageToHorizontalLayout(image);
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
		iv.setPadding(5, 5, 5, 5);
		Bitmap bitmap = generateImageThumbnail(mbi.getPath(),
				quarterScreenWidth, quarterScreenHeight);
		if (bitmap == null) {
			Log.w(TAG,
					"Unable to load image from the given path. Loading the default image:");
			bitmap = generateImageThumbnail(R.drawable.image_not_found,
					quarterScreenWidth, quarterScreenHeight);
		}
		iv.setImageBitmap(bitmap);
		iv.setOnDragListener(new MbiDragListener(this));
		iv.setOnLongClickListener(new MbiLongClickListener(this));

		addImageView(mbi, iv);
	}

	private Bitmap generateImageThumbnail(String imagePath,
			int width,
			int height) {
		Bitmap scaledBitmap = null ;
		try {
			scaledBitmap = MultiBackgroundUtilities.scaleDownImageAndDecode(
				imagePath, width, height);
		} catch(OutOfMemoryError oom) {
			Toast.makeText(this, "Image is very large to load. ", Toast.LENGTH_SHORT).show();
		}
		return scaledBitmap;
	}

	private Bitmap
			generateImageThumbnail(int resourceId, int width, int height) {
		Bitmap scaledBitmap = MultiBackgroundUtilities.scaleDownImageAndDecode(
				getResources(), resourceId, width, height);
		return scaledBitmap;
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
		MultiBackgroundImage newMbi = null;
		try {
			newMbi = databaseHelper.addMultiBackgroundImage(imagePath);
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
		int sourceIndex = sourceTargetViewIndices[0];
		int targetIndex = sourceTargetViewIndices[1];

		if (sourceIndex < 0 || targetIndex < 0) {
			Log.w(TAG,
					"Couldn't find the desired views. Drag is not a valid drag.");
			return;
		}

		if (sourceIndex == targetIndex) {
			Log.w(TAG,
					"The image and source views the are same. So no need to update any positions");
			return;
		}

		MultiBackgroundImage sourceMbi = mbiList.get(sourceIndex);
		MultiBackgroundImage targetMbi = mbiList.get(targetIndex);
		boolean isReorderResultSuccessful = databaseHelper.reorderImages(
				sourceMbi, targetMbi, sourceTargetViewIndices);
		if (isReorderResultSuccessful) {
			linearLayoutInsideHsv.removeView(sourceView);
			linearLayoutInsideHsv.addView(sourceView, targetIndex);
			imageViewList.remove(sourceIndex);
			imageViewList.add(targetIndex, sourceView);

			if (sourceIndex != 0) {
				mbiList.get(sourceIndex - 1).setNextImageNumber(
						sourceMbi.getNextImageNumber());
			}
			if (sourceIndex < targetIndex) {
				sourceMbi.setNextImageNumber(targetMbi.getNextImageNumber());
				targetMbi.setNextImageNumber(sourceMbi.get_id());

				mbiList.add(targetIndex, sourceMbi);
				mbiList.remove(sourceIndex);				
			} else {
				sourceMbi.setNextImageNumber(targetMbi.getNextImageNumber());
				if(targetIndex != 0) {
					mbiList.get(targetIndex -1).setNextImageNumber(sourceMbi.get_id());
				}
				mbiList.remove(sourceIndex);
				mbiList.add(targetIndex, sourceMbi);
			}

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
		indices[0] = getIndexOfImageView(sourceView);
		indices[1] = getIndexOfImageView(targetView);
		if (indices[0] > -1 && indices[1] > -1) {
			Log.d(TAG, "Found the source View index as: " + indices[0]
					+ " and targetView index as: " + indices[1]);
		}
		return indices;
	}

	private int getIndexOfImageView(ImageView imageView) {
		for (int i = 0; i < imageViewList.size(); i++) {
			ImageView currentView = imageViewList.get(i);
			if (currentView.equals(imageView)) {
				return i;
			}
		}
		Log.e(TAG, "Unable to find the index of the given image");
		return -1;
	}

	public void scrollHorizontalScrollView(int scrollBy) {
		hsv.smoothScrollBy(scrollBy, 0);
	}

	public void changeDeleteImageView(int imageId) {
		deleteImageView.setImageResource(imageId);
	}

	public void changeDeleteImageViewVisibilty(int visibility) {
		deleteImageView.setVisibility(visibility);
	}

	public void deleteImage(ImageView imageToBeDeleted) {
		int indexOfImage = getIndexOfImageView(imageToBeDeleted);
		MultiBackgroundImage mbiToBeDeleted = mbiList.get(indexOfImage);
		if (!databaseHelper.deleteMultibackgroundImage(mbiToBeDeleted)) {
			Toast.makeText(getApplicationContext(),
					"Unable to delete the desired image", Toast.LENGTH_SHORT)
					.show();
		} else {
			removeImageView(imageToBeDeleted, indexOfImage);
		}
	}

	private void addImageView(MultiBackgroundImage mbi, ImageView imageView) {
		linearLayoutInsideHsv.addView(imageView);
		imageViewList.add(imageView);
		if(mbiList.size() > 0) {
			mbiList.get(mbiList.size() -1).setNextImageNumber(mbi.get_id());
		}
		mbiList.add(mbi);
	}

	private void removeImageView(ImageView imageToBeDeleted, int indexOfImage) {
		linearLayoutInsideHsv.removeView(imageToBeDeleted);
		imageViewList.remove(indexOfImage);
		if (indexOfImage > 0) {
			/*
			 * Update the nextImageNumber of the previous object to point to the
			 * nextImage pointed by imageToBeDeleted's nextImageNumber. If its
			 * the first element, then no need to update the nextImage number
			 */
			MultiBackgroundImage mbiToBeDeleted = mbiList.get(indexOfImage);
			mbiList.get(indexOfImage - 1).setNextImageNumber(
					mbiToBeDeleted.getNextImageNumber());
		}
		mbiList.remove(indexOfImage);
	}

	private void removeAllImageViews() {
		linearLayoutInsideHsv.removeAllViews();
		imageViewList.clear();
		mbiList.clear();
	}

	public HorizontalScrollView getHorizontalScrollView() {
		return hsv;
	}
}