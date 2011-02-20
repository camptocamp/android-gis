package com.camptocamp.android.gis.providers;

import java.util.HashMap;

import com.camptocamp.android.gis.layer.Overlay;
import com.nutiteq.components.MapTile;

public class SwisstopoOverlay extends Overlay {
    // private static final String TAG = Map.D + "TestOverlay";
    private static final int TILESIZE = 256;
    private String baseUrl;

    public SwisstopoOverlay(final String baseUrl) {
        this.baseUrl = baseUrl;
        layersAll = new HashMap<String, String>() {
            private static final long serialVersionUID = 1L;
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
        double x1 = map.PIXtoCHx(tx);
        double y1 = map.PIXtoCHy(ty);
        double x2 = map.PIXtoCHx(tx + TILESIZE);
        double y2 = map.PIXtoCHy(ty + TILESIZE);
        return String.format(baseUrl, layersSelected, x1, y1, x2, y2);
    }

}
