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
import java.util.Locale;
import org.apache.commons.lang3.ObjectUtils;

/**
 * Builder for creating a CSVParser.
 * <p>Example code for using this class:<br><br>
 * <code>
 * final CSVParser parser =<br>
 * new CSVParserBuilder()<br>
 * .withSeparator('\t')<br>
 * .withIgnoreQuotations(true)<br>
 * .build();<br>
 * </code></p>
 *
 * @see CSVParser
 */
public class CSVParserBuilder {

    private char separator = ICSVParser.DEFAULT_SEPARATOR;
    private char quoteChar = ICSVParser.DEFAULT_QUOTE_CHARACTER;
    private char escapeChar = ICSVParser.DEFAULT_ESCAPE_CHARACTER;
    private boolean strictQuotes = ICSVParser.DEFAULT_STRICT_QUOTES;
    private boolean ignoreLeadingWhiteSpace = ICSVParser.DEFAULT_IGNORE_LEADING_WHITESPACE;
    private boolean ignoreQuotations = ICSVParser.DEFAULT_IGNORE_QUOTATIONS;
    private CSVReaderNullFieldIndicator nullFieldIndicator = CSVReaderNullFieldIndicator.NEITHER;
    private Locale errorLocale = Locale.getDefault();


    /**
     * Default constructor.
     */
    public CSVParserBuilder() {
    }

    /**
     * Sets the delimiter to use for separating entries.
     *
     * @param separator The delimiter to use for separating entries
     * @return The CSVParserBuilder
     */
    public CSVParserBuilder withSeparator(
            final char separator) {
        this.separator = separator;
        return this;
    }


    /**
     * Sets the character to use for quoted elements.
     *
     * @param quoteChar The character to use for quoted element.
     * @return The CSVParserBuilder
     */
    public CSVParserBuilder withQuoteChar(
            final char quoteChar) {
        this.quoteChar = quoteChar;
        return this;
    }


    /**
     * Sets the character to use for escaping a separator or quote.
     *
     * @param escapeChar The character to use for escaping a separator or quote.
     * @return The CSVParserBuilder
     */
    public CSVParserBuilder withEscapeChar(
            final char escapeChar) {
        this.escapeChar = escapeChar;
        return this;
    }


    /**
     * Sets the strict quotes setting - if true, characters
     * outside the quotes are ignored.
     *
     * @param strictQuotes If true, characters outside the quotes are ignored
     * @return The CSVParserBuilder
     */
    public CSVParserBuilder withStrictQuotes(
            final boolean strictQuotes) {
        this.strictQuotes = strictQuotes;
        return this;
    }

    /**
     * Sets the ignore leading whitespace setting - if true, white space
     * in front of a quote in a field is ignored.
     *
     * @param ignoreLeadingWhiteSpace If true, white space in front of a quote in a field is ignored
     * @return The CSVParserBuilder
     */
    public CSVParserBuilder withIgnoreLeadingWhiteSpace(
            final boolean ignoreLeadingWhiteSpace) {
        this.ignoreLeadingWhiteSpace = ignoreLeadingWhiteSpace;
        return this;
    }

    /**
     * Sets the ignore quotations mode - if true, quotations are ignored.
     *
     * @param ignoreQuotations If true, quotations are ignored
     * @return The CSVParserBuilder
     */
    public CSVParserBuilder withIgnoreQuotations(
            final boolean ignoreQuotations) {
        this.ignoreQuotations = ignoreQuotations;
        return this;
    }

    /**
     * Constructs CSVParser.
     *
     * @return A new CSVParser with defined settings.
     */
    public CSVParser build() {

        return new CSVParser(
                separator,
                quoteChar,
                escapeChar,
                strictQuotes,
                ignoreLeadingWhiteSpace,
                ignoreQuotations,
                nullFieldIndicator,
                errorLocale);
    }

    /**
     * @return The defined separator.
     */
    public char getSeparator() {
        return separator;
    }

    /**
     * @return The defined quotation character.
     */
    public char getQuoteChar() {
        return quoteChar;
    }

    /**
     * @return The defined escape character.
     */
    public char getEscapeChar() {
        return escapeChar;
    }

    /**
     * @return The defined strict quotation setting.
     */
    public boolean isStrictQuotes() {
        return strictQuotes;
    }

    /**
     * @return The defined ignoreLeadingWhiteSpace setting.
     */
    public boolean isIgnoreLeadingWhiteSpace() {
        return ignoreLeadingWhiteSpace;
    }

    /**
     * @return The defined ignoreQuotation setting.
     */
    public boolean isIgnoreQuotations() {
        return ignoreQuotations;
    }

    /**
     * Sets the NullFieldIndicator.
     *
     * @param fieldIndicator CSVReaderNullFieldIndicator set to what should be considered a null field.
     * @return The CSVParserBuilder
     */
    public CSVParserBuilder withFieldAsNull(final CSVReaderNullFieldIndicator fieldIndicator) {
        this.nullFieldIndicator = fieldIndicator;
        return this;
    }
    
    /**
     * Sets the locale for all error messages.
     * 
     * @param errorLocale Locale for error messages
     * @return this
     * @since 4.0
     */
    public CSVParserBuilder withErrorLocale(Locale errorLocale) {
        this.errorLocale = ObjectUtils.defaultIfNull(errorLocale, Locale.getDefault());
        return this;
    }
    
    /**
     * @return The null field indicator.
     */
    public CSVReaderNullFieldIndicator nullFieldIndicator() {
        return nullFieldIndicator;
    }
}
