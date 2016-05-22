package edu.iastate.libcompat.beans;

/**
 * Created by nishanthsivakumar on 3/2/16.
 */
public class PackageBean implements Comparable {

    private String name;
    private String description;
    private String version;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int hashCode() {
        return (this.getName()+this.getVersion()).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        PackageBean package2 = (PackageBean) obj;
        if(package2 != null && this.getName()!= null && this.getName().equals(package2.getName())){
            if(this.getVersion() == null && package2.getVersion() == null)
                return true;
            else if(this.getVersion()!= null && package2.getVersion()!= null && this.getVersion().equals(package2.getVersion()))
                return true;
            else
                return false;
        }else{
            return false;
        }
    }

    @Override
    public int compareTo(Object o) {
        PackageBean package2 = (PackageBean) o;
        if(this.equals(package2)){
            return 0;
        }else{
            return 1; //TODO Compare versions and return the value accordingly
        }
    }
}
