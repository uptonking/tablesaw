package com.opencsv.bean.mocks;

import com.opencsv.bean.CsvBindByName;

public class Bug154Bean {
    @CsvBindByName
    String a;
    @CsvBindByName
    int b;

    @Override
    public String toString() {
        return "Bug154Bean{" +
                "a='" + a + '\'' +
                ", b=" + b +
                '}';
    }
}
