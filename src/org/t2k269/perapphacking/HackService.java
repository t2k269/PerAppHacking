package org.t2k269.perapphacking;

import java.io.FileDescriptor;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;

import org.apache.http.HttpHost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
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
	
	private Context appContext;
	
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
    	if (prefs.getString(lpparam.packageName + "/timeMachine", "").length() > 0) {
    		try {
    			final long fakeTime = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(prefs.getString(lpparam.packageName + "/timeMachine", "")).getTime();
        		final Long[] baseHolder = new Long[1];
        		Class contextWrapperClass = XposedHelpers.findClass("java.lang.System", lpparam.classLoader);
        		XposedHelpers.findAndHookMethod(contextWrapperClass, "currentTimeMillis", new XC_MethodHook() {
    				@Override
        			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
    	    			if (baseHolder[0] == null) {
    	    				baseHolder[0] = (Long)param.getResult();
    	    				return;
    	    			}
    	    			long baseTime = baseHolder[0];
    	    			long currTime = (Long)param.getResult();
    	    			param.setResult(currTime - baseTime + fakeTime);
        			}
        		});
        		XposedHelpers.findAndHookMethod("android.text.format.Time", lpparam.classLoader, "setToNow", new XC_MethodHook() {
    				@Override
        			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
    	    			((android.text.format.Time)param.thisObject).set(fakeTime);
    	    			param.setResult(null);
        			}
        		});
    		} catch (Exception ex) {
    			// Ignore if the date is invalid
    		}
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
    	if (prefs.getBoolean(lpparam.packageName + "/muteIfSientInProfileGroup", false)) {
    		XposedHelpers.findAndHookMethod(Application.class, "onCreate", new XC_MethodHook() {
    			 protected void beforeHookedMethod(MethodHookParam param) throws Throwable{
    				 appContext = (Context)param.thisObject; 
    			 }
    		});
    		Class clazz = XposedHelpers.findClass("android.media.MediaPlayer", lpparam.classLoader);
    		XC_MethodHook hook = new XC_MethodHook() {
				@Override
    			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
	    			if (prefs.getBoolean(lpparam.packageName + "/muteIfSientInProfileGroup", false)) {
	    				if (appContext != null && !shouldPackagePlaySound(appContext, lpparam.packageName)) {
	    					// Skip the start method
	    					param.setResult(null);
	    				}
	    			}
				}
    		};
    		XposedHelpers.findAndHookMethod(clazz, "start", hook);
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
//		    				XposedBridge.log("Prevented " + lpparam.packageName + " to setAlarm(" + param.args[0] + ", " + param.args[1] + ", " + param.args[2] + ")");
		    				param.setResult(null);
		    				return;
	    				} else {
	    					long now = now((Integer)param.args[0]);
	    					long at = (Long)param.args[1];
	    					at = (at - now) * multiplier + now; 
		    				param.args[1] = at;
//		    				XposedBridge.log("Delay " + lpparam.packageName + " to setAlarm(" + param.args[0] + ", " + at + ", " + param.args[2] + ")");
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
//	    					XposedBridge.log("Prevented " + lpparam.packageName + " to setRepeatingAlarm(" + param.args[0] + ", " + param.args[1] + ", " + param.args[2] + ", " + param.args[3] + ")");
	    					param.setResult(null);
	    					return;
	    				} else {
	    					long now = now((Integer)param.args[0]);
	    					long at = (Long)param.args[1];
	    					long interval = (Long)param.args[2];
	    					at = (at - now) * multiplier + now; 
	    					interval = interval * multiplier;
//	    					XposedBridge.log("Delay " + lpparam.packageName + " to setRepeatingAlarm(" + param.args[0] + ", " + at + ", " + interval + ", " + param.args[3] + ")");
	    					param.args[1] = at;
	    					param.args[2] = interval;
//	    					XposedBridge.log("Delay " + lpparam.packageName + " to setRepeatingAlarm(" + param.args[0] + ", " + at + ", " + interval + ", " + param.args[3] + ")");
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
//							XposedBridge.log("Prevented " + lpparam.packageName + " to setInexactRepeatingAlarm(" + param.args[0] + ", " + param.args[1] + ", " + param.args[2] + ", " + param.args[3] + ")");
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
//	    					XposedBridge.log("Delay " + lpparam.packageName + " to setInexactRepeatingAlarm(" + param.args[0] + ", " + at + ", " + interval + ", " + param.args[3] + ")");
	    				}
	    			}
				}
    		});
    	}
    	if (prefs.getBoolean(lpparam.packageName + "/preventWakeLock", false)) {
    		Class wakeLockClass = XposedHelpers.findClass("android.os.PowerManager$WakeLock", lpparam.classLoader);
    		XposedHelpers.findAndHookMethod(wakeLockClass, "acquire", new XC_MethodHook() {
				@Override
    			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
	    			if (prefs.getBoolean(lpparam.packageName + "/preventWakeLock", false)) {
	    				param.setResult(null);
	    				return;
	    			}
				}
    		});
    		XposedHelpers.findAndHookMethod(wakeLockClass, "acquire", Long.class, new XC_MethodHook() {
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

	
	public enum Mode {
        SUPPRESS, DEFAULT, OVERRIDE;
    }
	
	private static boolean shouldPackagePlaySound(Context context, String packageName) {
        if (!packageName.equals("android")) {
        	try {
				Object pm = context.getSystemService("profile" /*Context.PROFILE_SERVICE*/);
				Method getActiveProfileGroupMethod = pm.getClass().getMethod("getActiveProfileGroup", String.class);
				Object profileGroup = getActiveProfileGroupMethod.invoke(pm, packageName);
				if (profileGroup != null) {
					Method getSoundModeMethod = profileGroup.getClass().getMethod("getSoundMode");
					Object mode = getSoundModeMethod.invoke(profileGroup);
					Method nameMethod = mode.getClass().getMethod("name");
					String name = (String)nameMethod.invoke(mode);
					if ("SUPPRESS".equals(name))
						return false;
				}
				return true;
        	} catch (Exception ex) {
            	XposedBridge.log("Retrieve profile group failed! " + ex.getMessage());
        		return true;
        	}
        } else {
    		return true;
        }
	}
}