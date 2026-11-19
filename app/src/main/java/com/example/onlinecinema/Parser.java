package com.example.onlinecinema;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Downloads JSON from the search endpoint and converts it into a list of maps.
 *
 * <p>The endpoint from the manual (api4.rhserv.vu) is no longer available.
 * If it cannot be reached, the parser transparently uses the public Kinopoisk
 * API and converts its response to the same shape: id / title / poster.</p>
 */
public class Parser {

    private static final String MANUAL_SEARCH_URL = "https://api4.rhserv.vu/search/";
    private static final String KINOPOISK_SEARCH_URL =
            "https://kinopoiskapiunofficial.tech/api/v2.1/films/search-by-keyword?keyword=";

    // Demo key published by the Kinopoisk API documentation.
    // Replace it with your own key if the daily limit is reached.
    private static final String KINOPOISK_API_KEY = "8c8e1a50-6322-4135-8875-5d40a5420d86";

    public static List<Map<String, Object>> parse(String urlString) throws IOException {
        try {
            return parseJson(fetch(urlString, null));
        } catch (Exception primaryError) {
            String query = extractQuery(urlString);
            if (query.isEmpty()) {
                throw asIOException(primaryError);
            }

            try {
                return searchKinopoisk(query);
            } catch (Exception fallbackError) {
                fallbackError.addSuppressed(primaryError);
                throw asIOException(fallbackError);
            }
        }
    }

    private static List<Map<String, Object>> searchKinopoisk(String query) throws IOException {
        String url = KINOPOISK_SEARCH_URL
                + URLEncoder.encode(query, StandardCharsets.UTF_8.name())
                + "&page=1";
        String content = fetch(url, KINOPOISK_API_KEY);

        Type mapType = new TypeToken<Map<String, Object>>() {
        }.getType();
        Map<String, Object> root = new Gson().fromJson(content, mapType);
        if (root == null || !(root.get("films") instanceof List<?>)) {
            return new ArrayList<>();
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Object value : (List<?>) root.get("films")) {
            if (!(value instanceof Map<?, ?>)) {
                continue;
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> raw = (Map<String, Object>) value;

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", firstNonEmpty(raw.get("filmId"), raw.get("kinopoiskId")));
            item.put("title", firstNonEmpty(raw.get("nameRu"), raw.get("nameEn"), raw.get("nameOriginal")));
            item.put("poster", firstNonEmpty(raw.get("posterUrl"), raw.get("posterUrlPreview")));
            item.put("year", raw.get("year"));
            item.put("raw_data", raw);
            item.put("average_rating", raw.get("rating"));
            result.add(item);
        }
        return result;
    }

    private static String fetch(String urlString, String apiKey) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(20000);
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("User-Agent", "OnlineCinema/1.0 (Android)");
        if (apiKey != null) {
            connection.setRequestProperty("X-API-KEY", apiKey);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode < 200 || responseCode >= 300) {
            InputStream errorStream = connection.getErrorStream();
            String details = errorStream == null ? "" : readAll(errorStream);
            connection.disconnect();
            throw new IOException("HTTP " + responseCode + (details.isEmpty() ? "" : ": " + details));
        }

        String content = readAll(connection.getInputStream());
        connection.disconnect();
        return content;
    }

    private static String readAll(InputStream stream) throws IOException {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8)
        );

        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }

        in.close();
        return content.toString();
    }

    private static List<Map<String, Object>> parseJson(String content) {
        Gson gson = new Gson();
        Type listType = new TypeToken<List<Map<String, Object>>>() {
        }.getType();
        List<Map<String, Object>> list = gson.fromJson(content, listType);
        return list == null ? new ArrayList<>() : list;
    }

    private static String extractQuery(String urlString) {
        if (urlString == null || !urlString.startsWith(MANUAL_SEARCH_URL)) {
            return "";
        }
        try {
            return URLDecoder.decode(urlString.substring(MANUAL_SEARCH_URL.length()),
                    StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            return "";
        }
    }

    private static Object firstNonEmpty(Object... values) {
        for (Object value : values) {
            if (value != null && !value.toString().trim().isEmpty()
                    && !"null".equalsIgnoreCase(value.toString())) {
                return value;
            }
        }
        return "";
    }

    private static IOException asIOException(Exception error) {
        if (error instanceof IOException) {
            return (IOException) error;
        }
        return new IOException(error.getMessage(), error);
    }
}
