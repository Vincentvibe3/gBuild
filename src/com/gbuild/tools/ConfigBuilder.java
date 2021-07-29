package com.gbuild.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.Manifest;

import com.gbuild.util.BuildConfig;
import com.gbuild.util.Logging;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class ConfigBuilder {

    boolean verbose = false;

    public ConfigBuilder(boolean verbosity){
        verbose = verbosity;
    }

    public BuildConfig read(String mode){
        System.out.println(Logging.TASK + "Fetching Config");
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
                Manifest manifest = ManifestBuilder.buildManifest(data.getJSONObject("manifest"), config);
                config.setManifest(manifest);
                if (verbose){
                    config.getManifestInfo();
                }
            }
   
        } catch(FileNotFoundException filefail){
            System.err.println(Logging.ERROR + "No config(build.json) found in current directory");
            System.err.println(Logging.ERROR + "Please create a build.json config file");
            System.exit(1);

        } catch (IOException iofail){
            System.err.print(Logging.ERROR);
            System.err.println(Logging.ERROR + "An error occurred while reading the build.json file");
            iofail.printStackTrace();
            System.exit(1);

        } catch(JSONException jsonfail){
            System.err.println(Logging.ERROR + "build.json is malformed");
            System.err.println(Logging.ERROR + "Check build.json");
            System.exit(1);

        }
        return config;
    }
}
