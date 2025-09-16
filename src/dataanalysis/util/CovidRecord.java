package dataanalysis.util;

public class CovidRecord {

    private String zipCode;
    private String etlTimestamp;
    private int partiallyVaccinated;
    private int fullyVaccinated;

    public CovidRecord(String zipCode, String etlTimestamp, int partiallyVaccinated, int fullyVaccinated) {
        this.zipCode = zipCode;
        this.etlTimestamp = etlTimestamp;
        this.partiallyVaccinated = partiallyVaccinated;
        this.fullyVaccinated = fullyVaccinated;
    }

    /**
     * @return the zipCode
     */
    public String getZipCode() {
        return zipCode;
    }

    /**
     * @return the etlTimestamp
     */
    public String getEtlTimestamp() {
        return etlTimestamp;
    }

    /**
     * @return the partiallyVaccinated
     */
    public int getPartiallyVaccinated() {
        return partiallyVaccinated;
    }

    /**
     * @return the fullyVaccinated
     */
    public int getFullyVaccinated() {
        return fullyVaccinated;
    }
}
