package edu.iastate.libcompat.util;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

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

    public static int getUnmatchedBracesCount(String line){
        final String METHOD_NAME = "getBracesCount";
        LOGGER.entering(CLASS_NAME, METHOD_NAME);

        int bracesCount = 0;
        for(int i=0;i<line.length();i++){
            if(line.charAt(i) == '{')
                bracesCount++;
            if(line.charAt(i) == '}')
                bracesCount--;
        }

        LOGGER.exiting(CLASS_NAME, METHOD_NAME);
        return bracesCount;
    }

   public static String getQuotedText(String line){
       final String METHOD_NAME = "getQuotedText";
       LOGGER.entering(CLASS_NAME, METHOD_NAME);

       String quotedText = null;
       int startIndex = -1;
       int endIndex = -1;
       if(line.contains("\'") ){
           startIndex = line.indexOf("\'");
           endIndex = line.lastIndexOf("\'");
           if(startIndex != -1 && endIndex != -1){
               if(startIndex != endIndex)
                   quotedText = line.substring(startIndex+1,endIndex);
           }

       }else if(line.contains("\"")){
           startIndex = line.indexOf("\"");
           endIndex = line.lastIndexOf("\"");
           if(startIndex != -1 && endIndex != -1){
               if(startIndex != endIndex)
                   quotedText = line.substring(startIndex+1,endIndex);
           }
       }
       LOGGER.exiting(CLASS_NAME, METHOD_NAME);
       return quotedText;
   }

    public static boolean isVersionString(String versionString){
        final String METHOD_NAME = "isVersionString";
        LOGGER.entering(CLASS_NAME, METHOD_NAME);

        boolean isVersionString = false;
        //TODO write proper regex for this check
        if(Pattern.matches("[0-9].[0-9].[0-9]",versionString)){
            isVersionString = true;
        }
        LOGGER.exiting(CLASS_NAME, METHOD_NAME);
        return isVersionString;
    }

}
