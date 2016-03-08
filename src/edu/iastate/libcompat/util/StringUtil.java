package edu.iastate.libcompat.util;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by nishanthsivakumar on 3/7/16.
 */
public class StringUtil {
    private static Logger LOGGER = Logger.getLogger(StringUtil.class.getName());
    private static final String CLASS_NAME = StringUtil.class.getName();

    public static String upgradeVersionString(String versionString){
        final String METHOD_NAME = "updateMinorVersionString";
        LOGGER.entering(CLASS_NAME, METHOD_NAME);

        if(versionString == null){
            return null;
        }
        StringBuffer updatedVersion = new StringBuffer();
        if(versionString.contains("~")){
            versionString = versionString.replaceAll("~","");
            if(versionString.contains(".")){
                String[] tokens = versionString.split("\\.");
                for(int i=0 ;i<tokens.length-1;i++){
                    updatedVersion.append(tokens[i]+".");
                }
                updatedVersion.append("x");
            }
        }else if(versionString.contains("^")){
            versionString = versionString.replaceAll("^","");
            if(versionString.contains(".")){
                String[] tokens = versionString.split("\\.");
                for(int i=0 ;i<tokens.length-2;i++){
                    updatedVersion.append(tokens[i]+".");
                }
                updatedVersion.append("x.");
                updatedVersion.append(tokens[tokens.length-1]);
            }
        }else{
            updatedVersion.append(versionString);
        }

        LOGGER.exiting(CLASS_NAME, METHOD_NAME);
        return updatedVersion.toString();
    }

}
