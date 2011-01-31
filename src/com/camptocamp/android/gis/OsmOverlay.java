package com.camptocamp.android.gis;

import com.nutiteq.components.MapTile;

public class OsmOverlay extends C2COverlay {

    // http://129.206.229.158/cached/hillshade?LAYERS=europe_wms%3Ahs_srtm_europa&
    // SRS=EPSG%3A900913&FORMAT=image%2FJPEG&TRANSPARENT=true&SERVICE=WMS&
    // VERSION=1.1.1&REQUEST=GetMap&STYLES=&EXCEPTIONS=application%2Fvnd.ogc.se_inimage&
    // BBOX=790053.12341406,6573584.4305477,790664.61964023,6574195.9267738&WIDTH=256&HEIGHT=256

    // private static final String TAG = Map.D + "OsmOverlay";
    // private String baseUrl;

    public OsmOverlay(final String baseUrl) {
        // this.baseUrl = baseUrl;
        // layers_all = new HashMap<String, String>() {
        // private static final long serialVersionUID = 1L;
        // {
        // put("o_shades", "europe_wms%3Ahs_srtm_europa");
        // }
        // };
    }

    @Override
    public String getOverlayTileUrl(MapTile tile) {
        return null;

        // final int x = tile.getX() >> 8;
        // final int y = tile.getY() >> 8;
        // final int zoom = tile.getZoom();
        // final StringBuffer url = new StringBuffer(baseUrl);
        // url.append(zoom);
        // url.append("/");
        // url.append(x);
        // url.append("/");
        // url.append(y);
        // url.append(".png");
        // return url.toString();

        // Point pt1 = tile.getMap().mapPosToWgs(new MapPos(tile.getX(),
        // tile.getY(), tile.getZoom()));
        // Point pt2 = tile.getMap().mapPosToWgs(
        // new MapPos(tile.getX() + 256, tile.getY() + 256, tile.getZoom()));
        // return String.format(baseUrl, layers_selected, (float) tile.getX(),
        // (float) tile.getY(),
        // (float) tile.getX() + 256, (float) tile.getY() + 256);
    }
}
