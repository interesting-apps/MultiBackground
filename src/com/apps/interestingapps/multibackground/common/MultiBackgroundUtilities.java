package com.apps.interestingapps.multibackground.common;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Log;

public class MultiBackgroundUtilities {

	private static final String TAG = "MultiBackgroundUtilities";

	public static Bitmap scaleDownImageAndDecode(String imagePath,
			int maxWidth,
			int maxHeight) throws OutOfMemoryError {
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
		 } while(compressedBitmap == null && retry < 5);

		Bitmap resizedAndRoatedBitmap = resizeBitmapAndCorrectBitmapOrientation(
				imagePath, compressedBitmap, maxWidth, maxHeight);
		return resizedAndRoatedBitmap;
	}

	public static Bitmap scaleDownImageAndDecode(Resources res,
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

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		int retry = 0;
		do {
			 try {
				 compressedBitmap = BitmapFactory.decodeResource(res, resourceId,
							options);
				 retry = 5;
			 } catch (OutOfMemoryError oom) {
				 options.inSampleSize *= 2;
				Log.i(TAG, "Increased the inSample size by 2 times to: "
						+ options.inSampleSize);
				 retry++;		 
			 }
		 } while(compressedBitmap == null && retry < 5);
		

		Bitmap resizedBitmap = Bitmap.createScaledBitmap(compressedBitmap, maxWidth, maxHeight, true);
		if(resizedBitmap != compressedBitmap) {
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
	 * This method recycles the original bitmap. So, after this method is used, the calling method cannot use original bitmap anymore. This is to prevent OutOfMemory errors
	 * @param pathToImage
	 * @param originalBitmap
	 * @param maxWidth
	 * @param maxHeight
	 * @return
	 */
	private static Bitmap
			resizeBitmapAndCorrectBitmapOrientation(String pathToImage,
					Bitmap originalBitmap,
					int maxWidth,
					int maxHeight) {
		Matrix rotationMatrix = new Matrix();
		int rotationRequired = getRequiredimageRotation(pathToImage);
		rotationMatrix.postRotate(rotationRequired);
		Bitmap roatedBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.getWidth(),
						originalBitmap.getHeight(), rotationMatrix, true);
		if (roatedBitmap != originalBitmap) {
			originalBitmap.recycle();
		}
		Bitmap scaledBitmap = Bitmap.createScaledBitmap(roatedBitmap, maxWidth, maxHeight, true);
		if(scaledBitmap != roatedBitmap) {
			roatedBitmap.recycle();
		}
		return scaledBitmap; 
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
			latestMbi = nextImageNumberToMbiMap.get((int) latestMbi.get_id());
		}
		Log.i(TAG, "Total size of the list is: " + imageListFromEnd.size());
		return CommonUtilities.reverseList(imageListFromEnd);
	}
}
