package dataanalysis.logging;

import java.io.FileOutputStream;
import java.io.PrintWriter;

public class LogFileWriter {

    // Singleton instance of this class
    private static LogFileWriter instance = new LogFileWriter();

    // init fields
    private static PrintWriter out;    // stores printer
    private static String logFilePath;     // store file path to log file
    private static int PrinterCreated;     // 0 if file printer not created, 1 if file, 2 if System.err
    private static FileOutputStream fos;

    // Private constructor for the LogFileWriter
    private LogFileWriter() {
        PrinterCreated = 0;
    }

    /**
     * Print file creator populates the otherwise NULL valued PrintWriter with an object which can write
     * to a log file
     * @param fileName String, filepath to the log file
     */
    private static void LogFilePrinterCreate(String fileName){
        try {
            fos = new FileOutputStream(fileName, true);
            out = new PrintWriter(fos);
            PrinterCreated = 1;
        }
        catch (Exception e) {
            System.out.println("Error: Unable to open log file '" + fileName + "' for writing.");
            PrinterCreated = 0;
        }
    }

    private static void LogFilePrinterCreate(){
        try {
            out = new PrintWriter(System.err);
            PrinterCreated = 2;
        }
        catch (Exception e) {
            System.out.println("Error: Unable to open write to System.err, canceling program.");
            PrinterCreated = 0;
        }
    }


    /**
     * writes string to log file
     * @param msg
     */
    // Requirement:
    // The logger should prepend each event with a timestamp, specifically the value returned by:
    // System.currentTimeMillis() followed by a space (“ ”, ASCII character 32), and then the message or event sent to the logger.
    // Timestamps should be printed before each message sent to the logger. These should be added by the logger and should not be part of the message sent by the caller.
    public void log(String msg) {
        String timestamped = System.currentTimeMillis() + " " + msg;
        out.println(timestamped);
        out.flush();
    }


    /**
     * writes string[] to log file
     * @param msgArray an Arrray of Strings, to be strung together in a single line in the log file
     */
    // Requirement:
    // The logger should prepend each event with a timestamp, specifically the value returned by:
    // System.currentTimeMillis() followed by a space (“ ”, ASCII character 32), and then the message or event sent to the logger.
    // Timestamps should be printed before each message sent to the logger. These should be added by the logger and should not be part of the message sent by the caller.
    public void log(String[] msgArray) {
        String singleStringMsgArray = String.join(" ", msgArray);
        String timestamped = System.currentTimeMillis() + " " + singleStringMsgArray;
        out.println(timestamped);
        out.flush();
    }

    // singleton accessor method
    public static LogFileWriter getInstance() {
        return instance;
    }


    // public exposed method to create printer for this class instance
    public boolean LogFilePrinterStart(String filePath){

        // update the log file path field of this class (static field)
        this.logFilePath = filePath;

        // If the last opened writer was for a file, close it
        if (PrinterCreated == 1) {
            out.close();
        }

        // if the file path is not null, pass it to the log file print creator - this attempts to append to a file
        // if the file path is null, then write to system.err
        if (this.logFilePath != null){
            LogFileWriter.LogFilePrinterCreate(logFilePath);
        } else {
            LogFileWriter.LogFilePrinterCreate();
        }

        return PrinterCreated != 0;
    }

}
