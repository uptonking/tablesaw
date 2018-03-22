package com.opencsv;

import java.io.IOException;
import java.io.Reader;

public class CSVReaderHeaderAwareBuilder extends CSVReaderBuilder {
    /**
     * Builder for CSVReaderHeaderAware. No transformations on the original parameters accepted by CSVReaderBuilder.
     *
     * @param reader The reader to an underlying CSV source.
     * @author Andre Rosot
     * @since 4.2
     */
    public CSVReaderHeaderAwareBuilder(Reader reader) {
        super(reader);
    }

    @Override
    public CSVReaderHeaderAware build() throws RuntimeException {
        final ICSVParser parser = getOrCreateCsvParser();
        try {
            return new CSVReaderHeaderAware(getReader(), getSkipLines(), parser, keepCarriageReturn(), isVerifyReader(), getMultilineLimit(), getErrorLocale());
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize CSVReaderHeaderAware", e);
        }
    }
}
