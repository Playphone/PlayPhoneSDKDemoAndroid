package com.playphone.multinet;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.conn.ssl.AllowAllHostnameVerifier;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.ConditionVariable;

import com.playphone.multinet.core.MNURLDownloader;

class MNInfoPanelUtils {
	public static class ImageDownloader extends MNURLDownloader implements
			MNURLDownloader.IErrorEventHandler {
		public ConditionVariable lock = new ConditionVariable();
		public Bitmap image = null;
		
		public void clean() {
			if (image != null) {
				image.recycle();
				image = null;
			}
		}
		
		public void loadURL(String url) {
			clean();
			setHttpsHostnameVerifier(new AllowAllHostnameVerifier());
			super.loadURL(url, null, this);
		}

		public void downloaderLoadFailed(MNURLDownloader downloader,
				ErrorInfo errorInfo) {
			clean();
			lock.open();
		}

		protected void readData(InputStream inputStream) throws IOException {
			image = BitmapFactory.decodeStream(inputStream);
			lock.open();
		}
	}
	
	public static Bitmap loadImageWithTimeout(String url, int timeout) {
		ImageDownloader id = new ImageDownloader();
		id.loadURL(url);
		id.lock.block(timeout);
		return id.image; 
	}
}
