package com.camptocamp.android.gis;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.xmlpull.v1.XmlSerializer;

import android.util.Xml;

import com.nutiteq.components.WgsPoint;

// https://code.google.com/intl/en/apis/kml/documentation/

public class ExportKML extends C2CExportTrace {

    @Override
    public boolean export(List<C2CLine> trace) {
        final File file = new File(PATH + name + ".kml");
        if (!file.exists()) {
            final XmlSerializer xml = Xml.newSerializer();
            try {
                xml.setOutput(new FileOutputStream(file), "utf-8");
                xml.startDocument("utf-8", true);
                xml.text("\r\n");
                xml.startTag(null, "kml");
                xml.attribute(null, "xmlns", "http://www.opengis.net/kml/2.2");
                xml.startTag(null, "Document");

                xml.startTag(null, "Name");
                xml.text(name);
                xml.endTag(null, "Name");

                xml.startTag(null, "Placemark");
                xml.startTag(null, "LineString");
                xml.startTag(null, "extrude");
                xml.text("0");
                xml.endTag(null, "extrude");
                xml.startTag(null, "tessellate");
                xml.text("1");
                xml.endTag(null, "tessellate");
                xml.startTag(null, "altitudeMode");
                xml.text("clampToGround");
                xml.endTag(null, "altitudeMode");
                xml.startTag(null, "coordinates");
                for (C2CLine line : trace) {
                    WgsPoint pt = line.getPoints()[0];
                    xml.text(pt.getLon() + "," + pt.getLat() + ",0 ");
                }
                xml.endTag(null, "coordinates");
                xml.endTag(null, "LineString");
                xml.endTag(null, "Placemark");

                xml.endTag(null, "Document");
                xml.endTag(null, "kml");
                xml.flush();
                xml.endDocument();
                return true;

            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
