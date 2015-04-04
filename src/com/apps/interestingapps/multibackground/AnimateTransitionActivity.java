package com.apps.interestingapps.multibackground;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.apps.interestingapps.multibackground.common.AnimationDetails;
import com.apps.interestingapps.multibackground.common.AnimationDetailsAdapter;
import com.apps.interestingapps.multibackground.common.AnimationGroupDetails;
import com.apps.interestingapps.multibackground.common.DatabaseHelper;
import com.apps.interestingapps.multibackground.common.MultiBackgroundImage.ImageSize;
import com.apps.interestingapps.multibackground.common.MultiBackgroundUtilities;
import com.apps.interestingapps.multibackground.listeners.AnimationDemoImageViewTouchListener;

public class AnimateTransitionActivity extends Activity {
	private final static String TAG = "AnimateTransitionActvity";

	private DatabaseHelper databaseHelper;
	private ListView animationDetailsListView;
	private ArrayAdapter<AnimationDetails> animationDetailsArrayAdapter;
	private boolean isListShown = true;
	private Animation animationInForDemoImageView,
			animationOutForDemoImageView;
	private Animation animationInForDetailsListView,
			animationOutForDetailsListView;
	private int screenWidth, screenHeight;
	private List<Bitmap> demoImageBitmapList = new ArrayList<Bitmap>();
	private AnimationDetails currentSelectedAnimationDetails;
	private SurfaceView animationDemoImageSurfaceView;
	private RelativeLayout animationDemoRelativeLayout;
	private ImageView animationDemoTransitionImageView;
	private int currentSelectedBitmapIndex = 0;

	private class ViewTranslateAnimationListener implements AnimationListener {

		public void onAnimationStart(Animation animation) {
		}

		public void onAnimationRepeat(Animation animation) {
		}

		public void onAnimationEnd(Animation animation) {
			if (isListShown) {
				if (animationDetailsListView != null) {
					animationDetailsListView.setVisibility(View.VISIBLE);
				}
				if (animationDemoTransitionImageView != null) {
					animationDemoTransitionImageView.setVisibility(View.GONE);
				}
			} else {
				if (animationDetailsListView != null) {
					animationDetailsListView.setVisibility(View.GONE);
				}
				if (animationDemoImageSurfaceView != null) {
					animationDemoImageSurfaceView.setVisibility(View.VISIBLE);
				}
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.animation_display_view);
		// this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		try {
			databaseHelper = DatabaseHelper.initializeDatabase(this);
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(),
					"Unable to get the list of available Animation.",
					Toast.LENGTH_LONG).show();
		}
		if (databaseHelper == null) {
			finish();
		}

		initializeListAndImageViewTransition();
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		screenWidth = metrics.widthPixels;
		screenHeight = metrics.heightPixels;

		Map<Integer, AnimationGroupDetails> animationGroupIdToObjectMap = databaseHelper
				.getAllAnimationGroupDetails();
		final List<AnimationDetails> allAnimationDetails = databaseHelper
				.getAllAnimationDetails(animationGroupIdToObjectMap);

		animationDetailsListView = (ListView) findViewById(R.id.animationListView);
		animationDemoTransitionImageView = (ImageView) findViewById(R.id.animationDemoTransitionImageView);
		animationDemoRelativeLayout = (RelativeLayout) findViewById(R.id.animationDemoRelativeLayout);

