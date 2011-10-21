package com.playphone.sdk.example;

import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.playphone.multinet.MNDirect;
import com.playphone.multinet.MNDirectUIHelper;
import com.playphone.multinet.MNUserInfo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class CurrentUserInfoActivity extends CustomTitleActivity {

	private ImageView avatarImage;
	private TableLayout infoTable;

	private class FillUserAvatarTask extends AsyncTask<String, Void, Bitmap> {
		@Override
		protected Bitmap doInBackground(String... paramArrayOfParams) {

			Bitmap result = null;

			for (String url : paramArrayOfParams) {

				DefaultHttpClient client = new DefaultHttpClient();
				HttpGet httpGet = new HttpGet(url);
				try {
					HttpResponse execute = client.execute(httpGet);
					InputStream content = execute.getEntity().getContent();

					result = BitmapFactory.decodeStream(content);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			return result;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			if (result != null) {
				avatarImage.setImageBitmap(result);
			}
		}
	}

	FillUserAvatarTask task = new FillUserAvatarTask();

	private void addRowToInfoTable(String text, String description) {
		TableRow tr = new TableRow(this);
		TextView cell;

		cell = new TextView(this);
		cell.setText(text);
		tr.addView(cell);

		cell = new TextView(this);
		cell.setText(description);
		tr.addView(cell);

		infoTable.addView(tr);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.post_userinfo);

		// set the breadcrumbs text
		TextView txtBreadCrumbs = (TextView) findViewById(R.id.txtBreadCrumbs);
		txtBreadCrumbs.setText("Home > Current User Info");

		TextView infoTitle = (TextView) findViewById(R.id.infoTitle);
		infoTitle.setText(R.string.details_current_user);

		avatarImage = (ImageView) findViewById(R.id.avatarImage);
		infoTable = (TableLayout) findViewById(R.id.infoTable);

		addRowToInfoTable("Username: ", MNDirect.getSession().getMyUserName());
		addRowToInfoTable("User id: ", String.valueOf(MNDirect.getSession().getMyUserId()));
		addRowToInfoTable("Current room: ", String.valueOf(MNDirect.getSession().getCurrentRoomId()));

		final MNUserInfo userInfo = MNDirect.getSession().getMyUserInfo();

		if (userInfo != null) {
			task.execute(userInfo.getAvatarUrl());
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		MNDirectUIHelper.setHostActivity(null);
		task.cancel(true);
	}

	@Override
	protected void onResume() {
		super.onResume();
		MNDirectUIHelper.setHostActivity(this);
	}

}
