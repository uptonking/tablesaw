/*
 * Copyright 2017 Andrew Rucker Jones.
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
package com.opencsv.bean.mocks;

import com.opencsv.bean.CsvBindAndSplitByPosition;
import java.util.List;
import java.util.Queue;

/**
 *
 * @author Andrew Rucker Jones
 */
public class AnnotatedMockBeanCollectionSplitByColumn {
    
    @CsvBindAndSplitByPosition(elementType = String.class, position = 0, writeDelimiter = ",")
    private List<String> stringList;
    
    @CsvBindAndSplitByPosition(elementType = Float.class, position = 1, required = true, writeDelimiter = " silly delimiter ")
    private Queue<Float> floatList;

    public List<String> getStringList() {
        return stringList;
    }

    public void setStringList(List<String> stringList) {
        this.stringList = stringList;
    }

    public Queue<Float> getFloatList() {
        return floatList;
    }

    public void setFloatList(Queue<Float> floatList) {
        this.floatList = floatList;
    }
}
