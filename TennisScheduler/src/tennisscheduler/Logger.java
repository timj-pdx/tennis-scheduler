
package tennisscheduler;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.File;
/**
 * Provide a Logging system for the Tennis Scheduler
 * Supports multiple levels of logging {Debug, Info, Error,None}.
 * The messages for a level can be sent to a the log file, stdout or both.
 * The "Error" level messages are always sent to stderr.
 * 
 * @author timj
 */
public class Logger {
    
    public enum LEVEL {Debug,Info,Error,None};
    
    private LEVEL       logLevel;
    private LEVEL       consoleLevel;
    private PrintStream logFileStream;
    
    Logger(String path, String logFileName) {
        // Remove the log directory
        File dir = new File(path);
        if (dir.exists()) {
            if (dir.isDirectory())
                for (File f : dir.listFiles())
                    f.delete();
            dir.delete();
        }
        dir.mkdir();
        // Open the log file
        logFileName = String.format("%s/%s", path, logFileName);
        FileOutputStream out;
        try {
            out           = new FileOutputStream(logFileName);
            logFileStream = new PrintStream(out);
        } catch (Exception e) {
            System.err.println ("Error in writing to file");
            System.exit(1);
        }
        logLevel     = TennisScheduler.schedProperties.getLogLevel();
        consoleLevel = LEVEL.Error;
        logFileStream.println("#### Logging Started ####");
    }
    /**
     * If the Err level has been enabled send the specified messages to the log file.
     * 
     * @param message The message to display
     */
    public void err(String message)   {printMessage(LEVEL.Error, message);}
    /**
     * If the Info level has been enabled send the specified messages to the log file.
     * 
     * @param message The message to display
     */
    public void info(String message)  {printMessage(LEVEL.Info,  message);}
    /**
     * If the Debug level has been enabled send the specified messages to the log file.
     * 
     * @param message The message to display
     */
    public void debug(String message) {printMessage(LEVEL.Debug, message);}
    /**
     * Specify the Level of messages to be sent to the log file.
     * 
     * @param level 
     */
    public void setFileLevel(LEVEL level)    {logLevel = level;}
    /**
     * Specify the Level of messages to be sent to the console. Error level
     * messages are alwent sent to stderr.
     * 
     * @param level 
     */
    public void setConsoleLevel(LEVEL level) {consoleLevel = level;}
    /**
     * If the Level is enabled, print the specified message to the console
     * and/or log file.
     * 
     * @param level
     * @param s 
     */
    private void printMessage(LEVEL level, String s) {
        String str;
        if (logLevel.compareTo(level) <= 0 || consoleLevel.compareTo(level) <= 0 ) {
            str = String.format("%-6s: %s%n", level.toString(), s);
            if (consoleLevel.compareTo(level) <= 0)
                if (level == LEVEL.Error)
                    System.err.print(str);
                else
                    System.out.print(str);
            if (logLevel.compareTo(level) <= 0)
                logFileStream.print(str);
        }
    }
}
