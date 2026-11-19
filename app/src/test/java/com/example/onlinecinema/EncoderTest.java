package com.example.onlinecinema;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class EncoderTest {

    @Test
    public void encodeAddsQueryToSearchUrl() throws Exception {
        assertEquals(
                "https://api4.rhserv.vu/search/%D0%BC%D0%B0%D1%82%D1%80%D0%B8%D1%86%D0%B0",
                Encoder.encode("матрица")
        );
    }

    @Test
    public void encodeEscapesSpaces() throws Exception {
        assertEquals(
                "https://api4.rhserv.vu/search/The+Matrix",
                Encoder.encode("The Matrix")
        );
    }
}
