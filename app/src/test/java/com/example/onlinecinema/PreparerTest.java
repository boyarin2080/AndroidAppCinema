package com.example.onlinecinema;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PreparerTest {

    @Test
    public void prepareExtractsIdTitleAndPoster() {
        Map<String, Object> film = new HashMap<>();
        film.put("id", 301.0);
        film.put("title", "Матрица");
        film.put("poster", "https://example.com/poster.jpg");

        List<List<String>> rows = Preparer.prepare(Collections.singletonList(film));

        assertEquals(1, rows.size());
        assertEquals("301.0", rows.get(0).get(0));
        assertEquals("Матрица", rows.get(0).get(1));
        assertEquals("https://example.com/poster.jpg", rows.get(0).get(2));
    }

    @Test
    public void prepareReturnsEmptyListForNullInput() {
        assertTrue(Preparer.prepare(null).isEmpty());
    }
}
