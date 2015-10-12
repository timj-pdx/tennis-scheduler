/*
 * Section Class (Extends Properties Class)
 * 
 * This functionality allows duplicate entries between 'Sections' but not
 * within them.
 * 
 * Example property file with 'Sections':
 *      # Comments are supported
 *      # Extra whitespace is ignored
 * 
 *      [Section1]
 *      name1 value
 *      name2 value
 * 
 *      [Section2]
 *      name1 value
 *      name2 value
 * 
 * Example call:
 *     Section sect  = new Section ("PropertyFile","SectionName");
 *     System.out.println(sect.getProp().getProperty("Property"));
 * 
 */

package tennisscheduler;

import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Properties;
/**
 * Section Class (Extends Properties Class)
 * 
 * Adds 'sections' to a property file.
 * 
 * The specified 'section' is extracted from the specified property file and
 * written to a temporary file. A 'Property' handle is then created for the
 * temporary properties file and the file is removed.
 * 
 * @author timj
 */
public class Section extends Properties {
    
    private String     propFile;
    private String     sectName;
    private Properties prop;
    private Logger     log       = TennisScheduler.log;
    
// Constructor
    Section(String s, String p) {
        propFile = p;
        sectName = s;
        String  strLine;
        String  reStart = String.format("^\\s*\\[%s\\]\\s*$",sectName);
        String  reEnd   = String.format("\\s*^\\[.*\\]\\s*$");
        boolean found   = false;
        File tmpFile;
        try {
            FileInputStream is = new FileInputStream(propFile);
            BufferedReader  br = new BufferedReader(new InputStreamReader(is));
            // For some dumb reason, 'prefix' must be at least 3 characters long
            tmpFile = File.createTempFile(sectName+"tj", ".tmp");
            BufferedWriter bw = new BufferedWriter(new FileWriter(tmpFile));
            tmpFile.deleteOnExit();
            while ((strLine = br.readLine()) != null) {
                if (strLine.matches(reStart)) {
                    //System.err.println(strLine);
                    found = true;
                    while ((strLine = br.readLine()) != null) {
                        if (strLine.matches(reEnd)) break;
                        bw.write(strLine);
                        bw.newLine();
                    }
                }
                if (found) break;
            }
            br.close();
            bw.close();
            if (!found) {
                log.err("Section() Could not find section: '" + sectName + "' in " + propFile);
                System.exit(1);
            }
            prop = new Properties();
            prop.load(new FileInputStream(tmpFile.getPath()));
            tmpFile.delete();
        } catch (Exception e) {
            System.err.println("Section() Error: " + e.getMessage());
        }
    }
    /**
     * Get the Property for the Section
     * 
     * @return The Property Class
     */
    public Properties getProp() {return prop;}
    
}
