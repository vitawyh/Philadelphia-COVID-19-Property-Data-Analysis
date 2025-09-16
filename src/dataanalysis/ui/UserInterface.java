package dataanalysis.ui;

import dataanalysis.logging.LogFileWriter;
import dataanalysis.processor.Processor;

import java.util.Map;
import java.util.Scanner;

public class UserInterface {

    private final Processor processor;
    private final Scanner scanner = new Scanner(System.in);
    private final LogFileWriter logger = LogFileWriter.getInstance();

    public UserInterface(Processor processor) {
        this.processor = processor;
    }

    /**
     * Main method to start the user interface.
     * Displays the menu, prompts for input, and processes user actions in a loop.
     */
    public void run() {
        printMenu();
        while (true) {
            System.out.print("> ");  // Prompt symbol for input
            System.out.flush();
            String input = scanner.nextLine().trim();

            // Validate input: must be a single digit from 0 to 7
            if (!input.matches("[0-7]")) {
                System.out.println("Invalid input. Please enter a number between 0 and 7.");
                continue;
            }

            int choice = Integer.parseInt(input);

            // Execute corresponding action based on user choice
            switch (choice) {
                case 0: // Exit program
                    return;
                case 1: // Show available actions
                    showAvailableActions();
                    break;
                case 2: // Show total population for all ZIP Codes
                    showTotalPopulation();
                    break;
                case 3: // Show total vaccinations per capita for each ZIP Code for specified date
                    showVaccinationsPerCapita();
                    break;
                case 4: // Show average market value for properties in a specified ZIP Code
                    showAverageMarketValue();
                    break;
                case 5: // Show average livable area for properties in a specified ZIP Code
                    showAverageLivableArea();
                    break;
                case 6: // Show total market value per capita for a specified ZIP Code
                    showMarketValuePerCapita();
                    break;
                case 7: // Show health risk index for a specified ZIP Code
                    showHealthRiskIndex();
                    break;
                default:
                    System.out.println("Invalid selection.");
                    break;
            }

            // Reprint menu after action completes
            printMenu();
        }
    }

    /**
     * Prints the main menu options to the user.
     */
    private void printMenu() {
        System.out.println("0. Exit the program.");
        System.out.println("1. Show the available actions.");
        System.out.println("2. Show the total population for all ZIP Codes.");
        System.out.println("3. Show the total vaccinations per capita for each ZIP Code for the specified date.");
        System.out.println("4. Show the average market value for properties in a specified ZIP Code.");
        System.out.println("5. Show the average total livable area for properties in a specified ZIP Code.");
        System.out.println("6. Show the total market value of properties, per capita, for a specified ZIP Code.");
        System.out.println("7. Show the health risk index for a specified ZIP Code.");
    }

    /**
     * Displays the available actions based on the data loaded.
     * Uses the helper method to wrap output between BEGIN OUTPUT and END OUTPUT.
     */
    private void showAvailableActions() {
        printBeginEndOutput(() -> {
            System.out.println("0");
            System.out.println("1");
            if (processor.hasPopulationData()) System.out.println("2");
            if (processor.hasCovidData() && processor.hasPopulationData()) System.out.println("3");
            if (processor.hasPropertyData()) System.out.println("4");
            if (processor.hasPropertyData()) System.out.println("5");
            if (processor.hasPropertyData() && processor.hasPopulationData()) System.out.println("6");
            if (processor.hasPropertyData() && processor.hasPopulationData() && processor.hasCovidData()) System.out.println("7");
        });
    }

    /**
     * Displays the total population across all ZIP Codes.
     * If population data is unavailable, outputs an error message.
     */
    private void showTotalPopulation() {
        if (!processor.hasPopulationData()) {
            printBeginEndOutput(() -> System.out.println("Population data not available."));
            return;
        }
        printBeginEndOutput(() -> System.out.println(processor.getTotalPopulation()));
    }

