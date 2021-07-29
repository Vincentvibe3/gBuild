package com.gbuild.build;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;

import java.util.jar.Manifest;
import java.util.stream.Stream;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.JSONException;

public class build {
    private static boolean verbose = false;
    private static String mode = "";
    private static String usage = "Usage: [-v] [clean | compile | package | build]";
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
        BuildConfig config = readConfig();
        if (mode.equals("clean")){
            clean(config);

        } else if (mode.equals("compile")){
            Compiler compiler = new Compiler(verbose);
            clean(config);
            compiler.compile(config);

        } else if (mode.equals("package")){
            Packager packager = new Packager(verbose);
            packager.createJar(config);

        } else if (mode.equals("build")){
            Compiler compiler = new Compiler(verbose);
            Packager packager = new Packager(verbose);
            clean(config);
            compiler.compile(config);
            packager.createJar(config);

        } else {
            System.out.println("Please specify a mode");
            System.out.println(usage);
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
                Manifest manifest = manifestBuilder.buildManifest(data.getJSONObject("manifest"), config);
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

}