package com.apps.interestingapps.multibackground;

import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class MyPreferencesActivity extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(Build.VERSION.SDK_INT >= 11) {
			addPreferencesFromResource(R.xml.prefs);
		} else {
			addPreferencesFromResource(R.xml.prefs7);
		}
	}

	// /**
	// * Checks that a preference is a valid numerical value
	// */
	//
	// Preference.OnPreferenceChangeListener numberCheckListener = new
	// OnPreferenceChangeListener() {
	//
	// @Override
	// public boolean onPreferenceChange(Preference preference, Object newValue)
	// {
	// // check that the string is an integer
	// if (newValue != null && newValue.toString().length() > 0
	// && newValue.toString().matches("\\d*")) {
	// return true;
	// }
	// // If now create a message to the user
	// Toast.makeText(MyPreferencesActivity.this, "Invalid Input",
	// Toast.LENGTH_SHORT).show();
	// return false;
	// }
	// };

}