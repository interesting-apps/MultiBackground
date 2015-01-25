package com.apps.interestingapps.multibackground;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import com.apps.interestingapps.multibackground.common.CropButtonDimensions;
import com.apps.interestingapps.multibackground.common.DatabaseHelper;
import com.apps.interestingapps.multibackground.common.MultiBackgroundImage;
import com.apps.interestingapps.multibackground.common.MultiBackgroundImage.ImageSize;
import com.apps.interestingapps.multibackground.common.MultiBackgroundLocalImage;
import com.apps.interestingapps.multibackground.common.MultiBackgroundUtilities;

public class MyWallpaperService extends WallpaperService {

	private static Random rand = new Random();

	@Override
	public Engine onCreateEngine() {
		return new MyWallpaperEngine();
	}

	private class MyWallpaperEngine extends Engine {
		private final Handler handler = new Handler();
		private float downX, upX;
		private final double THRESHOLD_FOR_SCREEN_CHANGE = 0.48;
		private final double SCREEN_COVERAGE_FACTOR = 0.45;
		private int screenX, screenY;
		private boolean changeBackground = false;
		private double screenCoverageRequired;
		private DatabaseHelper databaseHelper;
		private List<MultiBackgroundImage> imageList;
		private List<Integer> usedIntegers;
		private int currentImageNumber = 0;
		private static final String TAG = "MyWallpaperService";
		private float actualDistanceX;
		private Bitmap currentBitmap;
		private CropButtonDimensions cbd;
		private int cbdLength = 0, cbdHeight = 0;
		private Map<Integer, Bitmap> leftMiddleAndRightBitmaps = new HashMap<Integer, Bitmap>();

		private int imageOrder = 1;

		private final Runnable drawRunner = new Runnable() {

			public void run() {
				draw();
			}

		};
		private boolean visible = true;

		@SuppressWarnings("deprecation")
		public MyWallpaperEngine() {
			downX = 0;
			upX = 0;
			WindowManager wm = (WindowManager) getApplicationContext()
					.getSystemService(Context.WINDOW_SERVICE);
			Display display = wm.getDefaultDisplay();

			screenX = display.getWidth();
			screenY = display.getHeight();
			screenCoverageRequired = screenX * SCREEN_COVERAGE_FACTOR;
		}

		@Override
		public void onCreate(SurfaceHolder surfaceHolder) {
			super.onCreate(surfaceHolder);
			setTouchEventsEnabled(true);
			databaseHelper = DatabaseHelper
					.initializeDatabase(getApplicationContext());
			if (databaseHelper != null) {
				imageList = databaseHelper.getAllImages();
			}
			changeBackground = true;
			usedIntegers = new ArrayList<Integer>();
			Log.i(TAG, "MyWallpaperService On create called");
			handler.post(drawRunner);
		}

		@Override
		public void onVisibilityChanged(boolean visible) {
			this.visible = visible;
			if (visible) {
				SharedPreferences prefs = PreferenceManager
						.getDefaultSharedPreferences(MyWallpaperService.this);
				if (Build.VERSION.SDK_INT >= 11) {
					imageOrder = Integer.parseInt(prefs.getString(
							getApplicationContext().getResources().getString(
									R.string.preference_key), "1"));
				} else {
					// Default number for API < 11 is Random
					imageOrder = Integer.parseInt(prefs.getString(
							getApplicationContext().getResources().getString(
									R.string.preference_key), "2"));
				}
				handler.post(drawRunner);
			} else {
				handler.removeCallbacks(drawRunner);
			}
		}

		@Override
		public void onDestroy() {
			if (databaseHelper != null) {
				databaseHelper.closeDatabase();
				handler.removeCallbacks(drawRunner);
			}
		}

		@Override
		public void onSurfaceDestroyed(SurfaceHolder holder) {
			super.onSurfaceDestroyed(holder);

			this.visible = false;
			handler.removeCallbacks(drawRunner);
		}

		@Override
		public void onSurfaceChanged(SurfaceHolder holder,
				int format,
				int width,
				int height) {
			super.onSurfaceChanged(holder, format, width, height);
		}

		@Override
		public void onTouchEvent(MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_UP:
				upX = event.getX();
				long upTime = event.getEventTime();
				long downTime = event.getDownTime();
				long timeDiff = upTime - downTime;
				actualDistanceX = upX - downX;
				float absoluteDistanceX = Math.abs(actualDistanceX);
				if (timeDiff != 0 && absoluteDistanceX != 0) {
					double threshold = absoluteDistanceX / timeDiff;

					if (absoluteDistanceX >= screenCoverageRequired
							|| threshold >= THRESHOLD_FOR_SCREEN_CHANGE) {
						/*
						 * TODO: take into consideration :
						 *
						 * 1. long press can be done before/after down/up events
						 * --> Checked if distance is greater than half the
						 * screen size, it will change the home screen
						 *
						 * 2. Current screen can be the last screen in the
						 * direction of movement --> Currently couldn't find a
						 * better way, so changing the feature of the app.
						 *
						 * 3. Multitouch events
						 */
						changeBackground = true;
						draw();
					}
				}
				break;
			case MotionEvent.ACTION_DOWN:
				downX = event.getX();
				break;
			}
			super.onTouchEvent(event);
		}

