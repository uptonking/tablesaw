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

import java.util.Arrays;
import java.util.Collection;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.lang3.ArrayUtils;

/**
 * A bi-directional mapping between column positions and header names.
 * A simple {@link org.apache.commons.collections4.BidiMap} will not do the
 * trick, because header names (or null in place of a header name) may appear
 * more than once.
 * 
 * @author Andrew Rucker Jones
 * @since 4.2
 */
public class HeaderIndex {
    
    /** The uni-directional map from column position to header name. */
    private String[] positionToHeader = new String[0];
    
    /**
     * The uni-directional map from header name to (possibly multiple) column
     * positions.
     */
    private MultiValuedMap<String, Integer> headerToPosition = new ArrayListValuedHashMap<>();
    
    /** Useless but explicit nullary constructor to make the style checker happy. */
    public HeaderIndex(){}
    
    /**
     * Empties the entire mapping.
     */
    public void clear() {
        positionToHeader = new String[0];
        headerToPosition = new ArrayListValuedHashMap<>();
    }
    
    /**
     * @return The maximum index that is mapped and will return a header name
     *   (or null if specifically mapped that way).
     */
    public int findMaxIndex() {
        return positionToHeader.length-1;
    }
    
    /**
     * Initializes the index with a list of header names in proper encounter
     * order.
     * "Proper encounter order" means the order in which they are expected to be
     * found in the input CSV. Header names may be listed more than once if the
     * destination field is annotated with {@link CsvBindAndJoinByPosition} or
     * {@link CsvBindAndJoinByName}. Values of {@code null} indicate the column
     * from the input should not be mapped to a bean field.
     * 
     * @param header A list of header names in the order in which they are
     *   expected in the CSV input
     */
    public void initializeHeaderIndex(String[] header) {
        positionToHeader = header != null ? ArrayUtils.clone(header): new String[0];
        headerToPosition.clear();
        int i = 0;
        while(i < positionToHeader.length) {
            headerToPosition.put(header[i], i);
            i++;
        }
    }
    
    /** @return Whether or not the mapping is empty */
    public boolean isEmpty() {
        return positionToHeader.length == 0;
    }
    
    /**
     * Retrieves the column position(s) associated with the given header name.
     * 
     * @param headerName The header name for which the associated column
     *   positions should be returned
     * @return The column positions associated with {@code headerName}
     */
    public int[] getByName(String headerName) {
        Collection<Integer> positions = headerToPosition.get(headerName);
        if(positions != null) {
            return ArrayUtils.toPrimitive(positions.toArray(new Integer[positions.size()]));
        }
        return new int[0];
    }
    
    /**
     * Retrieves the header associated with the given column position.
     * 
     * @param i The column position for which the header name is to be retrieved
     * @return The header name mapped by position {@code i}
     */
    public String getByPosition(int i) {
        if(i < positionToHeader.length) {
            return positionToHeader[i];
        }
        return null;
    }
    
    /**
     * @return The current list of headers mapped by this index in the proper
     *   order
     */
    public String[] getHeaderIndex() {
        return ArrayUtils.clone(positionToHeader);
    }
    
    /**
     * @return The length of the current mapping, including all fields unmapped
     */
    public int getHeaderIndexLength() {return positionToHeader.length;}
    
    /**
     * Adds a new mapping between a column position and a header.
     * The header may already be present, in which case the column position is
     * added to the list of column positions mapped to the header.
     * 
     * @param k The column position for the mapping
     * @param v The header to be associated with the column position
     */
    public void put(int k, String v) {
        if(k >= positionToHeader.length) {
            positionToHeader = Arrays.copyOf(positionToHeader, k+1);
            positionToHeader[k] = v;
        }
        headerToPosition.put(v, k);
    }
}
