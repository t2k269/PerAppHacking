package org.t2k269.perapphacking;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

class AppListAdapter extends BaseAdapter {
	
	interface DataProvider {
		List<AppInfo> getAppList();
	}

	private Context context;

	private boolean enabledOnly = false;
	private DataProvider dataProvider;
	private List<AppInfo> filteredApps = new ArrayList<AppInfo>(100);
	
	public AppListAdapter(Context context, DataProvider dataProvider, boolean enabledOnly) {
		this.context = context;
		this.dataProvider = dataProvider; 
		this.enabledOnly = enabledOnly;
	}
	
	@SuppressLint("DefaultLocale")
	void filter(String filter) {
		if (enabledOnly) {
			filteredApps.clear();
			for (AppInfo app : dataProvider.getAppList()) {
				if (app.isEnabled()) {
					filteredApps.add(app);
				}
			}
		} else {
			filteredApps.clear();
			filter = filter == null ? null : filter.trim().toLowerCase();
			for (AppInfo app : dataProvider.getAppList()) {
				if (filter == null || filter.length() == 0 ||
					app.name.toLowerCase().indexOf(filter) >= 0 ||
					app.packageName.toLowerCase().indexOf(filter) >= 0) {
					filteredApps.add(app);
				}
			}
		}
		this.notifyDataSetChanged();
	}
	
	public void refreshData() {
		filter(null);
	}
	
	@Override
	public int getCount() {
		return filteredApps.size();
	}

	@Override
	public Object getItem(int position) {
		return filteredApps.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	StringBuilder summaryBuilder = new StringBuilder();
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {	
		RowStateHolder stateHolder;
		View view;
		if (convertView == null) {
			stateHolder = new RowStateHolder(context, parent);
			view = stateHolder.getRootView();
		} else {
			view = convertView;
			stateHolder = (RowStateHolder)view.getTag();
		}

		AppInfo app = filteredApps.get(position);
		stateHolder.setAppName(app.name);
		stateHolder.setPackageName(app.packageName);
		summaryBuilder.setLength(0);
		summaryBuilder.append(app.proxyHost == null || app.proxyHost.trim().length() == 0 ? "No proxy" : "Proxy: " + app.proxyHost + ":" + app.proxyPort);
		if (app.preventAlarm) {
			summaryBuilder.append(", Prevent Alarm");
		}
		if (app.preventService) {
			summaryBuilder.append(", Prevent Service");
		}
		if (app.preventWakeLock) {
			summaryBuilder.append(", Prevent Wake Lock");
		}
		if (app.timeMachine != null) {
			summaryBuilder.append(", Time Machine");
		}
		if (app.limitBitmapDimensions) {
			summaryBuilder.append(", Limit bitmap");
		}
		if (app.muteIfSientInProfileGroup) {
			summaryBuilder.append(", Mute fix for CM");
		}
		stateHolder.setSummary(summaryBuilder.toString());
		stateHolder.setColor(app.isEnabled() ? Color.WHITE : Color.GRAY);
		return view;
	}
	
    public class RowStateHolder {
    	private View view;
    	
    	private TextView appNameText;
    	private TextView packageText;
    	private TextView summaryText;
    	
		public RowStateHolder(final Context context, ViewGroup parentView) {
			LayoutInflater mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = mInflater.inflate(R.layout.app_list_item, parentView, false);
			view.setTag(this);
			
			appNameText = (TextView)view.findViewById(R.id.appNameText);
			packageText = (TextView)view.findViewById(R.id.packageText);
			summaryText = (TextView)view.findViewById(R.id.summaryText);
		}
		
		public void setColor(int color) {
			appNameText.setTextColor(color);
			packageText.setTextColor(color);
			summaryText.setTextColor(color);
		}

		public void setAppName(String name) {
			appNameText.setText(name);
		}

		public void setPackageName(String packageName) {
			packageText.setText(packageName);
		}

		public void setSummary(String summary) {
			summaryText.setText(summary);
		}

		public View getRootView() {
			return view;
		}
    }
}