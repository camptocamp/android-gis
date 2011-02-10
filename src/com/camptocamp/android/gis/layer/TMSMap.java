package com.camptocamp.android.gis.layer;

import com.nutiteq.maps.projections.Projection;
import com.nutiteq.ui.Copyright;
import com.nutiteq.ui.StringCopyright;

public class TMSMap extends ProjectedUnstreamedMap {
    private String baseUrl;
    private String format;

    /**
     * Final URL will be generated as
     * <b>baseUrl</b><i>zoom</i>/<i>x</i>/<i>y</i><b>format</b>
     * 
     * @param copyright
     *            Copyright graphics drawn to the map
     * @param baseUrl
     *            URL beginning for the map tile request.
     * @param tileSize
     *            Size of tile image in pixels, usually 256
     * @param format
     *            Tile image format, usually ".png" or ".jpg".
     * @param minZoom
     *            Minimum (world) zoom level for service. Could be 0
     * @param maxZoom
     *            Maximum zoom level. E.g. for OSM set it to 19
     */
    public TMSMap(final String baseUrl, final int tileSize, final int minZoom,
            final int maxZoom, final String format, final Copyright copyright, Projection projection) {

        super(copyright, tileSize, minZoom, maxZoom, projection);
        this.format = format;
        this.baseUrl = baseUrl;
    }

    /**
     * Tiled Map Server based general map server API. Final URL will be
     * generated as <b>baseUrl</b><i>zoom</i>/<i>x</i>/<i>y</i><b>format</b>
     * 
     * @param copyright
     *            Copyright as string
     * @param baseUrl
     *            URL beginning for the map tile request.
     * @param tileSize
     *            Size of tile image in pixels, usually 256
     * @param format
     *            Tile image format, usually ".png" or ".jpg".
     * @param minZoom
     *            Minimum (world) zoom level for service. Could be 0
     * @param maxZoom
     *            Maximum zoom level. E.g. for OSM set it to 19
     */

    public TMSMap(final String baseUrl, final int tileSize, final int minZoom,
            final int maxZoom, final String format, final String copyright, Projection projection) {

        this(baseUrl, tileSize, minZoom, maxZoom, format, new StringCopyright(
                copyright), projection);

    }

    public String buildPath(final int mapX, final int mapY, final int zoom) {

        // Usable for TMS <baseurl>z/x/y<format> type of servers, 256-pixel

        final StringBuffer url = new StringBuffer(baseUrl);
        url.append(zoom);
        url.append("/");
        url.append(mapX / getTileSize()& ((1 << zoom) - 1));
        url.append("/");
        url.append(mapY / getTileSize());

        url.append(format);

        return url.toString();
    }
}
