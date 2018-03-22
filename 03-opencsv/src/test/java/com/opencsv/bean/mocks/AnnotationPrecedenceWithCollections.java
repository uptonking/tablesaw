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

import com.opencsv.bean.CsvBindAndSplitByName;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvCustomBindByName;
import com.opencsv.bean.customconverter.ConvertSplitOnWhitespace;
import java.util.List;

/**
 *
 * @author Andrew Rucker Jones
 */
public class AnnotationPrecedenceWithCollections {
    
    @CsvCustomBindByName(converter = ConvertSplitOnWhitespace.class)
    @CsvBindAndSplitByName(elementType = String.class, splitOn="[A-Za-z0-9]")
    private List<String> precedenceGoesToCustom;
    
    @CsvBindAndSplitByName(elementType = Integer.class)
    @CsvBindByName
    private List<Integer> precedenceGoesToCollection;

    public List<String> getPrecedenceGoesToCustom() {
        return precedenceGoesToCustom;
    }

    public void setPrecedenceGoesToCustom(List<String> precedenceGoesToCustom) {
        this.precedenceGoesToCustom = precedenceGoesToCustom;
    }

    public List<Integer> getPrecedenceGoesToCollection() {
        return precedenceGoesToCollection;
    }

    public void setPrecedenceGoesToCollection(List<Integer> precedenceGoesToCollection) {
        this.precedenceGoesToCollection = precedenceGoesToCollection;
    }
}
