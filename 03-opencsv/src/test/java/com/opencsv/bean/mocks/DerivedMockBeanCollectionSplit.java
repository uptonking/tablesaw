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
import java.util.Stack;
import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.SortedBag;

/**
 *
 * @author Andrew Rucker Jones
 */
public class DerivedMockBeanCollectionSplit extends AnnotatedMockBeanCollectionSplit {
    
    @CsvBindAndSplitByName(elementType = Integer.class)
    private Bag<Integer> bagType;
    
    @CsvBindAndSplitByName(elementType = Integer.class)
    private SortedBag<Integer> sortedBagType;
    
    @CsvBindAndSplitByName(elementType = Integer.class)
    private IntegerSetSortedToString nonparameterizedCollectionType;
    
    @CsvBindAndSplitByName(elementType = Integer.class, splitOn = "[a-z]+")
    private Stack<Integer> stackType;

    public Bag<Integer> getBagType() {
        return bagType;
    }

    public void setBagType(Bag<Integer> bagType) {
        this.bagType = bagType;
    }

    public SortedBag<Integer> getSortedBagType() {
        return sortedBagType;
    }

    public void setSortedBagType(SortedBag<Integer> sortedBagType) {
        this.sortedBagType = sortedBagType;
    }

    public IntegerSetSortedToString getNonparameterizedCollectionType() {
        return nonparameterizedCollectionType;
    }

    public void setNonparameterizedCollectionType(IntegerSetSortedToString nonparameterizedCollectionType) {
        this.nonparameterizedCollectionType = nonparameterizedCollectionType;
    }

    public Stack<Integer> getStackType() {
        return stackType;
    }

    public void setStackType(Stack<Integer> stackType) {
        this.stackType = stackType;
    }
}
