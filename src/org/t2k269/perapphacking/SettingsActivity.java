package org.t2k269.perapphacking;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity {

	private String packageName;

	@SuppressLint("WorldReadableFiles")
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		packageName = getIntent().getStringExtra("packageName");
		if (packageName == null) {
			Toast.makeText(this, "No package specified!", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		
		addPreferencesFromResource(R.xml.pref_general);

		SharedPreferences modPrefs = getApplicationContext().getSharedPreferences("ModSettings", Context.MODE_WORLD_READABLE);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		Editor editor = prefs.edit();
		for (int i = 0; i < this.getPreferenceScreen().getPreferenceCount(); i++) {
			Preference preference = getPreferenceScreen().getPreference(i);
			if (preference instanceof CheckBoxPreference) {
				boolean value = modPrefs.getBoolean(packageName + "/" + preference.getKey(), false);
				editor.putBoolean(preference.getKey(), value);
				((CheckBoxPreference)preference).setChecked(value);
			} else if (preference instanceof ListPreference) {
				String value = modPrefs.getString(packageName + "/" + preference.getKey(), "");
				editor.putString(preference.getKey(), value);
				((ListPreference)preference).setValue(value);
			} else if (preference instanceof EditTextPreference) {
				String value = modPrefs.getString(packageName + "/" + preference.getKey(), "");
				editor.putString(preference.getKey(), value);
				((EditTextPreference)preference).setText(value);
			}
			bindPreferenceSummaryToValue(preference);
		}
		editor.commit();

		for (int i = 0; i < this.getPreferenceScreen().getPreferenceCount(); i++) {
			Preference pref = getPreferenceScreen().getPreference(i);
			bindPreferenceSummaryToValue(pref);
		}
		
		Intent data = new Intent();
		data.putExtra("packageName", packageName);
		this.setResult(0, data);
	}

	private Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
		@SuppressLint("WorldReadableFiles")
		@Override
		public boolean onPreferenceChange(Preference preference, Object value) {
			if (preference.getKey().equals("preventAlarm")) {
				SettingsActivity.this.findPreference("alarmMultiplier").setEnabled(Boolean.TRUE.equals(value));
			}
			
			SharedPreferences modPrefs = getApplicationContext().getSharedPreferences("ModSettings", Context.MODE_WORLD_READABLE);
			if (preference instanceof CheckBoxPreference) {
				Editor editor = modPrefs.edit();
				editor.putBoolean(packageName + "/" + preference.getKey(), (Boolean)value);
				editor.commit();
			} else if (preference instanceof ListPreference) {
				String stringValue = value.toString();

				// For list preferences, look up the correct display value in
				// the preference's 'entries' list.
				ListPreference listPreference = (ListPreference) preference;
				int index = listPreference.findIndexOfValue(stringValue);
	
				// Set the summary to reflect the new value.
				preference.setSummary(index >= 0 ? listPreference.getEntries()[index]
								: null);
	
				Editor editor = modPrefs.edit();
				editor.putString(packageName + "/" + preference.getKey(), stringValue);
				editor.commit();
			} else if (preference instanceof EditTextPreference) {
				String stringValue = value.toString();

				// For all other preferences, set the summary to the value's
				// simple string representation.
				preference.setSummary(stringValue);
				
				Editor editor = modPrefs.edit();
				editor.putString(packageName + "/" + preference.getKey(), stringValue);
				editor.commit();
			}
			return true;
		}
	};


	/**
	 * Binds a preference's summary to its value. More specifically, when the
	 * preference's value is changed, its summary (line of text below the
	 * preference title) is updated to reflect the value. The summary is also
	 * immediately updated upon calling this method. The exact display format is
	 * dependent on the type of preference.
	 * 
	 * @see #sBindPreferenceSummaryToValueListener
	 */
	private void bindPreferenceSummaryToValue(Preference preference) {
		// Set the listener to watch for value changes.
		preference
				.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
		if (preference instanceof CheckBoxPreference) { 
			sBindPreferenceSummaryToValueListener.onPreferenceChange(
					preference,
					PreferenceManager.getDefaultSharedPreferences(
							preference.getContext()).getBoolean(preference.getKey(),
							false));
		}
		if (preference instanceof ListPreference || preference instanceof EditTextPreference) { 
			// Trigger the listener immediately with the preference's
			// current value.
			sBindPreferenceSummaryToValueListener.onPreferenceChange(
					preference,
					PreferenceManager.getDefaultSharedPreferences(
							preference.getContext()).getString(preference.getKey(),
							""));
		}
	}

}
