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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class CsvBadConverterExceptionTest {
    private static final String TEST_MESSAGE = "some test message";
    
    @Test
    public void codeCoverageConstructors() {
        Class c = TestCase80.class;

        CsvBadConverterException e3 = new CsvBadConverterException();
        assertNull(e3.getConverterClass());
        e3 = new CsvBadConverterException(c);
        assertEquals(c, e3.getConverterClass());
        e3 = new CsvBadConverterException(TEST_MESSAGE);
        assertEquals(TEST_MESSAGE, e3.getMessage());
    }
    
    @Test
    public void serializationDeserialization() throws IOException, ClassNotFoundException {
        MockBean bean = new MockBean();
        CsvBadConverterException orig = new CsvBadConverterException(bean.getClass(), TEST_MESSAGE);
        assertNotNull(orig.getConverterClass());
        assertNotNull(orig.getLocalizedMessage());
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(orig);
        }
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        CsvBadConverterException deserialized = (CsvBadConverterException) ois.readObject();
        assertEquals(orig.getConverterClass(), deserialized.getConverterClass());
        assertEquals(orig.getLocalizedMessage(), deserialized.getLocalizedMessage());
    }
}
