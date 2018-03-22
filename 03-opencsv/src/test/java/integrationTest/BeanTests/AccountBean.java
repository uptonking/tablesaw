package integrationTest.BeanTests;

import com.opencsv.bean.CsvBindByName;

public class AccountBean {
    @CsvBindByName(column = "Id", required = true)
    public String salesforceId;

    @CsvBindByName(column = "OwnerId", required = true)
    public String ownerId;

    @CsvBindByName(column = "Name", required = true)
    public String name;

    // billing address
    @CsvBindByName(column = "BillingStreet")
    String billingStreet;
    @CsvBindByName(column = "BillingCity")
    String billingCity;
    @CsvBindByName(column = "BillingState")
    String billingState;
    @CsvBindByName(column = "BillingPostalCode")
    String billingPostcode;
    @CsvBindByName(column = "BillingCountry")
    String billingCountry;

    public String getSalesforceId() {
        return salesforceId;
    }

    public void setSalesforceId(String salesforceId) {
        this.salesforceId = salesforceId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBillingStreet() {
        return billingStreet;
    }

    public void setBillingStreet(String billingStreet) {
        this.billingStreet = billingStreet;
    }

    public String getBillingCity() {
        return billingCity;
    }

    public void setBillingCity(String billingCity) {
        this.billingCity = billingCity;
    }

    public String getBillingState() {
        return billingState;
    }

    public void setBillingState(String billingState) {
        this.billingState = billingState;
    }

    public String getBillingPostcode() {
        return billingPostcode;
    }

    public void setBillingPostcode(String billingPostcode) {
        this.billingPostcode = billingPostcode;
    }

    public String getBillingCountry() {
        return billingCountry;
    }

    public void setBillingCountry(String billingCountry) {
        this.billingCountry = billingCountry;
    }
}
