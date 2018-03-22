package com.opencsv;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Andre Rosot
 * @since 4.2
 */
public class CSVReaderHeaderAwareBuilderTest {

    private CSVReaderHeaderAwareBuilder builder;

    @Before
    public void setup() {
        this.builder = new CSVReaderHeaderAwareBuilder(new StringReader("header"));
    }

    @Test
    public void shouldCreateCsvReaderHeaderAwareInstance() {
        assertThat(builder.build(), instanceOf(CSVReaderHeaderAware.class));
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowExceptionWhenCannotReadHeader() throws IOException {
        Reader reader = mock(Reader.class);
        when(reader.read(any((char[].class)), 0, 8192)).thenThrow(new IOException());
        new CSVReaderHeaderAwareBuilder(reader).build();
    }
}