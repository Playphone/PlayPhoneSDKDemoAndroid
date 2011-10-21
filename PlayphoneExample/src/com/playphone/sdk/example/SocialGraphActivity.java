package com.playphone.sdk.example;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.playphone.multinet.MNConst;
import com.playphone.multinet.MNDirect;
import com.playphone.multinet.MNDirectUIHelper;
import com.playphone.multinet.core.IMNSessionEventHandler;
import com.playphone.multinet.core.MNSessionEventHandlerAbstract;
import com.playphone.multinet.core.ws.IMNWSRequestEventHandler;
import com.playphone.multinet.core.ws.MNWSRequestContent;
import com.playphone.multinet.core.ws.MNWSRequestError;
import com.playphone.multinet.core.ws.MNWSRequestSender;
import com.playphone.multinet.core.ws.MNWSResponse;
import com.playphone.multinet.core.ws.data.MNWSBuddyListItem;

public class SocialGraphActivity extends CustomTitleListActivity implements
		Handler.Callback {

	ArrayList<MNWSBuddyListItem> buddiesList = new ArrayList<MNWSBuddyListItem>();
	ArrayAdapter<MNWSBuddyListItem> aa = null;

	protected void requestBuddyList() {
		// send request
		// create content object
		MNWSRequestContent content = new MNWSRequestContent();

		// add "friend list" request to request content.
		// store block name returned by the "add..." call to use it later to
		// extract friends information from response
		String blockName = content.addCurrUserBuddyList();

		// create "request sender" object
		MNWSRequestSender sender = new MNWSRequestSender(MNDirect.getSession());

		// send "authorized" request, passing created content object and event
		// handler
		sender.sendWSRequestAuthorized(content, new MyBuddyListResponseHandler(
				blockName));
	}

	IMNSessionEventHandler mnDirectEventHandler = new MNSessionEventHandlerAbstract() {
		protected long oldUser = MNConst.MN_USER_ID_UNDEFINED;

		@Override
		public void mnSessionUserChanged(long userId) {
			super.mnSessionUserChanged(userId);

			if (oldUser != userId) {
				oldUser = userId;
				handler.sendEmptyMessage(REQUEST_UPDATE_BUDDY_LIST);
			}
		}
	};

	protected void onBuddyListUpdated() {
		aa.notifyDataSetChanged();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.social_graph);

		// set the breadcrumbs text
		TextView txtBreadCrumbs = (TextView) findViewById(R.id.txtBreadCrumbs);
		txtBreadCrumbs.setText("Home > Social Graph");

		aa = new ArrayAdapter<MNWSBuddyListItem>(SocialGraphActivity.this,
				R.layout.social_graph_item, R.id.name, buddiesList) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View row = convertView;

				if (row == null) {
					LayoutInflater li = getLayoutInflater();
					row = li.inflate(R.layout.social_graph_item, null);
				}
				MNWSBuddyListItem bli = buddiesList.get(position);
				((TextView) row.findViewById(R.id.name)).setText(bli
						.getFriendUserNickName());
				((TextView) row.findViewById(R.id.status)).setText(bli
						.getFriendUserOnlineNow() ? "online now"
						: "offline");
				return row;
			}
		};

		setListAdapter(aa);

		getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> paramAdapterView,
					View paramView, int position, long paramLong) {
				MNWSBuddyListItem buddy = buddiesList.get(position);

				Intent intent = new Intent(SocialGraphActivity.this,
						SocialGraphDetailActivity.class);
				intent.putExtra("social", "yes");

				intent.putExtra("avatarurl", buddy.getFriendUserAvatarUrl());
				intent.putExtra("username", buddy.getFriendUserNickName());
				intent.putExtra("userid", buddy.getFriendUserId());
				intent.putExtra("online", buddy.getFriendUserOnlineNow());
				intent.putExtra("ingame", buddy.getFriendInGameName());
				intent.putExtra("hascurrentgame",
						buddy.getFriendHasCurrentGame());
				intent.putExtra("locale", buddy.getFriendUserLocale());
				intent.putExtra("isignored", buddy.getFriendIsIgnored());
				intent.putExtra("curroom", buddy.getFriendInGameId());
				intent.putExtra("ingameachievements",
						buddy.getFriendCurrGameAchievementsList());

				startActivity(intent);
			}
		});
	}

	protected class MyBuddyListResponseHandler implements
			IMNWSRequestEventHandler {
		private String blockName;

		// store the block name which is used to access data in
		// onRequestCompleted method
		public MyBuddyListResponseHandler(String blockName) {
			this.blockName = blockName;
		}

		@SuppressWarnings("unchecked")
		public void onRequestCompleted(MNWSResponse response) {
			Log.d("playphone", "Request completed");
			buddiesList.clear();
			buddiesList.addAll((List<MNWSBuddyListItem>) response
					.getDataForBlock(blockName));
			handler.sendEmptyMessage(ON_BUDDY_LIST_UPDATED);
			handler.sendEmptyMessage(UPDATING_IS_OK);
		}

		public void onRequestError(MNWSRequestError error) {
			// log error message
			Log.e("BuddyListResponse", error.getMessage());
			handler.sendEmptyMessage(UPDATING_IS_FAIL);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (MNDirect.isOnline()) {
			requestBuddyList();
		} else {
			Toast.makeText(this,
					"For view this need be online", Toast.LENGTH_SHORT).show();
		}
		// setup handler for catch our status is online
		MNDirect.getSession().addEventHandler(mnDirectEventHandler);
		MNDirectUIHelper.setHostActivity(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		// remove our session handler
		MNDirect.getSession().removeEventHandler(mnDirectEventHandler);
		MNDirectUIHelper.setHostActivity(null);
	}

	Handler handler = new Handler(this);
	
	private static final int ON_BUDDY_LIST_UPDATED = 0;
	private static final int UPDATING_IS_OK = 1;
	private static final int UPDATING_IS_FAIL = 2;
	private static final int REQUEST_UPDATE_BUDDY_LIST = 3;

	@Override
	public boolean handleMessage(Message msg) {

		if (msg.what == ON_BUDDY_LIST_UPDATED) {
			onBuddyListUpdated();
		} else if (msg.what == UPDATING_IS_FAIL) {
			Toast.makeText(this,
					"Social graph request error", Toast.LENGTH_SHORT).show();
		} else if (msg.what == UPDATING_IS_OK) {
			Toast.makeText(this,
					"Social graph updating is Ok", Toast.LENGTH_SHORT).show();
		} else if (msg.what == REQUEST_UPDATE_BUDDY_LIST) {
			requestBuddyList();
		} else {
			return false;
		}

		return true;
	}
}
