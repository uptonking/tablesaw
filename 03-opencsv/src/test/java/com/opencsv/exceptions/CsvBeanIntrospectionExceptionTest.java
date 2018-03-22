package com.opencsv.exceptions;


import com.opencsv.bean.mocks.MockBean;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.*;

public class CsvBeanIntrospectionExceptionTest {
    private static final String TEST_MESSAGE = "some test message";

    @Test
    public void defaultExceptionHasNoMessage() {
        CsvBeanIntrospectionException exception = new CsvBeanIntrospectionException();
        assertNull(exception.getMessage());
        assertNull(exception.getBean());
        assertNull(exception.getField());
    }

    @Test
    public void exceptionWithOnlyAMessage() {
        CsvBeanIntrospectionException exception = new CsvBeanIntrospectionException(TEST_MESSAGE);
        assertEquals(TEST_MESSAGE, exception.getMessage());
        assertNull(exception.getBean());
        assertNull(exception.getField());
    }

    @Test
    public void exceptionWithNoMessageButHasBeanAndField() {
        MockBean bean = new MockBean();
        Field field = bean.getClass().getDeclaredFields()[0];

        assertNotNull(bean);
        assertNotNull(field);

        CsvBeanIntrospectionException exception = new CsvBeanIntrospectionException(bean, field);
        String message = exception.getMessage();
        System.out.println(message);
        assertTrue(message.contains(bean.getClass().getCanonicalName()));
        assertTrue(message.contains(field.getName()));

        assertEquals(bean, exception.getBean());
        assertEquals(field, exception.getField());
    }

    @Test
    public void exceptionWithMessageBeanAndFieldWillReturnMessage() {
        MockBean bean = new MockBean();
        Field field = bean.getClass().getDeclaredFields()[0];

        assertNotNull(bean);
        assertNotNull(field);

        CsvBeanIntrospectionException exception = new CsvBeanIntrospectionException(bean, field, TEST_MESSAGE);
        assertEquals(bean, exception.getBean());
        assertEquals(field, exception.getField());
        assertEquals(TEST_MESSAGE, exception.getMessage());
    }
    
    @Test
    public void serializationDeserialization() throws IOException, ClassNotFoundException {
        MockBean bean = new MockBean();
        Field field = bean.getClass().getDeclaredFields()[0];
        CsvBeanIntrospectionException orig = new CsvBeanIntrospectionException(bean, field, TEST_MESSAGE);
        assertNotNull(orig.getBean());
        assertNotNull(orig.getField());
        assertNotNull(orig.getLocalizedMessage());
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(orig);
        }
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        CsvBeanIntrospectionException deserialized = (CsvBeanIntrospectionException) ois.readObject();
        assertNull(deserialized.getBean());
        assertNull(deserialized.getField());
        assertEquals(orig.getLocalizedMessage(), deserialized.getLocalizedMessage());
    }
}
