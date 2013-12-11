package org.t2k269.perapphacking;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;

/**
 * A dummy fragment representing a section of the app, but that simply
 * displays dummy text.
 */
public class AllAppsSectionFragment extends Fragment implements AppsListFragment {
	private AppListAdapter adapter;
	
	private EditText text;
	private ListView appsList;
	
	public AllAppsSectionFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_app_list, container, false);

		text = (EditText)rootView.findViewById(R.id.filterText);
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
		appsList = (ListView)rootView.findViewById(R.id.appsList);
		adapter = new AppListAdapter(getActivity(), (TabsActivity)getActivity(), false);
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
	public void appsChanged() {
		if (adapter != null) {
			adapter.filter(text.getText().toString());
			adapter.notifyDataSetChanged();
		}
	}
}