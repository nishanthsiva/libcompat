package edu.iastate.libcompat.parser;

import edu.iastate.libcompat.util.FileFilter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by nishanthsivakumar on 2/29/16.
 */
public abstract class DependencyParser {

    private Logger LOGGER = Logger.getLogger(DependencyParser.class.getName());
    private final String CLASS_NAME = DependencyParser.class.getName();
    public String fileType;

    public DependencyParser(String fileType){
        this.fileType = fileType;
    }

    public String[] getFilesByType(){
        final String METHOD_NAME = "getFilesByType";
        LOGGER.entering(CLASS_NAME, METHOD_NAME);

        String[] fileNames = null;
        try {
            File file = new File("libcompat.properties");
            FileInputStream fileInput = new FileInputStream(file);
            Properties properties = new Properties();
            properties.load(fileInput);
            fileInput.close();

            StringBuffer repoPath = new StringBuffer();
            repoPath.append(properties.getProperty(fileType,""));
            FileFilter filter = new FileFilter(repoPath.toString());
            fileNames = filter.filterByFiletype(this.fileType);

        } catch (FileNotFoundException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
            e.printStackTrace();
        }



        LOGGER.exiting(CLASS_NAME, METHOD_NAME);
        return fileNames;
    }

    public abstract void parseFiles();

}
