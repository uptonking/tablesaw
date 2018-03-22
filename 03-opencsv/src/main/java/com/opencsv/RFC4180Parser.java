package com.opencsv;

import com.opencsv.enums.CSVReaderNullFieldIndicator;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * This Parser is meant to parse according to the RFC4180 specification.
 * <p>Since it shares the same interface with the CSVParser there are methods here that will do nothing.
 * For example the RFC4180 specification does not have an concept of an escape character so the getEscape method
 * will return char 0.  The methods that are not supported are noted in the Javadocs.</p>
 * <p>Another departure from the CSVParser is that there is only two constructors and only one is available publicly.
 * The intent is that if you want to create anything other than a default RFC4180Parser you should use the
 * CSVParserBuilder.  This way the code will not become cluttered with constructors as the CSVParser did.</p>
 * <p>Examples:</p>
 * {@code
 * ICSVParser parser = new RFC4180Parser();
 * }
 * <p>or</p>
 * {@code
 * CSVParserBuilder builder = new CSVParserBuilder()
 * ICSVParser parser = builder.withParserType(ParserType.RFC4180Parser).build()
 * }
 *
 * @author Scott Conway
 * @since 3.9
 */

public class RFC4180Parser implements ICSVParser {

    /**
     * This is needed by the split command in case the separator character is a regex special character.
     */
    private static final Pattern SPECIAL_REGEX_CHARS = Pattern.compile("[{}()\\[\\].+*?^$\\\\|]");

    /**
     * This is the character that the RFC4180Parser will treat as the separator.
     */
    private final char separator;

    /**
     * Separator character as String (used for split command).
     */
    private final String separatorAsString;

    /**
     * This is the character that the RFC4180Parser will treat as the quotation character.
     */
    private final char quotechar;
    private final String quoteCharString;

    /**
     * This is the fields that the parser will automatically return null.
     */
    private final CSVReaderNullFieldIndicator nullFieldIndicator;

    /**
     * This is what was from a previous read of a multi-lined csv record.
     */
    private String pending;

    /**
     * Default constructor for the RFC4180Parser.  Uses values from the ICSVParser.
     */
    public RFC4180Parser() {
        this(ICSVParser.DEFAULT_QUOTE_CHARACTER, ICSVParser.DEFAULT_SEPARATOR, CSVReaderNullFieldIndicator.NEITHER);
    }

    /**
     * Constructor used by the CSVParserBuilder.
     *
     * @param separator The delimiter to use for separating entries
     * @param quoteChar The character to use for quoted elements
     * @param nullFieldIndicator Indicate what should be considered null
     */
    RFC4180Parser(char quoteChar, char separator, CSVReaderNullFieldIndicator nullFieldIndicator) {
        this.quotechar = quoteChar;
        this.quoteCharString = Character.toString(quoteChar);
        this.separator = separator;
        this.separatorAsString = SPECIAL_REGEX_CHARS.matcher(Character.toString(separator)).replaceAll("\\\\$0");
        this.nullFieldIndicator = nullFieldIndicator;
    }

    @Override
    public char getSeparator() {
        return separator;
    }

    @Override
    public char getQuotechar() {
        return quotechar;
    }

    @Override
    public boolean isPending() {
        return pending != null;
    }

    @Override
    public String[] parseLineMulti(String nextLine) throws IOException {
        return parseLine(nextLine, true);
    }

    @Override
    public String[] parseLine(String nextLine) throws IOException {
        return parseLine(nextLine, false);
    }

    @Override
    public String parseToLine(String[] values) {
        StringBuilder builder = new StringBuilder(ICSVParser.INITIAL_READ_SIZE);

        for (int i = 0; i < values.length; i++) {
            builder.append(convertToCsvValue(values[i]));
            if (i < values.length - 1) {
                builder.append(getSeparator());
            }
        }
        return builder.toString();
    }

    private String convertToCsvValue(String value) {
        String testValue = (value == null && !nullFieldIndicator.equals(CSVReaderNullFieldIndicator.NEITHER)) ? "" : value;
        StringBuilder builder = new StringBuilder(testValue == null ? MAX_SIZE_FOR_EMPTY_FIELD : (testValue.length() * 2));
        boolean containsQuoteChar = testValue != null && testValue.contains(Character.toString(getQuotechar()));
        boolean surroundWithQuotes = isSurroundWithQuotes(value, containsQuoteChar);

        String convertedString = !containsQuoteChar ? testValue : testValue.replaceAll(Character.toString(getQuotechar()), Character.toString(getQuotechar()) + Character.toString(getQuotechar()));

        if (surroundWithQuotes) {
            builder.append(getQuotechar());
        }

        builder.append(convertedString);

        if (surroundWithQuotes) {
            builder.append(getQuotechar());
        }

        return builder.toString();
    }

    private boolean isSurroundWithQuotes(String value, boolean containsQuoteChar) {
        if (value == null) {
            return nullFieldIndicator.equals(CSVReaderNullFieldIndicator.EMPTY_QUOTES);
        }

        return containsQuoteChar || value.contains(Character.toString(getSeparator())) || value.contains(NEWLINE);
    }

