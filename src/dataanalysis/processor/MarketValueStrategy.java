package dataanalysis.processor;

import dataanalysis.util.Property;

//TODO: David to check the new logic
public class MarketValueStrategy implements AverageCalculationStrategy {

    /**
     * Extracts the market value of the given property.
     *
     * @param property The property from which to extract the value.
     * @return The market value of the property, or 0 if the value is null.
     */
    @Override
    public int extractValue(Property property) {
        Integer value = property.getMarketValue();
        return (value != null) ? value : 0; // Default to 0 if no market value is provided
    }
}