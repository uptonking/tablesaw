package com.opencsv

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

/**
 * The purpose of this class is to test the CSVReader with different
 * (default) parsers and simple strings to make
 * sure they work the same.
 */
class CSVReaderAndParserIntegrationSpec extends Specification {

    @Shared
    RFC4180ParserBuilder rfc4180ParserBuilder = new RFC4180ParserBuilder()
    @Shared
    CSVParserBuilder csvParserBuilder = new CSVParserBuilder()

    @Shared
    ICSVParser rfc4180Parser = rfc4180ParserBuilder.build()
    @Shared
    ICSVParser csvParser = csvParserBuilder.build()

    @Unroll
    def 'parsing with #parserName'() {
        given:
        StringBuilder sb = new StringBuilder(ICSVParser.INITIAL_READ_SIZE)
        sb.append("a,b,c").append("\n")   // standard case
        sb.append("a,\"b,b,b\",c").append("\n")  // quoted elements
        sb.append(",,").append("\n") // empty elements
        sb.append("a,\"PO Box 123,\nKippax,ACT. 2615.\nAustralia\",d.\n")
        StringReader sr = new StringReader(sb.toString())

        CSVReaderBuilder builder = new CSVReaderBuilder(sr)
        CSVReader reader = builder.withCSVParser(parser).build()

        expect:

        reader.readNext() == ["a", "b", "c"]
        reader.readNext() == ["a", "b,b,b", "c"]
        reader.readNext() == ["", "", ""]
        reader.readNext() == ["a", "PO Box 123,\n" +
                "Kippax,ACT. 2615.\n" +
                "Australia", "d."]
        where:
        parser        | parserName
        csvParser     | "CSVParser"
        rfc4180Parser | "RFC4180Parser"

    }

    @Unroll
    def 'Bug 143 - Quote Character should be the escaper for #parserName'() {
        given:
        StringBuilder sb = new StringBuilder(ICSVParser.INITIAL_READ_SIZE)
        String value1 = "100"
        String value2 = "\"that \"\"if the reptile is killed the baby will pine away and die a few weeks later.\"\"\r\n\r\nThese mystical snakes will protect their owner and be a topic of conversation for sure.\""
        String readValue2 = "that \"if the reptile is killed the baby will pine away and die a few weeks later.\"\r\n\r\nThese mystical snakes will protect their owner and be a topic of conversation for sure."
        String value3 = "300"

        sb.append(value1 + "," + value2 + "," + value3)
        StringReader sr = new StringReader(sb.toString())

        CSVReaderBuilder builder = new CSVReaderBuilder(sr)
        CSVReader reader = builder.withCSVParser(parser).withKeepCarriageReturn(true).build()

        when:
        String[] values = reader.readNext()

        then:
        values[0] == value1
        values[1] == readValue2
        values[2] == value3
        values.length == 3

        where:
        parser        | parserName
        csvParser     | "CSVParser"
        rfc4180Parser | "RFC4180Parser"
    }
}
