package com.apps.interestingapps.multibackground.listeners;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.apps.interestingapps.multibackground.SetWallpaperActivity;
import com.apps.interestingapps.multibackground.common.CropButtonBoundary;

public class CropRectangleTouchListener implements OnTouchListener {

	private ImageView leftTopCropButton, leftBottomCropButton,
			rightTopCropButton, rightBottomCropButton;
	private ImageView currentImageView;
	private float downX, downY;
	private static String TAG = "CropRectangleTouchListener";
	private SetWallpaperActivity setWallpaperActivity;
	private RelativeLayout parentRelativeLayout;
	private RelativeLayout.LayoutParams relLayoutParams;
	private boolean isMoveWithinBoundary = false;
	private int halfCropButtonWidth, halfCropButtonHeight;

	public CropRectangleTouchListener(ImageView currentImageView,
			ImageView leftTopCropButton,
			ImageView leftBottomCropButton,
			ImageView rightTopCropButton,
			ImageView rightBottomCropButton,
			SetWallpaperActivity setWallpaperActivity,
			RelativeLayout parentRelativeLayout) {
		this.leftTopCropButton = leftTopCropButton;
		this.leftBottomCropButton = leftBottomCropButton;
		this.rightTopCropButton = rightTopCropButton;
		this.rightBottomCropButton = rightBottomCropButton;
		this.currentImageView = currentImageView;
		this.setWallpaperActivity = setWallpaperActivity;
		this.parentRelativeLayout = parentRelativeLayout;
		if (parentRelativeLayout != null) {
			relLayoutParams = (RelativeLayout.LayoutParams) this.parentRelativeLayout
					.getLayoutParams();
		}
	}

	public boolean onTouch(View v, MotionEvent event) {

		int action = event.getAction();

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			downX = event.getRawX();
			downY = event.getRawY();
			break;
		case MotionEvent.ACTION_MOVE:
			float currentX = event.getRawX();
			float currentY = event.getRawY();

			float dx = currentX - downX;
			float dy = currentY - downY;

			downX = currentX;
			downY = currentY;
			moveCropRectangleAndButtons((ImageView) v, dx, dy);
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			if (isMoveWithinBoundary) {
				setWallpaperActivity.updateCropRectangleCoordinates();
				isMoveWithinBoundary = false;
			}
			break;
		default:
			break;
		}
		return true;
	}

	private void moveCropRectangleAndButtons(ImageView cropRectangleImageView,
			float dx,
			float dy) {
		if (isMoveWithinBoundary(cropRectangleImageView, dx, dy)) {
			isMoveWithinBoundary = true;
			setImagePosition(cropRectangleImageView, dx, dy);
			setImagePosition(leftTopCropButton, dx, dy);
			setImagePosition(leftBottomCropButton, dx, dy);
			setImagePosition(rightBottomCropButton, dx, dy);
			setImagePosition(rightTopCropButton, dx, dy);
		}
	}

	private void setImagePosition(ImageView imageView, float dx, float dy) {
		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) imageView
				.getLayoutParams();
		layoutParams.leftMargin = (int) (layoutParams.leftMargin + dx);
		layoutParams.topMargin = (int) (layoutParams.topMargin + dy);
		imageView.setLayoutParams(layoutParams);
	}

	private CropButtonBoundary calculateBoundary() {
		if (halfCropButtonHeight <= 0 || halfCropButtonWidth <= 0) {
			this.halfCropButtonWidth = leftTopCropButton.getWidth() / 2;
			this.halfCropButtonHeight = leftTopCropButton.getHeight() / 2;
		}
		CropButtonBoundary cropRectangleBoundary = new CropButtonBoundary();
		int xOffset = (relLayoutParams.width / 2)
				- (currentImageView.getWidth() / 2);
		int yOffset = (relLayoutParams.height / 2)
				- (currentImageView.getHeight() / 2);
		cropRectangleBoundary.setLeftBoundary(relLayoutParams.leftMargin
				+ xOffset);
		cropRectangleBoundary.setTopBoundary(relLayoutParams.topMargin
				+ yOffset + halfCropButtonHeight);
		cropRectangleBoundary.setRightBoundary(relLayoutParams.leftMargin
				+ xOffset + currentImageView.getWidth());
		cropRectangleBoundary
				.setBottomBoundary(relLayoutParams.topMargin + yOffset
						+ currentImageView.getHeight() - halfCropButtonHeight);
		return cropRectangleBoundary;
	}

	private boolean isMoveWithinBoundary(ImageView cropRectangleImageView,
			float dx,
			float dy) {
		boolean withinBoundary = false;
		RelativeLayout.LayoutParams cropRectangleLayoutParams = (RelativeLayout.LayoutParams) cropRectangleImageView
				.getLayoutParams();
		CropButtonBoundary cropRectangleMoveBoundary = calculateBoundary();
		int rectLeft = (int) (cropRectangleLayoutParams.leftMargin + dx);
		int rectRight = (int) (cropRectangleLayoutParams.leftMargin
				+ cropRectangleImageView.getWidth() + dx);
		int rectTop = (int) (cropRectangleLayoutParams.topMargin + dy);
		int rectBottom = (int) (cropRectangleLayoutParams.topMargin
				+ cropRectangleImageView.getHeight() + dy);

		if (rectLeft >= cropRectangleMoveBoundary.getLeftBoundary()
				&& rectRight <= cropRectangleMoveBoundary.getRightBoundary()) {
			if (rectTop >= cropRectangleMoveBoundary.getTopBoundary()
					&& rectBottom <= cropRectangleMoveBoundary
							.getBottomBoundary()) {
				withinBoundary = true;
			}
		}
		return withinBoundary;
	}
}
