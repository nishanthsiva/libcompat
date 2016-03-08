package edu.iastate.libcompat.parser;

import edu.iastate.libcompat.constants.StringConstants;
import edu.iastate.libcompat.util.StringUtil;

import java.io.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by nishanthsivakumar on 2/29/16.
 */
public class GradleDependencyParser extends DependencyParser {
    Logger LOGGER = Logger.getLogger(GradleDependencyParser.class.getName());
    private final String CLASS_NAME = GradleDependencyParser.class.getName();

    public GradleDependencyParser(){
        super("build.gradle");
        super.setLoggerLevel(LOGGER, Level.FINE);

    }


    @Override
    public void parseFiles(){
        final String METHOD_NAME = "parseFiles";
        LOGGER.entering(CLASS_NAME, METHOD_NAME);

        String[] files = getFilesByType();

        for(String filename: files){
            LOGGER.log(Level.FINE, "Parsing File - "+filename);
            try{
                FileReader fileReader = new FileReader(new File(filename));
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                String dependencyBlock = getDependencyBlock(bufferedReader);
                LOGGER.log(Level.FINER, "Dependency Block - \n"+dependencyBlock);


            }catch(Exception e){
                LOGGER.log(Level.WARNING, e.getMessage());
                e.printStackTrace();
            }


        }


        LOGGER.exiting(CLASS_NAME, METHOD_NAME);
    }

    private String getDependencyBlock(BufferedReader bufferedReader) throws IOException {
        final String METHOD_NAME = "getDependencyBlock";
        LOGGER.entering(CLASS_NAME, METHOD_NAME);

        StringBuffer dependencyBlock = new StringBuffer();
        boolean blockStartFound = false;
        int bracesCount = 0;
        while(bufferedReader.ready()){
            String line = bufferedReader.readLine();
            if(line.toLowerCase().contains(StringConstants.GRD_BLK_NAME_DEPENDENCIES)){
                LOGGER.log(Level.FINEST, "Dependency block found" );
                dependencyBlock.append(line+"\n");
                blockStartFound = true;
                bracesCount += StringUtil.getUnmatchedBracesCount(line+"\n");
                if(bracesCount != 0)
                    continue;
            }
            if(blockStartFound && bracesCount != 0){
                bracesCount += StringUtil.getUnmatchedBracesCount(line);
                dependencyBlock.append(line+"\n");
            }
            if(blockStartFound && bracesCount == 0)
                break;

        }

        LOGGER.exiting(CLASS_NAME, METHOD_NAME);
        return dependencyBlock.toString();
    }

    public static void main(String args[]){
        DependencyParser dp = new GradleDependencyParser();
        dp.parseFiles();
    }
}
