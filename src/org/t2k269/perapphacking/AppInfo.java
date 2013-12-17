package org.t2k269.perapphacking;

import java.util.Date;

public class AppInfo implements Comparable<AppInfo> {
	public final String name;
	public final String packageName;
	
	public String proxyHost = null;
	public int proxyPort = -1;
	
	public String timeMachine = null;
	
	public boolean limitBitmapDimensions = false;
	public boolean muteIfSientInProfileGroup = false;

	public boolean preventService = false;
	public boolean preventWakeLock = false;
	
	public boolean preventAlarm = false;
	public int alarmMultiplier = 0;
	
	public AppInfo(String name, String packageName) {
		this.name = name;
		this.packageName = packageName;
	}
	
	public boolean isEnabled() {
		return (proxyHost != null && proxyPort > 0) || timeMachine != null || limitBitmapDimensions || muteIfSientInProfileGroup || preventAlarm || preventService || preventWakeLock;
	}

	@Override
	public int compareTo(AppInfo o) {
		return name.compareToIgnoreCase(o.name);
	}
}
