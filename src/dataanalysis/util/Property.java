package dataanalysis.util;

public class Property {

    // TODO: David to review since I updated the datatype to Integer to allow null value
    private Integer marketValue;        // Nullable
    private Integer totalLivableArea;   // Nullable
    private String zipCode;

    public Property(String zipCode, Integer marketValue, Integer totalLivableArea) {
        this.zipCode = zipCode;
        this.marketValue = marketValue;
        this.totalLivableArea = totalLivableArea;
    }

    /**
     * @return the marketValue
     */
    public Integer getMarketValue() {
        return marketValue;
    }

    /**
     * @return the totalLivableArea
     */
    public Integer getTotalLivableArea() {
        return totalLivableArea;
    }

    /**
     * @return the zipCode
     */
    public String getZipCode() {
        return zipCode;
    }
}
