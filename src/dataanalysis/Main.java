
package dataanalysis;

import dataanalysis.logging.LogFileWriter;
import dataanalysis.processor.Processor;
import dataanalysis.ui.UserInterface;
import dataanalysis.datamanagement.*;


import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) {
        String covidFile = null;
        String populationFile = null;
        String propertyFile = null;
        String logFile = null;

        Set<String> seenArgs = new HashSet<>();
        Pattern pattern = Pattern.compile("^--(?<name>.+?)=(?<value>.+)$");
//test
        //another test
        // Argument parsing with validation
        for (String arg : args) {
            Matcher matcher = pattern.matcher(arg);
            if (!matcher.matches()) {
                System.out.println("Error: Invalid argument format '" + arg + "'. Expected format: --name=value");
                return;
            }

            String name = matcher.group("name").toLowerCase();
            String value = matcher.group("value");

            if (!Set.of("covid", "population", "properties", "log").contains(name)) {
                System.out.println("Error: Unknown argument name --" + name);
                return;
            }

            if (!seenArgs.add(name)) {
                System.out.println("Error: Duplicate argument --" + name);
                return;
            }

            switch (name) {
                case "covid":
                    covidFile = value;
                    break;
                case "population":
                    populationFile = value;
                    break;
                case "properties":
                    propertyFile = value;
                    break;
                case "test":
                    break;
                case "log":
                    logFile = value;
                    break;
            }
        }

        // init the logger and create a writer based on the LogFilePath argument
        // if null, writes to err
        LogFileWriter logger = LogFileWriter.getInstance();

        // handles logic for writing to file or err if logFile is null
        if (!logger.LogFilePrinterStart(logFile)) {
            System.out.println("Error: Failed to initialize logger.");
            return;
        }

        // Log the command line arguments as a single space-separated entry (requirement)
        if (args.length > 0) {
            logger.log(String.join(" ", args));
        }

        Reader covidReader = null;
        if (covidFile != null) {
            try {
                checkFileReadable(covidFile);
                if (covidFile.toLowerCase().endsWith(".csv")) {
                    covidReader = new CSVCovidDataReader(new CSVFileReader(new CharacterReader(covidFile)));
                } else if (covidFile.toLowerCase().endsWith(".json")) {
                    covidReader = new JSONCovidDataReader(covidFile);
                } else {
                    System.out.println("Error: Unknown COVID file format.");
                    return;
                }
                logger.log(covidFile);
            } catch (Exception e) {
                System.out.println("Error opening COVID file: " + e.getMessage());
                return;
            }
        }

        PopulationReader populationReader = null;
        if (populationFile != null) {
            try {
                checkFileReadable(populationFile);
                populationReader = new PopulationReader(new CSVFileReader(new CharacterReader(populationFile)));
                logger.log(populationFile);
            } catch (Exception e) {
                System.out.println("Error opening population file: " + e.getMessage());
                return;
            }
        }

        PropertyReader propertyReader = null;
        if (propertyFile != null) {
            try {
                checkFileReadable(propertyFile);
                propertyReader = new PropertyReader(new CSVFileReader(new CharacterReader(propertyFile)));
                logger.log(propertyFile);
            } catch (Exception e) {
                System.out.println("Error opening property file: " + e.getMessage());
                return;
            }
        }

        Processor processor = new Processor(covidReader, populationReader, propertyReader);
        UserInterface ui = new UserInterface(processor);
        ui.run();
    }

    private static void checkFileReadable(String filename) throws IOException {
        File file = new File(filename);
        if (!file.exists()) throw new IOException("File does not exist: " + filename);
        if (!file.canRead()) throw new IOException("File not readable: " + filename);
    }
}