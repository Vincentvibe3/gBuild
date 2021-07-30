package com.gbuild.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.function.Consumer;

import com.gbuild.util.Logging;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class ProjectGenerator {

    static boolean verbose = false;

    public static void createProject(boolean v){
        verbose = v;
        String templateChoice = getTemplateChoice();
        String templateLocation = findTemplates(templateChoice);
        File configFile = Paths.get(templateLocation, "project.json").toFile();
        ProjectConfig config = new ProjectConfig(configFile);
        createFilesAndDir(config, templateLocation);
        createBuildConfig(config);
    }

    public static void createBuildConfig(ProjectConfig config){
        Logging.print("Creating build.json", Logging.OutTypes.TASK);
        String projectDir = config.getProjectDir();
        TemplateBuildConfig buildConfig = new TemplateBuildConfig(config);
        File buildFile = Paths.get(projectDir, "build.json").toFile();
        buildConfig.write(buildFile);
    }

    public static void createFilesAndDir(ProjectConfig config, String templateLocation){
        Logging.print("Creating placeholder files", Logging.OutTypes.TASK);
        String projectDir = config.getProjectDir();
        String sourceDir = config.getSourceDir();
        String libDir  = config.getDependenciesDir();
        Paths.get(projectDir, sourceDir).toFile().mkdirs();
        Paths.get(projectDir, libDir).toFile().mkdirs();
        Path javatemplate = Paths.get(templateLocation, "template.java");
        JSONArray arrayObj = config.getFiles();
        ArrayList<JSONObject> filesToAdd = new ArrayList<>();
        String javaTemp = getJavaTemplate(javatemplate);
        Consumer<JSONObject> writeFiles = item -> {
            String name = item.getString("name");
            String location = item.getString("location");
            if (name.endsWith(".java")){
                String content = "package " + location.replace("\\", ".").replace("/", ".") + ";\n\n" + javaTemp.replace("{template}", name.replace(".java", ""));
                if (verbose){
                    Logging.print("Creating File " + name, Logging.OutTypes.INFO);
                }
                Paths.get(projectDir, sourceDir, location).toFile().mkdirs();
                File file = Paths.get(projectDir, sourceDir, location, name).toFile();
                try {
                    FileOutputStream writer = new FileOutputStream(file);
                    writer.write(content.getBytes());
                } catch (IOException e){
                    Logging.print("Failed to write file " + name, Logging.OutTypes.ERROR);
                    e.printStackTrace();
                    System.exit(1);
                }
            } else {
                File file = Paths.get(projectDir, sourceDir, location, name).toFile();
                try{
                    FileWriter writer = new FileWriter(file);
                    writer.write("");
                } catch (IOException e){
                    Logging.print("Failed to write file " + name, Logging.OutTypes.ERROR);
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        };
        Consumer<Object> addfile = item -> {
            if (item.getClass() == JSONObject.class){
                filesToAdd.add((JSONObject) item);
            }
        };
        arrayObj.forEach(addfile);
        filesToAdd.forEach(writeFiles);
    }

    public static String getJavaTemplate(Path template){
        String fileString=null;
        try {
            fileString = Files.readString(template);;
        } catch (FileNotFoundException e){
            Logging.print("template not found", Logging.OutTypes.ERROR);
            System.exit(1);
        } catch (IOException e){
            Logging.print("An error occured while reading the template", Logging.OutTypes.ERROR);
            e.printStackTrace();
            System.exit(1);
        }
        return fileString;
    }

    public static String getTemplateChoice(){
        Logging.print("Get template name", Logging.OutTypes.TASK);
        Logging.print("Enter the template name(leave empty for default): ", Logging.OutTypes.ACTION);
        Scanner inScan = new Scanner(System.in);
        String name = inScan.nextLine();
        if (name.isEmpty()){
            name = "default";
        }
        return name;
    }

    public static String findTemplates(String templateChoice){
        Logging.print("Finding templates location", Logging.OutTypes.TASK);
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
            Logging.print("gBuild was not found in the PATH", Logging.OutTypes.ERROR);
            System.exit(1);
        }
        File buildDefault=null;
        File templateDefault=null;
        boolean foundTemplates = false;
        String templatesLocation = null;
        String templateLocation = null;
        for (String location:gBuildlocations){
            templatesLocation =  Paths.get(Path.of(location).getParent().toString(), "templates").toString();
            buildDefault = Paths.get(templatesLocation, templateChoice, "/project.json").toFile();
            templateDefault = Paths.get(templatesLocation, templateChoice, "/template.java").toFile();
            if (buildDefault.exists()&&templateDefault.exists()){
                foundTemplates = true;
                templateLocation = Paths.get(templatesLocation, templateChoice).toString();
                break;
            }
        }
        if (!foundTemplates){
            Logging.print("Could not find required template files", Logging.OutTypes.ERROR);
            System.exit(1);
        }
        return templateLocation;
        
    }
}

class ProjectConfig{

    private String projectDir;
    private String projectName;
    private String dependenciesDir;
    private String sourceDir;
    private String mainClass;
    private JSONArray files;

    public ProjectConfig(File config){
        Logging.print("Building project config", Logging.OutTypes.TASK);
        Logging.print("Enter the project name: ", Logging.OutTypes.ACTION);
        Scanner inScan = new Scanner(System.in);
        String name = inScan.nextLine();
        projectDir = name.replace(" ", "_");
        projectName = name;
        inScan.close();
        readConfig(config);
    }

    public void readConfig(File config){
        try{
            FileReader reader = new FileReader(config);
            JSONObject data = new JSONObject(new JSONTokener(reader));
            dependenciesDir = data.getString("dependenciesDir");
            sourceDir = data.getString("sourceDir");
            files = data.getJSONArray("files");
            mainClass = setMainClass(files);
        } catch (FileNotFoundException e){
            Logging.print("Could not find the project.json file", Logging.OutTypes.ERROR);
            System.exit(1);
        }
        
    }

    private String setMainClass(JSONArray files){
        ArrayList<String> main = new ArrayList<>();
        ArrayList<JSONObject> filesToCheck = new ArrayList<>();
        Consumer<JSONObject> checkFiles = item -> {
            if (item.has("mainClass")){
                
                if (item.getBoolean("mainClass")){
                    String packageName = item.getString("location").replace("\\", ".").replace("/", ".");
                    String className = item.getString("name").replace(".java", "");
                    main.add(packageName + "." + className);
                }
            }
        };
        Consumer<Object> addfile = item -> {
            if (item.getClass() == JSONObject.class){
                filesToCheck.add((JSONObject) item);
            }
        };
        files.forEach(addfile);
        filesToCheck.forEach(checkFiles);
        if (main.size()!=1){
            Logging.print("You must specify 1 Main Class to build a config", Logging.OutTypes.ERROR);
            System.exit(1);
        }
        return main.get(0);
    }

    public String getProjectDir(){
        return projectDir;
    }

    public String getProjectName(){
        return projectName;
    }

    public String getDependenciesDir(){
        return dependenciesDir;
    }

    public String getSourceDir(){
        return sourceDir;
    }

    public String getMainClass(){
        return mainClass;
    }

    public JSONArray getFiles(){
        return files;
    }

}

class TemplateBuildConfig{
    JSONObject buildConfig = new JSONObject();

    public TemplateBuildConfig(ProjectConfig config){
        JSONObject manifest = buildManifest(config.getMainClass());
        buildConfig.put("name", config.getProjectName());
        buildConfig.put("dependencies", "lib");
        buildConfig.put("source", "src");
        buildConfig.put("buildDir", "target");
        buildConfig.put("binDir", "bin");
        buildConfig.put("Version", "1.0");
        buildConfig.put("manifest", manifest);

    }

    public String toString(){
        return buildConfig.toString(4);
    }

    public void write(File location){
        try {
            FileOutputStream writer = new FileOutputStream(location);
            writer.write(toString().getBytes());
        } catch (IOException e){
            Logging.print("An error occured when writing build.json", Logging.OutTypes.ERROR);
            e.printStackTrace();
            System.exit(1);
        }
    }

    public JSONObject buildManifest(String mainclass){
        JSONObject manifest = new JSONObject();
        manifest.put("MainClass", mainclass);
        return manifest;
    }

}