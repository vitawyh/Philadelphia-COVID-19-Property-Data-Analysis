package dataanalysis.datamanagement;

import dataanalysis.util.CovidRecord;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class reads COVID data from a JSON file and converts it into a list of CovidRecord objects.
 */
public class JSONCovidDataReader implements Reader {

    private final String fileName;

    public JSONCovidDataReader(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Parses the JSON file and returns a list of valid CovidRecord entries.
     */
    @Override
    public List<CovidRecord> getCovidData() {
        List<CovidRecord> covidData = new ArrayList<>();
        JSONParser parser = new JSONParser();

        // Regex to validate timestamp format: YYYY-MM-DD HH:MM:SS
        String timestampRegex = "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$";

        try (FileReader reader = new FileReader(fileName)) {

            // Parse the JSON file into an array of objects
            JSONArray jsonArray = (JSONArray) parser.parse(reader);

            // Iterate over each object in the array
            for (Object obj : jsonArray) {
                if (obj instanceof JSONObject) {
                    JSONObject covidObj = (JSONObject) obj;

                    try {
                        // Extract and validate ZIP code
                        String zipCode = String.valueOf(covidObj.get("zip_code"));
                        if (!zipCode.matches("\\d{5}")) continue;

                        // Extract and validate timestamp
                        String etlTimestampStr = (String) covidObj.get("etl_timestamp");
                        if (etlTimestampStr == null || !etlTimestampStr.matches(timestampRegex)) continue;

                        // Extract vaccinated count (default to 0 if missing)
                        int partiallyVaccinated = covidObj.get("partially_vaccinated") != null
                                ? ((Long) covidObj.get("partially_vaccinated")).intValue()
                                : 0;

                        int fullyVaccinated = covidObj.get("fully_vaccinated") != null
                                ? ((Long) covidObj.get("fully_vaccinated")).intValue()
                                : 0;

                        // Create and store the valid CovidRecord
                        CovidRecord record = new CovidRecord(zipCode, etlTimestampStr, partiallyVaccinated, fullyVaccinated);
                        covidData.add(record);

                    } catch (Exception e) {
                        // skip invalid record
                    }
                }
            }
        } catch (IOException | ParseException e) {
            throw new RuntimeException("Failed to read JSON file: " + fileName, e);
        }
        return covidData;
    }
}

