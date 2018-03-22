/*
 * Copyright 2018 Andrew Rucker Jones.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.opencsv.bean;

import com.opencsv.ICSVParser;
import com.opencsv.exceptions.CsvBadConverterException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;

/**
 * Maps any column position matching a range definition to a {@link BeanField}.
 *
 * @author Andrew Rucker Jones
 */
public class PositionToBeanField extends AbstractFieldMapEntry<String, Integer> implements Iterable<FieldMapByPositionEntry> {
    
    /**
     * This is the string used to initialize this set of ranges.
     * This is necessary because the ranges may be attenuated later by
     * {@link #attenuateRanges(int)}, rendering a reconstruction of the original
     * initialization information impossible.
     */
    private final String initializer;

    /** A list of ranges of column indices that should be mapped to the associated bean. */
    private final List<Range<Integer>> ranges;

    /**
     * Initializes this mapping with a list of ranges and the associated
     * {@link BeanField}.
     * 
     * @param rangeDefinition A definition of ranges as documented in
     *   {@link CsvBindAndJoinByPosition#position()}
     * @param maxIndex The maximum index allowed for a range. Ranges will be
     *   adjusted as documented in {@link #attenuateRanges(int)}.
     * @param field The {@link BeanField} this mapping maps to
     * @param errorLocale The locale for error messages
     * @throws CsvBadConverterException If {@code rangeDefinition} cannot be parsed
     */
    public PositionToBeanField(final String rangeDefinition, int maxIndex, final BeanField field, Locale errorLocale) {
        super(field, errorLocale);
        initializer = rangeDefinition;
        ranges = new LinkedList<>();

        // Error on empty range
        if(StringUtils.isBlank(rangeDefinition)) {
            throw new CsvBadConverterException(
                    BeanFieldJoin.class,
                    String.format(
                        ResourceBundle.getBundle(ICSVParser.DEFAULT_BUNDLE_NAME, this.errorLocale)
                                .getString("invalid.range.definition"),
                        rangeDefinition));
        }

        final String[] partialRangeDefinitions = rangeDefinition.split(",");
        try {
            for(String r : partialRangeDefinitions) {
                if(StringUtils.isNotEmpty(r)) {
                    Range<Integer> range;
                    
                    // Create the next range
                    if(r.contains("-")) {
                        final String[] endpoints = r.split("-", 2);
                        final Integer min = StringUtils.isEmpty(endpoints[0]) ? Integer.valueOf(0) : Integer.valueOf(endpoints[0].trim());
                        Integer max = maxIndex;
                        if(endpoints.length == 2 && StringUtils.isNotEmpty(endpoints[1])) {
                            max = Integer.valueOf(endpoints[1].trim());
                        }
                        if(max >= maxIndex) {
                            if(min >= maxIndex) {
                                max = min;
                            }
                            else {
                                max = maxIndex;
                            }
                        }
                        range = Range.between(min, max);
                    }
                    else {
                        range = Range.is(Integer.valueOf(r));
                    }

                    // Find out if this new range overlaps any of the
                    // preexisting ranges, and consolidate as much as possible
                    final ListIterator<Range<Integer>> it = ranges.listIterator();
                    boolean completelyContained = false;
                    while(it.hasNext() && ! completelyContained) {
                        final Range<Integer> next = it.next();
                        if(next.containsRange(range)) {
                            completelyContained = true;
                        }
                        else {
                            if(next.isOverlappedBy(range)) {
                                range = Range.between(
                                        Math.min(next.getMinimum(), range.getMinimum()),
                                        Math.max(next.getMaximum(), range.getMaximum()));
                                it.remove();
                            }
                            else if(next.getMaximum()+1 == range.getMinimum()) {
                                range = Range.between(next.getMinimum(), range.getMaximum());
                            }
                            else if(range.getMaximum()+1 == next.getMinimum()) {
                                range = Range.between(range.getMinimum(), next.getMaximum());
                            }
                        }
                    }
                    if(!completelyContained) {
                        ranges.add(range);
                    }
                }
            }
        }
        catch(NumberFormatException e) {
            // If the programmer specified non-numbers in the range
            final CsvBadConverterException csve = new CsvBadConverterException(
                    BeanFieldJoin.class,
                    String.format(
                        ResourceBundle.getBundle(ICSVParser.DEFAULT_BUNDLE_NAME, this.errorLocale)
                                .getString("invalid.range.definition"),
                        rangeDefinition));
            csve.initCause(e);
            throw csve;
        }
    }
    
