package com.camptocamp.android.gis;

import java.util.HashMap;

import com.nutiteq.components.MapTile;

public class SwisstopoOverlay extends C2COverlay {
    // private static final String TAG = Map.D + "TestOverlay";
    private static final int TILESIZE = 256;
    private String baseUrl;

    public SwisstopoOverlay(final String baseUrl) {
        this.baseUrl = baseUrl;
        layers_all = new HashMap<String, String>() {
            private static final long serialVersionUID = -341470925482792417L;
            {
                put(
                        "o_cycling",
                        "VelolandRoutenNational,VelolandRoutenRegional,VelolandRoutenLokal,VelolandMiet,VelolandEbikestation,VelolandService");
                put("o_hiking",
                        "WanderlandRoutenNational,WanderlandRoutenRegional,WanderlandRoutenLokal,Wanderwegnetz");
                put("o_mountainbiking",
                        "MtblandRoutenNational,MtblandRoutenRegional,MtblandRoutenLokal,MtblandMiet,MtblandService");
                put("o_skating",
                        "SkatinglandRoutenNational,SkatinglandRoutenRegional,SkatinglandRoutenLokal");
                put("o_canoeing",
                        "KanulandRoutenNational,KanulandRoutenRegional,KanulandRafting,KanulandClub");
                put(
                        "o_transport",
                        "OffentlicherBahn,OffentlicherBus,OffentlicherTramBus,OffentlicherSchiff,OffentlicherSeilbahn,OffentlicherStandseilbahn");
                put("o_places", "Orte");
                put(
                        "o_accomodation",
                        "PointsHotel,PointsBedBreak,PointsJugen,PointsBackpacker,PointsGruppen,PointsUbernachten,PointsBauernhof,PointsFerien,PointsCamping,PointsBerghuette");
                put("o_shopping", "Migros");
                put("o_poi", "Natur,Kultur,Erlebnisse");
            }
        };
    }

    @Override
    public String getOverlayTileUrl(MapTile tile) {
        SwisstopoMap map = (SwisstopoMap) tile.getMap();
        int tx = tile.getX();
        int ty = tile.getY() + TILESIZE;
        double x1 = map.PIXtoCHx(tx, tile.getZoom());
        double y1 = map.PIXtoCHy(ty, tile.getZoom());
        double x2 = map.PIXtoCHx(tx + TILESIZE, tile.getZoom());
        double y2 = map.PIXtoCHy(ty + TILESIZE, tile.getZoom());
        return String.format(baseUrl, layers_selected, x1, y1, x2, y2);
    }

}
