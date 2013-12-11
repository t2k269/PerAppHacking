package org.t2k269.perapphacking;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.Gson;

/**
 * A dummy fragment representing a section of the app, but that simply
 * displays dummy text.
 */
public class BackupFragment extends Fragment {

	public BackupFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_backup, container, false);

		((Button)rootView.findViewById(R.id.backupButton)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				File dir = Environment.getExternalStorageDirectory();
				File file = new File(dir, "PerAppHacking.txt");
				List<AppInfo> apps = ((TabsActivity)getActivity()).getAppList();
				List<AppInfo> enabledApps = new ArrayList<AppInfo>();
				for (AppInfo app : apps) {
					if (app.isEnabled()) {
						enabledApps.add(app);
					}
				}
				String json = new Gson().toJson(enabledApps);
				try {
					OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
					try {
						writer.write(json);
						
					} finally {
						writer.close();
					}
					Toast.makeText(getActivity(), "Saved to " + file, Toast.LENGTH_LONG).show();
				} catch (IOException e) {
					Toast.makeText(getActivity(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
				}
			}
		});
		((Button)rootView.findViewById(R.id.restoreButton)).setOnClickListener(new OnClickListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void onClick(View v) {
				File dir = Environment.getExternalStorageDirectory();
				File file = new File(dir, "PerAppHacking.txt");
				try {
					InputStreamReader reader = new InputStreamReader(new FileInputStream(file), "UTF-8");
					try {
						ArrayList<?> apps = new Gson().fromJson(reader, ArrayList.class);
						for (Object data : apps) {
							Map<String,Object> map = (Map<String,Object>)data;
							((TabsActivity)getActivity()).saveAppSettings(map);
						}
					} finally {
						reader.close();
					}
					Toast.makeText(getActivity(), "Restored from " + file, Toast.LENGTH_LONG).show();
					((TabsActivity)getActivity()).reloadAppListAsync();
				} catch (IOException e) {
					Toast.makeText(getActivity(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
				}
			}
		});
		
		return rootView;
	}
	
	
}