package edu.iastate.libcompat.beans;

/**
 * Created by nishanthsivakumar on 3/2/16.
 */
public class DependencyBean {
    private PackageBean packageBean;
    private boolean optional;
    private int type;

    public PackageBean getPackageBean() {
        return packageBean;
    }

    public void setPackageBean(PackageBean packageBean) {
        this.packageBean = packageBean;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
