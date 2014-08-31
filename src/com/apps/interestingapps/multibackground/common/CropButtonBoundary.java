package com.apps.interestingapps.multibackground.common;

public class CropButtonBoundary {

	private int leftBoundary, rightBoundary, bottomBoundary, topBoundary;

	public CropButtonBoundary(int leftBoundary,
			int rightBoundary,
			int bottomBoundary,
			int topBoundary) {
		this.leftBoundary = leftBoundary;
		this.rightBoundary = rightBoundary;
		this.bottomBoundary = bottomBoundary;
		this.topBoundary = topBoundary;
	}

	public CropButtonBoundary() {
	}

	public int getLeftBoundary() {
		return leftBoundary;
	}

	public void setLeftBoundary(int leftBoundary) {
		this.leftBoundary = leftBoundary;
	}

	public int getRightBoundary() {
		return rightBoundary;
	}

	public void setRightBoundary(int rightBoundary) {
		this.rightBoundary = rightBoundary;
	}

	public int getBottomBoundary() {
		return bottomBoundary;
	}

	public void setBottomBoundary(int bottomBoundary) {
		this.bottomBoundary = bottomBoundary;
	}

	public int getTopBoundary() {
		return topBoundary;
	}

	public void setTopBoundary(int topBoundary) {
		this.topBoundary = topBoundary;
	}

}
