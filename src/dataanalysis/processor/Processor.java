package dataanalysis.processor;

import dataanalysis.datamanagement.*;
import dataanalysis.util.*;

import java.util.*;

public class Processor {

    // Data readers for each type of input data
    protected Reader covidDataReader;
    protected PopulationReader populationReader;
    protected PropertyReader propertyReader;

    // List of data records
    protected List<CovidRecord> covidRecords;
    protected List<Population> populationRecords;
    protected List<Property> propertyRecords;

    // Memoization caches to improve performance
    private Integer totalPopulationCache;
    private Map<String, List<Property>> propertyDataByZip = new HashMap<>();
    private Map<String, Integer> populationByZip = new HashMap<>();
    private Map<String, Map<String, Integer>> vaccinationCountsCache = new HashMap<>();
    private final Map<String, Map<String, Double>> vaccinationPerCapitaCache = new HashMap<>();
    private Map<String, Map<String, Double>> healthRiskIndexCache = new HashMap<>();
    private final Map<String, Integer> avgMarketValueCache = new HashMap<>();
    private final Map<String, Integer> avgLivableAreaCache = new HashMap<>();
    private final Map<String, Integer> marketValuePerCapitaCache = new HashMap<>();

    /**
     * Constructor loads data using available readers.
     * Null readers are skipped to allow partial data loading.
     */
    public Processor(Reader covidDataReader, PopulationReader populationReader, PropertyReader propertyReader) {
        this.covidDataReader = covidDataReader;
        this.populationReader = populationReader;
        this.propertyReader = propertyReader;

        // Null readers are skipped
        if (this.covidDataReader != null){
            this.covidRecords = covidDataReader.getCovidData();
        }
        if (this.populationReader != null){
            this.populationRecords = populationReader.getPopulationData();
        }
        if (this.propertyReader != null){
            this.propertyRecords = propertyReader.getPropertyData();
        }
    }

    /**
     * Returns total population.
     */
    public int getTotalPopulation() {

        if (totalPopulationCache != null) return totalPopulationCache;

        int total = 0;
        for (Population pop : populationRecords) {

            populationByZip.put(pop.getZipCode(), pop.getPopulation());

            // update the running total population
            total += pop.getPopulation();
        }
        totalPopulationCache = total;   // update cache of total population
        return total;
    }

    /**
     * Returns vaccination per capita by ZIP code for a given date and type.
     */
    public Map<String, Double> getVaccinationPerCapita(String type, String date) {
        String cacheKey = type.toLowerCase() + "|" + date;

        // Return cached value if available
        if (vaccinationPerCapitaCache.containsKey(cacheKey)) {
            return vaccinationPerCapitaCache.get(cacheKey);
        }

        Map<String, Integer> vaccinationsByZip = getVaccinationCountsByZip(type, date);
        Map<String, Double> result = new TreeMap<>();

        // Initialize population cache if empty
        if (populationByZip.isEmpty()) {
            for (Population pop : populationRecords) {
                populationByZip.put(pop.getZipCode(), pop.getPopulation());
            }
        }

        for (String zip : vaccinationsByZip.keySet()) {
            int vaccinated = vaccinationsByZip.get(zip);
            int population = populationByZip.getOrDefault(zip, 0);
            if (vaccinated == 0 || population == 0) continue;

            double perCapita = (double) vaccinated / population;
            result.put(zip, Math.round(perCapita * 10000.0) / 10000.0);
        }
        vaccinationPerCapitaCache.put(cacheKey, result);
        return result;
    }

    /**
     * Helper function to return a cached or newly computed map of vaccination counts by ZIP.
     * Filters covidRecords by date and type.
     */
    private Map<String, Integer> getVaccinationCountsByZip(String type, String date) {
        String cacheKey = type.toLowerCase() + "|" + date;

        if (vaccinationCountsCache.containsKey(cacheKey)) {
            return vaccinationCountsCache.get(cacheKey);
        }

        Map<String, Integer> vaccinationCounts = new HashMap<>();
        for (CovidRecord record : covidRecords) {
            String zip = record.getZipCode();
            String timestamp = record.getEtlTimestamp();

            // Extract only date part of timestamp (first 10 characters)
            if (timestamp.length() < 10 || !timestamp.substring(0, 10).equals(date)) continue;

            int vaccinationCount = 0;
            String lower = type.toLowerCase();
            if (lower.equals("partial")) {
                vaccinationCount = record.getPartiallyVaccinated();
            } else if (lower.equals("full")) {
                vaccinationCount = record.getFullyVaccinated();
            }

            if (vaccinationCount <= 0) continue;

            vaccinationCounts.put(zip, vaccinationCounts.getOrDefault(zip, 0) + vaccinationCount);
        }

        vaccinationCountsCache.put(cacheKey, vaccinationCounts); // update the cache
        return vaccinationCounts;
    }

    /**
     * Returns the average market value of properties for a given ZIP.
     * Relies on a cached calculation if it exists, else it calculates it
     */
    public int calculateAverageMarketValue(String zip) {

        // if the average house market value for this zip is already cached, then return the cached value
        if (avgMarketValueCache.containsKey(zip)){
            return avgMarketValueCache.get(zip);
        } else {
            // else calculate the new value, add it to cache, and return it
            int averageHousePriceInZip = calculateAverageByStrategy(zip, new MarketValueStrategy());
            avgMarketValueCache.put(zip, averageHousePriceInZip);
            return averageHousePriceInZip;
        }
    }

