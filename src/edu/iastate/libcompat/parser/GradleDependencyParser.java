package edu.iastate.libcompat.parser;

import java.util.logging.Logger;

/**
 * Created by nishanthsivakumar on 2/29/16.
 */
public class GradleDependencyParser extends DependencyParser {
    Logger LOGGER = Logger.getLogger(GradleDependencyParser.class.getName());
    private final String CLASS_NAME = GradleDependencyParser.class.getName();

    public GradleDependencyParser(String fileType){
        super(fileType);
    }

    @Override
    public void parseFiles(){
        final String METHOD_NAME = "parseFiles";
        LOGGER.entering(CLASS_NAME, METHOD_NAME);

        String[] files = getFilesByType();

        LOGGER.exiting(CLASS_NAME, METHOD_NAME);
    }
}
