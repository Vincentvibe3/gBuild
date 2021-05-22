package com.gbuild.build;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Stream;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.JSONException;

public class main {
    private static boolean verbose = false;
    private static String mode = "";
    private static String usage = "Usage: [-v] [clean | compile | build]";
    public static void main(String[] args) throws Exception {
        long startTime = Instant.now().toEpochMilli();
        try{
            if (args.length > 2){
                System.out.println("Too many arguments were passed");
                System.out.println(usage);
                System.exit(1);
            }

            else if (args[0].equals("-v")){
                verbose = true;
                mode = args[1];
                
            } else if (args[0].equals("clean") || args[0].equals("build") || args[0].equals("compile")){
                mode = args[0];

            } else {
                System.out.println("Invalid argument");
                System.out.println(usage);
                System.exit(1);

            }

        } catch (IndexOutOfBoundsException indexfail){
            System.out.println("Please specify a mode");
            System.out.println(usage);
            System.exit(1);
        }

        if (mode.equals("clean")){
            BuildConfig config = readConfig();
            clean(config);

        } else if (mode.equals("compile")){
            BuildConfig config = readConfig();
            clean(config);
            Path[] files = getFilesToCompile(config.getSource());
            compile(files, config);

        } else if (mode.equals("build")){
            BuildConfig config = readConfig();
            clean(config);
            Path[] files = getFilesToCompile(config.getSource());
            compile(files, config);
            createJar(config);

        } else {
            System.out.println("Please specify a mode");
            System.out.println("Usage: [clean | compile | build]");
            System.exit(1);
        }

        long endTime = Instant.now().toEpochMilli();
        System.out.println("[[92m TASKS COMPLETE [0m] Completed in "+(endTime-startTime)+" ms");
        System.exit(0);

    }

    public static void clean(BuildConfig config){
        System.out.println("[[36m TASK [0m]: Cleaning Project");
        String buildDirName = config.getBuildDir();
        String binDirName = config.getBinDir();
        String[] dirs = {buildDirName, binDirName};
        for (String dirname : dirs){
            try {
                Stream<Path> paths = Files.walk(Paths.get(dirname));
                Path[] elements = paths.toArray(Path[]::new);
                for (var i = elements.length-1; i>=0; i--){
                    boolean success = elements[i].toFile().delete();
                    if (success && verbose){
                        System.out.println("[[92m REMOVED [0m]: " + elements[i]);
                    } else if (verbose){
                        System.out.println("[[31m FAILED [0m]: "+ elements[i]);
                    }
                }
                paths.close();

            } catch (NoSuchFileException filefail){
                if(verbose){
                    System.err.println("[[33m INFO [0m]: "+dirname+" is clean");
                }

            } catch (IOException iofail) {
                System.err.print("[[31m FAILED [0m]: ");
                iofail.printStackTrace();
                System.exit(1);

            }
        }
    }

    public static String getClassPath(String libPath){
        String classPath = ".;";
        StringBuilder builder = new StringBuilder();
        try {
            Stream<Path> paths = Files.walk(Paths.get(libPath));
            Path[] allFiles = paths.filter(item -> item.toFile().isFile()).toArray(Path[]::new);
            for (Path filename: allFiles){
                builder.append("./");
                builder.append(filename.toString().replace("\\", "/"));
                if (System.getProperty("os.name").startsWith("Windows")){
                    builder.append("/;");
                } else{
                    builder.append("/:");
                }
            }
            paths.close();
            classPath = builder.toString();

        } catch (IOException iofail) {
            System.err.print("[[31m FAILED [0m]: ");
            iofail.printStackTrace();
            System.exit(1);
        }
        return classPath;
    }

    public static String getClassPathManifest(String libPath){
        String classPath = ".;";
        StringBuilder builder = new StringBuilder();

        try {
            Stream<Path> paths = Files.walk(Paths.get(libPath));
            Path[] allFiles = paths.filter(item -> item.toFile().isFile()).toArray(Path[]::new);
            for (Path filename: allFiles){
                builder.append("../");
                builder.append(filename.toString().replace("\\", "/"));
                builder.append(" ");
            }
            paths.close();
            classPath = builder.toString();

        } catch (IOException iofail) {
            System.err.print("[[31m FAILED [0m]: ");
            iofail.printStackTrace();
            System.exit(1);
        }
        return classPath;
    }

    public static Path[] getFilesToCompile(String basePath){
        System.out.println("[[36m TASK [0m]: Fetching Files");
        Path[] allFiles = null;
        try {
            
            Stream<Path> paths = Files.walk(Paths.get(basePath));
            allFiles = paths.filter(item -> item.toFile().isFile()).toArray(Path[]::new);
            paths.close();

        } catch (IOException iofail) {
            System.err.print("[[31m FAILED [0m]: ");
            iofail.printStackTrace();
            System.exit(1);
        }     
        return allFiles;
    }