    /**
     * Displays total vaccinations per capita by ZIP Code for a user-specified date and vaccination type.
     * Prompts the user for vaccination type and date (validated via promptForDate()).
     * Outputs the result or an error message if required data is unavailable.
     */
    private void showVaccinationsPerCapita() {
        if (!processor.hasCovidData() || !processor.hasPopulationData()) {
            printBeginEndOutput(() -> System.out.println("Vaccination or population data not available."));
            return;
        }

        // Prompt manually for 'partial' or 'full' (no helper used here)
        String type;
        while (true) {
            System.out.println("Enter 'partial' or 'full':");
            System.out.print("> ");
            System.out.flush();
            type = scanner.nextLine().trim().toLowerCase();
            logger.log(type);
            if (type.equals("partial") || type.equals("full")) {
                break;
            }
            System.out.println("Invalid input.");
        }

        // Use helper method for validated date input
        String date = promptForDate();

        // Fetch and print results
        Map<String, Double> result = processor.getVaccinationPerCapita(type, date);
        printBeginEndOutput(() -> {
            if (result.isEmpty()) {
                System.out.println("0");
            } else {
                for (Map.Entry<String, Double> entry : result.entrySet()) {
                    System.out.printf("%s %.4f%n", entry.getKey(), entry.getValue());
                }
            }
        });
    }

    /**
     * Displays the average market value of properties for a user-specified ZIP Code.
     * If property data is missing, outputs an error message.
     */
    private void showAverageMarketValue() {
        if (!processor.hasPropertyData()) {
            printBeginEndOutput(() -> System.out.println("Property data not available."));
            return;
        }

        String zip = promptForZip();
        printBeginEndOutput(() -> System.out.println(processor.calculateAverageMarketValue(zip)));
    }

    /**
     * Displays the average total livable area of properties for a user-specified ZIP Code.
     * If property data is missing, outputs an error message.
     */
    private void showAverageLivableArea() {
        if (!processor.hasPropertyData()) {
            printBeginEndOutput(() -> System.out.println("Property data not available."));
            return;
        }

        String zip = promptForZip();
        printBeginEndOutput(() -> System.out.println(processor.calculateAverageLivableArea(zip)));
    }

    /**
     * Displays the total market value of properties per capita for a user-specified ZIP Code.
     * Outputs an error message if required data is missing.
     */
    private void showMarketValuePerCapita() {
        if (!processor.hasPopulationData() || !processor.hasPropertyData()) {
            printBeginEndOutput(() -> System.out.println("Required data not available."));
            return;
        }

        String zip = promptForZip();
        int result = processor.calculateMarketValuePerCapita(zip);
        printBeginEndOutput(() -> System.out.println(result));
    }

    /**
     * Displays the health risk index for each ZIP Code on a user-specified date.
     * Outputs an error message if required data is missing.
     */
    private void showHealthRiskIndex() {
        if (!processor.hasPopulationData() || !processor.hasPropertyData() || !processor.hasCovidData()) {
            printBeginEndOutput(() -> System.out.println("Required data not available."));
            return;
        }

        String date = promptForDate();
        Map<String, Double> result = processor.getHealthRiskIndex(date);
        printBeginEndOutput(() -> {
            for (Map.Entry<String, Double> entry : result.entrySet()) {
                System.out.printf("%s %.4f%n", entry.getKey(), entry.getValue());
            }
        });
    }

    /**
     * Helper function to prompt the user to enter a date in the format YYYY-MM-DD.
     * Continues prompting until a valid date format is entered.
     *
     * @return the valid date string entered by the user
     */
    private String promptForDate() {
        while (true) {
            System.out.println("Enter date in format YYYY-MM-DD:");
            System.out.print("> ");
            System.out.flush();
            String date = scanner.nextLine().trim();
            logger.log(date);
            if (date.matches("\\d{4}-\\d{2}-\\d{2}")) {
                return date;
            }
            System.out.println("Invalid date format.");
        }
    }

    /**
     * Helper function to prompt the user to enter a valid 5-digit ZIP Code.
     * Continues prompting until a valid ZIP Code is entered.
     *
     * @return the valid ZIP Code string entered by the user
     */
    private String promptForZip() {
        while (true) {
            System.out.println("Enter a 5-digit ZIP Code:");
            System.out.print("> ");
            System.out.flush();
            String zip = scanner.nextLine().trim();
            logger.log(zip);
            if (zip.matches("\\d{5}")) {
                return zip;
            }
            System.out.println("Invalid ZIP Code.");
        }
    }

    /**
     * Helper method to print "BEGIN OUTPUT" and "END OUTPUT" markers around the output
     * generated by the given Runnable task. This ensures output formatting for automated evaluation.
     *
     * @param task a Runnable containing the code that prints output
     */
    private void printBeginEndOutput(Runnable task) {
        System.out.println();
        System.out.println("BEGIN OUTPUT");
        task.run();
        System.out.println("END OUTPUT");
    }
}