		Collections.sort(allAnimationDetails);
		Log.i(TAG, "Total records: " + allAnimationDetails.size());
		animationDetailsArrayAdapter = new AnimationDetailsAdapter(
				getApplicationContext(), allAnimationDetails);
		animationDetailsListView.setAdapter(animationDetailsArrayAdapter);
		animationDetailsListView
				.setOnItemClickListener(new OnItemClickListener() {
					public void onItemClick(AdapterView<?> arg0,
							View arg1,
							int itemNumber,
							long arg3) {
						currentSelectedAnimationDetails = allAnimationDetails
								.get(itemNumber);
						databaseHelper
								.updateSelectedAnimationDetails(allAnimationDetails
										.get(itemNumber));
						animationDemoImageSurfaceView = new SurfaceView(
								AnimateTransitionActivity.this);
						animationDemoImageSurfaceView
								.setBackgroundResource(R.drawable.animation_demo_1);
						animationDemoRelativeLayout
								.addView(animationDemoImageSurfaceView);
						animationDemoImageSurfaceView
								.setOnTouchListener(new AnimationDemoImageViewTouchListener(
										AnimateTransitionActivity.this,
										demoImageBitmapList, screenWidth,
										screenHeight));
						isListShown = false;
						animationDemoImageSurfaceView
								.startAnimation(animationInForDemoImageView);
						animationDetailsListView
								.startAnimation(animationOutForDetailsListView);
					}
				});
		demoImageBitmapList
				.add(MultiBackgroundUtilities.scaleDownImageAndDecode(
						getResources(), ImageSize.COVER_FULL_SCREEN,
						R.drawable.animation_demo_1, screenWidth, screenHeight));
		demoImageBitmapList
				.add(MultiBackgroundUtilities.scaleDownImageAndDecode(
						getResources(), ImageSize.BEST_FIT,
						R.drawable.animation_demo_2, screenWidth, screenHeight));
		demoImageBitmapList
				.add(MultiBackgroundUtilities.scaleDownImageAndDecode(
						getResources(), ImageSize.COVER_FULL_SCREEN,
						R.drawable.animation_demo_3, screenWidth, screenHeight));
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (databaseHelper != null) {
			databaseHelper.closeDatabase();
		}
		for (Bitmap bitmap : demoImageBitmapList) {
			bitmap.recycle();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onBackPressed() {
		if (!isListShown) {
			if (animationDetailsListView != null) {
				isListShown = true;
				animationDemoTransitionImageView
						.setImageBitmap((demoImageBitmapList
								.get(currentSelectedBitmapIndex)));
				animationDemoTransitionImageView.setVisibility(View.VISIBLE);
				synchronized (animationDemoRelativeLayout) {
					if (animationDemoImageSurfaceView != null) {
						animationDemoImageSurfaceView.setVisibility(View.GONE);
						new Handler().post(new Runnable() {
							public void run() {
								animationDemoRelativeLayout
										.removeView(animationDemoImageSurfaceView);
								animationDemoImageSurfaceView = null;
							}
						});
					}
				}
				animationDetailsListView
						.startAnimation(animationInForDetailsListView);
				animationDemoTransitionImageView
						.startAnimation(animationOutForDemoImageView);
			}
		} else {
			super.onBackPressed();
		}
	}

	private void initializeListAndImageViewTransition() {
		int animationDuration = 500;
		animationInForDemoImageView = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, 1f, Animation.RELATIVE_TO_PARENT,
				0f, Animation.RELATIVE_TO_PARENT, 0f,
				Animation.RELATIVE_TO_PARENT, 0f);
		animationInForDemoImageView
				.setAnimationListener(new ViewTranslateAnimationListener());
		animationInForDemoImageView.setDuration(animationDuration);
		animationInForDemoImageView.setInterpolator(new LinearInterpolator());

		animationOutForDemoImageView = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, 0f, Animation.RELATIVE_TO_PARENT,
				1f, Animation.RELATIVE_TO_PARENT, 0f,
				Animation.RELATIVE_TO_PARENT, 0f);
		animationOutForDemoImageView
				.setAnimationListener(new ViewTranslateAnimationListener());
		animationOutForDemoImageView.setDuration(animationDuration);
		animationOutForDemoImageView.setInterpolator(new LinearInterpolator());

		animationInForDetailsListView = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, -1f,
				Animation.RELATIVE_TO_PARENT, 0f, Animation.RELATIVE_TO_PARENT,
				0f, Animation.RELATIVE_TO_PARENT, 0f);
		animationInForDetailsListView
				.setAnimationListener(new ViewTranslateAnimationListener());
		animationInForDetailsListView.setDuration(animationDuration);
		animationInForDetailsListView.setInterpolator(new LinearInterpolator());

		animationOutForDetailsListView = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, 0f, Animation.RELATIVE_TO_PARENT,
				-1f, Animation.RELATIVE_TO_PARENT, 0f,
				Animation.RELATIVE_TO_PARENT, 0f);
		animationOutForDetailsListView
				.setAnimationListener(new ViewTranslateAnimationListener());
		animationOutForDetailsListView.setDuration(animationDuration);
		animationOutForDetailsListView
				.setInterpolator(new LinearInterpolator());
	}

	public AnimationDetails getCurrentSelectedAnimationDetails() {
		return currentSelectedAnimationDetails;
	}

	public void setCurrentSelectedBitmapIndex(int currentSelectedBitmapIndex) {
		this.currentSelectedBitmapIndex = currentSelectedBitmapIndex;
	}
}
