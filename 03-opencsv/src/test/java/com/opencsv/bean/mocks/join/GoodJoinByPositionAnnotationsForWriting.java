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
import com.opencsv.bean.CsvDate;
import java.util.Date;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;

/**
 *
 * @author Andrew Rucker Jones
 */
public class GoodJoinByPositionAnnotationsForWriting {
    
    @CsvBindAndJoinByPosition(position = "0", elementType = Integer.class, required = true)
    private MultiValuedMap<Integer, Integer> map1;
    
    @CsvBindAndJoinByPosition(position = "1,16-", elementType = Date.class, locale = "de-DE", required = true)
    @CsvDate(value = "dd. MMM yyyy")
    private MultiValuedMap<Integer, Date> map2;
    
    @CsvBindAndJoinByPosition(position = "3-2", elementType = Integer.class, mapType = HashSetValuedHashMap.class)
    private MultiValuedMap<Integer, Integer> map3;
    
    @CsvBindAndJoinByPosition(position = "4-6 , 7 - 9,8-10,12,13,15", elementType = String.class)
    private ArrayListValuedHashMap<Integer, String> map4;
    
    @CsvBindAndJoinByPosition(position = "11", elementType = Integer.class, locale = "de")
    private MultiValuedMap<Integer, Integer> map5;
    
    public GoodJoinByPositionAnnotationsForWriting() {
        map1 = new ArrayListValuedHashMap<>();
        map1.put(Integer.MAX_VALUE, Integer.MIN_VALUE);
    }
    
    public MultiValuedMap<Integer, Integer> getMap1() {
        return map1;
    }

    public void setMap1(MultiValuedMap<Integer, Integer> map1) {
        this.map1 = map1;
    }

    public MultiValuedMap<Integer, Date> getMap2() {
        return map2;
    }

    public void setMap2(MultiValuedMap<Integer, Date> map2) {
        this.map2 = map2;
    }

    public MultiValuedMap<Integer, Integer> getMap3() {
        return map3;
    }

    public void setMap3(MultiValuedMap<Integer, Integer> map3) {
        this.map3 = map3;
    }

    public ArrayListValuedHashMap<Integer, String> getMap4() {
        return map4;
    }

    public void setMap4(ArrayListValuedHashMap<Integer, String> map4) {
        this.map4 = map4;
    }

    public MultiValuedMap<Integer, Integer> getMap5() {
        return map5;
    }

    public void setMap5(MultiValuedMap<Integer, Integer> map5) {
        this.map5 = map5;
    }

}
