package com.example.onlinecinema;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Extracts the fields required by the UI: id, title and poster URL.
 */
public class Preparer {

    public static List<List<String>> prepare(List<Map<String, Object>> data) {
        List<List<String>> megalist = new ArrayList<>();
        if (data == null) {
            return megalist;
        }

        for (Map<String, Object> elem : data) {
            if (elem == null) {
                continue;
            }

            List<String> row = new ArrayList<>();
            row.add(valueOf(elem.get("id")));
            row.add(valueOf(elem.get("title")));
            row.add(valueOf(elem.get("poster")));
            megalist.add(row);
        }

        return megalist;
    }

    private static String valueOf(Object value) {
        return value == null ? "" : value.toString();
    }
}
