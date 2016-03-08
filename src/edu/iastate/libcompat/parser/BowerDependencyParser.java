package edu.iastate.libcompat.parser;


import edu.iastate.libcompat.beans.DependencyBean;
import edu.iastate.libcompat.beans.PackageBean;
import edu.iastate.libcompat.constants.StringConstants;
import edu.iastate.libcompat.util.StringUtil;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by nishanthsivakumar on 3/7/16.
 */
public class BowerDependencyParser extends DependencyParser {

    private Logger LOGGER = Logger.getLogger(BowerDependencyParser.class.getName());
    private String CLASS_NAME = BowerDependencyParser.class.getName();
    private JSONObject jsonObject;

    public BowerDependencyParser(){
        super("package.json");
    }

    @Override
    public void parseFiles() {
        final String METHOD_NAME = "parseFiles";
        LOGGER.entering(CLASS_NAME, METHOD_NAME);
        LOGGER.setLevel(Level.FINEST);
        String[] files = getFilesByType();

        for(String fileName: files){
            LOGGER.log(Level.FINE, "Parsing File - "+fileName);
            try {
                StringBuffer fileString = new StringBuffer();
                FileReader fileReader = new FileReader(new File(fileName));
                BufferedReader bufferedReader  = new BufferedReader(fileReader);
                while(bufferedReader.ready()){
                  fileString.append(bufferedReader.readLine()+"\n");
                }
                LOGGER.log(Level.FINEST,fileString.toString());
                JSONObject jsonObject = new JSONObject(fileString.toString());

                PackageBean packageBean = new PackageBean();
                populatePackageMetadata(jsonObject, packageBean);

                List<DependencyBean> dependencyList = getDependencyList(jsonObject);

                LOGGER.log(Level.INFO,"Package - "+packageBean.getName()+"\nDependencies Found - "+dependencyList.size());

                //store to database

            }catch(Exception e){
                LOGGER.log(Level.WARNING, e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private List<DependencyBean> getDependencyList(JSONObject jsonObject) {
        final String METHOD_NAME = "getDependencyList";
        LOGGER.entering(CLASS_NAME, METHOD_NAME);

        List<DependencyBean> dependencyList = new ArrayList<DependencyBean>();
        if(jsonObject.has(StringConstants.BWR_KEY_NAME_DEPENDENCIES)){
            JSONObject dependencies = (JSONObject) jsonObject.get(StringConstants.BWR_KEY_NAME_DEPENDENCIES);
            if(dependencies != null){
                Iterator<String> keyIterator = dependencies.keys();
                while(keyIterator.hasNext()){
                    String key = keyIterator.next();
                    PackageBean packageBean = new PackageBean();
                    packageBean.setName(key);
                    packageBean.setVersion(StringUtil.upgradeVersionString(dependencies.getString(key)));

                    DependencyBean dependencyBean = new DependencyBean();
                    dependencyBean.setPackageBean(packageBean);
                    dependencyBean.setType(2);
                    dependencyList.add(dependencyBean);
                }
            }
        }

        LOGGER.exiting(CLASS_NAME, METHOD_NAME);
        return dependencyList;
    }

    private void populatePackageMetadata(JSONObject jsonObject, PackageBean packageBean) {
        final String METHOD_NAME = "populatePackageMetadata";
        LOGGER.entering(CLASS_NAME, METHOD_NAME);
        if(jsonObject.has(StringConstants.BWR_KEY_NAME_NAME))
            packageBean.setName(jsonObject.getString(StringConstants.BWR_KEY_NAME_NAME));
        if(jsonObject.has(StringConstants.BWR_KEY_NAME_DESCRIPTION))
            packageBean.setDescription(jsonObject.getString(StringConstants.BWR_KEY_NAME_DESCRIPTION));
        if(jsonObject.has(StringConstants.BWR_KEY_NAME_VERSION))
            packageBean.setVersion(jsonObject.getString(StringConstants.BWR_KEY_NAME_VERSION));

        LOGGER.exiting(CLASS_NAME, METHOD_NAME);
    }

    public static void main(String args[]){
        DependencyParser dp = new BowerDependencyParser();
        dp.parseFiles();
    }
}
