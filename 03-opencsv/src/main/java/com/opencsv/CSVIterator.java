package com.opencsv;

import org.apache.commons.lang3.ObjectUtils;

import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;

/**
 * Provides an Iterator over the data found in opencsv.
 * <p><em>Fair warning!</em> This mechanism of getting at the data opencsv
 * delivers has limitations when used with the opencsv annotations. Locales and
 * custom converters are not supported. Further features may or may not work.</p>
 */
public class CSVIterator implements Iterator<String[]> {
   private final CSVReader reader;
   private String[] nextLine;
   
   /** Locale for all translations. */
   private Locale errorLocale = Locale.getDefault();

   /**
    * @param reader Reader for the CSV data.
    * @throws IOException If unable to read data from the reader.
    */
   public CSVIterator(CSVReader reader) throws IOException {
      this.reader = reader;
      nextLine = reader.readNext();
   }
   
    /**
     * Sets the locale for error messages.
     * @param errorLocale Locale for error messages. If null, the default locale
     *   is used.
     * @since 4.0
     */
    public void setErrorLocale(Locale errorLocale) {
        this.errorLocale = ObjectUtils.defaultIfNull(errorLocale, Locale.getDefault());
    }
    
   /**
    * Returns true if the iteration has more elements.
    * In other words, returns true if {@link #next()} would return an element
    * rather than throwing an exception.
    *
    * @return True if the CSVIterator has more elements.
    */
   @Override
   public boolean hasNext() {
      return nextLine != null;
   }

   /**
    *
    * Returns the next element in the iterator.
    *
    * @return The next element of the iterator.
    */
   @Override
   public String[] next() {
      String[] temp = nextLine;
      try {
         nextLine = reader.readNext();
      } catch (IOException e) {
         throw new NoSuchElementException();
      }
      return temp;
   }

   /**
    * This method is not supported by opencsv and will throw an
    * {@link java.lang.UnsupportedOperationException} if called.
    */
   @Override
   public void remove() {
      throw new UnsupportedOperationException(ResourceBundle.getBundle(ICSVParser.DEFAULT_BUNDLE_NAME, errorLocale).getString("read.only.iterator"));
   }
}
