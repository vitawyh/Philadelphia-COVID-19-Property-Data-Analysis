package dataanalysis.datamanagement;

import dataanalysis.util.Property;

import java.io.IOException;
import java.util.*;

/**
 * PropertyReader reads a CSV file containing property data
 * and returns a list of valid Property objects.
 */
public class PropertyReader {

    private final CSVFileReader reader;

    public PropertyReader(CSVFileReader reader) {
        this.reader = reader;
    }

    /**
     * Reads and parses property data from the CSV file.
     */
    public List<Property> getPropertyData() {
        List<Property> properties = new ArrayList<>();
        Map<String, Integer> headerMap = new HashMap<>();

        try {
            // Read and validate header row
            String[] header = reader.readRow();
            if (header == null) throw new IOException("CSV file is empty or missing header.");

            // Map column names to their indices (case-insensitive)
            for (int i = 0; i < header.length; i++) {
                headerMap.put(header[i].trim().toLowerCase(), i);
            }

            // Get column indices for required fields
            Integer zipIndex = headerMap.get("zip_code");
            Integer valueIndex = headerMap.get("market_value");
            Integer areaIndex = headerMap.get("total_livable_area");

            // Ensure required fields exist in the header
            if (zipIndex == null || valueIndex == null || areaIndex == null) {
                throw new IOException("Missing required headers: zip_code, market_value, total_livable_area");
            }

            String[] row;
            // Process each row in the CSV file
            while ((row = reader.readRow()) != null) {

                // Skip rows that are too short to contain all required fields
                if (row.length <= Math.max(zipIndex, Math.max(valueIndex, areaIndex))) continue;

                // Extract and validate a 5-digit ZIP code
                String zipRaw = row[zipIndex].trim();
                String zip = extractValidZip(zipRaw);
                if (zip == null) continue; // skip malformed zip

                // Extract market value and livable area fields
                String marketStr = row[valueIndex].trim();
                String areaStr = row[areaIndex].trim();

                // Convert to nullable integers
                Integer marketValue = parseNullableInt(marketStr);
                Integer livableArea = parseNullableInt(areaStr);

                // Create a Property object and add it to the result list
                Property property = new Property(zip, marketValue, livableArea);
                properties.add(property);
            }

        } catch (IOException e) {
            System.err.println("Error reading property CSV: " + e.getMessage());
        }

        return properties;
    }

    /**
     * Extracts the first 5 characters from a ZIP string if valid (exactly 5 digits).
     * @param rawZip the raw ZIP code string from the file
     * @return a valid 5-digit ZIP code, or null if invalid
     */
    private String extractValidZip(String rawZip) {
        if (rawZip == null || rawZip.length() < 5) return null;

        String zip = rawZip.substring(0, 5);
        if (!zip.matches("^\\d{5}")) return null;

        return zip;
    }

    /**
     * Parses a string into an Integer if valid, or returns null if empty.
     *
     * @param value the string value to parse
     * @return Integer value or null
     */
    private Integer parseNullableInt(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return (int) Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
