package com.opencsv;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Handy reader when there's insufficient motivation to use the bean binding but
 * the header mapping is still desired.
 *
 * @author Andre Rosot
 * @since 4.2
 */
public class CSVReaderHeaderAware extends CSVReader {

    private final Map<String, Integer> headerIndex = new HashMap<>();

    /**
     * Constructor with supplied reader.
     *
     * @param reader The reader to an underlying CSV source
     * @throws IOException If there is an error when reading the header
     */
    public CSVReaderHeaderAware(Reader reader) throws IOException {
        super(reader);
        initializeHeader();
    }

    /**
     * Supports non-deprecated constructor from the parent class.
     * 
     * @param reader         The reader to an underlying CSV source
     * @param skipLines      The number of lines to skip before reading
     * @param parser         The parser to use to parse input
     * @param keepCR         True to keep carriage returns in data read, false otherwise
     * @param verifyReader   True to verify reader before each read, false otherwise
     * @param multilineLimit Allow the user to define the limit to the number of lines in a multiline record. Less than one means no limit.
     * @param errorLocale    Set the locale for error messages. If null, the default locale is used.
     * @throws IOException   If bad things happen while initializing the header
     */
    public CSVReaderHeaderAware(Reader reader, int skipLines, ICSVParser parser, boolean keepCR, boolean verifyReader, int multilineLimit, Locale errorLocale) throws IOException {
        super(reader, skipLines, parser, keepCR, verifyReader, multilineLimit, errorLocale);
        initializeHeader();
    }

    /**
     * Retrieves a specific data element from a line based on the value of the header.
     *
     * @param headerNames Name of the header element whose data we are trying to find
     * @return The data element whose position matches that of the header whose value is passed in. Will return null when there are no more data elements.
     * @throws IOException              An error occured during the read or there is a mismatch in the number of data items in a row
     *                                  and the number of header items
     * @throws IllegalArgumentException If headerName does not exist
     */
    public String[] readNext(String... headerNames) throws IOException {
        if (headerNames == null) {
            return super.readNext();
        }

        String[] strings = readNext();
        if (strings == null) {
            return null;
        }

        String[] response = new String[headerNames.length];

        for (int i = 0; i < headerNames.length; i++) {
            String headerName = headerNames[i];

            Integer index = headerIndex.get(headerName);
            if (index == null) {
                throw new IllegalArgumentException(String.format(
                        ResourceBundle.getBundle(ICSVParser.DEFAULT_BUNDLE_NAME, errorLocale)
                                .getString("header.nonexistant"),
                        headerName));
            }

            if (strings.length != headerIndex.size()) {
                throw new IOException(String.format(
                        ResourceBundle.getBundle(ICSVParser.DEFAULT_BUNDLE_NAME, errorLocale)
                                .getString("header.data.mismatch.with.line.number"),
                        getRecordsRead()));
            }

            response[i] = strings[index];
        }
        return response;
    }

    /**
     * Reads the next line and returns a map of header values and data values.
     *
     * @return A map whose key is the header row of the data file and the values is the data values. Or null if the line is blank.
     * @throws IOException An error occured during the read or there is a mismatch in the number of data items in a row
     *                     and the number of header items.
     */
    public Map<String, String> readMap() throws IOException {
        String[] strings = readNext();
        if (strings == null) {
            return null;
        }
        if (strings.length != headerIndex.size()) {
            throw new IOException(String.format(
                    ResourceBundle.getBundle(ICSVParser.DEFAULT_BUNDLE_NAME, errorLocale)
                            .getString("header.data.mismatch.with.line.number"),
                    getRecordsRead()));
        }
        Map<String, String> mappedLine = new HashMap<>();
        for (Map.Entry<String, Integer> entry : headerIndex.entrySet()) {
            if (entry.getValue() < strings.length) {
                mappedLine.put(entry.getKey(), strings[entry.getValue()]);
            }
        }
        return mappedLine;
    }

    private void initializeHeader() throws IOException {
        String[] headers = super.readNext();
        for (int i = 0; i < headers.length; i++) {
            headerIndex.put(headers[i], i);
        }
    }

}
