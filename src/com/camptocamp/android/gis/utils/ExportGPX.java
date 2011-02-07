package com.camptocamp.android.gis.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.xmlpull.v1.XmlSerializer;

import android.util.Xml;

import com.camptocamp.android.gis.C2CExportTrace;
import com.camptocamp.android.gis.C2CLine;
import com.camptocamp.android.gis.Map;
import com.nutiteq.components.WgsPoint;

// http://www.topografix.com/gpx.asp

public class ExportGPX extends C2CExportTrace {

    private final static String ISO8601 = "yyyy-MM-dd HH:mm:ss.SSSZ";

    private final static String GPX = "gpx";
    private final static String GPX_VERSION = "version";
    private final static String GPX_XMLNS = "xmlns";
    private final static String GPX_XMLNSXSI = "xmlns:xsi";
    private final static String GPX_XSI = "xsi:schemaLocation";
    private final static String GPX_CREATOR = "creator";
    private final static String GPX_METADATA = "metadata";
    private final static String GPX_TIME = "time";
    private final static String GPX_NAME = "name";
    private final static String GPX_TRK = "trk";
    private final static String GPX_TRKSEG = "trkseg";
    private final static String GPX_TRKPT = "trkpt";
    private final static String GPX_ELE = "ele";
    private final static String GPX_LAT = "lat";
    private final static String GPX_LON = "lon";

    private final static String VAL_ELE = "0";
    private final static String VAL_VERSION = "1.1";
    private final static String VAL_XMLNS = "http://www.topografix.com/GPX/1/1";
    private final static String VAL_XMLNSXSI = "http://www.w3.org/2001/XMLSchema-instance";
    private final static String VAL_XSI = "http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd";

    @Override
    public String export(List<C2CLine> trace) {
        final File file = new File(PATH + name + ".gpx");
        if (!file.exists()) {
            DateFormat df = new SimpleDateFormat(ISO8601);
            final XmlSerializer xml = Xml.newSerializer();
            try {
                xml.setOutput(new FileOutputStream(file), UTF8);
                xml.startDocument(UTF8, true);
                xml.startTag(null, GPX);
                xml.attribute(null, GPX_VERSION, VAL_VERSION);
                xml.attribute(null, GPX_XMLNS, VAL_XMLNS);
                xml.attribute(null, GPX_XMLNSXSI, VAL_XMLNSXSI);
                xml.attribute(null, GPX_XSI, VAL_XSI);
                xml.attribute(null, GPX_CREATOR, Map.PKG);

                xml.startTag(null, GPX_METADATA);
                xml.startTag(null, GPX_TIME);
                xml.text(name);
                xml.endTag(null, GPX_TIME);
                xml.endTag(null, GPX_METADATA);

                xml.startTag(null, GPX_TRK);
                xml.startTag(null, GPX_NAME);
                xml.text(name);
                xml.endTag(null, GPX_NAME);
                xml.startTag(null, GPX_TRKSEG);
                for (C2CLine line : trace) {
                    WgsPoint pt = line.getPoints()[0];
                    xml.startTag(null, GPX_TRKPT);
                    xml.attribute(null, GPX_LAT, "" + pt.getLat());
                    xml.attribute(null, GPX_LON, "" + pt.getLon());
                    xml.startTag(null, GPX_ELE);
                    xml.text(VAL_ELE);
                    xml.endTag(null, GPX_ELE);
                    xml.startTag(null, GPX_TIME);
                    xml.text(df.format(new Date(line.time)));
                    xml.endTag(null, GPX_TIME);
                    xml.endTag(null, GPX_TRKPT);
                }
                xml.endTag(null, GPX_TRKSEG);
                xml.endTag(null, GPX_TRK);

                xml.endTag(null, GPX);
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
