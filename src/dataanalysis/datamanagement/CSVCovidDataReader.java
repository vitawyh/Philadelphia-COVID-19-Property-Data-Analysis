package dataanalysis.datamanagement;

import dataanalysis.util.CovidRecord;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Class responsible for reading COVID data from a CSV file.
 */
public class CSVCovidDataReader implements Reader {

    private final CSVFileReader reader;

    // Regex pattern to validate timestamp format: "YYYY-MM-DD hh:mm:ss"
    private static final Pattern TIMESTAMP_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$");

    public CSVCovidDataReader(CSVFileReader reader) {
        this.reader = reader;
    }

    /**
     * Reads and parses the COVID data CSV into a list of CovidRecord objects.
     */
    @Override
    public List<CovidRecord> getCovidData() {
        List<CovidRecord> covidData = new ArrayList<>();
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
            Integer partialIndex = headerMap.get("partially_vaccinated");
            Integer fullIndex = headerMap.get("fully_vaccinated");
            Integer timestampIndex = headerMap.get("etl_timestamp");

            // Ensure required fields exist in the header
            if (zipIndex == null || timestampIndex == null) {
                throw new IOException("Missing required headers: zip_code and/or etl_timestamp");
            }

            String[] row;
            // Process each row in the CSV file
            while ((row = reader.readRow()) != null) {

                // Skip rows that are too short to contain all required fields
                int maxIndex = Math.max(Math.max(zipIndex, timestampIndex),
                        Math.max(partialIndex != null ? partialIndex : 0,
                                fullIndex != null ? fullIndex : 0));
                if (row.length <= maxIndex) continue;

                // Validate and extract ZIP code
                String zip = row[zipIndex].trim();
                if (zip.isEmpty() || !zip.matches("\\d{5}")) {
                    continue;
                }

                // Validate and extract timestamp
                String etlTimestampStr = row[timestampIndex].trim();
                if (!TIMESTAMP_PATTERN.matcher(etlTimestampStr).matches()) {
                    continue;
                }

                // Parse vaccination numbers; use 0 if missing
                int partiallyVaccinated = partialIndex != null ? parseIntOrZero(row[partialIndex]) : 0;
                int fullyVaccinated = fullIndex != null ? parseIntOrZero(row[fullIndex]) : 0;

                // Create CovidRecord object and add to the list
                CovidRecord record = new CovidRecord(zip, etlTimestampStr, partiallyVaccinated, fullyVaccinated);
                covidData.add(record);
            }

        } catch (IOException e) {
            System.err.println("Error reading CSV: " + e.getMessage());
            e.printStackTrace();
        }

        return covidData;
    }

    /**
     * Helper function to parse a string to an integer.
     * Returns 0 if the string is empty or cannot be parsed.
     */
    private int parseIntOrZero(String s) {
        if (s == null || s.trim().isEmpty()) return 0;
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}