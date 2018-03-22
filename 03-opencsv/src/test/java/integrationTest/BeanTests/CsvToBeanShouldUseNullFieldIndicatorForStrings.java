package integrationTest.BeanTests;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import org.junit.Test;

import java.io.StringReader;
import java.util.List;

import static org.junit.Assert.*;

/**
 * This is for Bug #158 in sourceforge.  When creating beans from openCSV the Parser null field indicator
 * should be respected.
 *
 * @see <a href="https://sourceforge.net/p/opencsv/bugs/158/">sourceforge Bug #158</a>
 * @see <a href="https://stackoverflow.com/questions/42856200/opencsv-csvreadernullfieldindicator-seems-to-make-no-difference">stack overflow question</a>
 */
public class CsvToBeanShouldUseNullFieldIndicatorForStrings {

    private static final String CSVDATA = "Id,OwnerId,Name,BillingStreet,BillingCity,BillingState,BillingPostalCode,BillingCountry\n" +
            "some Id, some owner Id, a name,,\"\", ,\" \",";

    @Test
    public void emptySeparators() {
        CsvToBean<AccountBean> csvToBean = new CsvToBeanBuilder<AccountBean>(new StringReader(CSVDATA))
                .withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_SEPARATORS)
                .withType(AccountBean.class)
                .build();

        List<AccountBean> beans = csvToBean.parse();

        assertEquals(1, beans.size());
        AccountBean accountBean = beans.get(0);
        assertNull(accountBean.getBillingStreet());
        assertTrue(accountBean.getBillingCity().isEmpty());
        assertEquals(" ", accountBean.getBillingState());
        assertEquals(" ", accountBean.getBillingPostcode());
        assertNull(accountBean.getBillingCountry());
    }

    @Test
    public void emptyQuotes() {
        CsvToBean<AccountBean> csvToBean = new CsvToBeanBuilder<AccountBean>(new StringReader(CSVDATA))
                .withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_QUOTES)
                .withType(AccountBean.class)
                .build();

        List<AccountBean> beans = csvToBean.parse();

        assertEquals(1, beans.size());
        AccountBean accountBean = beans.get(0);
        assertTrue(accountBean.getBillingStreet().isEmpty());
        assertNull(accountBean.getBillingCity());
        assertEquals(" ", accountBean.getBillingState());
        assertEquals(" ", accountBean.getBillingPostcode());
        assertTrue(accountBean.getBillingCountry().isEmpty());
    }

    @Test
    public void neither() {
        CsvToBean<AccountBean> csvToBean = new CsvToBeanBuilder<AccountBean>(new StringReader(CSVDATA))
                .withFieldAsNull(CSVReaderNullFieldIndicator.NEITHER)
                .withType(AccountBean.class)
                .build();

        List<AccountBean> beans = csvToBean.parse();

        assertEquals(1, beans.size());
        AccountBean accountBean = beans.get(0);
        assertTrue(accountBean.getBillingStreet().isEmpty());
        assertTrue(accountBean.getBillingCity().isEmpty());
        assertEquals(" ", accountBean.getBillingState());
        assertEquals(" ", accountBean.getBillingPostcode());
        assertTrue(accountBean.getBillingCountry().isEmpty());
    }


    @Test
    public void both() {
        CsvToBean<AccountBean> csvToBean = new CsvToBeanBuilder<AccountBean>(new StringReader(CSVDATA))
                .withFieldAsNull(CSVReaderNullFieldIndicator.BOTH)
                .withType(AccountBean.class)
                .build();

        List<AccountBean> beans = csvToBean.parse();

        assertEquals(1, beans.size());
        AccountBean accountBean = beans.get(0);
        assertNull(accountBean.getBillingStreet());
        assertNull(accountBean.getBillingCity());
        assertEquals(" ", accountBean.getBillingState());
        assertEquals(" ", accountBean.getBillingPostcode());
        assertNull(accountBean.getBillingCountry());
    }
}
