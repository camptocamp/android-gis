package com.camptocamp.android.gis;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.xmlpull.v1.XmlSerializer;

import android.util.Xml;

import com.nutiteq.components.WgsPoint;

// http://www.topografix.com/gpx.asp

public class ExportGPX extends C2CExportTrace {

    @Override
    public boolean export(List<C2CLine> trace) {
        final File file = new File(PATH + name + ".gpx");
        if (!file.exists()) {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");
            final XmlSerializer xml = Xml.newSerializer();
            try {
                xml.setOutput(new FileOutputStream(file), "utf-8");
                xml.startDocument("utf-8", true);
                xml.text("\r\n");
                xml.startTag(null, "gpx");
                xml.attribute(null, "version", "1.1");
                xml.attribute(null, "xmlns", "http://www.topografix.com/GPX/1/1");
                xml.attribute(null, "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
                xml
                        .attribute(null, "xsi:schemaLocation",
                                "http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd");
                xml.attribute(null, "creator", Map.PKG);

                xml.startTag(null, "metadata");
                xml.startTag(null, "time");
                xml.text(name);
                xml.endTag(null, "time");
                xml.endTag(null, "metadata");

                xml.startTag(null, "trk");
                xml.startTag(null, "name");
                xml.text(name);
                xml.endTag(null, "name");
                xml.startTag(null, "trkseg");
                for (C2CLine line : trace) {
                    WgsPoint pt = line.getPoints()[0];
                    xml.startTag(null, "trkpt");
                    xml.attribute(null, "lat", "" + pt.getLat());
                    xml.attribute(null, "lon", "" + pt.getLon());
                    xml.startTag(null, "ele");
                    xml.text("0");
                    xml.endTag(null, "ele");
                    xml.startTag(null, "time");
                    xml.text(df.format(new Date(line.time)));
                    xml.endTag(null, "time");
                    xml.endTag(null, "trkpt");
                }
                xml.endTag(null, "trkseg");
                xml.endTag(null, "trk");

                xml.endTag(null, "gpx");
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
