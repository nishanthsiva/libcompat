package edu.iastate.libcompat.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import edu.iastate.libcompat.constants.StringConstants;
import edu.iastate.libcompat.beans.DependencyBean;
import edu.iastate.libcompat.beans.PackageBean;
import edu.iastate.libcompat.util.DatabaseUtility;
import edu.iastate.libcompat.util.StringUtil;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

/**
 * Created by nishanthsivakumar on 2/29/16.
 */
public class MavenDependencyParser extends DependencyParser {
    private Logger LOGGER = Logger.getLogger(MavenDependencyParser.class.getName());
    private String CLASS_NAME = MavenDependencyParser.class.getName();
    private DocumentBuilder documentBuilder;
    private DocumentBuilderFactory documentBuilderFactory;

    public MavenDependencyParser(){
        super("pom.xml");
        super.setLoggerLevel(LOGGER, Level.INFO);
        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try{
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        }catch(ParserConfigurationException e){
            e.printStackTrace();
        }
    }

    private void populatePackageMetadata(Document document, PackageBean packageBean){
        final String METHOD_NAME = "populatePackageMetadata";
        LOGGER.entering(CLASS_NAME, METHOD_NAME);
        try {

            //save package name and version
           /* NodeList parentTags = document.getElementsByTagName(StringConstants.MVN_TAG_NAME_PARENT);
            if (parentTags.getLength() > 0 && parentTags.item(0).hasChildNodes()) {
                Node parentTag = parentTags.item(0);
                NodeList childNodes = parentTag.getChildNodes();
                for (int i = 0; i < childNodes.getLength(); i++) {
                    if (childNodes.item(i).getNodeName().equals(StringConstants.MVN_TAG_NAME_ARTIFACT_ID)) {
                        packageBean.setName(childNodes.item(i).getTextContent());
                    }
                    if (childNodes.item(i).getNodeName().equals(StringConstants.MVN_TAG_NAME_VERSION)) {
                        packageBean.setVersion(childNodes.item(i).getTextContent());
                    }
                    if (packageBean.getName() != null && packageBean.getVersion() != null) {
                        break;
                    }
                }
            }*/
            //save package description
            NodeList projectTags = document.getElementsByTagName(StringConstants.MVN_TAG_NAME_PROJECT);
            if (projectTags.getLength() > 0 && projectTags.item(0).hasChildNodes()) {
                Node projectTag = projectTags.item(0);
                NodeList childNodes = projectTag.getChildNodes();
                for (int i = 0; i < childNodes.getLength(); i++) {
                    if (childNodes.item(i).getNodeName().equals(StringConstants.MVN_TAG_NAME_ARTIFACT_ID)) {
                        packageBean.setName(childNodes.item(i).getTextContent());
                    }
                    if(childNodes.item(i).getNodeName().equals(StringConstants.MVN_TAG_NAME_DESCRIPTION)){
                        packageBean.setDescription(childNodes.item(i).getTextContent());
                    }
                    if(childNodes.item(i).getNodeName().equals(StringConstants.MVN_TAG_NAME_VERSION)){
                        packageBean.setVersion(StringUtil.getVersionString(childNodes.item(i).getTextContent()));
                    }
                    if (packageBean.getName() != null && packageBean.getVersion() != null && packageBean.getDescription() != null) {
                        break;
                    }

                }
            }
        }catch(Exception e){
            e.printStackTrace();
            LOGGER.log(Level.SEVERE, e.getMessage());
        }

        LOGGER.exiting(CLASS_NAME, METHOD_NAME);

    }

