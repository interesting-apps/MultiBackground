package com.apps.interestingapps.multibackground.listeners;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.apps.interestingapps.multibackground.SetWallpaperActivity;
import com.apps.interestingapps.multibackground.common.CropButtonBoundary;

public class CropButtonTouchListener implements OnTouchListener {

	private static ImageView[] cropButtons = new ImageView[4];
	private String TAG = "CropTouchListener";
	private CropButtonPosition cropButtonPosition;
	private ImageView currentImageView, cropRectangleImageView;
	private float downX, downY;
	private Bitmap cropRectangleBitmap;
	private Drawable cropRectangleDrawable;
	private SetWallpaperActivity setWallpaperActivity;
	private RelativeLayout parentRelativeLayout;
	private RelativeLayout.LayoutParams relLayoutParams;
	private boolean isMoveWithinBoundary = false;
	private ImageView cropButtonImageView;
	private int halfCropButtonWidth, halfCropButtonHeight;

	public static enum CropButtonPosition {
		LEFT_TOP(0),

		LEFT_BOTTOM(1),

		RIGHT_BOTTOM(2),

		RIGHT_TOP(3);

		private int buttonNumber;

		private CropButtonPosition(int buttonNumber) {
			this.buttonNumber = buttonNumber;
		}

		public int getButtonNumber() {
			return buttonNumber;
		}
	}

	public CropButtonTouchListener(CropButtonPosition cropButtonPosition,
			ImageView cropButtonImageView,
			ImageView cropRectangleImageView,
			ImageView currentImageView,
			Drawable cropRectangleDrawable,
			SetWallpaperActivity setWallpaperActivity,
			RelativeLayout parentRelativeLayout) {
		this.cropButtonPosition = cropButtonPosition;
		this.currentImageView = currentImageView;
		cropButtons[cropButtonPosition.getButtonNumber()] = cropButtonImageView;
		this.cropButtonImageView = cropButtonImageView;
		this.cropRectangleImageView = cropRectangleImageView;
		this.cropRectangleDrawable = cropRectangleDrawable;
		this.setWallpaperActivity = setWallpaperActivity;
		this.parentRelativeLayout = parentRelativeLayout;
		if (parentRelativeLayout != null) {
			relLayoutParams = (RelativeLayout.LayoutParams) this.parentRelativeLayout
					.getLayoutParams();
		}
		this.halfCropButtonWidth = cropButtonImageView.getWidth() / 2;
		this.halfCropButtonHeight = cropButtonImageView.getHeight() / 2;
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
			updateButtonPositions(currentX, currentY, dx, dy);
			drawRectangle();
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

	private void updateButtonPositions(float currentX,
			float currentY,
			float dx,
			float dy) {
		switch (cropButtonPosition) {
		case LEFT_TOP:
			cropButtonMoved(CropButtonPosition.LEFT_TOP,
					CropButtonPosition.LEFT_BOTTOM,
					CropButtonPosition.RIGHT_TOP, dx, dy);
			break;
		case LEFT_BOTTOM:
			cropButtonMoved(CropButtonPosition.LEFT_BOTTOM,
					CropButtonPosition.LEFT_TOP,
					CropButtonPosition.RIGHT_BOTTOM, dx, dy);
			break;
		case RIGHT_BOTTOM:
			cropButtonMoved(CropButtonPosition.RIGHT_BOTTOM,
					CropButtonPosition.RIGHT_TOP,
					CropButtonPosition.LEFT_BOTTOM, dx, dy);
			break;
		case RIGHT_TOP:
			cropButtonMoved(CropButtonPosition.RIGHT_TOP,
					CropButtonPosition.RIGHT_BOTTOM,
					CropButtonPosition.LEFT_TOP, dx, dy);
			break;
		}
	}

	private void cropButtonMoved(CropButtonPosition currentButtonPosition,
			CropButtonPosition xButtonPosition,
			CropButtonPosition yButtonPosition,
			float dx,
			float dy) {

		boolean currentButtonMoveWithinBoundary = isMoveWithinBoundary(
				currentButtonPosition, dx, dy);
		if (currentButtonMoveWithinBoundary) {
			isMoveWithinBoundary = true;
			setButtonPosition(currentButtonPosition, dx, dy);
			setButtonPosition(xButtonPosition, dx, 0f);
			setButtonPosition(yButtonPosition, 0f, dy);
		}
	}

	private void setButtonPosition(CropButtonPosition buttonPosition,
			float dx,
			float dy) {
		ImageView currentCropButtonImageView = cropButtons[buttonPosition
				.getButtonNumber()];
		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) currentCropButtonImageView
				.getLayoutParams();
		layoutParams.leftMargin = (int) (layoutParams.leftMargin + dx);
		layoutParams.topMargin = (int) (layoutParams.topMargin + dy);
		currentCropButtonImageView.setLayoutParams(layoutParams);
	}

