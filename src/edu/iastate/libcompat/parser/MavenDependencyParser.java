package edu.iastate.libcompat.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import edu.iastate.libcompat.StringConstants;
import edu.iastate.libcompat.beans.PackageBean;
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
        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try{
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        }catch(ParserConfigurationException e){
            e.printStackTrace();
        }
    }

    private void populateParentMetadata(Document document, PackageBean packageBean){
        final String METHOD_NAME = "getDependencyMetadata";
        LOGGER.entering(CLASS_NAME, METHOD_NAME);
        try {

            //save dependency name and version
            NodeList parentTags = document.getElementsByTagName(StringConstants.MVN_TAG_NAME_PARENT);
            if (parentTags.getLength() > 0 && parentTags.item(0).hasChildNodes()) {
                Node parentTag = parentTags.item(0);
                NodeList childNodes = parentTag.getChildNodes();
                for (int i = 0; i < childNodes.getLength(); i++) {
                    if (childNodes.item(i).getNodeName() == StringConstants.MVN_TAG_NAME_ARTIFACT_ID) {
                        packageBean.setName(childNodes.item(i).getTextContent());
                    }
                    if (childNodes.item(i).getNodeName() == StringConstants.MVN_TAG_NAME_VERSION) {
                        packageBean.setVersion(childNodes.item(i).getTextContent());
                    }
                    if (packageBean.getName() != null && packageBean.getVersion() != null) {
                        break;
                    }
                }
            }
            //save dependency description
            NodeList projectTags = document.getElementsByTagName(StringConstants.MVN_TAG_NAME_PROJECT);
            if (projectTags.getLength() > 0 && projectTags.item(0).hasChildNodes()) {
                Node projectTag = projectTags.item(0);
                NodeList childNodes = projectTag.getChildNodes();
                for (int i = 0; i < childNodes.getLength(); i++) {
                    if (childNodes.item(i).getNodeName() == StringConstants.MVN_TAG_NAME_NAME) {
                        packageBean.setDescription(childNodes.item(i).getTextContent());
                        break;
                    }
                }
            }
        }catch(Exception e){
            //e.printStackTrace();
            LOGGER.log(Level.SEVERE, e.getMessage());
        }

        LOGGER.exiting(CLASS_NAME, METHOD_NAME);

    }

    private PackageBean getDependencyMetadata(Node dependencyTag){
        final String METHOD_NAME = "getDependencyMetadata";
        LOGGER.entering(CLASS_NAME, METHOD_NAME);
        PackageBean packageBean = new PackageBean();
        NodeList childNodes = dependencyTag.getChildNodes();
        for(int i=0;i<childNodes.getLength();i++){
            if(childNodes.item(i).getNodeName() == StringConstants.MVN_TAG_NAME_ARTIFACT_ID)
                packageBean.setName(childNodes.item(i).getTextContent());
            if(childNodes.item(i).getNodeName() == StringConstants.MVN_TAG_NAME_VERSION)
                packageBean.setVersion(childNodes.item(i).getTextContent());
            if (packageBean.getName() != null && packageBean.getVersion() != null) break;
        }

        LOGGER.exiting(CLASS_NAME, METHOD_NAME);
        return packageBean;
    }

    private List<PackageBean> getDependencyList(Document document, PackageBean parentBean){
        final String METHOD_NAME = "getDependencyList";
        LOGGER.entering(CLASS_NAME, METHOD_NAME);

        List<PackageBean> dependencyList = new ArrayList<>();
        NodeList dependenciesTag = document.getElementsByTagName(StringConstants.MVN_TAG_NAME_DEPENDENCIES);
        for(int i=0;i<dependenciesTag.getLength();i++) {
            NodeList dependencyTags = dependenciesTag.item(i).getChildNodes();
            for(int j=0; j< dependencyTags.getLength(); j++){
                //check only for direct dependencies
                //TODO Include profile dependencies
                //TODO Include build dependencies
                if(dependencyTags.item(j).getParentNode().getNodeName() == StringConstants.MVN_TAG_NAME_PARENT){
                    dependencyList.add(getDependencyMetadata(dependencyTags.item(j)));
                }
            }

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

        boolean found = false;
        for(String fileName: files){
            LOGGER.log(Level.FINE, "Parsing File - "+fileName);
            boolean dep1 = false;
            boolean dep2 = false;
            try {
                Document document = documentBuilder.parse(new File(fileName));
                document.getDocumentElement().normalize();
                LOGGER.log(Level.FINEST, "parsed successfully");

                PackageBean packageBean = new PackageBean();
                populateParentMetadata(document, packageBean);

                List<PackageBean> dependencyList = getDependencyList(document, packageBean);

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
