package edu.iastate.libcompat.parser;

import edu.iastate.libcompat.util.FileFilter;

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

        FileFilter filter = new FileFilter("/Users/nishanthsivakumar/Documents/libcompat/project_repo/maven_projects/");

        LOGGER.exiting(CLASS_NAME, METHOD_NAME);
        return filter.filterByFiletype(this.fileType);
    }

    public abstract void parseFiles();

}
