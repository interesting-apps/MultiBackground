package com.apps.interestingapps.multibackground.common;

import java.io.File;
import java.io.FileOutputStream;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Log;

public class MultiBackgroundUtilities {

	// private static final String TAG = "MultiBackgroundUtilities";

	public static Bitmap scaleDownImageAndDecode(String imagePath,
			int requiredWidth,
			int requiredHeight) {
		Bitmap scaledImageBitmap = null;

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(imagePath, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, requiredWidth,
				requiredHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		scaledImageBitmap = BitmapFactory.decodeFile(imagePath, options);
		return scaledImageBitmap;
	}

	public static Bitmap scaleDownImageAndDecode(Resources res,
			int resourceId,
			int requiredWidth,
			int requiredHeight) {
		Bitmap scaledImageBitmap = null;

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, resourceId, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, requiredWidth,
				requiredHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		scaledImageBitmap = BitmapFactory.decodeResource(res, resourceId,
				options);
		return scaledImageBitmap;
	}

	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth,
			int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and
			// keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight
					&& (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}

	public static Bitmap
			resizeBitmapAndCorrectBitmapOrientation(String pathToImage,
					Bitmap originalBitmap,
					int desiredWidth,
					int desiredHeight) {
		// Matrix matrix = getResizeBitmapMatrix(originalBitmap, desiredWidth,
		// desiredHeight);
		int rotationRequired = getRequiredimageRotation(pathToImage);
		Matrix matrix = new Matrix();
		matrix.postRotate(rotationRequired);
		return Bitmap.createBitmap(originalBitmap, 0, 0, desiredWidth,
				desiredHeight, matrix, true);
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

			Log.i("RotateImage", "Exif orientation: " + orientation);
			Log.i("RotateImage", "Rotate value: " + rotate);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rotate;
	}

	public static Matrix getResizeBitmapMatrix(Bitmap source,
			int newWidth,
			int newHeight) {
		int sourceWidth = source.getWidth();
		int sourceHeight = source.getHeight();
		float xScale = (float) newWidth / sourceWidth;
		float yScale = (float) newHeight / sourceHeight;
		float scale = Math.max(xScale, yScale);

		// get the resulting size after scaling
		float scaledWidth = scale * sourceWidth;
		float scaledHeight = scale * sourceHeight;

		// figure out where we should translate to
		float dx = (newWidth - scaledWidth) / 2;
		float dy = (newHeight - scaledHeight) / 2;

		Matrix matrix = new Matrix();
		matrix.postScale(scale, scale);
		matrix.postTranslate(dx, dy);
		return matrix;
	}

	/**
	 * Resizes the given Bitmap to the give height and width by keeping the
	 * aspect ratio same
	 * 
	 * @param source
	 * @param newWidth
	 * @param newHeight
	 */
	public static void resizeBitmap(Bitmap source, int newWidth, int newHeight) {
		Matrix matrix = getResizeBitmapMatrix(source, newWidth, newHeight);
		Canvas canvas = new Canvas(source);
		canvas.drawBitmap(source, matrix, null);
	}
}