	private void drawRectangle() {
		int rectLeft = ((RelativeLayout.LayoutParams) cropButtons[CropButtonPosition.LEFT_TOP
				.getButtonNumber()].getLayoutParams()).leftMargin;
		int rectTop = ((RelativeLayout.LayoutParams) cropButtons[CropButtonPosition.LEFT_TOP
				.getButtonNumber()].getLayoutParams()).topMargin;
		int rectRight = ((RelativeLayout.LayoutParams) cropButtons[CropButtonPosition.RIGHT_BOTTOM
				.getButtonNumber()].getLayoutParams()).leftMargin;
		int rectBottom = ((RelativeLayout.LayoutParams) cropButtons[CropButtonPosition.RIGHT_BOTTOM
				.getButtonNumber()].getLayoutParams()).topMargin;

		int width = rectRight - rectLeft;
		int height = rectBottom - rectTop;

		if (height > 0 && width > 0) {
			cropRectangleBitmap = Bitmap.createScaledBitmap(
					((BitmapDrawable) cropRectangleDrawable).getBitmap(),
					width, height, true);
			cropRectangleImageView.setImageBitmap(cropRectangleBitmap);
			RelativeLayout.LayoutParams cropRectangleLayoutParams = (RelativeLayout.LayoutParams) cropRectangleImageView
					.getLayoutParams();
			cropRectangleLayoutParams.leftMargin = rectLeft
					+ (halfCropButtonWidth);
			cropRectangleLayoutParams.topMargin = rectTop
					+ (halfCropButtonHeight);
			cropRectangleImageView.setLayoutParams(cropRectangleLayoutParams);
			cropRectangleImageView.setVisibility(View.VISIBLE);
		}

	}

