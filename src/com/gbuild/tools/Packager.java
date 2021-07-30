package com.gbuild.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Stream;

import com.gbuild.util.BuildConfig;
import com.gbuild.util.Logging;

public class Packager {

    boolean verbose = false;

    public Packager(boolean verbosity){
        verbose = verbosity;
    }

    public static String[] getCompiledClasses(BuildConfig config){
        String dir = config.getBuildDir();
        ArrayList<String> allClasses = new ArrayList<>();

        try {
            Stream<Path> paths = Files.walk(Paths.get(dir));
            paths.filter(item -> item.toFile().isFile()).forEach(item -> allClasses.add(item.toString()));
            paths.close();

        } catch (IOException iofail) {
            Logging.print("An error occurred while fetching compiled classes", Logging.OutTypes.ERROR);
            iofail.printStackTrace();
            System.exit(1);
        }
        return allClasses.toArray(String[]::new);
    }

    public void createJar(BuildConfig config){
        Logging.print("Packaging Jar", Logging.OutTypes.TASK);
        Manifest manifest = config.getManifest();

        try {
            new File("./"+config.getBinDir()).mkdirs();
            File jarfile = Paths.get(config.getBinDir()+"/"+config.getName()+"-"+config.getVer()+".jar").toFile();
            FileOutputStream jar = new FileOutputStream(jarfile);
            JarOutputStream jarsStream = new JarOutputStream(jar, manifest);
            String[] compiledClasses = getCompiledClasses(config);
            for (String filename : compiledClasses){
                if (verbose){
                    Logging.print("Adding " + filename, Logging.OutTypes.INFO);
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
            Logging.print("A file could not found", Logging.OutTypes.ERROR);
            filefail.printStackTrace();
            System.exit(1);

        } catch (IOException iofail){
            Logging.print("An error occurred while adding a file", Logging.OutTypes.ERROR);
            iofail.printStackTrace();
            System.exit(1);
        }
        
    }
}
