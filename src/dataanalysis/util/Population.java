package dataanalysis.util;

public class Population {

    private String zipCode;
    private int population;

    public Population (String zipCode, int population) {
        this.zipCode = zipCode;
        this.population = population;
    }

    /**
     * @return the zipCode
     */
    public String getZipCode() {
        return zipCode;
    }

    /**
     * @return the population
     */
    public int getPopulation() {
        return population;
    }
}
