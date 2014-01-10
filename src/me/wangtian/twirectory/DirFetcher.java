package me.wangtian.twirectory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.xml.sax.SAXException;

import android.util.Log;

/**
 * Utilities to download a Twitter directory page
 */
public class DirFetcher {

  public static List<DirEntry> fetchFrom(String urlStr) throws SAXException, IOException {
    URL url = new URL(urlStr);
    Log.w("", "---------Fetching url: " + url);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setReadTimeout(10 * 1000);
    conn.setConnectTimeout(15 * 1000);
    conn.setRequestMethod("GET");
    conn.setDoInput(true);
    conn.connect();
    DirParser parser = new DirParser();
    Log.w("", "---------Response code: " + conn.getResponseCode());
    return parser.parse(conn.getInputStream());
  }
}
