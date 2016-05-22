package edu.iastate.libcompat.algorithms;

import edu.iastate.libcompat.constants.NodeLabels;
import edu.iastate.libcompat.constants.DependencyType;
import edu.iastate.libcompat.constants.StringConstants;
import edu.iastate.libcompat.util.DatabaseUtility;
import edu.iastate.libcompat.util.LoggingUtility;
import org.neo4j.graphdb.*;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by nishanthsivakumar on 4/19/16.
 */
public class MergeNodes {

    private static Logger LOGGER = Logger.getLogger(MergeNodes.class.getName());
    private static final String CLASS_NAME = MergeNodes.class.getName();

    static{
            LoggingUtility.setLoggerLevel(LOGGER,Level.SEVERE);
    }

    public static void getMergeCandidates(Label sourceLabel){
        final String METHOD_NAME = "getMergeCandidates";
        LOGGER.entering(CLASS_NAME,METHOD_NAME);

        GraphDatabaseService graphDB = DatabaseUtility.getGraphDBService();
        Transaction tx = graphDB.beginTx();
        ResourceIterator<Node> iterator = graphDB.findNodes(sourceLabel);
        Set<String> packageList = new TreeSet<>();
        while(iterator.hasNext()){
            Node packageNode = iterator.next();
            packageList.add(packageNode.getProperty(StringConstants.DB_PROPERTY_NAME).toString());
        }
        int mergeCount = 0;
        int packageWithMerge = 0;
        for(String packageName : packageList){//for each package with the given label
            ResourceIterator<Node> versionIterator = graphDB.findNodes(sourceLabel,//TODO - analyze why keeping this to package label results in more merges
                    StringConstants.DB_PROPERTY_NAME,packageName); // get all the version nodes of that package
            List<Node> versionList  = new ArrayList<>();
            while(versionIterator.hasNext()){
                versionList.add(versionIterator.next());
            }
            if(versionList.size() > 1){
                LOGGER.log(Level.FINER,packageName+"\t versions found = "+versionList.size());
                boolean merge = false;
                for(int i=0;i<versionList.size();i++){
                    for(int j=0;j<versionList.size() && j!= i;j++){
                        if(versionList.get(i).hasProperty(StringConstants.DB_PROPERTY_VERSION) &&
                                versionList.get(j).hasProperty(StringConstants.DB_PROPERTY_VERSION)){
                            boolean hasSameDependencies = compareDependencies(versionList.get(i),versionList.get(j));
                            if(hasSameDependencies){
                                LOGGER.log(Level.INFO,"Merge "+versionList.get(i).getProperty(StringConstants.DB_PROPERTY_VERSION)+" and " +
                                        versionList.get(j).getProperty(StringConstants.DB_PROPERTY_VERSION)+" of "+packageName);
                                mergeCount++;
                                merge = true;
                            }
                        }

                    }
                }
                if(merge)
                    packageWithMerge++;
            }

        }
        LOGGER.log(Level.SEVERE,"Possible Merges = "+mergeCount);
        LOGGER.log(Level.SEVERE,"Possible package merge = "+packageWithMerge);
        LOGGER.log(Level.INFO,packageList.toString());
        LOGGER.log(Level.SEVERE,"Packages Found = "+packageList.size());
        tx.success();
        tx.close();
    }

    private static boolean compareDependencies(Node versionA, Node versionB) {
        final String METHOD_NAME = "compareDependencies";
        LOGGER.entering(CLASS_NAME,METHOD_NAME);

        Iterator<Relationship> relationsIterator = versionA.getRelationships().iterator();
        List<Node> dependencyAList = new ArrayList<>();
        while(relationsIterator.hasNext()){
            dependencyAList.add(relationsIterator.next().getEndNode());
        }

        relationsIterator = versionB.getRelationships().iterator();
        List<Node> dependencyBList = new ArrayList<>();
        while(relationsIterator.hasNext()){
            dependencyBList.add(relationsIterator.next().getEndNode());
        }

        for(int i=0;i<dependencyAList.size();i++){
            int found = 1;
            for(int j=0;j<dependencyBList.size();j++){
                found = compareNodes(dependencyAList.get(i),dependencyBList.get(j));
            }
            if(found == 1)
                return false;
        }
        return true;
    }

    private static int compareNodes(Node nodeA, Node nodeB){
        if(nodeA.getProperty(StringConstants.DB_PROPERTY_NAME).toString().equals(
                nodeB.getProperties(StringConstants.DB_PROPERTY_NAME).toString())){
            String versionA = nodeA.getProperty(StringConstants.DB_PROPERTY_VERSION).toString();
            String versionB = nodeB.getProperty(StringConstants.DB_PROPERTY_VERSION).toString();
            if(versionA.trim() != "" && versionB.trim() != "" && versionA.equals(versionB))
                return 0;
        }

        return 1;
    }

    public static void analyzeRelationships(){
        final String METHOD_NAME = "analyzeRelationships";
        LOGGER.entering(CLASS_NAME,METHOD_NAME);

        GraphDatabaseService graphDB = DatabaseUtility.getGraphDBService();
        Transaction tx = graphDB.beginTx();
        ResourceIterator<Node> iterator = graphDB.findNodes(NodeLabels.packageLabel);
        int highestAssertion = 0;
        String maxSource=null,maxSink= null;
        while(iterator.hasNext()){

            Node packageNode = iterator.next();
            Iterator<Relationship> relationshipIterator = packageNode.getRelationships(Direction.OUTGOING).iterator();
            while(relationshipIterator.hasNext()){
                Relationship rel = relationshipIterator.next();
                int freq = (Integer)rel.getProperty(StringConstants.DB_PROPERTY_FREQ);
                if(freq > 2){
                    if(freq > highestAssertion)
                        highestAssertion = freq;
                    maxSource = rel.getStartNode().getProperty(StringConstants.DB_PROPERTY_NAME).toString();
                    maxSink = rel.getEndNode().getProperty(StringConstants.DB_PROPERTY_NAME).toString();
                    LOGGER.log(Level.SEVERE, maxSource+" to "+maxSink+" with "+freq);
                }
            }
        }
        tx.close();
        LOGGER.log(Level.SEVERE,"Highest Assertion = "+highestAssertion+" from "+maxSource+" to "+maxSink);
    }

