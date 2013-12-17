package org.t2k269.perapphacking;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;

public class TabsActivity extends FragmentActivity implements AppListAdapter.DataProvider {

	private static final String TAG = TabsActivity.class.getSimpleName();

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	private List<AppInfo> apps;
	
	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tabs);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		reloadAppListAsync();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode,  resultCode,  data);
	}
	
	void updatePackage(String packageName) {
		AppInfo _target = null;
		for (int i = apps.size() - 1; i >= 0; i--) {
			AppInfo app = (AppInfo)apps.get(i);
			if (app.packageName.equals(packageName)) {
				_target = app;
				break;
			}
		}
		
		if (_target != null) {
			final AppInfo target = _target;
			
			loadAppSettings(target);
			for (Fragment f : mSectionsPagerAdapter.fragments)
				if (f instanceof AppsListFragment) {
				((AppsListFragment)f).appsChanged();
			}
		}
	}

	void reloadAppListAsync() {
		AsyncTask<Integer, Void, List<AppInfo>> task = new AsyncTask<Integer, Void, List<AppInfo>>() {
			
			private ProgressDialog dialog;
			
			@Override
			protected List<AppInfo> doInBackground(Integer... args) {
				try {
					ArrayList<AppInfo> apps = new ArrayList<AppInfo>();
		            PackageManager pm = getPackageManager();
		            List<PackageInfo> appListInfo = pm.getInstalledPackages(0);
		            for (PackageInfo p : appListInfo) {
		            	String name = p.applicationInfo.packageName;
		            	try {
		            		name = p.applicationInfo.loadLabel(pm).toString();
		            	} catch (Exception ex) {
		            	}
		            	AppInfo app = new AppInfo(name, p.applicationInfo.packageName);
		    			apps.add(app);
		    			loadAppSettings(app);
		            }
		            Collections.sort(apps);
		            return apps;
		        } catch (Exception e) {
		        	Log.e(TAG, "Error while load packages!", e);
		        	return null;
		        }
		        finally {
		        }			
		    }

			@Override
			protected void onPreExecute() {
				dialog = ProgressDialog.show(TabsActivity.this, "", "Loading. Please wait...", true);
				dialog.setIndeterminate(true);
				dialog.show();
			}

			@Override
			protected void onPostExecute(List<AppInfo> result) {
				dialog.dismiss();
				apps = result;
				for (Fragment f : mSectionsPagerAdapter.fragments)
					if (f instanceof AppsListFragment) {
					((AppsListFragment)f).appsChanged();
				}
			}
		};
		task.execute();
	}

	@SuppressLint("WorldReadableFiles")
	private void loadAppSettings(AppInfo app) {
		SharedPreferences prefs = getApplicationContext().getSharedPreferences("ModSettings", Context.MODE_WORLD_READABLE);
		if (prefs.getBoolean(app.packageName + "/" + "proxyEnabled", false)) {
			app.proxyHost = prefs.getString(app.packageName + "/" + "proxyHost", "");
			try {
				app.proxyPort = Integer.parseInt(prefs.getString(app.packageName + "/" + "proxyPort", ""));
			} catch (NumberFormatException ex) {
				app.proxyPort = -1;
			}
		} else {
			app.proxyHost = null;
			app.proxyPort = -1;
		}
		app.timeMachine = prefs.getString(app.packageName + "/timeMachine", "");
		if (app.timeMachine.length() == 0)
			app.timeMachine = null;
		app.limitBitmapDimensions = prefs.getBoolean(app.packageName + "/" + "limitBitmapDimensions", false);
		app.muteIfSientInProfileGroup = prefs.getBoolean(app.packageName + "/" + "muteIfSientInProfileGroup", false);
		app.preventService = prefs.getBoolean(app.packageName + "/" + "preventService", false);
		app.preventWakeLock = prefs.getBoolean(app.packageName + "/" + "preventWakeLock", false);
		app.preventAlarm = prefs.getBoolean(app.packageName + "/" + "preventAlarm", false);
		try {
			app.alarmMultiplier = Integer.parseInt(prefs.getString(app.packageName + "/" + "alarmMultiplier", "0"));
		} catch (NumberFormatException ex) {
			app.alarmMultiplier = 0;
		}
	}
	
	@SuppressLint("WorldReadableFiles")
	void saveAppSettings(Map<String,Object> app) {
		SharedPreferences prefs = getApplicationContext().getSharedPreferences("ModSettings", Context.MODE_WORLD_READABLE);
		Editor e = prefs.edit();
		String packageName = (String)app.get("packageName");
		e.putBoolean(packageName + "/" + "proxyEnabled", app.get("proxyHost") != null);
		e.putString(packageName + "/" + "proxyHost", (String)app.get("proxyHost"));
		int port = ((Number)app.get("proxyPort")).intValue();
		if (port <= 0)
			e.remove(packageName + "/" + "proxyPort");
		else
			e.putString(packageName + "/" + "proxyPort", String.valueOf(port));

		e.putString(packageName + "/" + "timeMachine", (String)app.get("timeMachine"));
		e.putBoolean(packageName + "/" + "limitBitmapDimensions", (Boolean)app.get("limitBitmapDimensions"));
		e.putBoolean(packageName + "/" + "muteIfSientInProfileGroup", (Boolean)app.get("muteIfSientInProfileGroup"));
		e.putBoolean(packageName + "/" + "preventService", (Boolean)app.get("preventService"));
		e.putBoolean(packageName + "/" + "preventWakeLock", (Boolean)app.get("preventWakeLock"));
		e.putBoolean(packageName + "/" + "preventAlarm", (Boolean)app.get("preventAlarm"));
		e.putString(packageName + "/" + "alarmMultiplier", app.containsKey("alarmMultiplier") ? String.valueOf(((Number)app.get("alarmMultiplier")).intValue()) : "0");
		e.commit();
	}
	
	@Override
	public List<AppInfo> getAppList() {
		return apps;
	}


	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		Fragment[] fragments = {
				new AllAppsSectionFragment(),
				new EnabledAppsSectionFragment(),
				new BackupFragment()
		};
		
		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			return fragments[position];
		}

		@Override
		public int getCount() {
			return fragments.length;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case 0:
				return "All Apps";
			case 1:
				return "Enabled Apps";
			case 2:
				return "Backup/Restore";
			}
			return null;
		}
	}

	
}
