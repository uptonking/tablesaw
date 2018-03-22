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

import com.opencsv.CSVReader;
import com.opencsv.bean.BeanField;
import com.opencsv.bean.MappingStrategy;
import com.opencsv.exceptions.CsvBadConverterException;
import com.opencsv.exceptions.CsvConstraintViolationException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;
import org.apache.commons.lang3.ArrayUtils;

public class ErrorLineMappingStrategy<T> implements MappingStrategy<T> {
    @Override
    public PropertyDescriptor findDescriptor(int col) {
       return null;
    }

    @Override
    public BeanField findField(int col) {
       return null;
    }

    @Override
    public T createBean() throws InstantiationException {
       throw new InstantiationException("this is a test Exception");
    }

    @Override
    public void captureHeader(CSVReader reader) {
    }

    @Override
    public Integer getColumnIndex(String name) {
       return null;
    }

    @Override
    public boolean isAnnotationDriven() {
       return false;
    }
    
    @Override
    public String[] generateHeader(T bean) {
        return new String[0];
    }
    
    @Override
    public int findMaxFieldIndex() {
        return -1;
    }
    
    @Override
    public void verifyLineLength(int numberOfFields) {}

    @Override
    public void setErrorLocale(Locale errorLocale) {}

    @Override
    public void setType(Class type) throws CsvBadConverterException {}

    @Override
    public T populateNewBean(String[] line) throws InstantiationException {
       throw new InstantiationException("this is a test Exception");
    }

    @Override
    public T populateNewBeanWithIntrospection(String[] line) {
        return null;
    }

    @Override
    public String[] transmuteBean(T bean) {
        return ArrayUtils.EMPTY_STRING_ARRAY;
    }
}
