package edu.iastate.libcompat.parser;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by nishanthsivakumar on 3/22/16.
 */
public class DocumentationDependencyParser extends DependencyParser {

    private Logger LOGGER = Logger.getLogger(DocumentationDependencyParser.class.getName());
    private String CLASS_NAME = DocumentationDependencyParser.class.getName();

    public DocumentationDependencyParser(){
        super("README.md");
        super.setLoggerLevel(LOGGER, Level.FINE);
    }
    @Override
    public void parseFiles() {
        final String METHOD_NAME = "parseFiles";
        LOGGER.entering(CLASS_NAME, METHOD_NAME);

        String[] filenames = getFilesByType();
        LOGGER.log(Level.INFO,filenames.length+" Files found!");
        int count = 0;
        for(String filename: filenames){
            StringBuffer bufLines = new StringBuffer();
            File file = new File(filename);
            FileReader fileReader = null;
            try {
                fileReader = new FileReader(file);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                while(bufferedReader.ready()){
                    String tempLine = bufferedReader.readLine();
                    if(tempLine.toLowerCase().contains("incompatib") || tempLine.toLowerCase().contains("not compatib")){
                        count++;
                        break;
                    }
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, e.getMessage());
                e.printStackTrace();
            }

        }
        LOGGER.log(Level.INFO,count+" Files have incompatibility information");
        LOGGER.exiting(CLASS_NAME, METHOD_NAME);

    }

    public static void main(String arg[]){
        DependencyParser dependencyParser = new DocumentationDependencyParser();
        dependencyParser.parseFiles();
    }
}
