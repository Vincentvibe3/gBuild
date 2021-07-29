package com.gbuild.tools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import com.gbuild.util.BuildConfig;
import com.gbuild.util.Logging;

public class Cleaner {

    boolean verbose = false;

    public Cleaner(boolean verbosity){
        verbose=verbosity;
    }
    
    public void clean(BuildConfig config){
        System.out.println(Logging.TASK + "Cleaning Project");
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
                        System.out.println(Logging.REMOVE + elements[i]);
                    } else if (verbose){
                        System.out.println(Logging.ERROR + elements[i]);
                    }
                }
                paths.close();

            } catch (NoSuchFileException filefail){
                if(verbose){
                    System.err.println(Logging.INFO + dirname + " is clean");
                }

            } catch (IOException iofail) {
                System.err.println(Logging.ERROR + "An error occurred while finding files to remove");
                iofail.printStackTrace();
                System.exit(1);

            }
        }
    }
}
