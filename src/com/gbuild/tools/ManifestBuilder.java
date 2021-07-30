package com.gbuild.tools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Stream;

import com.gbuild.util.BuildConfig;
import com.gbuild.util.Logging;

import org.json.JSONObject;

public class ManifestBuilder {
    public static Manifest buildManifest(JSONObject attr, BuildConfig config){
        Manifest manifest = new Manifest();
        if(config.getVer() != null){
            manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, config.getVer());
        }

        if(attr.has("MainClass")){
            manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, attr.getString("MainClass"));
        }

        if(config.getDependencies() != null){
            manifest.getMainAttributes().put(Attributes.Name.CLASS_PATH, getClassPath(config.getDependencies()));
        }

        return manifest;
    }

    public static String getClassPath(String libPath){
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
            Logging.print("An error occurred while finding files", Logging.OutTypes.ERROR);
            iofail.printStackTrace();
            System.exit(1);
        }
        return classPath;
    }
}
