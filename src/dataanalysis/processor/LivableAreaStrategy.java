package dataanalysis.processor;

import dataanalysis.util.Property;

// TODO: David to check the new logic
public class LivableAreaStrategy implements AverageCalculationStrategy {

    /**
     * Extracts the total livable area of the given property.
     *
     * @param property The property from which to extract the value.
     * @return The total livable area of the property, or 0 if the value is null.
     */
    @Override
    public int extractValue(Property property) {
        Integer area = property.getTotalLivableArea();
        return (area != null) ? area : 0; // Return 0 if livable area is not provided
    }
}