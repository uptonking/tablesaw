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
package com.opencsv.bean.mocks.join;

import com.opencsv.bean.CsvBindAndJoinByPosition;
import org.apache.commons.collections4.MultiValuedMap;

/**
 *
 * @author Andrew Rucker Jones
 */
public class EmptyRange {
    
    @CsvBindAndJoinByPosition(position = " ", elementType = String.class)
    private MultiValuedMap<Integer, String> map;

    public MultiValuedMap<Integer, String> getMap() {
        return map;
    }

    public void setMap(MultiValuedMap<Integer, String> map) {
        this.map = map;
    }
}