		private void draw() {
			SurfaceHolder holder = getSurfaceHolder();
			Canvas canvas = null;
			if (changeBackground) {
				try {
					canvas = holder.lockCanvas();
					if (canvas != null) {
						changeBackground(canvas);
					}
				} finally {
					if (canvas != null)
						holder.unlockCanvasAndPost(canvas);
				}
			}
			handler.removeCallbacks(drawRunner);
			if (visible) {
				handler.postDelayed(drawRunner, 50000);
			}
		}

		// Surface view requires that all elements are drawn completely
		private void changeBackground(Canvas canvas) {
			if (changeBackground) {
				Bitmap scaledBitmap = null;

				// /*
				// * TODO: Try to optimize this, so that if recycling is not
				// * required, we can skip this
				// */
				// if (currentBitmap != null) {
				// currentBitmap.recycle();
				// }
				scaledBitmap = updateBitmapMap(actualDistanceX < 0);
				changeBackground = false;
				canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);
				if (scaledBitmap != null) {
					int wallpaperX = screenX / 2 - scaledBitmap.getWidth() / 2;
					int wallpaperY = screenY / 2 - scaledBitmap.getHeight() / 2;
					canvas.drawBitmap(scaledBitmap, wallpaperX > 0 ? wallpaperX
							: 0, wallpaperY > 0 ? wallpaperY : 0, null);
					currentBitmap = scaledBitmap;
				}
			}
		}

		private void getNextImageNumber() {
			if (databaseHelper.isDatabaseUpdated()) {
				imageList = databaseHelper.getAllImages();
				databaseHelper.setDatabaseUpdated(false);
				leftMiddleAndRightBitmaps.put(1, null);
				leftMiddleAndRightBitmaps.put(2, null);
				leftMiddleAndRightBitmaps.put(3, null);
			}
			if (imageList.size() == 0) {
				currentImageNumber = -1;
			} else {
				int newImageNumber = 0;
				switch (imageOrder) {
				case 1:
					// Maintain image order
					int delta = -1;
					if (actualDistanceX < 0) {
						delta = 1;
					}
					newImageNumber = currentImageNumber + delta;
					break;

				case 2:
					// Random order
					newImageNumber = getRandomNumber(imageList, usedIntegers);
					break;
				default:
					break;
				}
				if (newImageNumber < 0) {
					newImageNumber = imageList.size() - 1;
				}
				currentImageNumber = newImageNumber % imageList.size();
			}

		}

		private int getRandomNumber(List<MultiBackgroundImage> mbiList,
				List<Integer> usedIntegers) {
			if (usedIntegers.size() >= mbiList.size()) {
				Log.d(TAG, "Showed all the images. Resetting the list");
				usedIntegers.clear();
			}
			int high = mbiList.size() - 1;
			int low = 0;
			int randomNumber = 0;
			while (usedIntegers.contains(randomNumber)) {
				randomNumber = rand.nextInt((high - low) + 1) + low;
			}
			usedIntegers.add(randomNumber);
			return randomNumber;

		}

		private Bitmap updateBitmapMap(boolean isMoveToRight) {
			getNextImageNumber();
			if (imageList.size() > 3) {
				if (isMoveToRight) {
					Bitmap bitmapToRecycle = leftMiddleAndRightBitmaps.get(1);

					leftMiddleAndRightBitmaps.put(1, leftMiddleAndRightBitmaps
							.get(2));
					leftMiddleAndRightBitmaps.put(2, leftMiddleAndRightBitmaps
							.get(3));

					if (bitmapToRecycle != null) {
						bitmapToRecycle.recycle();
						leftMiddleAndRightBitmaps.put(1, null);
					}
					final int rightImageNumber = getRightImageNumber(currentImageNumber);
					new Thread(new Runnable() {
						public void run() {
							Bitmap bitmap = readBitmapToMemory(rightImageNumber);
							leftMiddleAndRightBitmaps.put(3, bitmap);
						}
					}).start();
				} else {
					Bitmap bitmapToRecycle = leftMiddleAndRightBitmaps.get(3);
					leftMiddleAndRightBitmaps.put(3, leftMiddleAndRightBitmaps
							.get(2));
					leftMiddleAndRightBitmaps.put(2, leftMiddleAndRightBitmaps
							.get(1));
					if (bitmapToRecycle != null) {
						bitmapToRecycle.recycle();
						leftMiddleAndRightBitmaps.put(3, null);
					}
					final int leftImageNumber = getLeftImageNumber(currentImageNumber);
					new Thread(new Runnable() {
						public void run() {
							Bitmap bitmap = readBitmapToMemory(leftImageNumber);
							leftMiddleAndRightBitmaps.put(1, bitmap);
						}
					}).start();
				}
				Bitmap result = leftMiddleAndRightBitmaps.get(2);
				if (result == null) {
					result = readBitmapToMemory(currentImageNumber);
					leftMiddleAndRightBitmaps.put(2, result);
				} else if (result.isRecycled()) {
					Log.i(TAG, "Using recycled Bitmap");
					result = readBitmapToMemory(currentImageNumber);
				}
				return result;
			} else {
				int mapKey = currentImageNumber + 1;
				Bitmap nextBitmap = leftMiddleAndRightBitmaps.get(mapKey);
				if (nextBitmap == null) {
					nextBitmap = readBitmapToMemory(currentImageNumber);
					leftMiddleAndRightBitmaps.put(mapKey, nextBitmap);
				}
				return nextBitmap;
			}
		}

		private int getLeftImageNumber(int imageNumber) {
			if (imageList.size() == 0 || imageNumber < 0) {
				return -1;
			}
			switch (imageOrder) {
			case 1:
				if (imageNumber == 0) {
					imageNumber = imageList.size();
				}
				return imageNumber - 1;
			case 2:
				// Random order
				return getRandomNumber(imageList, usedIntegers);
			default:
				return -1;
			}

		}

		private int getRightImageNumber(int imageNumber) {
			if (imageList.size() == 0 || imageNumber < 0) {
				return -1;
			}

			switch (imageOrder) {
			case 1:
				return (imageNumber + 1) % imageList.size();
			case 2:
				// Random order
				return getRandomNumber(imageList, usedIntegers);
			default:
				return -1;
			}
		}

		private Bitmap readBitmapToMemory(int imageNumber) {
			Bitmap scaledBitmap = null;
			ImageSize imageSize = null;
			if (imageList.size() != 0 && imageNumber != -1) {
				MultiBackgroundImage mbi = imageList.get(imageNumber);
				int imageId = mbi.get_id();
				imageSize = mbi.getImageSize();
				boolean isBitmapLoaded = false;
				if (mbi.isImagePathRowUpdated() > 0) {
					scaledBitmap = MultiBackgroundUtilities
							.createLocalImageAndSave(getApplicationContext(),
									databaseHelper, screenX, screenY, cbd, mbi);
					if (scaledBitmap != null) {
						isBitmapLoaded = true;
					}

				}
				if (!isBitmapLoaded) {
					MultiBackgroundLocalImage mbli = databaseHelper
							.getLocalImagePath(imageId);
					if (mbli == null) {
						try {
							mbi.setImagePathRowUpdated(1);
							scaledBitmap = MultiBackgroundUtilities
									.createLocalImageAndSave(
											getApplicationContext(),
											databaseHelper, screenX, screenY,
											cbd, mbi);
						} catch (Exception e) {
							Log.d(TAG,
									"Unable to create local for the bitmap due to: "
											+ e);
						}
					} else {
						try {
							scaledBitmap = MultiBackgroundUtilities
									.scaleDownImageAndDecode(mbli
											.getLocalImagePath(), screenX,
											screenY, imageSize);
						} catch (Exception e) {

							if (mbli.isImageOnExternalStorage() > 0) {
								Log.d(TAG,
										"Unable to read local image from external storage due to: "
												+ e);
								/*
								 * Failed to load image from external storage.
								 * Save it to internal storage and load the
								 * bitmap
								 */
								mbi.setImagePathRowUpdated(1);
								try {
									scaledBitmap = MultiBackgroundUtilities
											.createLocalImageAndSave(
													getApplicationContext(),
													databaseHelper, screenX,
													screenY, cbd, mbi);
								} catch (Exception e1) {
									Log.d(TAG, "Unable to load image from "
											+ "internal storage due to: " + e1);
								}
							} else {
								Log.d(TAG, "Unable to load image from "
										+ "internal storage due to: " + e);
							}
						}
					}
				}
				if (scaledBitmap == null) {
					scaledBitmap = MultiBackgroundUtilities
							.scaleDownImageAndDecode(getResources(),
									R.drawable.default_wallpaper, screenX,
									screenY);
				}
			} else {
				/*
				 * Show some default background if there is some problem in
				 * opening database
				 */
				Log.i(TAG, "CurrentImageNumber : " + imageNumber);
				scaledBitmap = MultiBackgroundUtilities
						.scaleDownImageAndDecode(getResources(),
								R.drawable.default_wallpaper, screenX, screenY);

			}
			return scaledBitmap;
		}
	}
}