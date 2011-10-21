package com.playphone.sdk.example;

import java.util.ArrayList;

import com.playphone.multinet.MNDirect;
import com.playphone.multinet.MNDirectUIHelper;
import com.playphone.multinet.providers.MNGameSettingsProvider;
import com.playphone.multinet.providers.MNGameSettingsProvider.GameSettingInfo;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class GameSettingActivity extends CustomTitleListActivity {
	private ArrayList<MNGameSettingsProvider.GameSettingInfo> gameSettingsArray = new ArrayList<MNGameSettingsProvider.GameSettingInfo>();
	private ArrayAdapter<MNGameSettingsProvider.GameSettingInfo> aa = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game_setting);

		TextView txtBreadCrumbs = (TextView) findViewById(R.id.txtBreadCrumbs);
		txtBreadCrumbs.setText("Home > Game Settings");

		if (MNDirect.getGameSettingsProvider().isGameSettingListNeedUpdate()) {
			Toast.makeText(this, "Game setting list begin update ",
					Toast.LENGTH_SHORT).show();
			MNDirect.getGameSettingsProvider().doGameSettingListUpdate();
		}

		aa = new ArrayAdapter<GameSettingInfo>(this,
				R.layout.game_setting_item, R.id.item, gameSettingsArray) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View row = convertView;

				if (row == null) {
					LayoutInflater li = getLayoutInflater();
					row = li.inflate(R.layout.game_setting_item, null);
				}
				try {
					int id = MNDirect.getGameSettingsProvider()
							.getGameSettingList()[position].getId();
					((TextView) row.findViewById(R.id.item)).setText("id:" + id
							+ ((id == 0) ? " (default)" : ""));
					((TextView) row.findViewById(R.id.name))
							.setText(MNDirect.getGameSettingsProvider()
									.getGameSettingList()[position].getName());
				} catch (Exception e) {
				}

				return row;
			}
		};

		setListAdapter(aa);

		getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> paramAdapterView,
					View paramView, int position, long paramLong) {

				MNGameSettingsProvider.GameSettingInfo gsi = MNDirect
						.getGameSettingsProvider().getGameSettingList()[position];

				Intent intent = new Intent(GameSettingActivity.this,
						GameSettingDetailActivity.class);
				intent.putExtra("gamesetting",true);
				intent.putExtra("id", gsi.getId());
				intent.putExtra("name", gsi.getName());
				intent.putExtra("params", gsi.getParams());
				intent.putExtra("sysparams", gsi.getSysParams());
				
				startActivity(intent);
			}
		});

		onDataUpdated();
	}

	private void onDataUpdated() {
		gameSettingsArray.clear();
		for (MNGameSettingsProvider.GameSettingInfo info : MNDirect
				.getGameSettingsProvider().getGameSettingList()) {
			gameSettingsArray.add(info);
		}
		aa.notifyDataSetChanged();
	}
	
	@Override
	protected void onResume() {
		MNDirect.getGameSettingsProvider().addEventHandler(gsEventHandler);
		MNDirectUIHelper.setHostActivity(this);
		super.onResume();
	}

	@Override
	protected void onPause() {
		MNDirect.getGameSettingsProvider().removeEventHandler(gsEventHandler);
		MNDirectUIHelper.setHostActivity(null);
		super.onPause();
	}

	MNGameSettingsProvider.IEventHandler gsEventHandler = new MNGameSettingsProvider.IEventHandler() {
		@Override
		public void onGameSettingListUpdated() {
			onDataUpdated();
		}
	};
}
