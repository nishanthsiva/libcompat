package edu.iastate.libcompat.util;

import edu.iastate.libcompat.beans.DependencyBean;
import edu.iastate.libcompat.beans.PackageBean;
import edu.iastate.libcompat.constants.DependencyLabels;
import edu.iastate.libcompat.constants.DependencyType;
import edu.iastate.libcompat.constants.StringConstants;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by nishanthsivakumar on 3/20/16.
 */
public class DatabaseUtility {

    private static Logger LOGGER = Logger.getLogger(DatabaseUtility.class.getName());
    private static final String CLASS_NAME = DatabaseUtility.class.getName();

    private static GraphDatabaseService graphDb;
    private static final String DB_PATH = "/Users/nishanthsivakumar/Documents/Neo4j/libcompat.graphdb";

    static{
        graphDb = new GraphDatabaseFactory().newEmbeddedDatabase( DB_PATH );
        registerShutdownHook( graphDb );
        LoggingUtility.setLoggerLevel(LOGGER,Level.FINER);
    }

    private static void registerShutdownHook( final GraphDatabaseService graphDb )
    {
        // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running application).
        Runtime.getRuntime().addShutdownHook( new Thread()
        {
            @Override
            public void run()
            {
                graphDb.shutdown();
            }
        } );
    }

    public static void addPackageDependency(PackageBean packageBean, List<DependencyBean> dependencyList){
        final String METHOD_NAME = "addPackageDependency";
        LOGGER.entering(CLASS_NAME, METHOD_NAME);

        try
        {
            Transaction tx = graphDb.beginTx();
            if(packageBean != null){

                Node packageNode = fetchOrCreatePackageNode(packageBean);
                for(DependencyBean dependencyBean : dependencyList){
                    Node dependencyNode = fetchOrCreatePackageNode(dependencyBean.getPackageBean());
                    if(dependencyNode != null){
                        packageNode.createRelationshipTo(dependencyNode, DependencyType.DEPENDS_ON);
                    }

                }
                //TODO for each dependency add compatible with relationship with every other dependency
            }

            tx.success();
            tx.close();
        }catch (Exception e) {
            LOGGER.log(Level.WARNING, e.getMessage());
            e.printStackTrace();
        }
        LOGGER.exiting(CLASS_NAME, METHOD_NAME);
    }

    private static Node fetchOrCreatePackageNode(PackageBean packageBean){
        final String METHOD_NAME = "fetchOrCreatePackageNode";
        LOGGER.entering(CLASS_NAME, METHOD_NAME);

        Node packageNode = null;
        ResourceIterator<Node> existingNodes = graphDb.findNodes(DependencyLabels.packageLabel,StringConstants.DB_PROPERTY_NAME,packageBean.getName());
        while(existingNodes.hasNext()){
            Node temp = existingNodes.next();
            if(packageBean.getVersion() != null && temp.hasProperty(StringConstants.DB_PROPERTY_VERSION) &&
                    temp.getProperty(StringConstants.DB_PROPERTY_VERSION).equals(packageBean.getVersion())){
                //TODO add check to return nodes with 1.5.x as version if the version being checked is 1.5
                packageNode = temp;
                break;
            }
            if(packageBean.getVersion() == null && !temp.hasProperty(StringConstants.DB_PROPERTY_VERSION)){
                packageNode = temp;
                break;
            }
        }
        if(packageNode == null){
            LOGGER.log(Level.FINER,"No existing nodes found");
            if(packageBean.getName() != null){
                packageNode = graphDb.createNode(DependencyLabels.packageLabel);
                packageNode.setProperty(StringConstants.DB_PROPERTY_NAME,packageBean.getName());
                if(packageBean.getVersion() != null)
                    packageNode.setProperty(StringConstants.DB_PROPERTY_VERSION,packageBean.getVersion());
                if(packageBean.getDescription() != null)
                    packageNode.setProperty(StringConstants.DB_PROPERTY_DESC,packageBean.getDescription());
            }

        }else{
            LOGGER.log(Level.FINER,"existing nodes found");
            if(!packageNode.hasLabel(DependencyLabels.packageLabel)){
                packageNode.addLabel(DependencyLabels.packageLabel);
            }
            if(!packageNode.hasProperty(StringConstants.DB_PROPERTY_DESC) && packageBean.getDescription() != null){
                packageNode.setProperty(StringConstants.DB_PROPERTY_DESC,packageBean.getDescription());
            }
        }
        LOGGER.exiting(CLASS_NAME, METHOD_NAME);
        return packageNode;
    }

}
