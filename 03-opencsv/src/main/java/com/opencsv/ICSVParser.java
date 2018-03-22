package com.opencsv;

import com.opencsv.enums.CSVReaderNullFieldIndicator;

import java.io.IOException;

import static com.opencsv.enums.CSVReaderNullFieldIndicator.NEITHER;
import java.util.Locale;

/**
 * This interface defines all of the behavior {@link com.opencsv.CSVReader}
 * needs from a parser to tokenize an input line for further processing.
 *
 * @since 3.9
 */
public interface ICSVParser {

    /**
     * The default separator to use if none is supplied to the constructor.
     */
    char DEFAULT_SEPARATOR = ',';

    /**
     * The average size of a line read by opencsv (used for setting the size of StringBuilders).
     */
    int INITIAL_READ_SIZE = 1024;

    /**
     * In most cases we know the size of the line we want to read.  In that case we will set the initial read
     * to that plus an buffer size.
     */
    int READ_BUFFER_SIZE = 128;

    /**
     * The default quote character to use if none is supplied to the
     * constructor.
     */
    char DEFAULT_QUOTE_CHARACTER = '"';

    /**
     * The default escape character to use if none is supplied to the
     * constructor.
     */
    char DEFAULT_ESCAPE_CHARACTER = '\\';

    /**
     * The default strict quote behavior to use if none is supplied to the
     * constructor.
     */
    boolean DEFAULT_STRICT_QUOTES = false;

    /**
     * The default leading whitespace behavior to use if none is supplied to the
     * constructor.
     */
    boolean DEFAULT_IGNORE_LEADING_WHITESPACE = true;

    /**
     * If the quote character is set to null then there is no quote character.
     */
    boolean DEFAULT_IGNORE_QUOTATIONS = false;

    /**
     * This is the "null" character - if a value is set to this then it is ignored.
     */
    char NULL_CHARACTER = '\0';

    /**
     * Denotes what field contents will cause the parser to return null:  EMPTY_SEPARATORS, EMPTY_QUOTES, BOTH, NEITHER (default).
     */
    CSVReaderNullFieldIndicator DEFAULT_NULL_FIELD_INDICATOR = NEITHER;
    
    /**
     * The name of the resource bundle for translations of error messages in opencsv.
     */
    String DEFAULT_BUNDLE_NAME = "opencsv";

    /**
     * When creating builders this should be the smallest size to account for quotes and any possible escape characters.
     */
    int MAX_SIZE_FOR_EMPTY_FIELD = 16;

    /**
     * Default newline character for the parser.
     */
    String NEWLINE = "\n";

    /**
     * @return The default separator for this parser.
     */
    char getSeparator();

    /**
     * @return The default quotation character for this parser.
     */
    char getQuotechar();

    /**
     * @return True if something was left over from last call(s)
     */
    boolean isPending();

    /**
     * Parses an incoming String and returns an array of elements.
     * This method is used when the data spans multiple lines.
     *
     * @param nextLine Current line to be processed
     * @return The comma-tokenized list of elements, or null if nextLine is null
     * @throws IOException If bad things happen during the read
     */
    String[] parseLineMulti(String nextLine) throws IOException;

    /**
     * Parses an incoming String and returns an array of elements.
     * This method is used when all data is contained in a single line.
     *
     * @param nextLine Line to be parsed.
     * @return The list of elements, or null if nextLine is null
     * @throws IOException If bad things happen during the read
     */
    String[] parseLine(String nextLine) throws IOException;

    /**
     * Essentially a "Reverse parse" where an array of values are concatenating to a
     * csv delimited string.
     *
     * NOTE: as of the 4.1 release this functionality is not considered production ready and
     * has not been fully tested (and the ability to add a parser to the CSVWriter has not
     * been implemented yet.   I am adding this now because because I need to do the 4.1
     * release because of the number of defect fixes and I do not want to strip this out.
     *
     * @param values List of elements to parse
     * @return CSV formatted string representing the values in the array.
     * @since 4.1
     */
    String parseToLine(String[] values);

    /**
     * @return The null field indicator.
     */
    CSVReaderNullFieldIndicator nullFieldIndicator();
    
    /**
     * If a parser is in the middle of parsing a multiline field, this will
     * return the text collected so far.
     * 
     * @return The incomplete text for a multiline field. If there is no
     *   pending text, this returns an empty string.
     * @since 4.1
     */
    String getPendingText();
    
    /**
     * Sets the locale for all error messages.
     * @param errorLocale Locale for error messages. If null, the default locale
     *   is used.
     * @since 4.2
     */
    void setErrorLocale(Locale errorLocale);
}