    /**
     * Parses an incoming String and returns an array of elements.
     *
     * @param nextLine The string to parse
     * @param multi    Does it take multiple lines to form a single record?
     * @return The list of elements, or null if nextLine is null
     */
    protected String[] parseLine(String nextLine, boolean multi) {
        String[] elements;

        if (!multi && pending != null) {
            pending = null;
        }

        if (nextLine == null) {
            if (pending != null) {
                String s = pending;
                pending = null;
                return new String[]{s};
            }
            return null;
        }

        String lineToProcess = multi && pending != null ? pending + nextLine : nextLine;
        pending = null;

        if (!StringUtils.contains(lineToProcess, quotechar)) {
            elements = handleEmptySeparators(tokenizeStringIntoArray(lineToProcess));
        } else {
            elements = handleEmptySeparators(splitWhileNotInQuotes(lineToProcess, multi));
            for (int i = 0; i < elements.length; i++) {
                if (StringUtils.contains(elements[i], quotechar)) {
                    elements[i] = handleQuotes(elements[i]);
                }
            }
        }
        return elements;
    }

    private String[] tokenizeStringIntoArray(String nextLine) {
        return nextLine.split(separatorAsString, -1);
    }

    private String[] handleEmptySeparators(String[] strings) {
        if (nullFieldIndicator == CSVReaderNullFieldIndicator.EMPTY_SEPARATORS || nullFieldIndicator == CSVReaderNullFieldIndicator.BOTH) {
            for (int i = 0; i < strings.length; i++) {
                if (strings[i].isEmpty()) {
                    strings[i] = null;
                }
            }
        }
        return strings;
    }

    private String[] splitWhileNotInQuotes(String nextLine, boolean multi) {
        int currentPosition = 0;
        List<String> elements = new ArrayList<>();
        int nextSeparator;
        int nextQuote;


        while (currentPosition < nextLine.length()) {
            nextSeparator = nextLine.indexOf(separator, currentPosition);
            nextQuote = nextLine.indexOf(quotechar, currentPosition);

            if (nextSeparator == -1) {
                elements.add(nextLine.substring(currentPosition));
                currentPosition = nextLine.length();
            } else if (nextQuote == -1 || nextQuote > nextSeparator || nextQuote != currentPosition) {
                elements.add(nextLine.substring(currentPosition, nextSeparator));
                currentPosition = nextSeparator + 1;
            } else {
                int fieldEnd = findEndOfFieldFromPosition(nextLine, currentPosition);

                elements.add(fieldEnd >= nextLine.length() ? nextLine.substring(currentPosition) : nextLine.substring(currentPosition, fieldEnd));

                currentPosition = fieldEnd + 1;
            }

        }

        if (multi && lastElementStartedWithQuoteButDidNotEndInOne(elements)) {
            pending = elements.get(elements.size() - 1) + NEWLINE;
            elements.remove(elements.size() - 1);
        } else if (nextLine.lastIndexOf(separator) == nextLine.length() - 1) {
            elements.add("");
        }
        return elements.toArray(new String[elements.size()]);
    }

    private boolean lastElementStartedWithQuoteButDidNotEndInOne(List<String> elements) {
        String lastElement = elements.get(elements.size() - 1);
        return startsButDoesNotEndWithQuote(lastElement) || hasOnlyOneQuote(lastElement);
    }

    private boolean hasOnlyOneQuote(String lastElement) {
        return StringUtils.countMatches(lastElement, quotechar) == 1;
    }

    private boolean startsButDoesNotEndWithQuote(String lastElement) {
        return lastElement.startsWith(Character.toString(quotechar)) && !lastElement.endsWith(Character.toString(quotechar));
    }

    private int findEndOfFieldFromPosition(String nextLine, int currentPosition) {
        int nextQuote = nextLine.indexOf(quotechar, currentPosition + 1);

        boolean inQuote = false;
        while (haveNotFoundLastQuote(nextLine, nextQuote)) {
            if (!inQuote && nextLine.charAt(nextQuote + 1) == separator) {
                return nextQuote + 1;
            }

            do {
                nextQuote = nextLine.indexOf(quotechar, nextQuote + 1);
                inQuote = !inQuote;
            } while (haveNotFoundLastQuote(nextLine, nextQuote) && nextLine.charAt(nextQuote + 1) == quotechar);
        }

        return nextLine.length();
    }

    private boolean haveNotFoundLastQuote(String nextLine, int nextQuote) {
        return nextQuote != -1 && nextQuote < nextLine.length() - 1;
    }

    private String handleQuotes(String element) {
        String ret = element;

        if (!hasOnlyOneQuote(ret) && StringUtils.startsWith(ret, quoteCharString)) {
            ret = StringUtils.removeStart(ret, quoteCharString);

            if (StringUtils.endsWith(ret, quoteCharString)) {
                ret = StringUtils.removeEnd(ret, quoteCharString);
            }
        }
        ret = StringUtils.replace(ret, quoteCharString + quoteCharString, quoteCharString);
        if (ret.isEmpty() && (nullFieldIndicator == CSVReaderNullFieldIndicator.BOTH || nullFieldIndicator == CSVReaderNullFieldIndicator.EMPTY_QUOTES)) {
            ret = null;
        }
        return ret;
    }

    @Override
    public CSVReaderNullFieldIndicator nullFieldIndicator() {
        return nullFieldIndicator;
    }
    
    @Override
    public String getPendingText() {
        return StringUtils.defaultString(pending);
    }
    
    @Override
    public void setErrorLocale(Locale errorLocale) {
        // Curiously enough, this implementation never throws exceptions and so
        // has no need of translations.
    }
}
