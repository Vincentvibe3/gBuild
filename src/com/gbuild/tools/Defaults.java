package com.gbuild.tools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;

public class Defaults {

    public static void createProject(){
        new File("src").mkdir();
        new File("lib").mkdir();
        String path = System.getenv("Path");
        ArrayList<String> gBuildlocations = new ArrayList<>();
        Consumer<String> findGbuild = item -> {
            if(item.contains("gBuild")&&item.endsWith("bin")){
                gBuildlocations.add(item);
            }
        };
        if (System.getProperty("os.name").startsWith("Windows")){
            Arrays.asList(path.split(";")).forEach(findGbuild);
        } else {
            Arrays.asList(path.split(":")).forEach(findGbuild);
        }
        if (gBuildlocations.isEmpty()){
            System.err.println("gBuild was not found in the PATH");
            System.exit(1);
        }
        boolean foundTemplates = false;
        File buildDefault=null;
        File mainDefault=null;
        for (String location:gBuildlocations){
            String templateLocation = location.substring(0, location.length()-3)+"templates";
            buildDefault = new File(templateLocation+"/build.json");
            mainDefault = new File(templateLocation+"/Main.java");
            if (buildDefault.exists()&&mainDefault.exists()){
                foundTemplates = true;
                break;
            }
        }
        File buildNewLocation = new File("build.json");
        File mainNewLocation = new File("src/Main.java");
        if (foundTemplates){
            try {
                Files.copy(buildDefault.toPath(), buildNewLocation.toPath());
                Files.copy(mainDefault.toPath(), mainNewLocation.toPath());
            } catch (IOException e){
                e.printStackTrace();
                System.exit(1);
            }
        } else {
            System.err.println("template files were not found");
            System.exit(1);
        }
        
    }
}
