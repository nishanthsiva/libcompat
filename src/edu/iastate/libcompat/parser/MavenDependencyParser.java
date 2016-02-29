package edu.iastate.libcompat.parser;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

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
            try{
                Document document = documentBuilder.parse(new File(fileName));
                document.getDocumentElement().normalize();
                NodeList dependenciesTag = document.getElementsByTagName("dependencies");
                LOGGER.log(Level.FINEST, "parsed successfully");
                for(int i=0;i<dependenciesTag.getLength();i++){
                    NodeList dependencyTags = dependenciesTag.item(i).getChildNodes();
                    for(int j=0;j<dependencyTags.getLength(); j++){
                       NodeList dependencyAttr = dependencyTags.item(j).getChildNodes();
                        for(int k=0;k< dependencyAttr.getLength(); k++){
                            if(dependencyAttr.item(k).getNodeName() == "artifactId"){
                                Node artifact = dependencyAttr.item(k);
                                LOGGER.log(Level.FINEST, artifact.getTextContent());
                                if(artifact.getTextContent().contains("junit")){
                                    dep1 = true;
                                }else if(artifact.getTextContent().contains("log4j")){
                                    dep2 = true;
                                }
                            }
                            if(dep1 && dep2){
                                found = true;
                                break;
                            }
                        }
                        if(found){
                            break;
                        }
                    }
                    if(found){
                        break;
                    }
                }
                if(found){
                   LOGGER.log(Level.SEVERE, "Yes Match found!");
                    break;
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