	private CropButtonBoundary
			calculateBoundary(CropButtonPosition cropButtonPosition) {
		CropButtonBoundary boundary = new CropButtonBoundary();
		RelativeLayout.LayoutParams leftTopCropButtonLayoutParams = (RelativeLayout.LayoutParams) cropButtons[CropButtonPosition.LEFT_TOP
				.getButtonNumber()].getLayoutParams();
		RelativeLayout.LayoutParams leftBottomCropButtonLayoutParams = (RelativeLayout.LayoutParams) cropButtons[CropButtonPosition.LEFT_BOTTOM
				.getButtonNumber()].getLayoutParams();
		RelativeLayout.LayoutParams rightBottomCropButtonLayoutParams = (RelativeLayout.LayoutParams) cropButtons[CropButtonPosition.RIGHT_BOTTOM
				.getButtonNumber()].getLayoutParams();

		int xOffset = (relLayoutParams.width / 2)
				- (currentImageView.getWidth() / 2);
		int yOffset = (relLayoutParams.height / 2)
				- (currentImageView.getHeight() / 2);
		if (halfCropButtonHeight <= 0 || halfCropButtonWidth <= 0) {
			this.halfCropButtonWidth = cropButtonImageView.getWidth() / 2;
			this.halfCropButtonHeight = cropButtonImageView.getHeight() / 2;
		}

		switch (cropButtonPosition) {
		case LEFT_TOP:
			boundary.setLeftBoundary(relLayoutParams.leftMargin + xOffset
					- halfCropButtonWidth);
			boundary.setBottomBoundary(leftBottomCropButtonLayoutParams.topMargin
					- cropButtonImageView.getHeight());
			boundary.setRightBoundary(rightBottomCropButtonLayoutParams.leftMargin
					- cropButtonImageView.getWidth());
			boundary.setTopBoundary(relLayoutParams.topMargin + yOffset);
			break;
		case LEFT_BOTTOM:
			boundary.setLeftBoundary(relLayoutParams.leftMargin + xOffset
					- halfCropButtonWidth);
			boundary.setBottomBoundary(relLayoutParams.topMargin + yOffset
					+ currentImageView.getHeight());
			boundary.setRightBoundary(rightBottomCropButtonLayoutParams.leftMargin
					- cropButtonImageView.getWidth());
			boundary.setTopBoundary(leftTopCropButtonLayoutParams.topMargin
					+ cropButtonImageView.getHeight());
			break;
		case RIGHT_BOTTOM:
			boundary.setLeftBoundary(leftTopCropButtonLayoutParams.leftMargin
					+ cropButtonImageView.getWidth());
			boundary.setBottomBoundary(relLayoutParams.topMargin + yOffset
					+ currentImageView.getHeight());
			boundary.setRightBoundary(relLayoutParams.leftMargin + xOffset
					+ currentImageView.getWidth()
 + halfCropButtonWidth);
			boundary.setTopBoundary(leftTopCropButtonLayoutParams.topMargin
					+ cropButtonImageView.getHeight());
			break;
		case RIGHT_TOP:
			boundary.setLeftBoundary(leftTopCropButtonLayoutParams.leftMargin
					+ cropButtonImageView.getWidth());
			boundary.setBottomBoundary(rightBottomCropButtonLayoutParams.topMargin
					- cropButtonImageView.getHeight());
			boundary.setRightBoundary(relLayoutParams.leftMargin + xOffset
					+ currentImageView.getWidth()
 + halfCropButtonWidth);
			boundary.setTopBoundary(relLayoutParams.topMargin + yOffset);
			break;
		}
		return boundary;
	}

	private boolean isMoveWithinBoundary(CropButtonPosition cropButtonPosition,
			float dx,
			float dy) {
		boolean withinBoundary = false;
		ImageView currentCropButtonImageView = cropButtons[cropButtonPosition
				.getButtonNumber()];
		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) currentCropButtonImageView
				.getLayoutParams();
		int leftMargin = (int) (layoutParams.leftMargin + dx);
		int topMargin = (int) (layoutParams.topMargin + dy);
		CropButtonBoundary cropButtonBoundary = calculateBoundary(cropButtonPosition);
		int rightBoundaryDelta = 0, bottomBoundaryDelta = 0;

		if (cropButtonPosition == CropButtonPosition.RIGHT_BOTTOM
				|| cropButtonPosition == CropButtonPosition.RIGHT_TOP) {
			rightBoundaryDelta = currentCropButtonImageView.getWidth();
		}
		if (cropButtonPosition == CropButtonPosition.RIGHT_BOTTOM
				|| cropButtonPosition == CropButtonPosition.LEFT_BOTTOM) {
			bottomBoundaryDelta = currentCropButtonImageView.getHeight();
		}

		if (leftMargin >= (cropButtonBoundary.getLeftBoundary())
				&& leftMargin <= (cropButtonBoundary.getRightBoundary() - rightBoundaryDelta)) {
			if (topMargin >= (cropButtonBoundary.getTopBoundary())
					&& topMargin <= (cropButtonBoundary.getBottomBoundary() - bottomBoundaryDelta)) {
				withinBoundary = true;
			}
		}
		return withinBoundary;
	}
}
