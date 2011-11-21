package com.playphone.multinet;

import java.io.IOException;
import java.io.InputStream;

import com.playphone.multinet.core.MNURLDownloader;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Message;
import android.util.Log;

/**
 *  Score progress processing utility methods
 */
class MNScoreProgressUtil {

	/**
	 * Call for loading image from application resources
	 * 
	 * @param res
	 *            application resources object
	 * @param id
	 *            resource id
	 * @return requested Bitmap
	 */
	public static Bitmap getBitmapImageById(Resources res, int id) {
		return BitmapFactory.decodeResource(res, id);
	}

	/**
	 * 
	 * Call for asynchronous loading bitmap image by url
	 * 
	 * @param msg
	 *            Object will be linked to bitmap image by url on finish of loading.  
	 *            After message will be sent .  
	 * @param url 
	 *            download image url
	 * @param defaultImg
	 *            default image bitmap. This will be sent as message object if
	 *            any download url image error by sendToTarget() method.
	 *            Create msg by handler.obtainMessage(...) function if need.    
	 */
	public static void downloadImageAssinc(final Message msg, final String url,
			final Bitmap defaultImg) {

		class ImageDownloader extends MNURLDownloader implements
				MNURLDownloader.IErrorEventHandler {

			public Bitmap result = null;

			public void loadURL(String url) {
				super.loadURL(url, null, this);
			}

			public void downloaderLoadFailed(MNURLDownloader downloader,
					ErrorInfo errorInfo) {
				msg.obj = defaultImg;
				msg.sendToTarget();
				Log.e("ImageDownloader", errorInfo.getMessage());
			}

			protected void readData(InputStream inputStream) throws IOException {
				result = BitmapFactory.decodeStream(inputStream);

				if (result == null) {
					result = defaultImg;
				}

				if (result != null) {
					msg.obj = result;
					msg.sendToTarget();
				}

				Log.i("ImageDownloader",
						"Image downloaded ok : height = "
								+ Integer.toString(result.getHeight())
								+ ", width = "
								+ Integer.toString(result.getWidth()));
			}

		}
		ImageDownloader downloader = new ImageDownloader();

		downloader.loadURL(url);
	}
}
