package com.apps.interestingapps.multibackground;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.WallpaperManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.apps.interestingapps.multibackground.common.CropButtonDimensions;
import com.apps.interestingapps.multibackground.common.DatabaseHelper;
import com.apps.interestingapps.multibackground.common.MultiBackgroundConstants;
import com.apps.interestingapps.multibackground.common.MultiBackgroundCropRectangle;
import com.apps.interestingapps.multibackground.common.MultiBackgroundImage;
import com.apps.interestingapps.multibackground.common.MultiBackgroundImage.ImageSize;
import com.apps.interestingapps.multibackground.common.MultiBackgroundUtilities;
import com.apps.interestingapps.multibackground.common.SaveLocalImageAsyncTask;
import com.apps.interestingapps.multibackground.listeners.AddImageClickListener;
import com.apps.interestingapps.multibackground.listeners.CropButtonTouchListener;
import com.apps.interestingapps.multibackground.listeners.CropButtonTouchListener.CropButtonPosition;
import com.apps.interestingapps.multibackground.listeners.CropRectangleTouchListener;
import com.apps.interestingapps.multibackground.listeners.DragToDeleteListener;
import com.apps.interestingapps.multibackground.listeners.MbiDragListener;
import com.apps.interestingapps.multibackground.listeners.MbiLongClickListener;
import com.apps.interestingapps.multibackground.listeners.MbiOnClickListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

/**
 * Class to Set the live wallpaper and select images to be used in the live
 * wallpaper
 */
@SuppressLint("NewApi")
public class SetWallpaperActivity extends Activity {

	private DatabaseHelper databaseHelper;
	private List<ImageView> imageViewList;
	private List<MultiBackgroundImage> mbiList;
	private LinearLayout linearLayoutInsideHsv;
	private static final String TAG = "SetWallpaperActivity";
	private ImageView plusImageView, currentImageView;
	private HorizontalScrollView hsv;
	private ImageView deleteImageView;
	private int screenWidth, screenHeight, quarterScreenWidth,
			quarterScreenHeight;
	private int halfScreenWidth, halfScreenHeight;
	private RadioGroup radioGroup;
	private MultiBackgroundImage currentSelectedMbi, previousSelectedMbi;
	private AdView adview;
	private RelativeLayout parentCropRelativeLayout;
	private RelativeLayout centralImageViewRelativeLayout;
	private ImageView previousClickedImageView;
	private int beforePauseClickedImageIndex = 0;
	private Button setWallpaperButton;
	private ImageView longClickedImageView;
	private ImageView leftTopCropButton, leftBottomCropButton,
			rightTopCropButton, rightBottomCropButton;
	private ImageView cropRectangleImageView;
	private boolean moveRectangleLeftToCenterOfButton = false;
	private boolean insertCropButtonDimensionsIntoDB = false;
	private CropButtonDimensions cropButtonDimensions;
	private boolean layoutPaddingSet = false;
	private boolean onCreateCalled = false;
	private int cbdLength = 0, cbdHeight = 0;
	private String extStorageDirectory = null;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		Display display = getWindowManager().getDefaultDisplay();
		screenWidth = display.getWidth();
		screenHeight = display.getHeight();
		quarterScreenWidth = screenWidth / 4;
		quarterScreenHeight = screenHeight / 4;
		halfScreenWidth = screenWidth / 2;
		halfScreenHeight = screenHeight / 2;

		databaseHelper = DatabaseHelper.initializeDatabase(this);
		cropButtonDimensions = databaseHelper.getCropButtonDimensions();
		if (cropButtonDimensions == null
				|| cropButtonDimensions.getCropButtonLength() == 0
				|| cropButtonDimensions.getCropButtonHeight() == 0) {
			insertCropButtonDimensionsIntoDB = true;
		}

		initializeStaticViews();

