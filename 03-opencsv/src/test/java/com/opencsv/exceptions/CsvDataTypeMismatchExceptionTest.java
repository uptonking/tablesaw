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
package com.opencsv.exceptions;

import com.opencsv.bean.mocks.MockBean;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class CsvDataTypeMismatchExceptionTest {
    private static final String TEST_MESSAGE = "some test message";
    
    @Test
    public void codeCoverageConstructors() {
        CsvDataTypeMismatchException e2 = new CsvDataTypeMismatchException();
        assertNull(e2.getDestinationClass());
        assertNull(e2.getSourceObject());
        e2 = new CsvDataTypeMismatchException(TEST_MESSAGE);
        assertEquals(TEST_MESSAGE, e2.getMessage());
    }
    
    @Test
    public void serializationDeserialization() throws IOException, ClassNotFoundException {
        MockBean bean = new MockBean();
        Field field = bean.getClass().getDeclaredFields()[0];
        CsvDataTypeMismatchException orig = new CsvDataTypeMismatchException(field, bean.getClass(), TEST_MESSAGE);
        orig.setLineNumber(Long.MAX_VALUE);
        assertNotNull(orig.getDestinationClass());
        assertNotNull(orig.getSourceObject());
        assertEquals(Long.MAX_VALUE, orig.getLineNumber());
        assertNotNull(orig.getLocalizedMessage());
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(orig);
        }
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        CsvDataTypeMismatchException deserialized = (CsvDataTypeMismatchException) ois.readObject();
        assertEquals(orig.getDestinationClass(), deserialized.getDestinationClass());
        assertNull(deserialized.getSourceObject());
        assertEquals(orig.getLocalizedMessage(), deserialized.getLocalizedMessage());
        assertEquals(orig.getLineNumber(), deserialized.getLineNumber());
    }
}