    public static void analyzeVersionInformation(){
        final String METHOD_NAME = "analyzeVersionInformation";
        LOGGER.entering(CLASS_NAME,METHOD_NAME);

        GraphDatabaseService graphDB = DatabaseUtility.getGraphDBService();
        Transaction tx = graphDB.beginTx();
        ResourceIterator<Node> iterator = graphDB.findNodes(NodeLabels.packageLabel);
        int highestAssertion = 0;
        int nodeCount = 0;
        HashMap<String,String> incompleteNodes = new HashMap<>();
        HashMap<String,Integer> sourceRanking = new HashMap<>();
        while(iterator.hasNext()){
            boolean noVersion = false;
            nodeCount++;
            Node packageNode = iterator.next();
            if(!packageNode.hasProperty(StringConstants.DB_PROPERTY_VERSION)){
                noVersion = true;
            }else{
                if(packageNode.getProperty(StringConstants.DB_PROPERTY_VERSION).toString().trim() == ""){
                    noVersion = true;
                }
            }
            if(noVersion){
                StringBuilder labelList = new StringBuilder();
                Iterator<Label> labelIterator = packageNode.getLabels().iterator();
                while(labelIterator.hasNext()){
                    Label label = labelIterator.next();
                    if(!label.name().equals(StringConstants.DB_LABEL_PACKAGE)){
                        labelList.append(label.name());
                        if(sourceRanking.containsKey(label.name())){
                            int oldVal = sourceRanking.get(label.name());
                            sourceRanking.put(label.name(),oldVal+1);
                        }else{
                            sourceRanking.put(label.name(),1);
                        }
                    }

                }
                incompleteNodes.put(packageNode.getProperty(StringConstants.DB_PROPERTY_NAME).toString(),labelList.toString());
                highestAssertion++;
            }
        }
        tx.close();
        LOGGER.log(Level.SEVERE,"Version Information missing = "+incompleteNodes.size());
        LOGGER.log(Level.INFO, "Missing Info sources = "+incompleteNodes);
        LOGGER.log(Level.SEVERE, "Source Ranking = "+sourceRanking);
        LOGGER.log(Level.SEVERE,"Nodes in Graph = "+nodeCount);
        LOGGER.exiting(CLASS_NAME,METHOD_NAME);

    }

    public static void analyzeDependencyDepth(){
        final String METHOD_NAME = "analyzeDependencyDepth";
        LOGGER.entering(CLASS_NAME,METHOD_NAME);

        GraphDatabaseService graphDB = DatabaseUtility.getGraphDBService();
        Transaction tx = graphDB.beginTx();
        ResourceIterator<Node> iterator = graphDB.findNodes(NodeLabels.packageLabel);
        int max = 0;
        String maxDepthPackage = "";
        while(iterator.hasNext()){
            Node node = iterator.next();
            if(node.hasRelationship(Direction.OUTGOING, DependencyType.DEPENDS_ON)){
                int depth = getTreeDepth(node, 0);
                LOGGER.log(Level.INFO, depth+" is the depth of "+node.getProperty(StringConstants.DB_PROPERTY_NAME).toString());
                if(depth > max) {
                    max = depth;
                    maxDepthPackage = node.getProperty(StringConstants.DB_PROPERTY_NAME).toString();
                }
            }

        }
        LOGGER.log(Level.SEVERE,"Max depth found = "+max+" for package "+maxDepthPackage);
    }

    public static int getTreeDepth(Node node, int depth){
        LOGGER.log(Level.FINER, "Current Depth "+depth+" for "+node.getProperty(StringConstants.DB_PROPERTY_NAME));
        if(node.hasRelationship(Direction.OUTGOING, DependencyType.DEPENDS_ON)){
            depth++;
            Iterator<Relationship> relationshipIterator = node.getRelationships().iterator();
            while(relationshipIterator.hasNext()){
                Node nextNode = relationshipIterator.next().getEndNode();
                if(!nextNode.getProperty(StringConstants.DB_PROPERTY_NAME).equals(node.getProperty(StringConstants.DB_PROPERTY_NAME)))
                    return getTreeDepth(nextNode, depth);
            }

        }
        return depth;
    }

    public static List<Node> getPackagesBySource(Label sourceLabel){
        final String METHOD_NAME = "getPackageBySource";
        LOGGER.entering(CLASS_NAME,METHOD_NAME);

        GraphDatabaseService graphDB = DatabaseUtility.getGraphDBService();
        Transaction tx = graphDB.beginTx();
        ResourceIterator<Node> iterator = graphDB.findNodes(sourceLabel);
        List<Node> nodesList = new ArrayList<>();
        while(iterator.hasNext()){
            nodesList.add(iterator.next());
        }

        LOGGER.exiting(CLASS_NAME,METHOD_NAME);
        return nodesList;
    }

    public static void main(String args[]){
       // MergeNodes.getMergeCandidates(NodeLabels.mavenLabel);
        MergeNodes.analyzeRelationships();
      //  MergeNodes.analyzeVersionInformation();
      //  MergeNodes.analyzeDependencyDepth();
      //  System.out.println(MergeNodes.getPackagesBySource(NodeLabels.packageLabel).size());

    }

}
