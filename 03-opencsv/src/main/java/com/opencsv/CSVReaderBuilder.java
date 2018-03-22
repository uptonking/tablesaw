/*
 Copyright 2005 Bytecode Pty Ltd.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package com.opencsv;


import com.opencsv.enums.CSVReaderNullFieldIndicator;
import org.apache.commons.lang3.ObjectUtils;

import java.io.Reader;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Builder for creating a CSVReader.
 * <p>This should be the preferred method of creating a Reader as there are so many
 * possible values to be set it is impossible to have constructors for all of
 * them and keep backwards compatibility with previous constructors.<br>
 *
 * <code>
 * final CSVParser parser =<br>
 * new CSVParserBuilder()<br>
 * .withSeparator('\t')<br>
 * .withIgnoreQuotations(true)<br>
 * .build();<br>
 * final CSVReader reader =<br>
 * new CSVReaderBuilder(new StringReader(csv))<br>
 * .withSkipLines(1)<br>
 * .withCSVParser(parser)<br>
 * .build();<br>
 * </code></p>
 *
 * @see com.opencsv.CSVReader
 */
public class CSVReaderBuilder {

    private final CSVParserBuilder parserBuilder = new CSVParserBuilder();
    private final Reader reader;
    private int skipLines = CSVReader.DEFAULT_SKIP_LINES;
    private ICSVParser icsvParser = null;
    private boolean keepCR;
    private boolean verifyReader = CSVReader.DEFAULT_VERIFY_READER;
    private CSVReaderNullFieldIndicator nullFieldIndicator = CSVReaderNullFieldIndicator.NEITHER;
    private int multilineLimit = CSVReader.DEFAULT_MULTILINE_LIMIT;
    private Locale errorLocale = Locale.getDefault();

    /**
     * Sets the reader to an underlying CSV source.
     *
     * @param reader The reader to an underlying CSV source.
     */
    public CSVReaderBuilder(
            final Reader reader) {
        if (reader == null) {
            throw new IllegalArgumentException(ResourceBundle.getBundle(ICSVParser.DEFAULT_BUNDLE_NAME).getString("reader.null"));
        }
        this.reader = reader;
    }

    /**
     * Used by unit tests.
     *
     * @return The reader.
     */
    protected Reader getReader() {
        return reader;
    }

    /**
     * Used by unit tests.
     *
     * @return The set number of lines to skip
     */
    protected int getSkipLines() {
        return skipLines;
    }

    /**
     * Used by unit tests.
     *
     * @return The CSVParser used by the builder.
     */
    protected ICSVParser getCsvParser() {
        return icsvParser;
    }

    /**
     * Used by unit tests.
     *
     * @return The upper limit on lines in multiline records.
     */
    protected int getMultilineLimit() {
        return multilineLimit;
    }

    /**
     * Sets the number of lines to skip before reading.
     *
     * @param skipLines The number of lines to skip before reading.
     * @return The CSVReaderBuilder with skipLines set.
     */
    public CSVReaderBuilder withSkipLines(
            final int skipLines) {
        this.skipLines = skipLines <= 0 ? 0 : skipLines;
        return this;
    }


    /**
     * Sets the parser to use to parse the input.
     *
     * @param icsvParser The parser to use to parse the input.
     * @return The CSVReaderBuilder with the CSVParser set.
     */
    public CSVReaderBuilder withCSVParser(
            final ICSVParser icsvParser) {
        this.icsvParser = icsvParser;
        return this;
    }


    /**
     * Creates the CSVReader.
     * @return The CSVReader based on the set criteria.
     */
    public CSVReader build() {
        final ICSVParser parser = getOrCreateCsvParser();
        return new CSVReader(reader, skipLines, parser, keepCR, verifyReader, multilineLimit, errorLocale);
    }

    /**
     * Sets if the reader will keep or discard carriage returns.
     *
     * @param keepCR True to keep carriage returns, false to discard.
     * @return The CSVReaderBuilder based on the set criteria.
     */
    public CSVReaderBuilder withKeepCarriageReturn(boolean keepCR) {
        this.keepCR = keepCR;
        return this;
    }

    /**
     * Returns if the reader built will keep or discard carriage returns.
     *
     * @return True if the reader built will keep carriage returns, false otherwise.
     */
    protected boolean keepCarriageReturn() {
        return this.keepCR;
    }

    /**
     * Creates a new parser if the class does't already hold one.
     * 
     * @return A parser, newly created only if necessary
     */
    protected ICSVParser getOrCreateCsvParser() {
        return ObjectUtils.defaultIfNull(icsvParser,
                parserBuilder
                        .withFieldAsNull(nullFieldIndicator)
                        .withErrorLocale(errorLocale)
                        .build());
    }

    /**
     * Checks to see if the CSVReader should verify the reader state before
     * reads or not.
     *
     * <p>This should be set to false if you are using some form of asynchronous
     * reader (like readers created by the java.nio.* classes).</p>
     *
     * <p>The default value is true.</p>
     *
     * @param verifyReader True if CSVReader should verify reader before each read, false otherwise.
     * @return The CSVReaderBuilder based on this criteria.
     */
    public CSVReaderBuilder withVerifyReader(boolean verifyReader) {
        this.verifyReader = verifyReader;
        return this;
    }

    /**
     * @return The flag indicating whtether the reader should be verified before each read.
     */
    public boolean isVerifyReader() {
        return verifyReader;
    }

    /**
     * Checks to see if it should treat a field with two separators, two quotes, or both as a null field.
     *
     * @param indicator CSVReaderNullFieldIndicator set to what should be considered a null field.
     * @return The CSVReaderBuilder based on this criteria.
     */
    public CSVReaderBuilder withFieldAsNull(CSVReaderNullFieldIndicator indicator) {
        this.nullFieldIndicator = indicator;
        return this;
    }

    /**
     * Sets the maximum number of lines allowed in a multiline record.
     * More than this number in one record results in an IOException.
     *
     * @param multilineLimit No more than this number of lines is allowed in a
     *                       single input record. The default is {@link CSVReader#DEFAULT_MULTILINE_LIMIT}.
     * @return The CSVReaderBuilder based on this criteria.
     * @see CSVReader#setMultilineLimit(int)
     */
    public CSVReaderBuilder withMultilineLimit(int multilineLimit) {
        this.multilineLimit = multilineLimit;
        return this;
    }

    /**
     * Sets the locale for all error messages.
     *
     * @param errorLocale Locale for error messages
     * @return this
     * @since 4.0
     */
    public CSVReaderBuilder withErrorLocale(Locale errorLocale) {
        this.errorLocale = ObjectUtils.defaultIfNull(errorLocale, Locale.getDefault());
        return this;
    }

    /**
     * @return The locale for error messages
     */
    public Locale getErrorLocale() {
        return errorLocale;
    }
}