    public static BuildConfig readConfig(){
        System.out.println("[[36m TASK [0m]: Fetching Config");
        Path path = Paths.get("build.json");
        File configFile = path.toFile();

        BuildConfig config = new BuildConfig();

        
        try {
            FileReader reader = new FileReader(configFile);
            JSONObject data = new JSONObject(new JSONTokener(reader));
            reader.close();
            config.setBuildDir(data.getString("buildDir"));
            config.setBinDir(data.getString("binDir"));
            if (!mode.equals("clean")){
                config.setDependencies(data.getString("dependencies"));
                config.setName(data.getString("name"));
                config.setVer(data.getString("Version"));
                config.setSource(data.getString("source"));
                Manifest manifest = buildManifest(data.getJSONObject("manifest"), config);
                config.setManifest(manifest);
                if (verbose){
                    config.getManifestInfo();
                }
            }
   
        } catch(FileNotFoundException filefail){
            System.err.println("[[31m FAILED [0m]: No config(build.json) found in current directory");
            System.err.println("[[31m FAILED [0m]: Please create a build.json config file");
            System.exit(1);

        } catch (IOException iofail){
            System.err.print("[[31m FAILED [0m]: ");
            iofail.printStackTrace();
            System.exit(1);

        } catch(JSONException jsonfail){
            System.err.println("[[31m FAILED [0m]: build.json is malformed");
            System.err.println("[[31m FAILED [0m]: Check build.json");
            System.exit(1);

        }
        return config;
    }

    public static Manifest buildManifest(JSONObject attr, BuildConfig config){
        Manifest manifest = new Manifest();
        if(config.getVer() != null){
            manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, config.getVer());
        }

        if(attr.has("MainClass")){
            manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, attr.getString("MainClass"));
        }

        if(config.getDependencies() != null){
            manifest.getMainAttributes().put(Attributes.Name.CLASS_PATH, getClassPathManifest(config.getDependencies()));
        }

        return manifest;
    }

    public static void compile(Path[] files, BuildConfig config){
        System.out.println("[[36m TASK [0m]: Compiling");
        JavaCompiler compiler =  ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager filemanager = compiler.getStandardFileManager(null, null, null);
        Iterable<? extends JavaFileObject> toCompile = filemanager.getJavaFileObjectsFromPaths(Arrays.asList(files));
        List<String> options = getOptions(config);
        compiler.getTask(null, filemanager, null, options, null, toCompile).call();

        try{
            filemanager.close();

        } catch(IOException iofail){
            iofail.printStackTrace();
            System.exit(1);
        }
        
    }

    public static List<String> getOptions(BuildConfig config){
        List<String> options = new ArrayList<>();
        String dependencies = config.getDependencies();
        String classPath = getClassPath(dependencies);
        String buildDir = config.getBuildDir();
        options.addAll(Arrays.asList("-classpath", classPath));
        options.addAll(Arrays.asList("-d", "./"+buildDir));
        if (verbose){
            System.out.println("[[33m INFO [0m]: Options: "+options);
        }
        return options;
    }

    public static String[] getCompiledClasses(BuildConfig config){
        String dir = config.getBuildDir();
        ArrayList<String> allClasses = new ArrayList<>();

        try {
            Stream<Path> paths = Files.walk(Paths.get(dir));
            paths.filter(item -> item.toFile().isFile()).forEach(item -> allClasses.add(item.toString()));
            paths.close();

        } catch (IOException iofail) {
            System.err.print("[[31m FAILED [0m]: ");
            iofail.printStackTrace();
            System.exit(1);
        }
        return allClasses.toArray(String[]::new);
    }

    public static void createJar(BuildConfig config){
        System.out.println("[[36m TASK [0m]: Packaging Jar");
        Manifest manifest = config.getManifest();

        try {
            new File("./"+config.getBinDir()).mkdirs();
            File jarfile = Paths.get(config.getBinDir()+"/"+config.getName()+"-"+config.getVer()+".jar").toFile();
            FileOutputStream jar = new FileOutputStream(jarfile);
            JarOutputStream jarsStream = new JarOutputStream(jar, manifest);
            String[] compiledClasses = getCompiledClasses(config);
            for (String filename : compiledClasses){
                if (verbose){
                    System.out.println("[[33m INFO [0m]: Adding "+filename);
                }
                JarEntry nextEntry = new JarEntry(filename.replaceAll("\\\\", "/").replaceFirst(config.getBuildDir()+"/", ""));
                jarsStream.putNextEntry(nextEntry);
                File file = Paths.get(filename).toFile();
                FileInputStream fileInputStream = new FileInputStream(file);
                jarsStream.write(fileInputStream.readAllBytes());
                fileInputStream.close();
                jarsStream.closeEntry();
            }
            jarsStream.close();

        } catch (FileNotFoundException filefail){
            System.err.print("[[31m FAILED [0m]: ");
            filefail.printStackTrace();
            System.exit(1);

        } catch (IOException iofail){
            System.err.print("[[31m FAILED [0m]: ");
            iofail.printStackTrace();
            System.exit(1);
        }
        
    }

}

class BuildConfig{
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
        System.out.println("[[33m INFO [0m]: Manifest Info");
        Attributes.Name[] attrs = {Attributes.Name.MANIFEST_VERSION, Attributes.Name.MAIN_CLASS, Attributes.Name.CLASS_PATH};
        for (Attributes.Name attr : attrs){
            System.out.println("[[33m INFO [0m]: "+attr.toString()+": "+manifest.getMainAttributes().getValue(attr));
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
