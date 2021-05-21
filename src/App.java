
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

public class App {
    public static void main(String[] args) throws Exception {
        String mode = "";

        try{
            mode = args[0];

        } catch (IndexOutOfBoundsException indexfail){
            System.out.println("Please specify a mode");
            System.out.println("Usage: [clean | compile | build]");
            System.exit(1);
        }

        if (mode.equals("clean")){
            System.out.println("Fetching config");
            BuildConfig config = readConfig();
            System.out.println("Cleaning project");
            cleanBuild(config);
            cleanBin(config);
            System.out.println("DONE");

        } else if (mode.equals("compile")){
            System.out.println("Fetching config");
            BuildConfig config = readConfig();
            System.out.println("Cleaning project");
            cleanBuild(config);
            cleanBin(config);
            System.out.println("Fetching files");
            Path[] files = getFilesToCompile(config.getSource());
            System.out.println("Compiling");
            compile(files, config);
            System.out.println("DONE");

        } else if (mode.equals("build")){
            System.out.println("Fetching config");
            BuildConfig config = readConfig();
            System.out.println("Cleaning project");
            cleanBuild(config);
            cleanBin(config);
            System.out.println("Fetching files");
            Path[] files = getFilesToCompile(config.getSource());
            System.out.println("Compiling");
            compile(files, config);
            System.out.println("Creating jar");
            createJar(config);
            System.out.println("DONE");

        } else {
            System.out.println("Please specify a mode");
            System.out.println("Usage: [clean | compile | build]");
            System.exit(1);
        }

        System.exit(0);

    }

    public static void cleanBuild(BuildConfig config){
        String buildDirName = config.getBuildDir();

        try {
            Stream<Path> pathsBuild = Files.walk(Paths.get(buildDirName));
            Path[] buildElements = pathsBuild.toArray(Path[]::new);
            for (var i = buildElements.length-1; i>=0; i--){
                boolean success = buildElements[i].toFile().delete();
                if (success){
                    System.out.println("Removed: " + buildElements[i]);
                } else{
                    System.out.println("Failed: " + buildElements[i]);
                }
            }
            pathsBuild.close();

        } catch (NoSuchFileException filefail){
                System.err.println("No builds to clean");

        } catch (IOException iofail) {
            iofail.printStackTrace();
            System.exit(1);

        } 
    }

    public static void cleanBin(BuildConfig config){
        String binDirName = config.getBinDir();

        try {
            Stream<Path> pathsBin = Files.walk(Paths.get(binDirName));
            Path[] binElements = pathsBin.toArray(Path[]::new);
            for (var i = binElements.length-1; i>=0; i--){
                boolean success = binElements[i].toFile().delete();
                if (success){
                    System.out.println("Removed: " + binElements[i]);
                } else{
                    System.out.println("Failed: " + binElements[i]);
                }
            }
            pathsBin.close();

        } catch (NoSuchFileException filefail){
                System.err.println("No binaries to clean");

        } catch (IOException iofail) {
            iofail.printStackTrace();
            System.exit(1);

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
            iofail.printStackTrace();
            System.exit(1);
        }
        return classPath;
    }

    public static Path[] getFilesToCompile(String basePath){
        Path[] allFiles = null;
        try {
            
            Stream<Path> paths = Files.walk(Paths.get(basePath));
            allFiles = paths.filter(item -> item.toFile().isFile()).toArray(Path[]::new);
            paths.close();

        } catch (IOException iofail) {
            iofail.printStackTrace();
            System.exit(1);
        }     
        return allFiles;
    }

    public static BuildConfig readConfig(){
        Path path = Paths.get("build.json");
        File configFile = path.toFile();

        BuildConfig config = new BuildConfig();

        try {
            FileReader reader = new FileReader(configFile);
            JSONObject data = new JSONObject(new JSONTokener(reader));
            reader.close();
            config.setDependencies(data.getString("dependencies"));
            config.setSource(data.getString("source"));
            config.setBuildDir(data.getString("buildDir"));
            config.setName(data.getString("name"));
            config.setBinDir(data.getString("binDir"));
            config.setVer(data.getString("Version"));
            Manifest manifest = buildManifest(data.getJSONObject("manifest"), config);
            config.setManifest(manifest);
            config.getManifestInfo();

        } catch(FileNotFoundException filefail){
            System.err.println("No build config(build.xml) found in current directory");
            System.exit(1);

        } catch (IOException iofail){
            iofail.printStackTrace();
            System.exit(1);

        } catch(JSONException jsonfail){
            System.err.println("build.json file is malformed");
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
        System.out.println("Options: "+options);
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
            iofail.printStackTrace();
            System.exit(1);
        }
        return allClasses.toArray(String[]::new);
    }

    public static void createJar(BuildConfig config){
        Manifest manifest = config.getManifest();
        try {
            new File("./"+config.getBinDir()).mkdirs();
            File jarfile = Paths.get(config.getBinDir()+"/"+config.getName()+"-"+config.getVer()+".jar").toFile();
            FileOutputStream jar = new FileOutputStream(jarfile);
            JarOutputStream jarsStream = new JarOutputStream(jar, manifest);
            String[] compiledClasses = getCompiledClasses(config);
            for (String filename : compiledClasses){
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
            filefail.printStackTrace();
            System.exit(1);

        } catch (IOException iofail){
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
        System.out.println("Manifest Info:");
        Attributes.Name[] attrs = {Attributes.Name.MANIFEST_VERSION, Attributes.Name.MAIN_CLASS, Attributes.Name.CLASS_PATH};
        for (Attributes.Name attr : attrs){
            System.out.println(attr.toString()+": "+manifest.getMainAttributes().getValue(attr));
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
