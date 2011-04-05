package com.camptocamp.android.gis.control;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.nutiteq.utils.IOUtils;

public abstract class SearchProvider extends ContentProvider {

    protected byte[] getData(String url) {
        HttpEntity entity = null;
        InputStream is = null;
        HttpGet method = null;
        byte[] data = null;
        try {
            DefaultHttpClient client = new DefaultHttpClient();
            method = new HttpGet(new URI(url));
            HttpResponse response = client.execute(method);
            entity = response.getEntity();
            is = entity.getContent();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (is != null) {
                // TODO: Check this
                data = IOUtils.readFully(is);
            }
            if (method != null) {
                method.abort();
            }
            if (entity != null) {
                try {
                    entity.consumeContent();
                }
                catch (IOException e) {}
            }
            IOUtils.closeStream(is);
        }
        return data;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        return null;
    }

    @Override
    public boolean onCreate() {
        return false;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
