package com.camptocamp.android.gis.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.xmlpull.v1.XmlSerializer;

import android.util.Xml;

import com.camptocamp.android.gis.C2CExportTrace;
import com.camptocamp.android.gis.C2CLine;
import com.nutiteq.components.WgsPoint;

// https://code.google.com/intl/en/apis/kml/documentation/

public class ExportKML extends C2CExportTrace {

    private final static String KML = "kml";
    private final static String KML_XMLNS = "xmlns";
    private final static String KML_DOCUMENT = "Document";
    private final static String KML_NAME = "Name";
    private final static String KML_PLACEMARK = "Placemark";
    private final static String KML_LINESTRING = "LineString";
    private final static String KML_EXTRUDE = "extrude";
    private final static String KML_TESSELLATE = "tessellate";
    private final static String KML_ALTITUDEMODE = "altitudeMode";
    private final static String KML_COORDINATES = "coordinates";

    private final static String VAL_EXTRUDE = "0";
    private final static String VAL_TESSELLATE = "1";
    private final static String VAL_ALTITUDEMODE = "clampToGround";
    private final static String VAL_XMLNS = "http://www.opengis.net/kml/2.2";

    @Override
    public String export(List<C2CLine> trace) {
        final File file = new File(PATH + name + ".kml");
        if (!file.exists()) {
            final XmlSerializer xml = Xml.newSerializer();
            try {
                xml.setOutput(new FileOutputStream(file), UTF8);
                xml.startDocument(UTF8, true);
                xml.startTag(null, KML);
                xml.attribute(null, KML_XMLNS, VAL_XMLNS);
                xml.startTag(null, KML_DOCUMENT);

                xml.startTag(null, KML_NAME);
                xml.text(name);
                xml.endTag(null, KML_NAME);

                xml.startTag(null, KML_PLACEMARK);
                xml.startTag(null, KML_LINESTRING);
                xml.startTag(null, KML_EXTRUDE);
                xml.text(VAL_EXTRUDE);
                xml.endTag(null, KML_EXTRUDE);
                xml.startTag(null, KML_TESSELLATE);
                xml.text(VAL_TESSELLATE);
                xml.endTag(null, KML_TESSELLATE);
                xml.startTag(null, KML_ALTITUDEMODE);
                xml.text(VAL_ALTITUDEMODE);
                xml.endTag(null, KML_ALTITUDEMODE);
                xml.startTag(null, KML_COORDINATES);
                for (C2CLine line : trace) {
                    WgsPoint pt = line.getPoints()[0];
                    xml.text(pt.getLon() + "," + pt.getLat() + ",0 ");
                }
                xml.endTag(null, KML_COORDINATES);
                xml.endTag(null, KML_LINESTRING);
                xml.endTag(null, KML_PLACEMARK);

                xml.endTag(null, KML_DOCUMENT);
                xml.endTag(null, KML);
                xml.flush();
                xml.endDocument();
                return file.getAbsolutePath();

            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "";
    }
}
