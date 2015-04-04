package com.apps.interestingapps.multibackground.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.util.Log;

import com.apps.interestingapps.multibackground.common.MultiBackgroundImage.ImageSize;

public class MultiBackgroundUtilities {

	private static final String TAG = "MultiBackgroundUtilities";

	public static Bitmap scaleDownImageAndDecode(String imagePath,
			int maxWidth,
			int maxHeight,
			ImageSize imageSize) throws OutOfMemoryError {
		Bitmap compressedBitmap = null;
		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(imagePath, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, maxWidth,
				maxHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		int retry = 0;
		do {
			try {
				compressedBitmap = BitmapFactory.decodeFile(imagePath, options);
				retry = 5;
			} catch (OutOfMemoryError oom) {
				options.inSampleSize *= 2;
				Log.i(TAG, "Increased the inSample size by 2 times to: "
						+ options.inSampleSize);
				retry++;
			}
		} while (compressedBitmap == null && retry < 5);

		Bitmap resizedAndRoatedBitmap = resizeBitmapAndCorrectBitmapOrientation(
				imagePath, compressedBitmap, options, maxWidth, maxHeight,
				imageSize);
		return resizedAndRoatedBitmap;
	}

	public static Bitmap scaleDownImageAndDecode(Resources res,
			int resourceId,
			int maxWidth,
			int maxHeight) {
		return scaleDownImageAndDecode(res, ImageSize.BEST_FIT, resourceId,
				maxWidth, maxHeight);
	}

	public static Bitmap scaleDownImageAndDecode(Resources res,
			ImageSize imageSize,
			int resourceId,
			int maxWidth,
			int maxHeight) {
		Bitmap compressedBitmap = null;

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, resourceId, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, maxWidth,
				maxHeight);
		double aspectRatio = options.outWidth * 1.0 / options.outHeight;

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		int retry = 0;
		do {
			try {
				compressedBitmap = BitmapFactory.decodeResource(res,
						resourceId, options);
				retry = 5;
			} catch (OutOfMemoryError oom) {
				options.inSampleSize *= 2;
				Log.i(TAG, "Increased the inSample size by 2 times to: "
						+ options.inSampleSize);
				retry++;
			}
		} while (compressedBitmap == null && retry < 5);

		int[] scaledWidthHeight = getScaledWidthHeight(compressedBitmap,
				imageSize, aspectRatio, maxWidth, maxHeight);
		Bitmap resizedBitmap = Bitmap.createScaledBitmap(compressedBitmap,
				scaledWidthHeight[0], scaledWidthHeight[1], true);
		if (resizedBitmap != compressedBitmap) {
			compressedBitmap.recycle();
		}
		return resizedBitmap;
	}


	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth,
			int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			// Calculate the largest inSampleSize value that is a power of 2 and
			// keeps both
			// height and width larger than the requested height and width.
			while ((height / inSampleSize) > reqHeight
					&& (width / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}
		return inSampleSize;
	}

	/**
	 * This method recycles the original bitmap. So, after this method is used,
	 * the calling method cannot use original bitmap anymore. This is to prevent
	 * OutOfMemory errors
	 *
	 * @param pathToImage
	 * @param originalBitmap
	 * @param maxWidth
	 * @param maxHeight
	 * @return
	 */
	private static Bitmap
			resizeBitmapAndCorrectBitmapOrientation(String pathToImage,
					Bitmap originalBitmap,
					BitmapFactory.Options options,
					int maxWidth,
					int maxHeight,
					MultiBackgroundImage.ImageSize imageSize) {
		Matrix rotationMatrix = new Matrix();
		int rotationRequired = getRequiredimageRotation(pathToImage);
		rotationMatrix.postRotate(rotationRequired);
		Bitmap rotatedBitmap = Bitmap.createBitmap(originalBitmap, 0, 0,
				originalBitmap.getWidth(), originalBitmap.getHeight(),
				rotationMatrix, true);
		if (rotatedBitmap != originalBitmap) {
			originalBitmap.recycle();
		}
		int[] scaledWidthHeight = getScaledWidthHeight(rotatedBitmap,
				imageSize, getImageAspectRatio(options, rotationRequired),
				maxWidth, maxHeight);
		Bitmap scaledBitmap = Bitmap.createScaledBitmap(rotatedBitmap,
				scaledWidthHeight[0], scaledWidthHeight[1], true);
		if (scaledBitmap != rotatedBitmap) {
			rotatedBitmap.recycle();
		}
		return scaledBitmap;
	}

