package edu.iastate.libcompat.util;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by nishanthsivakumar on 2/29/16.
 */
public class FileFilter {

    private static final Logger LOGGER = Logger.getLogger(FileFilter.class.getName());
    private String CLASS_NAME = FileFilter.class.getName();

    private String directoryPath;

    public FileFilter(String directoryPath){
        this.directoryPath = directoryPath;
    }

    public String[] filterByFiletype(String fileType){
        final String METHOD_NAME = "filterByFileType";
        LOGGER.entering(CLASS_NAME, METHOD_NAME);


        LOGGER.exiting(CLASS_NAME, METHOD_NAME);
        return  getFiles(directoryPath, fileType).toArray(new String[1]);

    }

    private List<String> getFiles(String directoryPath, String fileType){
        final String METHOD_NAME = "getFiles";

        LOGGER.entering(CLASS_NAME, METHOD_NAME);
        List<String> fileNames = new ArrayList<>();

        File dirNode = new File(directoryPath);
        if(dirNode.isDirectory()){
            for(File node: dirNode.listFiles()){
                if(node.isDirectory()){
                    fileNames.addAll(getFiles(node.getAbsolutePath(), fileType));
                }else{
                    if(fileType != "*"){
                        if(node.getName().endsWith(fileType)){
                            fileNames.add(node.getAbsolutePath());
                        }
                    }else{
                        fileNames.add(node.getAbsolutePath());
                    }

                }
            }
        }else{
            fileNames.add(dirNode.getAbsolutePath());
        }



        LOGGER.exiting(CLASS_NAME, METHOD_NAME);

        return fileNames;
    }

    public static void main(String args[]){
        FileFilter filter = new FileFilter("/Users/nishanthsivakumar/Documents/libcompat/project_repo/maven_projects/");
        String[] paths = filter.filterByFiletype("pom.xml");
        for(String name : paths){
            System.out.println(name);
        }
        System.out.println(paths.length);
    }
}
