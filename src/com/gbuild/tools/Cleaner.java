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
        Logging.print("Cleaning Project", Logging.OutTypes.TASK);
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
                        Logging.print(elements[i].toString(), Logging.OutTypes.REMOVE);
                    } else if (verbose){
                        Logging.print(elements[i].toString(), Logging.OutTypes.ERROR);
                    }
                }
                paths.close();

            } catch (NoSuchFileException filefail){
                if(verbose){
                    Logging.print(dirname + " is clean", Logging.OutTypes.INFO);
                }

            } catch (IOException iofail) {
                Logging.print("An error occurred while finding files to remove", Logging.OutTypes.ERROR);
                iofail.printStackTrace();
                System.exit(1);

            }
        }
    }
}
