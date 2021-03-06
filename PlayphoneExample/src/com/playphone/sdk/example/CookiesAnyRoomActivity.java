package com.playphone.sdk.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.playphone.multinet.MNDirect;
import com.playphone.multinet.MNDirectUIHelper;

import com.playphone.multinet.core.ws.data.MNWSRoomListItem;

import com.playphone.multinet.providers.MNWSInfoRequestCurrGameRoomList;
import com.playphone.multinet.providers.MNGameRoomCookiesProvider;

import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class CookiesAnyRoomActivity extends CustomTitleActivity implements
		OnClickListener, Callback {

	private static final int ROOM_LIST_REQUEST_ERROR = 0;
	private static final int ROOM_LIST_REQUEST_SUCCESS = 1;
	private static final int GAME_COOKIE_DOWNLOAD_SUCCESS = 3;
	private static final int GAME_COOKIE_DOWNLOAD_FAIL = 4;
	private static final int START_ROOM_LIST_PROGRESS = 5;
	private static final int STOP_ROOM_LIST_PROGRESS = 6;

	private Handler handler = new Handler(this);

	private ProgressBar roomListProgress;
	private Button reloadListButton;
	private ListView roomListView;
	private TextView cookiesInfoText;

	HashMap<Integer, String> cookiesList = new HashMap<Integer, String>();

	protected ArrayList<MNWSRoomListItem> roomList = new ArrayList<MNWSRoomListItem>();
	private ArrayAdapter<MNWSRoomListItem> aa;

	private void roomListUpdated() {
		aa.notifyDataSetChanged();
	}

	public class RoomListRequestEventHandler implements
			MNWSInfoRequestCurrGameRoomList.IEventHandler {
          public void onCompleted (MNWSInfoRequestCurrGameRoomList.RequestResult result) {
            if (!result.hadError()) {
              CookiesAnyRoomActivity.this.roomList.clear();

              for (MNWSRoomListItem item : result.getDataEntry()) {
                CookiesAnyRoomActivity.this.roomList.add(item);
              }

              handler.sendEmptyMessage(ROOM_LIST_REQUEST_SUCCESS);
              handler.sendEmptyMessage(STOP_ROOM_LIST_PROGRESS);
            }
            else {
              Message msg = handler.obtainMessage(ROOM_LIST_REQUEST_ERROR,result.getErrorMessage());
              msg.sendToTarget();
              handler.sendEmptyMessage(STOP_ROOM_LIST_PROGRESS);
            }
          }
	}

	private void requestRoomList() {
          handler.sendEmptyMessage(START_ROOM_LIST_PROGRESS);

          MNDirect.getWSProvider().send
           (new MNWSInfoRequestCurrGameRoomList
             (new RoomListRequestEventHandler()));
	}

	MNGameRoomCookiesProvider.IEventHandler eventHandler = new MNGameRoomCookiesProvider.IEventHandler() {

		@Override
		public void onGameRoomCookieDownloadSucceeded(int roomSFId, int key,
				String cookie) {
			if (cookie != null) {
				cookiesList.put(key, cookie);
			} else {
				cookiesList.remove(key);
			}
			
			Message msg = handler.obtainMessage(GAME_COOKIE_DOWNLOAD_SUCCESS, new Integer(roomSFId));
			msg.sendToTarget();
		}

		@Override
		public void onGameRoomCookieDownloadFailedWithError(int roomSFId,
				int key, String error) {
			// skip
			Message msg = handler.obtainMessage(GAME_COOKIE_DOWNLOAD_FAIL, error);
			msg.sendToTarget();
		}

		@Override
		public void onCurrentGameRoomCookieUpdated(int key,
				String newCookieValue) {
			// unsupported call
		}
	};

	private void requestCookies(int roomSFId) {
		for (int index = 0; index < 5; index++) {
			MNDirect.getGameRoomCookiesProvider().downloadGameRoomCookie(
					roomSFId, index);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cookies_any_room);

		// set the breadcrumbs text
		TextView txtBreadCrumbs = (TextView) findViewById(R.id.txtBreadCrumbs);
		txtBreadCrumbs.setText("Home > Cookies > Any Room");

		roomListProgress = (ProgressBar) findViewById(R.id.roomListProgress);
		reloadListButton = (Button) findViewById(R.id.reloadListButton);
		roomListView = (ListView) findViewById(R.id.roomListView);
		cookiesInfoText = (TextView) findViewById(R.id.cookiesInfoText);

		reloadListButton.setOnClickListener(this);

		aa = new ArrayAdapter<MNWSRoomListItem>(this, 0, roomList) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View row = convertView;

				if (row == null) {
					row = new TextView(CookiesAnyRoomActivity.this);
				}

				TextView rowText = (TextView) row;
				MNWSRoomListItem item = roomList.get(position);
				StringBuffer line = new StringBuffer();
				line.append(" id (").append(item.getRoomSFId()).append(") ");
				line.append(" : ").append(item.getRoomName());
				rowText.setTextSize(20);
				rowText.setText(line.toString());

				return row;
			}
		};

		roomListView.setAdapter(aa);

		roomListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapter, View v, int pos,
					long id) {

				MNWSRoomListItem item = roomList.get(pos);
				requestCookies(item.getRoomSFId());
			}
		});

		if (MNDirect.isUserLoggedIn()) {
			requestRoomList();
		} else {
			handler.sendEmptyMessage(STOP_ROOM_LIST_PROGRESS);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		MNDirectUIHelper.setHostActivity(this);
		MNDirect.getGameRoomCookiesProvider().addEventHandler(eventHandler);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MNDirectUIHelper.setHostActivity(null);
		MNDirect.getGameRoomCookiesProvider().removeEventHandler(eventHandler);
	}

	@Override
	public void onClick(View v) {
		if (!MNDirect.isUserLoggedIn()) {
			Toast.makeText(this, "You need be logged in", Toast.LENGTH_LONG)
					.show();
			return;
		}

		if (v == reloadListButton) {
			requestRoomList();
		}
	}

	@Override
	public boolean handleMessage(Message msg) {
		if (ROOM_LIST_REQUEST_ERROR == msg.what) {
			String errorMessage = (String)msg.obj;
			Toast.makeText(this,
					"Room list request error :\n   " + errorMessage,
					Toast.LENGTH_LONG).show();
		} else if (ROOM_LIST_REQUEST_SUCCESS == msg.what) {
			roomListUpdated();
		} else if (msg.what == GAME_COOKIE_DOWNLOAD_SUCCESS) {
			Integer roomSfId = (Integer) msg.obj;
			updateResult(roomSfId);
		} else if (msg.what == GAME_COOKIE_DOWNLOAD_FAIL) {
			String text = (String) msg.obj;
			cookiesInfoText.setText(text);
		} else if (msg.what == START_ROOM_LIST_PROGRESS) {
			roomListProgress.setVisibility(View.VISIBLE);
			roomListProgress.setIndeterminate(true);
		} else if (msg.what == STOP_ROOM_LIST_PROGRESS) {
			roomListProgress.setIndeterminate(false);
			roomListProgress.setVisibility(View.INVISIBLE);
		} else {
			return false;
		}

		return true;
	}

	private void updateResult(int roomSFId) {
		if (MNDirect.isUserLoggedIn()) {
			StringBuilder text = new StringBuilder();

			text.append("Room id : ").append(roomSFId).append("\n\n");

			for (int index = 0; index < 5; index++) {

				text.append("Id : ").append(index).append(",");
				text.append(" Value : ");

				if (cookiesList.containsKey(index)) {
					text.append(cookiesList.get(index));
				} else {
					text.append("(null)");
				}
				text.append("\n");
			}

			cookiesInfoText.setText(text.toString());
		}
	}
}
