package edu.iastate.libcompat.parser;

import edu.iastate.libcompat.beans.DependencyBean;
import edu.iastate.libcompat.beans.PackageBean;
import edu.iastate.libcompat.constants.StringConstants;
import edu.iastate.libcompat.util.StringUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by nishanthsivakumar on 3/19/16.
 */
public class BrewDependencyParser extends DependencyParser {

    private final Logger LOGGER = Logger.getLogger(BrewDependencyParser.class.getName());
    private final String CLASS_NAME = BrewDependencyParser.class.getName();


    public BrewDependencyParser(){
        super(".rb");
        super.setLoggerLevel(LOGGER, Level.FINE);

    }
    @Override
    public void parseFiles() {
        final String METHOD_NAME = "parseFiles";
        LOGGER.entering(CLASS_NAME, METHOD_NAME);

        String[] files = getFilesByType();

        for(String filename: files) {
            LOGGER.log(Level.FINE, "Parsing File - " + filename);
            try {
                File file = new File(filename);
                FileReader fileReader = new FileReader(file);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                PackageBean packageBean = new PackageBean();
                packageBean.setName(file.getName().replace(".rb",""));
                LOGGER.log(Level.FINE, "Package name - "+packageBean.getName());
                List<String> dependencyLines = new ArrayList<String>();
                String urlLine = null;
                while(bufferedReader.ready()){
                    String line = bufferedReader.readLine();
                    line = line.trim();
                    LOGGER.log(Level.FINEST,line);
                    if(line.toLowerCase().contains(StringConstants.BREW_KEYWORD_DEPENDS_ON)){
                        dependencyLines.add(line);
                    }
                    if(line.toLowerCase().split(" ")[0].contains(StringConstants.BREW_KEYWORD_URL) && urlLine == null){
                        urlLine = line;
                    }
                    if(line.toLowerCase().split(" ")[0].contains(StringConstants.BREW_KEYWORD_DESC)){
                        packageBean.setDescription(StringUtil.getQuotedText(line));
                    }
                }
                populatePackageDetails(packageBean, StringUtil.getQuotedText(urlLine));
                LOGGER.log(Level.FINE, "Package Version -"+packageBean.getVersion());
                List<DependencyBean> dependencyList = getDependencyList(dependencyLines);

            } catch (Exception e) {
                LOGGER.log(Level.WARNING, e.getMessage());
                e.printStackTrace();
                break;
            }
        }

        LOGGER.exiting(CLASS_NAME, METHOD_NAME);
    }

    private List<DependencyBean> getDependencyList(List<String> dependencyLines) {
        final String METHOD_NAME = "getDependencyList";
        LOGGER.entering(CLASS_NAME, METHOD_NAME);

        List<DependencyBean> dependencyList = new ArrayList<DependencyBean>();
        for(String depLine : dependencyLines){
            String[] tokens = depLine.split(" ");
            if(tokens[0].toLowerCase().equals(StringConstants.BREW_KEYWORD_DEPENDS_ON)){
                DependencyBean dependencyBean = new DependencyBean();
                PackageBean packageBean = new PackageBean();
                packageBean.setName(StringUtil.getQuotedText(tokens[1]));
                LOGGER.log(Level.FINE, "Dependency name - "+tokens[1]);
                for(String token: tokens){
                    if(token.contains("[0-9].")){
                        packageBean.setVersion(token);
                        LOGGER.log(Level.FINE, "Dependency version -"+token);
                    }
                    if(token.contains(StringConstants.BREW_KEYWORD_OPTIONAL)){
                        dependencyBean.setOptional(true);
                    }
                }
                dependencyBean.setPackageBean(packageBean);
                dependencyBean.setType(4);
                dependencyList.add(dependencyBean);
            }
        }
        LOGGER.exiting(CLASS_NAME, METHOD_NAME);
        return dependencyList;
    }

    private void populatePackageDetails(PackageBean packageBean, String urlLine) {
        final String METHOD_NAME = "populatePackageDetails";
        LOGGER.entering(CLASS_NAME, METHOD_NAME);


            try {
                URL depURL = new URL(urlLine); // this checks if it is a valid url

                String[] urlTokens = urlLine.split("/");
                String versionString = urlTokens[urlTokens.length-1].toLowerCase();
                versionString = versionString.replaceFirst(packageBean.getName(),"");
                versionString = versionString.replaceFirst("-","");

                packageBean.setVersion(getVersionString(versionString));
                LOGGER.log(Level.FINER, "Package version parse - "+versionString);
            }catch(MalformedURLException e){
                LOGGER.log(Level.WARNING, e.getMessage());
                e.printStackTrace();
            }


        LOGGER.exiting(CLASS_NAME, METHOD_NAME);
    }

    private String getVersionString(String versionString) {
        final String METHOD_NAME = "getVersionString";
        LOGGER.entering(CLASS_NAME, METHOD_NAME);

        StringBuffer buffer = new StringBuffer();
        for(int i=0;i<versionString.length();i++){
            String token = versionString.charAt(i)+"";
            if(! token.matches("[a-zA-Z]") ){
                buffer.append(versionString.charAt(i));
            }else{
                if(i!= 0 && versionString.charAt(i-1) == '.'){
                    buffer.deleteCharAt(buffer.lastIndexOf("."));
                }
                break;
            }

        }
        LOGGER.exiting(CLASS_NAME, METHOD_NAME);
        return buffer.toString();
    }

    public static void main(String args[]){
        DependencyParser parser = new BrewDependencyParser();
        parser.parseFiles();
    }
}
