package edu.iastate.libcompat.util;

import java.util.logging.Logger;

/**
 * Created by nishanthsivakumar on 3/7/16.
 */
public class StringUtil {
    Logger LOGGER = Logger.getLogger(StringUtil.class.getName());
    private final String CLASS_NAME = StringUtil.class.getName();

    public String upgradeMinorVersionString(String versionString){
        final String METHOD_NAME = "updateMinorVersionString";
        LOGGER.entering(CLASS_NAME, METHOD_NAME);

        StringBuffer updatedVersion = new StringBuffer();
        if(versionString.contains(".")){
            String[] tokens = versionString.split(".");
            for(int i=0 ;i<tokens.length-1;i++){
                updatedVersion.append(tokens[i]+".");
            }
            updatedVersion.append("x");
        }
        LOGGER.exiting(CLASS_NAME, METHOD_NAME);
        return updatedVersion.toString();
    }

    public String upgradeMajorVersionString(String versionString){
        final String METHOD_NAME = "upgradeMajorVersionString";
        LOGGER.entering(CLASS_NAME, METHOD_NAME);

        StringBuffer updatedVersion = new StringBuffer();
        if(versionString.contains(".")){
            String[] tokens = versionString.split(".");
            for(int i=0 ;i<tokens.length-2;i++){
                updatedVersion.append(tokens[i]+".");
            }
            updatedVersion.append("x.");
            updatedVersion.append(tokens[tokens.length-1]);
        }
        LOGGER.exiting(CLASS_NAME, METHOD_NAME);
        return updatedVersion.toString();
    }


}
