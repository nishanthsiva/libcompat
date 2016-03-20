package edu.iastate.libcompat.constants;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Label;

/**
 * Created by nishanthsivakumar on 3/20/16.
 */
public class DependencyLabels {
    public static final Label packageLabel = DynamicLabel.label(StringConstants.DB_LABEL_PACKAGE);
    public static final Label dependencyLabel = DynamicLabel.label(StringConstants.DB_LABEL_DEPENDENCY);
}
