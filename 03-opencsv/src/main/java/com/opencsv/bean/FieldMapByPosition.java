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
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.SortedMap;
import java.util.TreeMap;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.collections4.iterators.LazyIteratorChain;
import org.apache.commons.collections4.iterators.TransformIterator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StrBuilder;

/**
 * This class maintains a mapping from column position out of a CSV file to bean
 * fields.
 *
 * @author Andrew Rucker Jones
 * @since 4.2
 */
public class FieldMapByPosition extends AbstractFieldMap<String, Integer, PositionToBeanField> implements Iterable<FieldMapByPositionEntry> {
    
    private int maxIndex = Integer.MAX_VALUE;
    
    /**
     * Initializes this {@link FieldMap}.
     * 
     * @param errorLocale The locale to be used for error messages
     */
    public FieldMapByPosition(final Locale errorLocale) {
        super(errorLocale);
    }
    
    /**
     * This method generates a header that can be used for writing beans of the
     * type provided back to a file.
     * The ordering of the headers is ascending according to position, naturally.
     */
    // The rest of the Javadoc is inherited.
    @Override
    public String[] generateHeader(final Object bean) throws CsvRequiredFieldEmptyException {
        final List<Field> missingRequiredHeaders = new ArrayList<>();
        final SortedMap<Integer, String> headerMap = new TreeMap<>();
        for(Map.Entry<Integer, BeanField> entry : simpleMap.entrySet()) {
            headerMap.put(entry.getKey(), entry.getValue().getField().getName());
        }
        for(ComplexFieldMapEntry r : complexMapList) {
            final MultiValuedMap<Integer,Object> m = (MultiValuedMap) r.getBeanField().getFieldValue(bean);
            boolean oneEntryMatched = false;
            if(m != null && !m.isEmpty()) {
                for(Map.Entry<Integer,Object> entry : m.entries()) {
                    Integer key = entry.getKey();
                    if(r.contains(key)) {
                        headerMap.put(entry.getKey(), r.getBeanField().getField().getName());
                        oneEntryMatched = true;
                    }
                }
            }
            if(m == null || m.isEmpty() || !oneEntryMatched) {
                if(r.getBeanField().isRequired()) {
                    missingRequiredHeaders.add(r.getBeanField().getField());
                }
            }
        }
        
        // Convert to an array of header "names"
        int arraySize = headerMap.isEmpty() ? 0 : headerMap.lastKey()+1;
        final String[] headers = new String[arraySize];
        for(Map.Entry<Integer, String> entry : headerMap.entrySet()) {
            headers[entry.getKey()] = entry.getValue();
        }
        
        // Report headers that should have been present
        if(!missingRequiredHeaders.isEmpty()) {
            StrBuilder sb = new StrBuilder();
            for(Field f : missingRequiredHeaders) {
                sb.appendSeparator(' '); sb.append(f.getName());
            }
            String errorMessage = String.format(
                    ResourceBundle
                            .getBundle(ICSVParser.DEFAULT_BUNDLE_NAME, errorLocale)
                            .getString("header.required.field.absent"),
                    sb.toString(),
                    StringUtils.join(headers, ' '));
            throw new CsvRequiredFieldEmptyException(bean.getClass(), missingRequiredHeaders, errorMessage);
        }
        
        return headers;
    }
    
    /**
     * @param rangeDefinition A string describing the column positions to be
     *   matched.
     * @see CsvBindAndJoinByPosition#position() 
     */
    // The rest of the Javadoc is inherited
    @Override
    public void putComplex(final String rangeDefinition, final BeanField field) {
        complexMapList.add(new PositionToBeanField(rangeDefinition, maxIndex, field, errorLocale));
    }
    
    /**
     * Sets the maximum index for all ranges specified in the entire field map.
     * No ranges or mappings are ever removed so as to preserve information
     * about required fields, but upper boundries are shortened as much as
     * possible. If ranges or individual column positions were specified that
     * lie wholly above {@code maxIndex}, these are preserved, though ranges
     * are shortened to a single value (the lower boundry).
     * 
     * @param maxIndex The maximum index in the data being imported
     */
    public void setMaxIndex(int maxIndex) {
        this.maxIndex = maxIndex;
        
        // Attenuate all ranges that end past the last index down to the last index
        for(PositionToBeanField p : complexMapList) {
            p.attenuateRanges(maxIndex);
        }
    }

    @Override
    public Iterator<FieldMapByPositionEntry> iterator() {
        return new LazyIteratorChain<FieldMapByPositionEntry>() {
            
            @Override
            protected Iterator<FieldMapByPositionEntry> nextIterator(int count) {
                if(count <= complexMapList.size()) {
                    return complexMapList.get(count-1).iterator();
                }
                if(count == complexMapList.size()+1) {
                    return new TransformIterator<>(
                            simpleMap.entrySet().iterator(),
                            new Transformer<Map.Entry<Integer, BeanField>, FieldMapByPositionEntry>() {
                                @Override
                                public FieldMapByPositionEntry transform(Map.Entry<Integer, BeanField> input) {
                                    return new FieldMapByPositionEntry(input.getKey(), input.getValue());
                                }
                            });
                }
                return null;
            }
        };
    }
}
