package com.opencsv;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Locale;
import java.util.NoSuchElementException;
import org.junit.After;

import static org.junit.Assert.*;
import org.junit.BeforeClass;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CSVIteratorTest {
    private static final String[] STRINGS = {"test1", "test2"};
    private CSVIterator iterator;
    private CSVReader mockReader;

    private static Locale systemLocale;

    @BeforeClass
    public static void storeSystemLocale() {
        systemLocale = Locale.getDefault();
    }

    @After
    public void setSystemLocaleBackToDefault() {
        Locale.setDefault(systemLocale);
    }

    @Before
    public void setUp() throws IOException {
        Locale.setDefault(Locale.US);
        mockReader = mock(CSVReader.class);
        when(mockReader.readNext()).thenReturn(STRINGS);
        iterator = new CSVIterator(mockReader);
    }

    @Test(expected = NoSuchElementException.class)
    public void readerExceptionCausesRunTimeException() throws IOException {
        when(mockReader.readNext()).thenThrow(new IOException("reader threw test exception"));
        iterator.next();
    }

    @Test
    public void removethrowsUnsupportedOperationException() {
        String englishErrorMessage = null;
        try {
            iterator.remove();
            fail("UnsupportedOperationException should have been thrown by read-only iterator.");
        }
        catch(UnsupportedOperationException e) {
            englishErrorMessage = e.getLocalizedMessage();
        }
        
        // Now with a different locale
        iterator.setErrorLocale(Locale.GERMAN);
        try {
            iterator.remove();
            fail("UnsupportedOperationException should have been thrown by read-only iterator.");
        }
        catch(UnsupportedOperationException e) {
            assertNotEquals(englishErrorMessage, e.getLocalizedMessage());
        }
    }

    @Test
    public void initialReadReturnsStrings() {
        assertArrayEquals(STRINGS, iterator.next());
    }

    @Test
    public void hasNextWorks() throws IOException {
        when(mockReader.readNext()).thenReturn(null);
        assertTrue(iterator.hasNext()); // initial read from constructor
        iterator.next();
        assertFalse(iterator.hasNext());
    }
}
