package dataanalysis.datamanagement;

import dataanalysis.util.Population;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PopulationReader is responsible for reading population data from a CSV file
 * and converting it into a list of Population objects.
 */
public class PopulationReader {

    private final CSVFileReader reader;

    public PopulationReader(CSVFileReader reader) {
        this.reader = reader;
    }

    /**
     * Reads the population data from the CSV file.
     */
    public List<Population> getPopulationData() {

        List<Population> populationData = new ArrayList<>();
        Map<String, Integer> headerMap = new HashMap<>();

        try {
            // Read and validate header row
            String[] header = reader.readRow();
            if (header == null) {
                throw new IOException("CSV file is empty or missing header.");
            }

            // Map column names to their indices (case-insensitive)
            for (int i = 0; i < header.length; i++) {
                headerMap.put(header[i].trim().toLowerCase(), i);
            }

            // Get column indices for required fields
            Integer zipIndex = headerMap.get("zip_code");
            Integer populationIndex = headerMap.get("population");

            // Ensure required fields exist in the header
            if (zipIndex == null || populationIndex == null) {
                throw new IOException("Missing required headers: zip_code and/or population");
            }

            String[] row;
            // Process each row in the CSV file
            while ((row = reader.readRow()) != null) {

                // Skip rows that are too short to contain all required fields
                if (row.length <= Math.max(zipIndex, populationIndex)) continue;

                String zip = row[zipIndex].trim();
                String popStr = row[populationIndex].trim();

                // Validate and extract ZIP code
                if (!zip.matches("\\d{5}")) continue;

                try {
                    // Parse population and add to the result list
                    int population = Integer.parseInt(popStr);
                    populationData.add(new Population(zip, population));
                } catch (NumberFormatException e) {
                    // Skip invalid population values
                }
            }

        } catch (IOException e) {
            System.err.println("Error reading CSV: " + e.getMessage());
            e.printStackTrace();
        }
        return populationData;
    }
}
