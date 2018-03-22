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
import com.opencsv.bean.CsvDate;
import java.util.Collection;
import java.util.Date;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.NavigableSet;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;

/**
 *
 * @author Andrew Rucker Jones
 */
public class AnnotatedMockBeanCollectionSplit {
    
    @CsvBindAndSplitByName(elementType = Integer.class)
    private Collection<Integer> collectionType;
    
    @CsvBindAndSplitByName(collectionType = LinkedList.class, elementType = Integer.class)
    private List<Integer> listType;
    
    @CsvBindAndSplitByName(elementType = Date.class)
    @CsvDate("yyyy-MM-dd")
    private Set<Date> setType;
    
    @CsvBindAndSplitByName(elementType = Integer.class)
    private SortedSet<? extends Number> sortedSetType;
    
    @CsvBindAndSplitByName(elementType = Integer.class)
    private NavigableSet<Integer> navigableSetType;
    
    @CsvBindAndSplitByName(elementType = Integer.class)
    private Queue<Integer> queueType;
    
    @CsvBindAndSplitByName(elementType = Integer.class)
    private Deque<Integer> dequeType;

    public Collection<Integer> getCollectionType() {
        return collectionType;
    }

    public void setCollectionType(Collection<Integer> collectionType) {
        this.collectionType = collectionType;
    }

    public List<Integer> getListType() {
        return listType;
    }

    public void setListType(List<Integer> listType) {
        this.listType = listType;
    }

    public Set<Date> getSetType() {
        return setType;
    }

    public void setSetType(Set<Date> setType) {
        this.setType = setType;
    }

    public SortedSet<? extends Number> getSortedSetType() {
        return sortedSetType;
    }

    public void setSortedSetType(SortedSet<? extends Number> sortedSetType) {
        this.sortedSetType = sortedSetType;
    }

    public NavigableSet<Integer> getNavigableSetType() {
        return navigableSetType;
    }

    public void setNavigableSetType(NavigableSet<Integer> navigableSetType) {
        this.navigableSetType = navigableSetType;
    }

    public Queue<Integer> getQueueType() {
        return queueType;
    }

    public void setQueueType(Queue<Integer> queueType) {
        this.queueType = queueType;
    }

    public Deque<Integer> getDequeType() {
        return dequeType;
    }

    public void setDequeType(Deque<Integer> dequeType) {
        this.dequeType = dequeType;
    }
}
