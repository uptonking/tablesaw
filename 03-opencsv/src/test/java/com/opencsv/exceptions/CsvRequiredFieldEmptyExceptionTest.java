/*
 * Copyright 2017 Andrew Rucket Jones.
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
import com.opencsv.bean.mocks.TestCase80;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import static org.junit.Assert.*;
import org.junit.Test;

public class CsvRequiredFieldEmptyExceptionTest {
    private static final String TEST_MESSAGE = "some test message";
    
    @Test
    public void codeCoverageConstructors() throws NoSuchFieldException {
        Class c = TestCase80.class;
        Field f = c.getField("test");
        CsvRequiredFieldEmptyException e1 = new CsvRequiredFieldEmptyException(c, f);
        assertEquals(TestCase80.class, e1.getBeanClass());
        assertEquals(f, e1.getDestinationField());
        
        e1 = new CsvRequiredFieldEmptyException();
        assertNull(e1.getBeanClass());
        assertNull(e1.getDestinationField());
        assertNull(e1.getCause());
        assertNull(e1.getMessage());
        assertEquals(-1, e1.getLineNumber());

        String err = "Test";
        e1 = new CsvRequiredFieldEmptyException(err);
        assertNull(e1.getBeanClass());
        assertNull(e1.getDestinationField());
        assertNull(e1.getCause());
        assertEquals(err, e1.getMessage());
        assertEquals(-1, e1.getLineNumber());
        
    }
    
    @Test
    public void serializationDeserialization() throws IOException, ClassNotFoundException {
        MockBean bean = new MockBean();
        Field field = bean.getClass().getDeclaredFields()[0];
        CsvRequiredFieldEmptyException orig = new CsvRequiredFieldEmptyException(bean.getClass(), field, TEST_MESSAGE);
        orig.setLineNumber(Long.MAX_VALUE);
        assertNotNull(orig.getBeanClass());
        assertNotNull(orig.getDestinationField());
        assertEquals(Long.MAX_VALUE, orig.getLineNumber());
        assertNotNull(orig.getLocalizedMessage());
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(orig);
        }
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        CsvRequiredFieldEmptyException deserialized = (CsvRequiredFieldEmptyException) ois.readObject();
        assertEquals(orig.getBeanClass(), deserialized.getBeanClass());
        assertNull(deserialized.getDestinationField());
        assertEquals(orig.getLocalizedMessage(), deserialized.getLocalizedMessage());
        assertEquals(orig.getLineNumber(), deserialized.getLineNumber());
    }
}
