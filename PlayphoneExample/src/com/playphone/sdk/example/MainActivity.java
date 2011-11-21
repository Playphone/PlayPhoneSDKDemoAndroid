package com.playphone.sdk.example;

import com.playphone.multinet.*;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class MainActivity extends ListActivity {

	// application specific information
	final private int _GAMEID = MyPlayphoneCredentials._GAMEID;
	final private String TAB = "       ";
	private static MNEventHandler eventHandler = null;

	public static MNEventHandler getMNEventHandler() {
		return eventHandler;
	}

	protected interface Entry {
		public String toString();

		public void run();
	}

	protected Entry[] getEntries() {
		return new Entry[] { new Entry() {
			@Override
			public String toString() {
				return "1. Required Integration";
			}

			@Override
			public void run() {
			}
		}, new Entry() {
			@Override
			public String toString() {
				return TAB + "Login User";
			}

			@Override
			public void run() {
				startActivity(new Intent(MainActivity.this,
						LoginUserActivity.class));
			}
		}, new Entry() {
			@Override
			public String toString() {
				return TAB + "Dashboard";
			}

			@Override
			public void run() {
				startActivity(new Intent(MainActivity.this,
						DashboardPageActivity.class));
			}
		}, new Entry() {
			@Override
			public String toString() {
				return TAB + "Virtual Economy";
			}

			@Override
			public void run() {
				startActivity(new Intent(MainActivity.this,
						VirtualEconomyListActivity.class));
			}
		}, new Entry() {
			@Override
			public String toString() {
				return "2. Advanced Features";
			}

			@Override
			public void run() {
			}
		}, new Entry() {
			@Override
			public String toString() {
				return TAB + "Current User Info";
			}

			@Override
			public void run() {
				startActivity(new Intent(MainActivity.this,
						CurrentUserInfoActivity.class));
			}
		}, new Entry() {
			@Override
			public String toString() {
				return TAB + "Game Settings";
			}

			@Override
			public void run() {
				startActivity(new Intent(MainActivity.this,
						GameSettingActivity.class));
			}
		}, new Entry() {
			@Override
			public String toString() {
				return TAB + "Leaderboards";
			}

			@Override
			public void run() {
				startActivity(new Intent(MainActivity.this,
						LeaderboardHome.class));
			}
		}, new Entry() {
			@Override
			public String toString() {
				return TAB + "Achievements";
			}

			@Override
			public void run() {
				startActivity(new Intent(MainActivity.this,
						AchievementsHomeActivity.class));
			}
		}, new Entry() {
			@Override
			public String toString() {
				return TAB + "Social Graph";
			}

			@Override
			public void run() {
				startActivity(new Intent(MainActivity.this,
						SocialGraphActivity.class));
			}
		}, new Entry() {
			@Override
			public String toString() {
				return TAB + "Dashboard Control";
			}

			@Override
			public void run() {
				startActivity(new Intent(MainActivity.this,
						DashboardControlActivity.class));
			}
		},
		new Entry() {
			@Override
			public String toString() {
				return TAB + "Cloud Storage";
			}

			@Override
			public void run() {
				startActivity(new Intent(MainActivity.this,
						PostCloudStorageActivity.class));
			}
		},
		new Entry() {
			@Override
			public String toString() {
				return TAB + "Multiplayer Basics";
			}

			@Override
			public void run() {
				MNDirect.execAppCommand("jumpToUserHome", null);
				MNDirectUIHelper.showDashboard();
				Toast.makeText(
						MainActivity.this,
						"Click \"Play Now\" to join a multiplayer room and send messages",
						Toast.LENGTH_LONG).show();
			}

		},
		new Entry() {
			@Override
			public String toString() {
				return TAB + "Room cookies";
			}

			@Override
			public void run() {
				startActivity(new Intent(MainActivity.this,
						CookiesModeActivity.class));
			}
		}, new Entry() {
			@Override
			public String toString() {
				return TAB + "Server info";
			}

			@Override
			public void run() {
				startActivity(new Intent(MainActivity.this,
						ServerInfoActivity.class));
			}
		}, new Entry() {
			@Override
			public String toString() {
				return TAB + "Application Info";
			}

			@Override
			public void run() {
				startActivity(new Intent(MainActivity.this,
						ApplicationInfoActivity.class));
			}
		} };
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d("playphone", "onCreate has been called for MainActivity");
		super.onCreate(savedInstanceState);

		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);

		final Entry[] entries = getEntries();
		setListAdapter(new ArrayAdapter<Entry>(this, R.layout.main_menu_item,
				entries));

		ListView lv = getListView();
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				entries[position].run();
			}
		});

		eventHandler = new MNEventHandler();
		MNDirect.init(this._GAMEID, MNDirect.makeGameSecretByComponents(
				MyPlayphoneCredentials._APISECRET1,
				MyPlayphoneCredentials._APISECRET2,
				MyPlayphoneCredentials._APISECRET3,
				MyPlayphoneCredentials._APISECRET4), eventHandler, this);
		MNDirect.handleApplicationIntent(getIntent());
		MNDirectButton.initWithLocation(MNDirectButton.MNDIRECTBUTTON_TOPLEFT);
		MNDirectPopup.init(MNDirectPopup.MNDIRECTPOPUP_ALL);

		// MNDirectUIHelper.setDashboardStyle(MNDirectUIHelper.DASHBOARD_STYLE_FULLSCREEN);

		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.custom_title);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		MNDirect.shutdownSession();
	};

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

	protected class MNEventHandler extends MNDirectEventHandlerAbstract {

		private Handler handler;

		public Handler getHandler() {
			return handler;
		}

		public void setHandler(Handler handler) {
			this.handler = handler;
		}

		@Override
		public void mnDirectDoStartGameWithParams(MNGameParams params) {
			startActivity(new Intent(MainActivity.this,
					MultiPlayerActivity.class));
		}

		@Override
		public void mnDirectDidReceiveGameMessage(String message,
				MNUserInfo sender) {
			Log.d("playphone", "Received message: " + message);
			if (sender != null)
				Toast.makeText(
						getApplicationContext(),
						"User " + sender.userName + " sent message: " + message,
						Toast.LENGTH_LONG).show();
		}

		@Override
		public void mnDirectSessionStatusChanged(int newStatus) {
			if (handler != null) {
				Message msg = new Message();
				Bundle bundle = new Bundle();
				bundle.putInt("statusChange", newStatus);
				msg.setData(bundle);
				handler.sendMessage(msg);
			}
			Log.d("playphone", "The new status is " + newStatus);

		}
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
			dialogBuilder.setTitle(R.string.exit);
			dialogBuilder.setMessage(R.string.yousure);
			dialogBuilder.setNegativeButton(R.string.no,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					});
			dialogBuilder.setPositiveButton(R.string.yes,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					});
			dialogBuilder.show();
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}
}