    private DependencyBean getDependencyMetadata(Node dependencyTag, PackageBean parentBean,Document document){
        final String METHOD_NAME = "getDependencyMetadata";
        LOGGER.entering(CLASS_NAME, METHOD_NAME);
        DependencyBean dependencyBean = new DependencyBean();
        PackageBean packageBean = new PackageBean();
        NodeList childNodes = dependencyTag.getChildNodes();
        boolean optionalFlag = false;
        for(int i=0;i<childNodes.getLength();i++){
            Node child = childNodes.item(i);
            if(child.getNodeName().equals(StringConstants.MVN_TAG_NAME_ARTIFACT_ID))
                packageBean.setName(child.getTextContent());
            if(child.getNodeName().equals(StringConstants.MVN_TAG_NAME_VERSION)) {
                packageBean.setVersion(StringUtil.getVersionString(child.getTextContent()));
                if (packageBean.getVersion().equals(StringConstants.MVN_CONST_PROJECT_VERSION)) {
                    packageBean.setVersion(parentBean.getVersion());
                }else if(packageBean.getVersion().startsWith("${")){
                    String tagName = packageBean.getVersion().replace("${|}","");
                    Node tag = document.getElementsByTagName(tagName).item(0);
                    if(tag != null)
                        packageBean.setVersion(StringUtil.getVersionString(tag.getTextContent()));
                    else
                        packageBean.setVersion(null);
                }
            }
            if(child.getNodeName().equals(StringConstants.MVN_TAG_NAME_OPTIONAL)){
                optionalFlag = Boolean.parseBoolean(child.getTextContent());
            }
        }
        dependencyBean.setPackageBean(packageBean);
        dependencyBean.setOptional(optionalFlag);
        dependencyBean.setType(1);

        LOGGER.exiting(CLASS_NAME, METHOD_NAME);
        return dependencyBean;
    }

    private List<DependencyBean> getDependencyList(Document document, PackageBean parentBean){
        final String METHOD_NAME = "getDependencyList";
        LOGGER.entering(CLASS_NAME, METHOD_NAME);

        List<DependencyBean> dependencyList = new ArrayList<DependencyBean>();
        NodeList dependenciesTag = document.getElementsByTagName(StringConstants.MVN_TAG_NAME_DEPENDENCIES);
        for(int i=0;i<dependenciesTag.getLength();i++) {
            if(dependenciesTag.item(i).getParentNode().getNodeName().equals(StringConstants.MVN_TAG_NAME_PROJECT)){
                NodeList dependencyTags = dependenciesTag.item(i).getChildNodes();
                for(int j=0; j< dependencyTags.getLength(); j++){
                    //check only for direct dependencies
                    dependencyList.add(getDependencyMetadata(dependencyTags.item(j),parentBean,document));
                }
            }
            //TODO Include profile dependencies
            //TODO Include build dependencies

        }
        LOGGER.exiting(CLASS_NAME, METHOD_NAME);
        return dependencyList;
    }

    @Override
    public void parseFiles(){
        final String METHOD_NAME = "parseFiles";
        LOGGER.entering(CLASS_NAME, METHOD_NAME);
        LOGGER.setLevel(Level.FINEST);
        String[] files = getFilesByType();
        for(String fileName: files){
            LOGGER.log(Level.FINE, "Parsing File - "+fileName);
            try {
                Document document = documentBuilder.parse(new File(fileName));
                document.getDocumentElement().normalize();
                LOGGER.log(Level.FINEST, "parsed successfully");

                PackageBean packageBean = new PackageBean();
                populatePackageMetadata(document, packageBean);

                List<DependencyBean> dependencyList = getDependencyList(document, packageBean);
                if(packageBean.getName() != null){
                    LOGGER.log(Level.INFO, "Package parsed - "+packageBean.getName()+"\nDependencies found - "+dependencyList.size());
                    //store the package and the dependency list
                    DatabaseUtility.addPackageDependency(packageBean,dependencyList);
                }


            }catch(Exception e){
                //e.printStackTrace();
                LOGGER.log(Level.SEVERE, e.getMessage());
            }
        }
    }

    public static void main(String args[]){
        DependencyParser dp = new MavenDependencyParser();
        dp.parseFiles();
    }
}
