package edu.iastate.libcompat.constants;

import org.neo4j.graphdb.RelationshipType;

/**
 * Created by nishanthsivakumar on 3/20/16.
 */
public enum DependencyType implements RelationshipType{
    DEPENDS_ON,COMPATIBLE_WITH
}
