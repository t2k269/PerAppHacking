package org.t2k269.perapphacking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;

public class TabsActivity extends FragmentActivity {

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
	private List<AppsListFragment> fragments = new ArrayList<AppsListFragment>();
	
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

		loadAppListAsync();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode,  resultCode,  data);
	}
	
	private void updatePackage(String packageName) {
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
			for (AppsListFragment fragment : fragments) {
				fragment.appsChanged();
			}
		}
	}

	private void addFragment(AppsListFragment fragment) {
		fragments.add(fragment);
	}
	
	private void loadAppListAsync() {
		AsyncTask<Integer, Void, List<AppInfo>> task = new AsyncTask<Integer, Void, List<AppInfo>>() {
			
			private ProgressDialog dialog;
			
			@Override
			protected List<AppInfo> doInBackground(Integer... args) {
				try {
					ArrayList<AppInfo> apps = new ArrayList<AppInfo>();
		            PackageManager pm = getPackageManager();
		            List<PackageInfo> appListInfo = pm.getInstalledPackages(0);
		            for (PackageInfo p : appListInfo) {
		            	AppInfo app = new AppInfo(p.applicationInfo.loadLabel(pm).toString(), p.applicationInfo.packageName);
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
				for (AppsListFragment fragment : fragments) {
					fragment.setApps(apps);
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
		app.preventService = prefs.getBoolean(app.packageName + "/" + "preventService", false);
		app.preventWakeLock = prefs.getBoolean(app.packageName + "/" + "preventWakeLock", false);
		app.preventAlarm = prefs.getBoolean(app.packageName + "/" + "preventAlarm", false);
		try {
			app.alarmMultiplier = Integer.parseInt(prefs.getString(app.packageName + "/" + "alarmMultiplier", "0"));
		} catch (NumberFormatException ex) {
			app.alarmMultiplier = 0;
		}
	}


	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
			case 0:
				return new AllAppsSectionFragment();
			case 1:
				return new EnabledAppsSectionFragment();
			default:
				return null;
			}
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case 0:
				return "All Apps";
			case 1:
				return "Enabled Apps";
			}
			return null;
		}
	}
	
	interface AppsListFragment {
		void setApps(List<AppInfo> apps);
		void appsChanged();
	}

	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	public static class AllAppsSectionFragment extends Fragment implements AppsListFragment {
		private AppListAdapter adapter;
		
		public AllAppsSectionFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			adapter = new AppListAdapter(inflater.getContext(), false);

			View rootView = inflater.inflate(R.layout.fragment_app_list, container, false);
			EditText text = (EditText)rootView.findViewById(R.id.filterText);
			text.addTextChangedListener(new TextWatcher() {
				@Override
				public void afterTextChanged(Editable editable) {
					adapter.filter(editable.toString());
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count,
						int after) {
				}

				@Override
				public void onTextChanged(CharSequence s, int start, int before,
						int count) {
				}
				
			});
			ListView appsList = (ListView)rootView.findViewById(R.id.appsList);
			appsList.setAdapter(adapter);
			appsList.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
					Intent intent = new Intent(getActivity(), SettingsActivity.class);
					intent.putExtra("packageName", ((AppInfo)adapter.getItemAtPosition(position)).packageName);
					startActivityForResult(intent, 1);
				}
			});
			return rootView;
		}

		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			((TabsActivity)activity).addFragment(this);
		}

		@Override
		public void onActivityResult(int requestCode, int resultCode, Intent data) {
			if (requestCode == 1) {
				String packageName = data.getStringExtra("packageName");
				((TabsActivity)getActivity()).updatePackage(packageName);
			}
		}

		@Override
		public void setApps(List<AppInfo> apps) {
			adapter.setApps(apps);
		}

		@Override
		public void appsChanged() {
			adapter.notifyDataSetChanged();
		}
	}

	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	public static class EnabledAppsSectionFragment extends Fragment implements AppsListFragment {
		private AppListAdapter adapter;

		public EnabledAppsSectionFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			adapter = new AppListAdapter(inflater.getContext(), true);

			View rootView = inflater.inflate(R.layout.fragment_enabled_apps, container, false);
			ListView appsList = (ListView)rootView.findViewById(R.id.appsList);
			appsList.setAdapter(adapter);
			appsList.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
					Intent intent = new Intent(getActivity(), SettingsActivity.class);
					intent.putExtra("packageName", ((AppInfo)adapter.getItemAtPosition(position)).packageName);
					startActivityForResult(intent, 1);
				}
			});
			return rootView;
		}

		@Override
		public void onActivityResult(int requestCode, int resultCode, Intent data) {
			if (requestCode == 1) {
				String packageName = data.getStringExtra("packageName");
				((TabsActivity)getActivity()).updatePackage(packageName);
			}
		}

		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			((TabsActivity)activity).addFragment(this);
		}
		
		@Override
		public void setApps(List<AppInfo> apps) {
			adapter.setApps(apps);
		}

		@Override
		public void appsChanged() {
			adapter.filter(null);
			adapter.notifyDataSetChanged();
		}
	}

	
}
