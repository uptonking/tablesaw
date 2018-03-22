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
import com.opencsv.bean.mocks.TestCase80;
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

public class CsvConstraintViolationExceptionTest {
    private static final String TEST_MESSAGE = "some test message";
    
    @Test
    public void codeCoverageConstructors() throws NoSuchFieldException {
        Class c = TestCase80.class;
        Field f = c.getField("test");
        
        CsvConstraintViolationException e4 = new CsvConstraintViolationException();
        assertNull(e4.getSourceObject());
        e4 = new CsvConstraintViolationException(f);
        assertEquals(f, e4.getSourceObject());
        e4 = new CsvConstraintViolationException(TEST_MESSAGE);
        assertEquals(TEST_MESSAGE, e4.getMessage());
        e4 = new CsvConstraintViolationException(f, TEST_MESSAGE);
        assertEquals(f, e4.getSourceObject());
        assertEquals(TEST_MESSAGE, e4.getMessage());
    }
    
    @Test
    public void serializationDeserialization() throws IOException, ClassNotFoundException {
        MockBean bean = new MockBean();
        CsvConstraintViolationException orig = new CsvConstraintViolationException(bean, TEST_MESSAGE);
        orig.setLineNumber(Long.MAX_VALUE);
        assertNotNull(orig.getSourceObject());
        assertEquals(Long.MAX_VALUE, orig.getLineNumber());
        assertNotNull(orig.getLocalizedMessage());
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(orig);
        }
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        CsvConstraintViolationException deserialized = (CsvConstraintViolationException) ois.readObject();
        assertNull(deserialized.getSourceObject());
        assertEquals(orig.getLocalizedMessage(), deserialized.getLocalizedMessage());
        assertEquals(orig.getLineNumber(), deserialized.getLineNumber());
    }
}
