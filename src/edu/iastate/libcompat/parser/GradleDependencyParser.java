package edu.iastate.libcompat.parser;

import edu.iastate.libcompat.beans.DependencyBean;
import edu.iastate.libcompat.beans.PackageBean;
import edu.iastate.libcompat.constants.StringConstants;
import edu.iastate.libcompat.util.StringUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
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
                //
                FileReader fileReader = new FileReader(new File(filename));
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                String dependencyBlock = getDependencyBlock(bufferedReader);
                bufferedReader.close();
                fileReader.close();
                //TODO Find a way to extract package details
                LOGGER.log(Level.FINER, "Dependency Block - \n"+dependencyBlock);
                String[] depLines = dependencyBlock.split("\n");
                List<String> dependencyStrings = new ArrayList<String>();
                for(String dep : depLines){
                    LOGGER.log(Level.FINEST,dep);
                    dep = StringUtil.getQuotedText(dep);
                    if(dep != null)
                       dependencyStrings.add(dep);
                }
                List<DependencyBean> dependencyList  = getDependencyList(dependencyStrings);
                //store dependencyList

            }catch(Exception e){
                LOGGER.log(Level.WARNING, e.getMessage());
                e.printStackTrace();
            }


        }


        LOGGER.exiting(CLASS_NAME, METHOD_NAME);
    }

    private List<DependencyBean> getDependencyList(List<String> dependencyStrings) {
        final String METHOD_NAME = "getDependencyList";
        LOGGER.entering(CLASS_NAME,METHOD_NAME);

        List<DependencyBean> dependencyList = new ArrayList<DependencyBean>();
        for(String dep: dependencyStrings){
            if(dep.contains(":")){
                String[] tokens = dep.split(":");
                if(tokens.length == 3){
                    PackageBean packageBean = new PackageBean();
                    packageBean.setName(tokens[1]);
                    packageBean.setDescription(tokens[0]);
                    /*if(StringUtil.isVersionString(tokens[2])){
                        packageBean.setVersion(tokens[2]);
                    }*/
                    //TODO remove the comment line above and remove the line below
                    packageBean.setVersion(tokens[2]);
                    DependencyBean dependencyBean = new DependencyBean();
                    dependencyBean.setPackageBean(packageBean);
                    dependencyBean.setType(3);
                    //TODO populate optional parameter for dependencyBean
                    dependencyList.add(dependencyBean);
                }
            }
        }
        LOGGER.exiting(CLASS_NAME, METHOD_NAME);
        return dependencyList;

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