    /**
     * Returns the average livable area of properties for a given ZIP.
     * Relies on a cached calculation if it exists, else it calculates it
     */
    public int calculateAverageLivableArea(String zip) {

        // if the average house size for this zip is already cached, then return the cached value
        if (avgLivableAreaCache.containsKey(zip)){
            return avgLivableAreaCache.get(zip);
        } else {
            // else calculate the new value, add it to cache, and return it
            int averageHouseSizeInZip = calculateAverageByStrategy(zip, new LivableAreaStrategy());
            avgLivableAreaCache.put(zip, averageHouseSizeInZip);
            return averageHouseSizeInZip;
        }
    }

    /**
     * Strategy-based average calculator.
     * Applies the strategy to each property in the ZIP.
     */
    public int calculateAverageByStrategy(String zip, AverageCalculationStrategy strategy) {
        List<Property> properties = getPropertiesByZip(zip);
        if (properties.isEmpty()) return 0;

        long total = 0;
        int count = 0;

        for (Property property : properties) {
            Integer value = strategy.extractValue(property);
            if (property != null && value != null) {
                total += value;
                count++;
            }
        }
        return count > 0 ? (int)(total / count) : 0;
    }

    /**
     * Filters properties by ZIP code and memoizes the result.
     */
    private List<Property> getPropertiesByZip(String zip) {
        if (propertyDataByZip.containsKey(zip)) {
            return propertyDataByZip.get(zip);
        }

        List<Property> result = new ArrayList<>();
        for (Property property : propertyRecords) {
            if (zip.equals(property.getZipCode())) {
                result.add(property);
            }
        }

        propertyDataByZip.put(zip, result); // update the cache
        return result;
    }


    /**
     * Looks up population for a ZIP, populating cache if necessary.
     */
    private int getPopulationByZip(String zipCode){

        if (populationByZip.containsKey(zipCode)) {
            return populationByZip.get(zipCode);
        }

        for (Population pop : populationRecords) {

            populationByZip.put(pop.getZipCode(), pop.getPopulation());
            if (zipCode.equals(pop.getZipCode())) {
                return pop.getPopulation();
            }
        }
        return 0;
    }


    /**
     * Computes market value per capita for properties in a ZIP.
     * Returns 0 if population or property data is missing.
     */
    public int calculateMarketValuePerCapita(String zip) {

        if (marketValuePerCapitaCache.containsKey(zip)) {
            return marketValuePerCapitaCache.get(zip);
        }

        int population;

        // use cached values if zip:population exists, else search for the zip in the populations list
        if (populationByZip.containsKey(zip)){
            population = populationByZip.get(zip);
        } else {
            population = getPopulationByZip(zip);
        }

        // if either the population for this zip is 0 or there are no properties in this zip code then return 0
        if (population == 0) {
            marketValuePerCapitaCache.put(zip, 0);
            return 0;
        }

        List<Property> properties = getPropertiesByZip(zip);

        if (properties.isEmpty()) {
            marketValuePerCapitaCache.put(zip, 0);
            return 0;
        }

        // calculate the total Market Value per capita
        long totalMarketValue = 0;
        for (Property property : properties) {
            Integer mv = property.getMarketValue();
            if (property != null && mv != null) {
                totalMarketValue += mv;
            }
        }

        int valuePerCapita = (int)(totalMarketValue / population);
        marketValuePerCapitaCache.put(zip, valuePerCapita);
        return valuePerCapita;
    }

    /**
     * Computes the health risk index for each ZIP code on a given date.
     * Uses memoization to cache results by date.
     */
    public Map<String, Double> getHealthRiskIndex(String date) {
        if (healthRiskIndexCache.containsKey(date)) {
            return healthRiskIndexCache.get(date);
        }

        if (populationByZip.isEmpty()) {
            for (Population pop : populationRecords) {
                populationByZip.put(pop.getZipCode(), pop.getPopulation());
            }
        }

        Map<String, Integer> vaccinations = getVaccinationCountsByZip("full", date);

        // If no vaccination data found for the date, return 0 for all ZIPs
        if (vaccinations.isEmpty()) {
            Map<String, Double> zeroMap = new TreeMap<>();
            for (String zip : populationByZip.keySet()) {
                zeroMap.put(zip, 0.0);
            }
            healthRiskIndexCache.put(date, zeroMap);
            return zeroMap;
        }

        Map<String, Double> result = new TreeMap<>();

        for (String zip : populationByZip.keySet()) {
            int population = populationByZip.get(zip);
            int vaccinated = vaccinations.getOrDefault(zip, 0);
            long livableArea = getTotalLivableArea(zip);

            if (population == 0 || livableArea == 0) continue;

            double vaccinationRate = (double) vaccinated / population;
            double index = ((1.0 - vaccinationRate) * population) / livableArea;

            result.put(zip, Math.round(index * 10000.0) / 10000.0);
        }

        healthRiskIndexCache.put(date, result);
        return result;
    }

    /**
     * Returns the total livable area for properties in a ZIP code.
     */
    private long getTotalLivableArea(String zip) {
        List<Property> properties = getPropertiesByZip(zip);
        long total = 0;
        for (Property p : properties) {
            if (p != null && p.getTotalLivableArea() != null) {
                total += p.getTotalLivableArea();
            }
        }
        return total;
    }

    // ==== Utility methods to check availability of each dataset ====

    public boolean hasPopulationData() {
        return populationRecords != null && !populationRecords.isEmpty();
    }

    public boolean hasPropertyData() {
        return propertyRecords != null && !propertyRecords.isEmpty();
    }

    public boolean hasCovidData() {
        return covidRecords != null && !covidRecords.isEmpty();
    }
}
