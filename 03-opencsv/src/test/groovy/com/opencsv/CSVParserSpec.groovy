package com.opencsv

import spock.lang.Specification
import spock.lang.Unroll

class CSVParserSpec extends Specification {
    @Unroll
    def 'parsing #testLine from String to array back to String returns the same result'(String testLine) {
        given:
        CSVParserBuilder builder = new CSVParserBuilder()
        CSVParser parser = builder.build()
        String[] parsedValues = parser.parseLine(testLine)
        String finalString = parser.parseToLine(parsedValues)

        expect:
        finalString == testLine

        where:
        testLine                                             | _
        "This,is,a,test"                                     | _
        "7,seven,7.89,12/11/16"                              | _
        "a,\"b,b,b\",c"                                      | _
        "a,b,c"                                              | _
        "a,\"PO Box 123,\nKippax,ACT. 2615.\nAustralia\",d." | _
        "zo\"\"har\"\"at,10-04-1980,29,C:\\\\foo.txt"        | _
    }
}
