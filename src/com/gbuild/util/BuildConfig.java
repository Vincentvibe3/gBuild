package com.gbuild.util;

import java.util.jar.Manifest;
import java.util.jar.Attributes;

public class BuildConfig {

    private String dependencies;
    private String source;
    private Manifest manifest;
    private String buildDir;
    private String name;
    private String binDir;
    private String ver;

    public BuildConfig(){
        manifest = new Manifest();
    }

    public Manifest getManifest(){
        return manifest;
    }

    public void setManifest(Manifest newmanifest){
        manifest = newmanifest;
    }

    public String getVer(){
        return ver;
    }

    public void setVer(String newver){
        ver = newver;
    }

    public void getManifestInfo(){
        Logging.print("Manifest Info", Logging.OutTypes.INFO);
        Attributes.Name[] attrs = {Attributes.Name.MANIFEST_VERSION, Attributes.Name.MAIN_CLASS, Attributes.Name.CLASS_PATH};
        for (Attributes.Name attr : attrs){
            Logging.print(attr.toString() + ": " + manifest.getMainAttributes().getValue(attr), Logging.OutTypes.INFO);
        }
    }

    public void setDependencies(String dir){
        dependencies = dir;
    }

    public String getBuildDir(){
        return buildDir;
    }

    public void setBuildDir(String dir){
        buildDir = dir;
    }

    public String getBinDir(){
        return binDir;
    }

    public void setBinDir(String dir){
        binDir = dir;
    }

    public String getName(){
        return name;
    }

    public void setName(String filename){
        name = filename;
    }

    public String getDependencies(){
        return dependencies;
    }

    public void setSource(String src){
        source = src;
    }

    public String getSource(){
        return source;
    }

}
