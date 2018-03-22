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
import com.opencsv.bean.CsvBindByPosition;
import org.apache.commons.collections4.SetValuedMap;

/**
 *
 * @author Andrew Rucker Jones
 */
public class DoubleOpenRange {
    
    @CsvBindByPosition(position = 1)
    private int positionOne;
    
    @CsvBindAndJoinByPosition(position = "-", elementType = Integer.class)
    private SetValuedMap<Integer, Integer> otherPositions;

    public int getPositionOne() {
        return positionOne;
    }

    public void setPositionOne(int positionOne) {
        this.positionOne = positionOne;
    }

    public SetValuedMap<Integer, Integer> getOtherPositions() {
        return otherPositions;
    }

    public void setOtherPositions(SetValuedMap<Integer, Integer> otherPositions) {
        this.otherPositions = otherPositions;
    }
}
