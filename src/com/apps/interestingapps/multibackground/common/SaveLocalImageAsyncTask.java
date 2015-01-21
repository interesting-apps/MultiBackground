package com.apps.interestingapps.multibackground.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;

public class SaveLocalImageAsyncTask extends AsyncTask<Void, Void, Void> {

	private DatabaseHelper databaseHelper;
	private int screenX, screenY;
	private CropButtonDimensions cbd;
	private final String TAG = "SaveLocalImageAsyncTask";
	private Context context;
	private MultiBackgroundImage mbi;

	public SaveLocalImageAsyncTask(Context context,
			DatabaseHelper databaseHelper,
			MultiBackgroundImage mbi,
			int screenX,
			int screenY,
			CropButtonDimensions cbd) {
		this.databaseHelper = databaseHelper;
		this.screenX = screenX;
		this.screenY = screenY;
		this.cbd = cbd;
		this.context = context;
		this.mbi = mbi;
	}

	@Override
	protected Void doInBackground(Void... params) {
		Bitmap bitmap = MultiBackgroundUtilities.createLocalImageAndSave(
				context, databaseHelper, screenX, screenY, cbd, mbi);
		if (bitmap != null) {
			bitmap.recycle();
		}
		return null;
	}

	@Override
	protected void onPostExecute(Void voidValue) {

	}

}