		extStorageDirectory = Environment.getExternalStorageDirectory()
				.toString();
		SharedPreferences prefs = getSharedPreferences(
				MultiBackgroundConstants.PREFERENCES_FILE_NAME, 0);
		onCreateCalled = true;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (databaseHelper != null) {
			databaseHelper.closeDatabase();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (previousClickedImageView != null) {
			beforePauseClickedImageIndex = getIndexOfImageView(previousClickedImageView);
		} else {
			beforePauseClickedImageIndex = 0;
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		setWallpaperButton
				.setBackgroundResource(R.drawable.set_wallpaper_button);
		imageViewList = new ArrayList<ImageView>();
		mbiList = new ArrayList<MultiBackgroundImage>();
		getAllImages();
		parentCropRelativeLayout.getLayoutParams().width = halfScreenWidth;
		parentCropRelativeLayout.getLayoutParams().height = halfScreenHeight;

		if (mbiList.size() > 0) {
			if (beforePauseClickedImageIndex >= mbiList.size()) {
				beforePauseClickedImageIndex = 0;
			}
			MbiOnClickListener onClick = new MbiOnClickListener(this, mbiList
					.get(beforePauseClickedImageIndex), previousSelectedMbi,
					currentImageView, mbiList.get(beforePauseClickedImageIndex)
							.getPath(), halfScreenWidth, halfScreenHeight,
					radioGroup);
			onClick.onClick(imageViewList.get(beforePauseClickedImageIndex));
		}
		/*
		 * Android 4.0 device id: 64FFE02AABF389054771188E3CF39B63
		 *
		 * Sony Xperia X10 device id: 080A4A2357E9089FDAB344624A7181F5
		 *
		 * Nexus 4 - Varun's - device id: 7A107DF0AB377695D8973481767E5A76
		 */
		adview = (AdView) findViewById(R.id.adView);

		// /*
		// * TODO: Uncomment it while running tests. Comment this part while
		// * creating APK for production
		// */
		// AdRequest adRequest = new AdRequest.Builder().addTestDevice(
		// AdRequest.DEVICE_ID_EMULATOR).addTestDevice(
		// "64FFE02AABF389054771188E3CF39B63").build();

		AdRequest adRequest = new AdRequest.Builder().build();

		adview.loadAd(adRequest);
		if (onCreateCalled) {
			onCreateCalled = false;
			showRateDialog();
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		if (insertCropButtonDimensionsIntoDB) {
			if (cropButtonDimensions != null) {
				if (cropButtonDimensions.getCropButtonLength() == 0
						|| cropButtonDimensions.getCropButtonHeight() == 0) {
					databaseHelper.deleteCropButtonDimensions();
				}
			}
			cropButtonDimensions = databaseHelper
					.addCropButtonDimensions(leftTopCropButton.getWidth(),
							leftTopCropButton.getHeight());
			insertCropButtonDimensionsIntoDB = false;
		}

		if (moveRectangleLeftToCenterOfButton) {
			RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) cropRectangleImageView
					.getLayoutParams();
			setImagePosition(cropRectangleImageView, layoutParams.leftMargin
					+ (cropButtonDimensions.getCropButtonLength() / 2),
					layoutParams.topMargin
							+ (cropButtonDimensions.getCropButtonHeight() / 2));
			moveRectangleLeftToCenterOfButton = false;
		}
		if (!layoutPaddingSet) {
			if (cropButtonDimensions != null) {
				int leftAndRightPadding = cropButtonDimensions
						.getCropButtonLength() / 2;
				int topAndBottomPadding = cropButtonDimensions
						.getCropButtonHeight() / 2;
				centralImageViewRelativeLayout.setPadding(leftAndRightPadding,
						topAndBottomPadding, leftAndRightPadding,
						topAndBottomPadding);
				layoutPaddingSet = true;
			}
		}

	}

	private void initializeStaticViews() {
		setWallpaperButton = (Button) findViewById(R.id.button1);
		linearLayoutInsideHsv = (LinearLayout) findViewById(R.id.linearLayoutInsideHsv);
		plusImageView = (ImageView) findViewById(R.id.plusImageView);
		plusImageView.setOnClickListener(new AddImageClickListener(this));
		hsv = (HorizontalScrollView) findViewById(R.id.horizontalScrollView);
		deleteImageView = (ImageView) findViewById(R.id.deleteImageView);
		if (Build.VERSION.SDK_INT >= 11) {
			// Drag and Drop is available after API level 11
			deleteImageView.setOnDragListener(new DragToDeleteListener(this));
		}
		currentImageView = (ImageView) findViewById(R.id.cropCurrentImageView);
		radioGroup = (RadioGroup) findViewById(R.id.radio_image_size_group);
		parentCropRelativeLayout = (RelativeLayout) findViewById(R.id.cropImageRelativeLayout);
		centralImageViewRelativeLayout = (RelativeLayout) findViewById(R.id.cropImageRelLayout);
		initializeCropRectangleAndButtons();
	}

	private void initializeCropRectangleAndButtons() {
		cropRectangleImageView = (ImageView) findViewById(R.id.cropRectangle);
		Drawable cropRectangleDrawable = getResources().getDrawable(
				R.drawable.crop_rectangle);

		leftTopCropButton = (ImageView) findViewById(R.id.cropLeftTopButton);
		leftTopCropButton.setOnTouchListener(new CropButtonTouchListener(
				CropButtonPosition.LEFT_TOP, leftTopCropButton,
				cropRectangleImageView, currentImageView,
				cropRectangleDrawable, this, parentCropRelativeLayout));

		leftBottomCropButton = (ImageView) findViewById(R.id.cropLeftBottomButton);
		leftBottomCropButton.setOnTouchListener(new CropButtonTouchListener(
				CropButtonPosition.LEFT_BOTTOM, leftBottomCropButton,
				cropRectangleImageView, currentImageView,
				cropRectangleDrawable, this, parentCropRelativeLayout));

		rightTopCropButton = (ImageView) findViewById(R.id.cropRightTopButton);
		rightTopCropButton.setOnTouchListener(new CropButtonTouchListener(
				CropButtonPosition.RIGHT_TOP, rightTopCropButton,
				cropRectangleImageView, currentImageView,
				cropRectangleDrawable, this, parentCropRelativeLayout));

		rightBottomCropButton = (ImageView) findViewById(R.id.cropRightBottomButton);
		rightBottomCropButton.setOnTouchListener(new CropButtonTouchListener(
				CropButtonPosition.RIGHT_BOTTOM, rightBottomCropButton,
				cropRectangleImageView, currentImageView,
				cropRectangleDrawable, this, parentCropRelativeLayout));

		cropRectangleImageView
				.setOnTouchListener(new CropRectangleTouchListener(
						currentImageView, leftTopCropButton,
						leftBottomCropButton, rightTopCropButton,
						rightBottomCropButton, this, parentCropRelativeLayout));
	}

	/**
	 * Method to create a context menu when a list item pressed for long
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu,
			View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (Build.VERSION.SDK_INT < 11) {
			// Drag and Drop is not available before API level 11, so provide
			// context menu to delete image
			longClickedImageView = (ImageView) v;
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.mbi_context_menu, menu);

		}
	}

	/**
	 * Method to perform an action when an item on the context menu list is
	 * clicked
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.deleteMenuItem:
			if (longClickedImageView != null) {
				deleteImage(longClickedImageView);
				longClickedImageView = null;
			} else {
				Log.d(TAG, "Long clicked image is null");
			}
			return true;
		default:
			return super.onContextItemSelected(item);
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
		removeAllImageViews();
		List<MultiBackgroundImage> allImages = databaseHelper.getAllImages();
		final Bitmap[] imageThumbnails = new Bitmap[allImages.size()];
		final Thread[] thumbNailThreads = new Thread[allImages.size()];
		boolean errorInStartingThread = false;
		for (int i = 0; i < imageThumbnails.length; i++) {
			final MultiBackgroundImage image = allImages.get(i);
			final int x = i;
			Thread t = new Thread(new Runnable() {
				public void run() {
					Bitmap bitmap = generateImageThumbnail(image,
							quarterScreenWidth);
					imageThumbnails[x] = bitmap;
				}

			});
			thumbNailThreads[x] = t;
			try {
				t.start();
			} catch (Exception e) {
				Log.d(TAG, "Error occurred while starting thread.");
				errorInStartingThread = true;
			}
		}
		if (errorInStartingThread) {
			/*
			 * Switch back to old way of adding images serially.
			 */
			for (MultiBackgroundImage image : allImages) {
				addImageToHorizontalLayout(image, null);
			}
		} else {
			for (int i = 0; i < imageThumbnails.length; i++) {
				try {
					thumbNailThreads[i].join();
					addImageToHorizontalLayout(allImages.get(i),
							imageThumbnails[i]);
				} catch (InterruptedException e) {
					Log.d(TAG, "Unable to wait for thread number: " + i
							+ " to finish.");
					addImageToHorizontalLayout(allImages.get(i), null);
				}
			}
		}
	}

	public void onClick(View view) {
		Toast.makeText(getApplicationContext(),
				"Please select Multiple Wallpapers from the shown list.",
				Toast.LENGTH_LONG).show();
		setWallpaperButton
				.setBackgroundResource(R.drawable.set_wallpaper_button_pressed);
		Intent intent = new Intent(
				WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER);
		startActivity(intent);
	}

	/**
	 * Method to perform operation once an activity has been started
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		setWallpaperButton
				.setBackgroundResource(R.drawable.set_wallpaper_button);
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == MultiBackgroundConstants.SELECT_PICTURE_ACTIVITY) {
				Uri selectedImageUri = data.getData();
				if (selectedImageUri != null
						&& selectedImageUri.toString().length() > 0) {
					Log.i(TAG, "URI: " + selectedImageUri.toString());
					addNewImage(selectedImageUri);
				}
			}
		}
	}

	/**
	 * Gets path of image on Android version less than Kitkat or API 19
	 *
	 * @param uri
	 * @return
	 */
	private String getPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = getContentResolver().query(uri, projection, null, null,
				null);

		if (cursor != null && cursor.moveToFirst()) {
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			return cursor.getString(column_index);
		} else {
			Log.e(TAG, "Path not recognized");
			return "";
		}

	}

	/**
	 * Get a file path from a Uri. This will get the the path for Storage Access
	 * Framework Documents, as well as the _data field for the MediaStore and
	 * other file-based ContentProviders.
	 *
	 * @param context
	 *            The context.
	 * @param uri
	 *            The Uri to query.
	 * @author paulburke
	 */
	public String getPath(final Context context, final Uri uri) {

		final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

		// DocumentProvider
		if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
			// ExternalStorageProvider
			if (isExternalStorageDocument(uri)) {
				Log.i(TAG, "External Document");
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				if ("primary".equalsIgnoreCase(type)) {
					return Environment.getExternalStorageDirectory() + "/"
							+ split[1];
				}

				// TODO handle non-primary volumes
			}
			// DownloadsProvider
			else if (isDownloadsDocument(uri)) {
				Log.i(TAG, "Downloads  Document");
				final String id = DocumentsContract.getDocumentId(uri);
				final Uri contentUri = ContentUris.withAppendedId(Uri
						.parse("content://downloads/public_downloads"), Long
						.valueOf(id));

				return getDataColumn(context, contentUri, null, null);
			}
			// MediaProvider
			else if (isMediaDocument(uri)) {
				Log.i(TAG, "Media Document");
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				Uri contentUri = null;
				if ("image".equals(type)) {
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				} else if ("video".equals(type)) {
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				} else if ("audio".equals(type)) {
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}

				final String selection = "_id=?";
				final String[] selectionArgs = new String[] { split[1] };

				return getDataColumn(context, contentUri, selection,
						selectionArgs);
			}
		}
		// MediaStore (and general)
		else if ("content".equalsIgnoreCase(uri.getScheme())) {
			Log.i(TAG, "Media Store Document");
			// Return the remote address
			if (isGooglePhotosUri(uri)) {
				Log.i(TAG, "Google URI");
				return uri.getLastPathSegment();
			}

			return getDataColumn(context, uri, null, null);
		}
		// File
		else if ("file".equalsIgnoreCase(uri.getScheme())) {
			Log.i(TAG, "Files  Document");
			return uri.getPath();
		}

		return null;
	}

	/**
	 * Get the value of the data column for this Uri. This is useful for
	 * MediaStore Uris, and other file-based ContentProviders.
	 *
	 * @param context
	 *            The context.
	 * @param uri
	 *            The Uri to query.
	 * @param selection
	 *            (Optional) Filter used in the query.
	 * @param selectionArgs
	 *            (Optional) Selection arguments used in the query.
	 * @return The value of the _data column, which is typically a file path.
	 */
	public static String getDataColumn(Context context,
			Uri uri,
			String selection,
			String[] selectionArgs) {

		Cursor cursor = null;
		final String column = "_data";
		final String[] projection = { column };

		try {
			cursor = context.getContentResolver().query(uri, projection,
					selection, selectionArgs, null);
			if (cursor != null && cursor.moveToFirst()) {
				final int index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(index);
			}
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return null;
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is ExternalStorageProvider.
	 */
	public static boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri
				.getAuthority());
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is DownloadsProvider.
	 */
	public static boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri
				.getAuthority());
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is MediaProvider.
	 */
	public static boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri
				.getAuthority());
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is Google Photos.
	 */
	public static boolean isGooglePhotosUri(Uri uri) {
		return "com.google.android.apps.photos.content".equals(uri
				.getAuthority());
	}

	/**
	 * Updates the list of current Images in database and adds the provided
	 * image to the Horizontal Scroll View at the end
	 *
	 * @param imagePath
	 */
	private void addImageToHorizontalLayout(MultiBackgroundImage mbi,
			Bitmap bitmap) {
		ImageView iv = new ImageView(getApplicationContext());
		iv.setPadding(5, 5, 5, 5);
		if (bitmap == null) {
			bitmap = generateImageThumbnail(mbi, quarterScreenWidth);
		}
		iv.setImageBitmap(bitmap);
		if (Build.VERSION.SDK_INT >= 11) {
			// Drag and Drop is available after API level 11
			iv.setOnDragListener(new MbiDragListener(this));
			iv.setOnLongClickListener(new MbiLongClickListener(this));
		} else {
			// Register for context menu to enable deletion of image
			registerForContextMenu(iv);
		}
		iv.setOnClickListener(new MbiOnClickListener(this, mbi,
				currentSelectedMbi, currentImageView, mbi.getPath(),
				halfScreenWidth, halfScreenHeight, radioGroup));
		addImageView(mbi, iv);
	}

	private Bitmap generateImageThumbnail(MultiBackgroundImage mbi, int width) {
		Bitmap scaledBitmap = null;
		try {
			scaledBitmap = MultiBackgroundUtilities.scaleDownImageAndDecode(mbi
					.getPath(), width, width, ImageSize.COVER_FULL_SCREEN);
		} catch (OutOfMemoryError oom) {
			Toast.makeText(this, "Image is very large to load. ",
					Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Log.d(TAG, "Unable to create bitmap for the given path due to " + e);
		}
		if (scaledBitmap == null) {
			Log.w(TAG,
					"Unable to load image from the given path. Loading the default image:");
			scaledBitmap = generateImageThumbnail(R.drawable.image_not_found,
					quarterScreenWidth);
			mbi.setDeletedImage(true);
		}
		return scaledBitmap;
	}

	private Bitmap generateImageThumbnail(int resourceId, int width) {
		Bitmap scaledBitmap = MultiBackgroundUtilities.scaleDownImageAndDecode(
				getResources(), resourceId, width, width);
		return scaledBitmap;
	}

	/**
	 * Method to add an image to the database and current list of Image Views
	 *
	 * @param imageUri
	 */
	public void addNewImage(Uri imageUri) {
		String imagePath = getPath(imageUri);
		// String imagePath = getPath(getApplicationContext(), imageUri);
		if (imagePath == null || imagePath.length() == 0) {
			Toast.makeText(getApplicationContext(),
					"Unable to open selected image", Toast.LENGTH_LONG).show();
			return;
		}
		addNewImage(imagePath);
	}

	public void addNewImage(String imagePath) {
		MultiBackgroundImage newMbi = null;
		try {
			newMbi = databaseHelper.addMultiBackgroundImage(imagePath);
			if (newMbi == null) {
				Toast.makeText(
						getApplicationContext(),
						"Maximum allowed images ("
								+ MultiBackgroundConstants.MAX_IMAGES
								+ ") are already added to list.",
						Toast.LENGTH_SHORT).show();
			} else {
				addImageToHorizontalLayout(newMbi, null);

				MbiOnClickListener imageOnClickListener = new MbiOnClickListener(
						this, newMbi, currentSelectedMbi, currentImageView,
						newMbi.getPath(), halfScreenWidth, halfScreenHeight,
						radioGroup);
				imageOnClickListener.onClick(imageViewList.get(imageViewList
						.size() - 1));

				hsv.postDelayed(new Runnable() {
					public void run() {
						hsv.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
					}
				}, 100L);
				beforePauseClickedImageIndex = mbiList.size() - 1;
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
				if (targetIndex != 0) {
					mbiList.get(targetIndex - 1).setNextImageNumber(
							sourceMbi.get_id());
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
			if (currentSelectedMbi != null
					&& currentSelectedMbi.equals(mbiToBeDeleted)) {
				Log.d(TAG, "MBI to be deleted is currently selected");
				if (mbiList.size() > 0) {
					if (indexOfImage == 0) {
						MbiOnClickListener onClick = new MbiOnClickListener(
								this, mbiList.get(0), null, currentImageView,
								mbiList.get(0).getPath(), halfScreenWidth,
								halfScreenHeight, radioGroup);
						onClick.onClick(imageViewList.get(0));
						Log.i(TAG, "Image with index 0 deleted.");
					} else {
						MbiOnClickListener onClick = new MbiOnClickListener(
								this, mbiList.get(indexOfImage - 1), null,
								currentImageView, mbiList.get(indexOfImage - 1)
										.getPath(), halfScreenWidth,
								halfScreenHeight, radioGroup);
						onClick.onClick(imageViewList.get(indexOfImage - 1));
					}
				} else {
					currentImageView.setVisibility(View.INVISIBLE);
					radioGroup.setVisibility(View.INVISIBLE);
					hideCropButtonsAndRectangle();
				}
			}

		}
	}

	private void addImageView(MultiBackgroundImage mbi, ImageView imageView) {
		linearLayoutInsideHsv.addView(imageView);
		imageViewList.add(imageView);
		if (mbiList.size() > 0) {
			mbiList.get(mbiList.size() - 1).setNextImageNumber(mbi.get_id());
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

	public void setCurrentSelectedMbi(MultiBackgroundImage mbi) {
		if (currentSelectedMbi != null) {
			// MultiBackgroundImage cloneOfPreviousMbi = currentSelectedMbi
			// .clone();
			if (currentSelectedMbi.isImagePathRowUpdated() > 0) {
				new SaveLocalImageAsyncTask(getApplicationContext(),
						databaseHelper, currentSelectedMbi, screenWidth,
						screenHeight, cropButtonDimensions).execute();
			}
		}
		currentSelectedMbi = mbi;
	}

	public MultiBackgroundImage getCurrentSelectedMbi() {
		return currentSelectedMbi;
	}

	/**
	 * OnClick listener for Radio buttons.
	 *
	 * @param view
	 */
	public void onRadioButtonClicked(View view) {
		// Is the button now checked?
		boolean checked = ((RadioButton) view).isChecked();
		ImageSize selectedImageSize = ImageSize.BEST_FIT;
		// Check which radio button was clicked
		switch (view.getId()) {
		case R.id.radio_cover_full_screen:
			if (checked) {
				selectedImageSize = ImageSize.COVER_FULL_SCREEN;
				hideCropButtonsAndRectangle();
			}
			break;
		case R.id.radio_best_fit:
			if (checked) {
				selectedImageSize = ImageSize.BEST_FIT;
				hideCropButtonsAndRectangle();
			}
			break;
		case R.id.radio_crop_image:
			if (checked) {
				selectedImageSize = ImageSize.CROP_IMAGE;
			}
		}

		if (currentSelectedMbi == null) {
			Log.e(TAG, "Current selected image is null.");
		} else {
			if (currentSelectedMbi.getImageSize() != selectedImageSize) {
				databaseHelper.updateImageSize(currentSelectedMbi.get_id(),
						selectedImageSize);
				currentSelectedMbi.setImageSize(selectedImageSize);
				currentSelectedMbi.setImagePathRowUpdated(1);
				Bitmap sourceBitmap = ((BitmapDrawable) currentImageView
						.getDrawable()).getBitmap();
				int[] scaledWidthHeight = MultiBackgroundUtilities
						.getScaledWidthHeight(sourceBitmap, selectedImageSize,
								currentSelectedMbi.getAspectRatio(),
								halfScreenWidth, halfScreenHeight);
				Bitmap scaledCurrentImageViewBitmap = Bitmap
						.createScaledBitmap(sourceBitmap, scaledWidthHeight[0],
								scaledWidthHeight[1], true);
				currentImageView.setImageBitmap(scaledCurrentImageViewBitmap);
				if (scaledCurrentImageViewBitmap != sourceBitmap) {
					sourceBitmap.recycle();
				}
				if (selectedImageSize == ImageSize.CROP_IMAGE) {
					showCropButtonsAndRectangle(currentSelectedMbi.get_id(),
							scaledWidthHeight);
				}
			} else {
				Log.d(TAG, "The current image size is already selected.");
			}
		}
	}

	public void
			showCropButtonsAndRectangle(int imageId, int[] scaledWidthHeight) {
		MultiBackgroundCropRectangle cropRectangleDetails = databaseHelper
				.getCropRectangle(imageId);
		setCropButtonPositions(imageId, scaledWidthHeight, cropRectangleDetails);
	}

	private void setCropButtonPositions(int imageId,
			int[] scaledWidthHeight,
			MultiBackgroundCropRectangle cropRectangleDetails) {
		if (cropRectangleDetails == null) {
			// Set default location and write to database these default
			// locations so that next click
			// can use this information.
			RelativeLayout.LayoutParams relLayoutParams = (RelativeLayout.LayoutParams) parentCropRelativeLayout
					.getLayoutParams();
			int imageCenterLeft = relLayoutParams.width / 2;
			int imageCenterTop = relLayoutParams.height / 2;
			int defaultRectangleLeft = imageCenterLeft
					- (scaledWidthHeight[0] / 4);
			int defaultRectangleTop = imageCenterTop
					- (scaledWidthHeight[1] / 4);
			int imageOffsetLeft = imageCenterLeft - (scaledWidthHeight[0] / 2);
			int imageOffsetTop = imageCenterTop - (scaledWidthHeight[1] / 2);

			cropRectangleDetails = databaseHelper.addCropImageData(imageId,
					defaultRectangleLeft, defaultRectangleTop,
					scaledWidthHeight[0] / 2, scaledWidthHeight[1] / 2,
					imageOffsetLeft, imageOffsetTop);
			if (cropRectangleDetails == null) {
				Toast.makeText(getApplicationContext(),
						"Unable to crop the image.", Toast.LENGTH_LONG).show();
				return;
			}
			Log.i(TAG, cropRectangleDetails.toString());
		}
		setImagePosition(leftTopCropButton, cropRectangleDetails.getCropLeft(),
				cropRectangleDetails.getCropTop());
		setImagePosition(leftBottomCropButton, cropRectangleDetails
				.getCropLeft(), cropRectangleDetails.getCropTop()
				+ cropRectangleDetails.getCropHeight());
		setImagePosition(rightTopCropButton, cropRectangleDetails.getCropLeft()
				+ cropRectangleDetails.getCropLength(), cropRectangleDetails
				.getCropTop());
		setImagePosition(rightBottomCropButton, cropRectangleDetails
				.getCropLeft()
				+ cropRectangleDetails.getCropLength(), cropRectangleDetails
				.getCropTop()
				+ cropRectangleDetails.getCropHeight());
		leftTopCropButton.setVisibility(View.VISIBLE);
		leftBottomCropButton.setVisibility(View.VISIBLE);
		rightBottomCropButton.setVisibility(View.VISIBLE);
		rightTopCropButton.setVisibility(View.VISIBLE);

		/*
		 * Draw the rectangle
		 */
		if (cropRectangleDetails.getCropLength() > 0
				&& cropRectangleDetails.getCropHeight() > 0) {
			Drawable cropRectangleDrawable = getResources().getDrawable(
					R.drawable.crop_rectangle);
			Bitmap cropRectangleBitmap = Bitmap.createScaledBitmap(
					((BitmapDrawable) cropRectangleDrawable).getBitmap(),
					cropRectangleDetails.getCropLength(), cropRectangleDetails
							.getCropHeight(), true);

			cropRectangleImageView.setImageBitmap(cropRectangleBitmap);
			if (cropButtonDimensions != null) {
				setImagePosition(
						cropRectangleImageView,
						cropRectangleDetails.getCropLeft()
								+ (cropButtonDimensions.getCropButtonLength() / 2),
						cropRectangleDetails.getCropTop()
								+ (cropButtonDimensions.getCropButtonHeight() / 2));
			} else {
				moveRectangleLeftToCenterOfButton = true;
			}
			/*
			 * Now we have the details of the crop rectangle
			 */
			cropRectangleImageView.setVisibility(View.VISIBLE);
		} else {
			cropRectangleImageView.setVisibility(View.INVISIBLE);
		}
	}

	private void setImagePosition(ImageView imageView, int left, int top) {
		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) imageView
				.getLayoutParams();
		layoutParams.leftMargin = left;
		layoutParams.topMargin = top;
		imageView.setLayoutParams(layoutParams);
	}

	public void hideCropButtonsAndRectangle() {
		leftTopCropButton.setVisibility(View.INVISIBLE);
		leftBottomCropButton.setVisibility(View.INVISIBLE);
		rightBottomCropButton.setVisibility(View.INVISIBLE);
		rightTopCropButton.setVisibility(View.INVISIBLE);
		cropRectangleImageView.setVisibility(View.INVISIBLE);
	}

	public ImageView getPreviousClickedImageView() {
		return previousClickedImageView;
	}

	public void setPreviousClickedImageView(ImageView previousClickedImageView) {
		this.previousClickedImageView = previousClickedImageView;
	}

	/**
	 * Update the launch count and show rate dialog, if the count equals the
	 * preset value
	 */
	private void showRateDialog() {
		SharedPreferences prefs = getSharedPreferences(
				MultiBackgroundConstants.PREFERENCES_FILE_NAME, 0);
		if (prefs.getBoolean(getResources().getString(
				R.string.never_show_again_preference_string), false)) {
			return;
		}
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(getResources().getString(
				R.string.never_show_again_preference_string), false);
		editor.commit();
		// Increment launch counter
		long launchCount = prefs.getLong(getResources().getString(
				R.string.launch_count_preference_string), 0) + 1;
		Log.i(this.getLocalClassName(), "Number of Launches = "
				+ getResources().getInteger(R.integer.number_of_launches));
		if (launchCount >= getResources().getInteger(
				R.integer.number_of_launches)) {
			showRateDialog(SetWallpaperActivity.this, editor);
		} else {
			editor.putLong(getResources().getString(
					R.string.launch_count_preference_string), launchCount);
			editor.commit();
		}
	}

	/**
	 * Show the rate dialog
	 *
	 * @param mContext
	 * @param editor
	 */
	private void showRateDialog(final Context mContext,
			final SharedPreferences.Editor editor) {
		final Dialog dialog = new Dialog(mContext);
		dialog.setTitle(getResources()
				.getString(R.string.title_rate_app_dialog));
		dialog.getWindow().setBackgroundDrawableResource(R.color.Black);

		LayoutInflater inflater = getLayoutInflater();

		final View dialogView = inflater
				.inflate(R.layout.rate_app_dialog, null);
		dialog.setContentView(dialogView);

		Button rateAppButton = (Button) dialogView
				.findViewById(R.id.rateAppButton);
		rateAppButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				/*
				 * Set the launch count to negative so that it takes double to
				 * show the rate app dialog next time, if the use clicks on Rate
				 * button. This is to make sure that if the user clicks on Rate
				 * button, but does not actually rates it, the user would be
				 * asked again. But if the user has rated the App, he/she can
				 * then click on never rate again to stop the dialog to appear.
				 */
				editor.putLong(getResources().getString(
						R.string.launch_count_preference_string), (-1)
						* getResources().getInteger(
								R.integer.number_of_launches));
				editor.commit();
				mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri
						.parse("market://details?id="
								+ MultiBackgroundConstants.APP_PACKAGE)));

				dialog.dismiss();
			}
		});

		Button rateAppLaterButton = (Button) dialogView
				.findViewById(R.id.rateAppLaterButton);
		rateAppLaterButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				editor.putLong(getResources().getString(
						R.string.launch_count_preference_string), 0);
				editor.commit();
				dialog.dismiss();
			}
		});

		Button neverShowAgainButton = (Button) dialogView
				.findViewById(R.id.neverShowAgainButton);
		neverShowAgainButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (editor != null) {
					editor.putBoolean(getResources().getString(
							R.string.never_show_again_preference_string), true);
					editor.commit();
				}
				dialog.dismiss();
			}
		});
		dialog.show();
	}

	public synchronized void updateCropRectangleCoordinates() {
		RelativeLayout.LayoutParams leftTopButtonLayoutParams = (RelativeLayout.LayoutParams) leftTopCropButton
				.getLayoutParams();
		RelativeLayout.LayoutParams rightBottomButtonLayoutParams = (RelativeLayout.LayoutParams) rightBottomCropButton
				.getLayoutParams();
		int cropRectangleLength = rightBottomButtonLayoutParams.leftMargin
				- leftTopButtonLayoutParams.leftMargin;
		int cropRectangleHeight = rightBottomButtonLayoutParams.topMargin
				- leftTopButtonLayoutParams.topMargin;
		int updateResult = databaseHelper.updateCropRectangleDetails(
				currentSelectedMbi.get_id(),
				leftTopButtonLayoutParams.leftMargin,
				leftTopButtonLayoutParams.topMargin, cropRectangleLength,
				cropRectangleHeight);
		currentSelectedMbi.setImagePathRowUpdated(1);

		if (updateResult < 1) {
			Log.e(TAG, "Unable to update crop rectangle details");
		}
	}

	public MultiBackgroundImage getPreviousSelectedMbi() {
		return previousSelectedMbi;
	}

	public void
			setPreviousSelectedMbi(MultiBackgroundImage previousSelectedMbi) {
		this.previousSelectedMbi = previousSelectedMbi;
	}
}