    /**
     * If there are ranges in the list of ranges encompassed by this mapping
     * that stretch beyond the maximum index given, they are shortened to be
     * no longer than the maximum index.
     * Ranges that lie completely beyond the maximum index are shortened to a
     * one-element range consisting of the range's lower boundry. No ranges are
     * under any circumstances removed, as this might compromise checks for
     * required fields.
     * 
     * @param maxIndex The new maximum for ranges
     */
    public void attenuateRanges(int maxIndex) {
        for(int i = 0; i < ranges.size(); i++) {
            Range<Integer> r = ranges.get(i);
            if(r.getMaximum() > maxIndex) {
                if(r.getMinimum() > maxIndex) {
                    ranges.set(i, Range.is(r.getMinimum()));
                }
                else {
                    ranges.set(i, Range.between(r.getMinimum(), maxIndex));
                }
            }
        }
    }
    
    @Override
    public boolean contains(Integer key) {
        boolean foundRange = false;
        ListIterator<Range<Integer>> rangeIterator = ranges.listIterator();
        while(rangeIterator.hasNext() && !foundRange) {
            final Range<Integer> range = rangeIterator.next();
            if(range.contains(key)) {
                foundRange = true;
            }
        }
        return foundRange;
    }

    @Override
    public String getInitializer() {
        return initializer;
    }
    
    @Override
    public Iterator<FieldMapByPositionEntry> iterator() {
        return new PositionIterator();
    }
    
    /**
     * This iterator is designed to iterate over every element of all of the
     * ranges specified in the containing class.
     * <p>There is no guaranteed order.</p>
     * <p>There is one exception to returning all values: if a range ends at
     * {@link Integer#MAX_VALUE}, only the minimum in the range is returned.
     * This is to prevent a loop that for all practical purposes might as well
     * be infinite. Unless someone foolishly specifies {@link Integer#MAX_VALUE}
     * as a column position, this only occurs after reading in ranges and before
     * the first line of the input is read. There is no reason in the opencsv
     * code to iterate at this point, and it is not done. There should be no
     * reason for user code to use this iterator at all, but if it does, the
     * user is herewith warned.</p>
     */
    private class PositionIterator implements Iterator<FieldMapByPositionEntry> {
        
        private int listIndex;
        private int position;
        
        public PositionIterator() {
            listIndex = 0;
            if(ranges.isEmpty()) {
                position = -1;
            }
            else {
                position = ranges.get(0).getMinimum();
            }
        }

        @Override
        public boolean hasNext() {
            return position != -1;
        }

        @Override
        public FieldMapByPositionEntry next() {
            
            // Standard handling
            if(!hasNext()) {
                throw new NoSuchElementException();
            }
            
            // Value to return
            FieldMapByPositionEntry entry = new FieldMapByPositionEntry(position, field);
            
            // Advance the cursor. We add one extra precaution here: if a range
            // goes out to Integer.MAX_VALUE, we only return the minimum. This
            // is to prevent a seemingly endless loop on iteration.
            if(position == ranges.get(listIndex).getMaximum()
                    || Integer.MAX_VALUE == ranges.get(listIndex).getMaximum()) {
                if(listIndex == ranges.size()-1) {
                    position = -1;
                }
                else {
                    position = ranges.get(++listIndex).getMinimum();
                }
            }
            else {
                position++;
            }
            return entry;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
