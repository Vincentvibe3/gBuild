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

    public static void createProject(){
        String templateChoice = getTemplateChoice();
        String templateLocation = findTemplates(templateChoice);
        File configFile = Paths.get(templateLocation, "project.json").toFile();
        ProjectConfig config = new ProjectConfig(configFile);
        createFilesAndDir(config, templateLocation);
        createBuildConfig(config);
    }

    public static void createBuildConfig(ProjectConfig config){
        System.out.println(Logging.TASK + "Creating build.json");
        String projectDir = config.getProjectDir();
        TemplateBuildConfig buildConfig = new TemplateBuildConfig(config);
        File buildFile = Paths.get(projectDir, "build.json").toFile();
        buildConfig.write(buildFile);
    }

    public static void createFilesAndDir(ProjectConfig config, String templateLocation){
        System.out.println(Logging.TASK + "Creating placeholder files");
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
                Paths.get(projectDir, sourceDir, location).toFile().mkdirs();
                File file = Paths.get(projectDir, sourceDir, location, name).toFile();
                try {
                    FileOutputStream writer = new FileOutputStream(file);
                    writer.write(content.getBytes());
                } catch (IOException e){
                    System.err.println(Logging.ERROR + "Failed to write file " + name);
                    e.printStackTrace();
                    System.exit(1);
                }
            } else {
                File file = Paths.get(projectDir, sourceDir, location, name).toFile();
                try{
                    FileWriter writer = new FileWriter(file);
                    writer.write("");
                } catch (IOException e){
                    System.err.println(Logging.ERROR + "Failed to write file " + name);
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
            System.err.println(Logging.ERROR + "template not found");
            System.exit(1);
        } catch (IOException e){
            System.err.println(Logging.ERROR + "An error occured while reading the template");
            e.printStackTrace();
            System.exit(1);
        }
        return fileString;
    }

    public static String getTemplateChoice(){
        System.out.println(Logging.TASK + "Get template name");
        System.out.println(Logging.ACTION + "Enter the template name(leave empty for default): ");
        Scanner inScan = new Scanner(System.in);
        String name = inScan.nextLine();
        if (name.isEmpty()){
            name = "default";
        }
        return name;
    }

    public static String findTemplates(String templateChoice){
        System.out.println(Logging.TASK + "Finding templates location");
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
            System.err.println(Logging.ERROR + "Could not find required template files");
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
        System.out.println(Logging.TASK + "Building project config");
        System.out.println(Logging.ACTION + "Enter the project name: ");
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
            System.out.println(config.getAbsolutePath());
            System.err.println(Logging.ERROR + "Could not find the project.json file");
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
            System.err.println(Logging.ERROR + "You must specify 1 Main Class to build a config");
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
            System.err.println(Logging.ERROR + "An error occured when writing build.json");
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