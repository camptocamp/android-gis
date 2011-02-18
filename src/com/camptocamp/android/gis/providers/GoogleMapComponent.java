package com.camptocamp.android.gis.providers;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import com.camptocamp.android.gis.MapComponent;
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

// https://code.google.com/intl/en/apis/maps/documentation/staticmaps/#Limits
// https://code.google.com/intl/en/apis/maps/faq.html#tos_nonweb

public class GoogleMapComponent extends MapComponent {

    protected GoogleTile displayTile;
    protected GoogleTile neededTile;
    protected Image logo;
    protected final String baseUrl;

    protected Timer timer = new Timer();

    public GoogleMapComponent(String baseUrl, WgsBoundingBox bbox, WgsPoint middlePoint, int width,
            int height, int zoom) {
        super(bbox, middlePoint, width, height, zoom);
        this.baseUrl = baseUrl;
        try {
            logo = Image.createImage("/images/google_logo_small.png");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void paint(final Graphics g) {
        super.paint(g);
        if (logo != null) {
            // -45 to be over the zoom
            g.drawImage(logo, 5, displayHeight - 45, Graphics.LEFT | Graphics.BOTTOM);
        }
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
    protected boolean paintTile(final Graphics g, final MapTile mt, final MapPos centerCopy,
            final Rectangle change) {
        if (neededTile != null && neededTile.image != null) {
            displayTile = neededTile;
            neededTile = null;
        }
        if (displayTile != null && displayTile.image != null) {
            g.drawImage(displayTile.image, displayWidth / 2 + displayTile.middlePoint.getX()
                    - centerCopy.getX(), displayHeight / 2 + displayTile.middlePoint.getY()
                    - centerCopy.getY(), Graphics.HCENTER | Graphics.VCENTER);
        }
        return displayTile != null && displayTile.image != null;
    }

    protected void enqueueTiles() {
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
        neededTile = new GoogleTile(4 * displayWidth, 2 * displayHeight, getZoom(),
                getCenterPoint(), middlePoint);
        final MapTilesRequestor mapTilesRequestor = this;
        timer.schedule(new TimerTask() {
            GoogleTile tile = neededTile;
            @Override
            public void run() {
                if (tile.isValid()) {
                    taskRunner.enqueue(new GoogleTileTask(mapTilesRequestor, taskRunner, tile, 3));
                }
            }
        }, 50);
    }

    public class GoogleTileTask implements Task, ResourceRequestor, ResourceStreamWaiter {
        private final MapTilesRequestor tilesRequestor;
        private final TasksRunner taskRunner;
        private final GoogleTile toRetrieve;
        private final int retryLeft;

        public GoogleTileTask(final MapTilesRequestor tilesRequestor, final TasksRunner taskRunner,
                final GoogleTile toRetrieve, final int retryLeft) {
            this.tilesRequestor = tilesRequestor;
            this.taskRunner = taskRunner;
            this.toRetrieve = toRetrieve;
            this.retryLeft = retryLeft;
        }

        public void execute() {
            if (toRetrieve == null) {
                return;
            }
            taskRunner.enqueueDownload(this, Cache.CACHE_LEVEL_NONE);
        }

        public void retrieveErrorFor(final GoogleTile errorTile) {
            if (retryLeft != 0) {
                taskRunner.enqueue(new GoogleTileTask(tilesRequestor, taskRunner, errorTile,
                        retryLeft - 1));
            }
        }

        @Override
        public String resourcePath() {
            if (toRetrieve.isValid()) {
                String url = MessageFormat.format(baseUrl, toRetrieve.center.getLat(),
                        toRetrieve.center.getLon(), toRetrieve.zoom, toRetrieve.width,
                        toRetrieve.height);
                // android.util.Log.d(getClass().getName(), url);
                return url;
            }
            else {
                return null;
            }
        }

        @Override
        public void notifyError() {
        }

        @Override
        public int getCachingLevel() {
            return Cache.CACHE_LEVEL_NONE;
        }

        @Override
        public void streamOpened(InputStream stream, DownloadCounter counter, Cache networkCache)
                throws IOException {
            if (toRetrieve.isValid()) {
                toRetrieve.image = Image.createImage(stream);
                cleanMapBuffer();
                repaint();
            }
            else {
                stream.close();
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
