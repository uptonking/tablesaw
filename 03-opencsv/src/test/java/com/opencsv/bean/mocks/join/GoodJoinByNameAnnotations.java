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

import com.opencsv.bean.CsvBindAndJoinByName;
import com.opencsv.bean.CsvDate;
import java.util.Date;
import org.apache.commons.collections4.MultiValuedMap;

/**
 *
 * @author Andrew Rucker Jones
 */
public class GoodJoinByNameAnnotations {
    
    @CsvBindAndJoinByName(column = "index", elementType = Integer.class, required = true)
    private MultiValuedMap<String, Integer> map1;
    
    @CsvBindAndJoinByName(column = "date[0-9]", elementType = Date.class, locale = "de-DE", required = true)
    @CsvDate(value = "dd. MMM yyyy")
    private MultiValuedMap<String, Date> map2;
    
    @CsvBindAndJoinByName(column = "regular expression will never match", elementType = String.class)
    private MultiValuedMap<String, String> map3;
    
    @CsvBindAndJoinByName(column = "conversion", elementType = Integer.class, locale = "de")
    private MultiValuedMap<String, Integer> map4;

    public MultiValuedMap<String, Integer> getMap1() {
        return map1;
    }

    public void setMap1(MultiValuedMap<String, Integer> map1) {
        this.map1 = map1;
    }

    public MultiValuedMap<String, Date> getMap2() {
        return map2;
    }

    public void setMap2(MultiValuedMap<String, Date> map2) {
        this.map2 = map2;
    }

    public MultiValuedMap<String, String> getMap3() {
        return map3;
    }

    public void setMap3(MultiValuedMap<String, String> map3) {
        this.map3 = map3;
    }

    public MultiValuedMap<String, Integer> getMap4() {
        return map4;
    }

    public void setMap4(MultiValuedMap<String, Integer> map4) {
        this.map4 = map4;
    }
}
