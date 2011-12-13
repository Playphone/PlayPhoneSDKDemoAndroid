package com.playphone.sdk.example;

import com.playphone.multinet.MNDirect;
import com.playphone.multinet.MNDirectUIHelper;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class DashboardControlActivity extends CustomTitleListActivity {

	private static class DashboardCallItem {

		public DashboardCallItem(String name, String command) {
			this.name = name;
			this.command = command;
			this.param = null;
		}

		public DashboardCallItem(String name, String command, String param) {
			this.name = name;
			this.command = command;
			this.param = param;
		}

		public String name;
		public String command;
		public String param;
	}

	DashboardCallItem[] itemList = {
			new DashboardCallItem("Leaderboards", "jumpToLeaderboard"),
			new DashboardCallItem("Friend list", "jumpToBuddyList"),
			new DashboardCallItem("User Profile", "jumpToUserProfile"),
			new DashboardCallItem("User Home", "jumpToUserHome"),
			new DashboardCallItem("Achievements", "jumpToAchievements"),
			new DashboardCallItem("Game Info", "jumpToGameInfo"),
			new DashboardCallItem("Add Friends", "jumpToAddFriends"),
			new DashboardCallItem("PlayCredits Shop", "jumpToGameShop", "_credits"),
			new DashboardCallItem("Redeem", "jumpToGameShop", "_redeem"),
			new DashboardCallItem("Shop catalog", "jumpToGameShop") };

	ArrayAdapter<DashboardCallItem> aa = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dashboard_control);

		// set the breadcrumbs text
		TextView txtBreadCrumbs = (TextView) findViewById(R.id.txtBreadCrumbs);
		txtBreadCrumbs.setText("Home > Dashboard Control");

		aa = new ArrayAdapter<DashboardCallItem>(DashboardControlActivity.this,
				R.layout.dashboard_control_item, itemList) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View row = convertView;

				if (row == null) {
					LayoutInflater li = getLayoutInflater();
					row = li.inflate(R.layout.dashboard_control_item, null);
				}
				DashboardCallItem i = itemList[position];
				((TextView) row.findViewById(R.id.name)).setText(i.name);
				StringBuilder info = new StringBuilder(i.command);
				if (i.param != null) {
					info.append(" : ").append(i.param);
				}
				((TextView) row.findViewById(R.id.command)).setText(info
						.toString());
				return row;
			}
		};

		setListAdapter(aa);

		getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> paramAdapterView,
					View paramView, int position, long paramLong) {

				DashboardCallItem i = itemList[position];

				MNDirect.execAppCommand(i.command, i.param);
				MNDirectUIHelper.showDashboard();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		MNDirectUIHelper.setHostActivity(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MNDirectUIHelper.setHostActivity(null);
	}
}
