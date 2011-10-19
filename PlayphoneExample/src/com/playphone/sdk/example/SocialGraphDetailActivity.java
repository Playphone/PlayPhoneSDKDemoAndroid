package com.playphone.sdk.example;

import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class SocialGraphDetailActivity extends CustomTitleActivity {
	private TableLayout infoTable;
	private ImageView avatarImage;
	
	private class FillAvatarTask extends AsyncTask<String, Void, Bitmap> {
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

	FillAvatarTask avatarTask = new FillAvatarTask();
	
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
		//setContentView(R.layout.post_userinfo);
		setContentView(R.layout.social_graph_detail);
		
		// set the breadcrumbs text
		TextView txtBreadCrumbs = (TextView) findViewById(R.id.txtBreadCrumbs);
		txtBreadCrumbs.setText("Home > Social Graph > Detail");
		TextView infoTitle = (TextView) findViewById(R.id.infoTitle);
		infoTitle.setText(R.string.friend_details);
		
		infoTable = (TableLayout) findViewById(R.id.infoTable);
		avatarImage = (ImageView) findViewById(R.id.avatarImage);
		
		if(getIntent().hasExtra("social"))
		{
			avatarTask.execute(getIntent().getStringExtra("avatarurl"));
			
			addRowToInfoTable("User Name: ", getIntent().getStringExtra("username"));
			addRowToInfoTable("User ID: ",String.valueOf(getIntent().getLongExtra("userid",0)));
			addRowToInfoTable("Online: ",String.valueOf(getIntent().getBooleanExtra("online",false)));
			addRowToInfoTable("Playing game: ",String.valueOf(getIntent().getStringExtra("ingame")));
			addRowToInfoTable(" "," ");
			addRowToInfoTable("Has current game: ",String.valueOf(getIntent().getBooleanExtra("hascurrentgame",false)));
			addRowToInfoTable(" "," ");
			addRowToInfoTable("Locale: ",getIntent().getStringExtra("locale"));
			addRowToInfoTable("Is ignored: ",String.valueOf(getIntent().getBooleanExtra("isignored",false)));
			addRowToInfoTable("Curr. room: ",String.valueOf(getIntent().getIntExtra("curroom",-1)));
			addRowToInfoTable("Curr. game achievements: ",getIntent().getStringExtra("ingameachievements"));			
		}
	}

	@Override
	protected void onPause() {
		super.onResume();
		avatarTask.cancel(true);
	}
}

