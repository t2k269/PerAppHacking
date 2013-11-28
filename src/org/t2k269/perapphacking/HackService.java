package org.t2k269.perapphacking;

import java.io.FileDescriptor;
import java.io.InputStream;

import org.apache.http.HttpHost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.SystemClock;
import android.util.TypedValue;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;


public class HackService implements IXposedHookZygoteInit, IXposedHookLoadPackage {
	
//	private static final String TAG = HackService.class.getSimpleName();
	
	private XSharedPreferences prefs;
	
	@Override
	public void initZygote(IXposedHookZygoteInit.StartupParam paramStartupParam) throws Throwable {
		loadPrefs();
	}
	
	private void loadPrefs() {
    	prefs = new XSharedPreferences(Common.MY_PACKAGE_NAME, "ModSettings");
    	prefs.makeWorldReadable();
	}
	
	private static long now(int type) {
		if ((type & 0x2) != 0) {
			return SystemClock.elapsedRealtime();
		} else {
			return System.currentTimeMillis();
		}
	}
	
    @SuppressWarnings("rawtypes")
	public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
    	prefs.reload();
    	if (prefs.getBoolean(lpparam.packageName + "/preventService", false)) {
    		Class contextWrapperClass = XposedHelpers.findClass("android.content.ContextWrapper", lpparam.classLoader);
    		XposedHelpers.findAndHookMethod(contextWrapperClass, "startService", Intent.class, new XC_MethodHook() {
				@Override
    			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
	    			if (prefs.getBoolean(lpparam.packageName + "/preventService", false)) {
	    				param.setResult(null);
	    				return;
	    			}
    			}
    		});
    	}
    	if (prefs.getBoolean(lpparam.packageName + "/limitBitmapDimensions", false)) {
    		Class clazz = XposedHelpers.findClass("android.graphics.BitmapFactory", lpparam.classLoader);
    		XC_MethodHook hook = new XC_MethodHook() {
				@Override
    			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
	    			if (prefs.getBoolean(lpparam.packageName + "/limitBitmapDimensions", false)) {
	    				if (param.hasThrowable())
	    					return;
	    				Bitmap bitmap = (Bitmap)param.getResult();
	    				if (bitmap == null)
	    					return;
	    				
	    				int max = 2000;
	    				if (bitmap.getWidth() > max || bitmap.getHeight() > max) {
	    					int w, h;
	    					if (bitmap.getWidth() > bitmap.getHeight()) {
	    						w = max;
	    						h = max * bitmap.getHeight() / bitmap.getWidth();
	    					} else {
	    						h = max;
	    						w = max * bitmap.getWidth() / bitmap.getHeight();
	    					}
	    					param.setResult(Bitmap.createScaledBitmap(bitmap, w, h, false));
	    				}
	    			}
				}
    		};
    		XposedHelpers.findAndHookMethod(clazz, "decodeByteArray", byte[].class, int.class, int.class, BitmapFactory.Options.class, hook);
    		XposedHelpers.findAndHookMethod(clazz, "decodeByteArray", byte[].class, int.class, int.class, hook);
    		XposedHelpers.findAndHookMethod(clazz, "decodeFile", String.class, hook);
    		XposedHelpers.findAndHookMethod(clazz, "decodeFile", String.class, BitmapFactory.Options.class, hook);
    		XposedHelpers.findAndHookMethod(clazz, "decodeFileDescriptor", FileDescriptor.class, hook);
    		XposedHelpers.findAndHookMethod(clazz, "decodeFileDescriptor", FileDescriptor.class, Rect.class, BitmapFactory.Options.class, hook);
    		XposedHelpers.findAndHookMethod(clazz, "decodeResource", Resources.class, int.class, BitmapFactory.Options.class, hook);
    		XposedHelpers.findAndHookMethod(clazz, "decodeResource", Resources.class, int.class, hook);
    		XposedHelpers.findAndHookMethod(clazz, "decodeResourceStream", Resources.class, TypedValue.class, InputStream.class, Rect.class, BitmapFactory.Options.class, hook);
    		XposedHelpers.findAndHookMethod(clazz, "decodeStream", InputStream.class, hook);
    		XposedHelpers.findAndHookMethod(clazz, "decodeStream", InputStream.class, Rect.class, BitmapFactory.Options.class, hook);
    	}
    	if (prefs.getBoolean(lpparam.packageName + "/preventAlarm", false)) {
    		Class alarmManagerClass = XposedHelpers.findClass("android.app.AlarmManager", lpparam.classLoader);
    		XposedHelpers.findAndHookMethod(alarmManagerClass, "set", int.class, long.class, PendingIntent.class, new XC_MethodHook() {
				@Override
    			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
	    			if (prefs.getBoolean(lpparam.packageName + "/preventAlarm", false)) {
	    				int multiplier;
	    				try {
	    					multiplier = Integer.parseInt(prefs.getString(lpparam.packageName + "/alarmMultiplier", "0"));
	    				} catch (NumberFormatException ex) {
	    					multiplier = 0;
	    				}
	    				if (multiplier == 0) {
		    				XposedBridge.log("Prevented " + lpparam.packageName + " to setAlarm(" + param.args[0] + ", " + param.args[1] + ", " + param.args[2] + ")");
		    				param.setResult(null);
		    				return;
	    				} else {
	    					long now = now((Integer)param.args[0]);
	    					long at = (Long)param.args[1];
	    					at = (at - now) * multiplier + now; 
		    				param.args[1] = at;
		    				XposedBridge.log("Delay " + lpparam.packageName + " to setAlarm(" + param.args[0] + ", " + at + ", " + param.args[2] + ")");
	    				}
	    			}
				}
    		});
    		XposedHelpers.findAndHookMethod(alarmManagerClass, "setRepeating", int.class, long.class, long.class, PendingIntent.class, new XC_MethodHook() {
				@Override
    			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
	    			if (prefs.getBoolean(lpparam.packageName + "/preventAlarm", false)) {
	    				int multiplier;
	    				try {
	    					multiplier = Integer.parseInt(prefs.getString(lpparam.packageName + "/alarmMultiplier", "0"));
	    				} catch (NumberFormatException ex) {
	    					multiplier = 0;
	    				}
	    				if (multiplier == 0) {
	    					XposedBridge.log("Prevented " + lpparam.packageName + " to setRepeatingAlarm(" + param.args[0] + ", " + param.args[1] + ", " + param.args[2] + ", " + param.args[3] + ")");
	    					param.setResult(null);
	    					return;
	    				} else {
	    					long now = now((Integer)param.args[0]);
	    					long at = (Long)param.args[1];
	    					long interval = (Long)param.args[2];
	    					at = (at - now) * multiplier + now; 
	    					interval = interval * multiplier;
	    					XposedBridge.log("Delay " + lpparam.packageName + " to setRepeatingAlarm(" + param.args[0] + ", " + at + ", " + interval + ", " + param.args[3] + ")");
	    					param.args[1] = at;
	    					param.args[2] = interval;
	    					XposedBridge.log("Delay " + lpparam.packageName + " to setRepeatingAlarm(" + param.args[0] + ", " + at + ", " + interval + ", " + param.args[3] + ")");
	    				}
	    			}
				}
    		});
    		XposedHelpers.findAndHookMethod(alarmManagerClass, "setInexactRepeating", int.class, long.class, long.class, PendingIntent.class, new XC_MethodHook() {
				@Override
    			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					if (prefs.getBoolean(lpparam.packageName + "/preventAlarm", false)) {
	    				int multiplier;
	    				try {
	    					multiplier = Integer.parseInt(prefs.getString(lpparam.packageName + "/alarmMultiplier", "0"));
	    				} catch (NumberFormatException ex) {
	    					multiplier = 0;
	    				}
	    				if (multiplier == 0) {
							XposedBridge.log("Prevented " + lpparam.packageName + " to setInexactRepeatingAlarm(" + param.args[0] + ", " + param.args[1] + ", " + param.args[2] + ", " + param.args[3] + ")");
							param.setResult(null);
							return;
	    				} else {
	    					long now = now((Integer)param.args[0]);
	    					long at = (Long)param.args[1];
	    					long interval = (Long)param.args[2];
	    					at = (at - now) * multiplier + now; 
	    					interval = interval * multiplier;
	    					param.args[1] = at;
	    					param.args[2] = interval;
	    					XposedBridge.log("Delay " + lpparam.packageName + " to setInexactRepeatingAlarm(" + param.args[0] + ", " + at + ", " + interval + ", " + param.args[3] + ")");
	    				}
	    			}
				}
    		});
    	}
    	if (prefs.getBoolean(lpparam.packageName + "/preventWakeLock", false)) {
    		Class wakeLockClass = XposedHelpers.findClass("android.os.PowerManager$WakeLock", lpparam.classLoader);
    		XposedHelpers.findAndHookMethod(wakeLockClass, "acquireMethod", new XC_MethodHook() {
				@Override
    			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
	    			if (prefs.getBoolean(lpparam.packageName + "/preventWakeLock", false)) {
	    				param.setResult(null);
	    				return;
	    			}
				}
    		});
    		XposedHelpers.findAndHookMethod(wakeLockClass, "acquireTimeoutMethod", Long.class, new XC_MethodHook() {
				@Override
    			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
	    			if (prefs.getBoolean(lpparam.packageName + "/preventWakeLock", false)) {
	    				param.setResult(null);
	    				return;
	    			}
				}
    		});
    	}
    	if (prefs.getBoolean(lpparam.packageName + "/proxyEnabled", false)) {
	    	XposedBridge.hookAllConstructors(XposedHelpers.findClass("org.apache.http.impl.client.DefaultHttpClient", lpparam.classLoader), new XC_MethodHook() {
	    		protected void afterHookedMethod(MethodHookParam param) throws Throwable  {
	    			if (prefs.getBoolean(lpparam.packageName + "/proxyEnabled", false)) {
		    	    	String proxyHost = prefs.getString(lpparam.packageName + "/proxyHost", "");
		    	    	int proxyPort;
		    	    	try {
		    	    		proxyPort = Integer.parseInt(prefs.getString(lpparam.packageName + "/proxyPort", ""));
		    	    	} catch (NumberFormatException ex) {
		    	    		proxyPort = -1;
		    	    	}
		
		    			DefaultHttpClient httpClient = (DefaultHttpClient)param.thisObject;
		    			HttpHost proxy = new HttpHost(proxyHost, proxyPort);
		    			httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
	    			}
	    		}
	    	});
    	}
    }
}