	public static int[] getScaledWidthHeight(Bitmap bitmap,
			ImageSize imageSize,
			double aspectRatio,
			int maxWidth,
			int maxHeight) {
		int[] widthHeight = new int[] { bitmap.getWidth(), bitmap.getHeight() };

		if (imageSize == MultiBackgroundImage.ImageSize.COVER_FULL_SCREEN) {
			widthHeight[0] = maxWidth;
			widthHeight[1] = maxHeight;
		} else if (aspectRatio > 0) {
			widthHeight[0] = maxWidth;
			int tempHeight = (int) (maxWidth / aspectRatio);

			if (tempHeight > maxHeight) {
				tempHeight = maxHeight;
				widthHeight[0] = (int) (tempHeight * aspectRatio);
			}
			widthHeight[1] = tempHeight;
		}

		return widthHeight;

	}

	public static int getRequiredimageRotation(String pathToImage) {
		int rotate = 0;
		try {
			File imageFile = new File(pathToImage);

			ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
			int orientation = exif.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);

			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_270:
				rotate = 270;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				rotate = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_90:
				rotate = 90;
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rotate;
	}

	/**
	 * Creates a list of images in the order from first image(The one whose _id
	 * will not be in any other's nextImageNumber) to the last image (the one
	 * whose nextImageNumber is
	 * MultiBackgroundConstants.DEFAULT_NEXT_IMAGE_NUMBER
	 *
	 * @param nextImageNumberToMbiMap
	 * @return
	 */
	public static List<MultiBackgroundImage>
			getImagesFromMap(Map<Integer, MultiBackgroundImage> nextImageNumberToMbiMap) {
		ArrayList<MultiBackgroundImage> imageListFromEnd = new ArrayList<MultiBackgroundImage>();
		MultiBackgroundImage latestMbi = nextImageNumberToMbiMap
				.get(MultiBackgroundConstants.DEFAULT_NEXT_IMAGE_NUMBER);
		/*
		 * Get the images in reverse order from last to first. The frist image
		 * will be the one whose ID will not be stored as nextImageNumber in any
		 * of the rows
		 */
		while (latestMbi != null) {
			imageListFromEnd.add(latestMbi);
			latestMbi = nextImageNumberToMbiMap.get(latestMbi.get_id());
		}
		Log.i(TAG, "Total size of the list is: " + imageListFromEnd.size());
		return CommonUtilities.reverseList(imageListFromEnd);
	}

