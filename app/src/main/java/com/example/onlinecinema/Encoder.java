package com.example.onlinecinema;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Builds a search URL and safely encodes the user's query.
 */
public class Encoder {

    public static String encode(String query) throws UnsupportedEncodingException {
        String url = "https://api4.rhserv.vu/search/";
        String encodedQuery = URLEncoder.encode(query, "UTF-8");
        return url + encodedQuery;
    }
}
