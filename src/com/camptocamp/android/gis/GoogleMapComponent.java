package com.camptocamp.android.gis;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import com.nutiteq.cache.Cache;
import com.nutiteq.components.ImageBuffer;
import com.nutiteq.components.MapPos;
import com.nutiteq.components.MapTile;
import com.nutiteq.components.Rectangle;
import com.nutiteq.components.WgsBoundingBox;
import com.nutiteq.components.WgsPoint;
import com.nutiteq.io.ResourceRequestor;
import com.nutiteq.io.ResourceStreamWaiter;
import com.nutiteq.maps.MapTilesRequestor;
import com.nutiteq.net.DownloadCounter;
import com.nutiteq.task.Task;
import com.nutiteq.task.TasksRunner;

public class GoogleMapComponent extends C2CMapComponent {

	private GoogleTile displayTile;
	private GoogleTile neededTile;

	public GoogleMapComponent(WgsBoundingBox bbox, WgsPoint middlePoint,
			int width, int height, int zoom) {
		super(bbox, middlePoint, width, height, zoom);
	}

	protected Rectangle paintMap(final ImageBuffer buffer) {
		tileW = 1;
		tileH = 1;
		return super.paintMap(buffer);
	}

	/**
	 * Paint a map tile.
	 * 
	 * @param g
	 *            graphics object
	 * @param mt
	 *            map tile to paint
	 * @param centerCopy
	 *            copy of the current map center
	 * @param change
	 */
	protected boolean paintTile(final Graphics g, final MapTile mt,
			final MapPos centerCopy, final Rectangle change) {
		if (neededTile != null && neededTile.image != null) {
			displayTile = neededTile;
			neededTile = null;
		}
		if (displayTile != null && displayTile.image != null) {
			android.util.Log.d(getClass().getName(), "paintTile");
			android.util.Log.d(getClass().getName(), "x"
					+ displayTile.middlePoint.getX());
			android.util.Log.d(getClass().getName(), "x" + centerCopy.getX());

			g.drawImage(displayTile.image, displayWidth / 2
					+ displayTile.middlePoint.getX() - centerCopy.getX(),
					displayHeight / 2 + displayTile.middlePoint.getY()
							- centerCopy.getY(), Graphics.HCENTER
							| Graphics.VCENTER);
			return true;
		}
		return false;
	}

	protected void enqueueTiles() {
		android.util.Log.d(getClass().getName(), "enqueueTiles");

		/*
		 * if ((screenCache.find(mt) > 0) || neededTiles.contains(mt)) { return;
		 * }
		 */

		/*
		 * if (networkCache != null && networkCache.contains(mt.getIDString(),
		 * Cache.CACHE_LEVEL_MEMORY)) { return; }
		 */

		if (neededTile != null) {
			if (neededTile.middlePoint.equals(middlePoint)) {
				return;
			}
			neededTile.invalidate();
		}
		else if (displayTile != null) {
			if (displayTile.middlePoint.equals(middlePoint)) {
				return;
			}
		}
		neededTile = new GoogleTile(4 * displayWidth,
				2 * displayHeight, getZoom(), getCenterPoint(), middlePoint);
		taskRunner.enqueue(new GoogleTileTask(this, taskRunner, neededTile));
	}

	public class GoogleTileTask implements Task, ResourceRequestor,
			ResourceStreamWaiter {
		private final MapTilesRequestor tilesRequestor;
		private final TasksRunner taskRunner;
		private GoogleTile toRetrieve;

		public GoogleTileTask(final MapTilesRequestor tilesRequestor,
				final TasksRunner taskRunner, final GoogleTile toRetrieve) {
			android.util.Log.d(getClass().getName(), "GoogleTileTask");
			this.tilesRequestor = tilesRequestor;
			this.taskRunner = taskRunner;
			this.toRetrieve = toRetrieve;
		}

		public void execute() {
			if (toRetrieve == null) {
				return;
			}
			taskRunner.enqueueDownload(this, Cache.CACHE_LEVEL_NONE);
		}

		public void retrieveErrorFor(final GoogleTile errorTile) {
			taskRunner.enqueue(new GoogleTileTask(tilesRequestor, taskRunner,
					errorTile));
		}

		@Override
		public String resourcePath() {
			if (toRetrieve.isValid()) {
				String url = MessageFormat
						.format(
								"http://maps.google.com/maps/api/staticmap?center={0,number,0.0},{1,number,0.0}&zoom={2,number,0}&size={3,number,0}x{4,number,0}&maptype=roadmap&sensor=false",
								toRetrieve.center.getLat(), toRetrieve.center
										.getLon(), toRetrieve.zoom,
								toRetrieve.width, toRetrieve.height);
				android.util.Log.d(getClass().getName(), url);
				return url;
			} else {
				return null;
			}
		}

		@Override
		public void notifyError() {
			android.util.Log.d(getClass().getName(), "notifyError");
		}

		@Override
		public int getCachingLevel() {
			return Cache.CACHE_LEVEL_NONE;
		}

		@Override
		public void streamOpened(InputStream stream, DownloadCounter counter,
				Cache networkCache) throws IOException {
			if (toRetrieve.isValid()) {
				android.util.Log.d(getClass().getName(), "streamOpened");
				toRetrieve.image = Image.createImage(stream);
				cleanMapBuffer();
			}
		}
	}

	private class GoogleTile {
		private final int width;
		private final int height;
		private final int zoom;
		private final WgsPoint center;
		private final MapPos middlePoint;
		private Image image;
		private boolean valid = true;

		private GoogleTile(final int width, final int height, final int zoom,
				final WgsPoint center, MapPos middlePoint) {
			android.util.Log.d(getClass().getName(), "GoogleTile");
			this.width = width;
			this.height = height;
			this.zoom = zoom;
			this.center = center;
			this.middlePoint = middlePoint.copy();
		}

		public boolean isValid() {
			return valid;
		}

		public void invalidate() {
			this.valid = false;
		}
	}
}