	/**
	 * Get the aspect ratio of a resource
	 *
	 * @param res
	 * @param resourceId
	 * @return
	 */
	public static double getImageAspectRatio(Resources res, int resourceId) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		Bitmap bm = BitmapFactory.decodeResource(res, resourceId, options);
		if (bm == null) {
			return -1;
		}
		return options.outWidth * 1.0 / options.outHeight;
	}

	public static double getImageAspectRatio(String path) {
		double aspectRatio = -1;
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);
		if (options.outWidth <= 0 || options.outHeight <= 0) {
			Log.e(TAG, "Error opening image file for path: " + path);
			aspectRatio = -1;
		} else {
			int rotationRequired = MultiBackgroundUtilities
					.getRequiredimageRotation(path);
			aspectRatio = MultiBackgroundUtilities.getImageAspectRatio(options,
					rotationRequired);
		}
		return aspectRatio;
	}

	public static double getImageAspectRatio(BitmapFactory.Options options,
			int rotationRequired) {
		double aspectRatio = -1;
		if (rotationRequired == 90 || rotationRequired == 270) {
			aspectRatio = options.outHeight * 1.0 / options.outWidth;
		} else {
			aspectRatio = options.outWidth * 1.0 / options.outHeight;
		}
		return aspectRatio;
	}

	public static String generateLocalFilePath(Context context,
			String localImageFileName) {
		String localFileName = new StringBuilder("").append(
				context.getFilesDir()).append(File.separator).append(
				localImageFileName).toString();
		return localFileName;
	}

	/**
	 *
	 * @param imageId
	 * @param format
	 *            the format with which the file is stored starting with a ".".
	 *            For example: ".png" or ".jpeg" etc.
	 * @return
	 */
	public static String
			generateFileNameFromImageId(int imageId, String format) {
		String localFileName = new StringBuilder("").append(imageId).append(
				format).toString();
		return localFileName;
	}

	public static Bitmap createLocalImageAndSave(Context context,
			DatabaseHelper databaseHelper,
			int screenX,
			int screenY,
			CropButtonDimensions cbd,
			MultiBackgroundImage mbi) {
		if (mbi.isImagePathRowUpdated() == 0) {
			return null;
		}
		Bitmap scaledBitmap = null;
		MultiBackgroundImage.ImageSize imageSize = null;
		String imagePath = mbi.getPath();
		Rect srcBitmapRectangle = null;
		int cbdLength = 0;
		int cbdHeight = 0;
		try {
			imageSize = mbi.getImageSize();
			scaledBitmap = MultiBackgroundUtilities.scaleDownImageAndDecode(
					imagePath, screenX, screenY, imageSize);
			if (imageSize == ImageSize.CROP_IMAGE) {
				MultiBackgroundCropRectangle rect = databaseHelper
						.getCropRectangle(mbi.get_id());
				if (rect != null) {
					if (cbd == null || cbd.getCropButtonLength() == 0
							|| cbd.getCropButtonHeight() == 0) {
						cbd = databaseHelper.getCropButtonDimensions();
					}

					if (cbd != null) {
						cbdLength = cbd.getCropButtonLength() / 2;
						cbdHeight = cbd.getCropButtonHeight() / 2;
					}
					int cropLeft = rect.getCropLeft()
							- rect.getImageOffsetLeft();
					int cropTop = rect.getCropTop() - rect.getImageOffsetTop();
					double lengthScaling = 2 * ((1.0 * scaledBitmap.getWidth() / 2) / (scaledBitmap
							.getWidth() / 2 - cbdLength * 2));
					double heightScaling = 2 * ((1.0 * scaledBitmap.getHeight() / 2) / (scaledBitmap
							.getHeight() / 2 - cbdHeight * 2));

					srcBitmapRectangle = new Rect(
							(int) ((cropLeft) * lengthScaling),
							(int) ((cropTop) * heightScaling),
							(int) (((cropLeft + rect.getCropLength())) * lengthScaling),
							(int) (((cropTop + rect.getCropHeight())) * heightScaling));
					Bitmap croppedBitmap = Bitmap.createBitmap(scaledBitmap,
							srcBitmapRectangle.left, srcBitmapRectangle.top,
							srcBitmapRectangle.right - srcBitmapRectangle.left,
							srcBitmapRectangle.bottom - srcBitmapRectangle.top);
					if (croppedBitmap != scaledBitmap) {
						scaledBitmap.recycle();
					}
					Bitmap finalScaledBitmap = Bitmap.createScaledBitmap(
							croppedBitmap, screenX, screenY, true);
					if (finalScaledBitmap != croppedBitmap) {
						croppedBitmap.recycle();
					}
					scaledBitmap = finalScaledBitmap;
				}
			}
		} catch (Exception e) {
			Log.d(TAG, "Unable to load image for the given path due to: " + e);
		}
		if (scaledBitmap == null) {
			/*
			 * If scaled bitmap is null, then don't store anything in DB. just
			 * show the default image whenever there is a missing entry in the
			 * "Local_image_path" table.
			 */
			return null;
		}
		try {
			createImageFileFromBitmap(context, databaseHelper, screenX,
					screenY, mbi, scaledBitmap);
		} catch (Exception e) {
			Log.e(TAG, "Error occurred while adding local image file: "
					+ e.getMessage());
		}
		return scaledBitmap;
	}

	private static void createImageFileFromBitmap(final Context context,
			final DatabaseHelper databaseHelper,
			int screenX,
			int screenY,
			final MultiBackgroundImage mbi,
			Bitmap srcBitmap) throws IOException {

		final int imageId = mbi.get_id();
		final Bitmap copiedBitmap = srcBitmap.copy(srcBitmap.getConfig(), true);
		// Handler handler = new Handler(Looper.getMainLooper());
		// handler.postDelayed(new Runnable() {
		new Thread(new Runnable() {
			public void run() {

				String localImageFileName = MultiBackgroundUtilities
						.generateFileNameFromImageId(imageId,
								MultiBackgroundConstants.LOCAL_IMAGE_FORMAT);
				OutputStream outStream = null;
				String localImagePath = null;
				int isImageOnExternalStorage = 0;
				try {
					/*
					 * Attempt to store the bitmap on external storage first. If
					 * this fails due to some reason, store the bitmap on
					 * internal storage.
					 */
					if (MultiBackgroundStorageUtilities
							.isExternalStorageAvailable()
							&& !MultiBackgroundStorageUtilities
									.isExternalStorageReadOnly()) {
						File outFile = new File(context
								.getExternalFilesDir(null), localImageFileName);
						if (outFile != null) {
							outStream = new FileOutputStream(outFile);
							isImageOnExternalStorage = 1;
							localImagePath = outFile.getAbsolutePath();
							Log.d(TAG,
									"Using external storage to store local image");
						}
					}
					/*
					 * If SD card is not mounted or there was an issue in
					 * opening the file, use internal storage
					 */
					if (isImageOnExternalStorage <= 0) {
						outStream = context.openFileOutput(localImageFileName,
								Context.MODE_PRIVATE);
						localImagePath = MultiBackgroundUtilities
								.generateLocalFilePath(context,
										localImageFileName);
						isImageOnExternalStorage = 0;
						Log.d(TAG,
								"Using internal storage to store local image");
					}
					copiedBitmap.compress(Bitmap.CompressFormat.JPEG, 90,
							outStream);
					Log.d(TAG, "Copied the image file locally.");
					databaseHelper.addLocalmagePathToDatabase(mbi.get_id(),
							localImagePath, isImageOnExternalStorage);
					databaseHelper.setImagePathRowUpdated(mbi.get_id(), 0);
					mbi.setImagePathRowUpdated(0);
					Log.d(TAG, "Saved local image file for image with ID: "
							+ mbi.get_id());
					/*
					 * Delete the file from internal storage if external storage
					 * file is stored successfully.
					 */
					if (isImageOnExternalStorage > 0) {
						String internalStorageFilePath = MultiBackgroundUtilities
								.generateLocalFilePath(context,
										localImageFileName);
						File file = new File(internalStorageFilePath);
						if (file.exists()) {
							file.delete();
						}
					}
				} catch (Exception e) {
					Log.e(TAG, "Unable to copy image file locally: "
							+ e.getMessage());
				} finally {
					if (outStream != null) {
						try {
							outStream.flush();
							outStream.close();
						} catch (IOException e) {
							Log.e(TAG, "Unable to close output stream");
						}
						if (copiedBitmap != null) {
							copiedBitmap.recycle();
						}
					}
				}
				try {
					String oldFormatFileName = MultiBackgroundUtilities
							.generateFileNameFromImageId(
									imageId,
									MultiBackgroundConstants.OLD_LOCAL_IMAGE_FORMAT);
					File externalFile = new File(context.getExternalFilesDir(null),
							oldFormatFileName);
					if (externalFile != null && externalFile.exists()) {
						externalFile.delete();
						Log.d(TAG,
								"Succesfully old format file from External storage.");
					}
					String internalStorageFilePath = MultiBackgroundUtilities
							.generateLocalFilePath(context, oldFormatFileName);
					File internalFile = new File(internalStorageFilePath);
					if (internalFile != null && internalFile.exists()) {
						internalFile.delete();
						Log.d(TAG,
								"Succesfully old format file from Internal storage.");
					}
				} catch (Exception e) {
					Log.e(TAG, "Failed to delete old fromat file from storage.");
				}
			}
		}).start();
	}
}
