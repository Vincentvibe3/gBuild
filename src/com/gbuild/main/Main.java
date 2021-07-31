package com.gbuild.main;

import java.time.Instant;

import com.gbuild.tools.Cleaner;
import com.gbuild.tools.Compiler;
import com.gbuild.tools.ConfigBuilder;
import com.gbuild.tools.ProjectGenerator;
import com.gbuild.tools.Packager;
import com.gbuild.util.BuildConfig;
import com.gbuild.util.Logging;

public class Main {
    private static boolean verbose = false;
    private static Modes mode = null;

    public enum Modes{
        CREATE,
        CLEAN,
        BUILD,
        PACKAGE,
        COMPILE,
        HELP
    }

    public static void main(String[] args) throws Exception {
        long startTime = Instant.now().toEpochMilli();
        Logging.enableColors();
        try{
            String toCheck = "";
            if (args.length > 2){
                Logging.usage(Logging.UsageErrors.INVALID_ARG_COUNT);
                System.exit(1);
            }
            else if (args[0].equals("-v")){
                verbose = true;
                toCheck = args[1];
            } else {
                toCheck = args[0];
            }
            mode = Modes.valueOf(toCheck.toUpperCase());
        } catch (IllegalArgumentException e){
            Logging.usage(Logging.UsageErrors.INVALID_ARG);
            System.exit(1);

        } catch (IndexOutOfBoundsException indexfail){
            Logging.usage(Logging.UsageErrors.NOMODE);
            System.exit(1);
        }
        
        if (mode.equals(Modes.CREATE)){
            ProjectGenerator.createProject(verbose);

        } else if (mode.equals(Modes.HELP)){
            Logging.usage(Logging.UsageErrors.NONE);

        } else {
            ConfigBuilder builder = new ConfigBuilder(verbose);
            BuildConfig config = builder.read(mode.toString().toLowerCase());
            
            Cleaner cleaner;
            Compiler compiler;
            Packager packager;

            switch (mode){
                case CLEAN:
                    cleaner = new Cleaner(verbose);
                    cleaner.clean(config);
                    break;
                case COMPILE:
                    compiler = new Compiler(verbose);
                    compiler.compile(config);
                    break;
                case PACKAGE:
                    packager = new Packager(verbose);
                    packager.createJar(config);
                    break;
                case BUILD:
                    compiler = new Compiler(verbose);
                    packager = new Packager(verbose);
                    cleaner = new Cleaner(verbose);
                    cleaner.clean(config);
                    compiler.compile(config);
                    packager.createJar(config);
                    break;
                default:
                    Logging.usage(Logging.UsageErrors.NOMODE);
                    System.exit(1);
            }
        }
        

        long endTime = Instant.now().toEpochMilli();
        if (!mode.equals(Modes.HELP)){
            Logging.print("Completed in " + (endTime-startTime) + " ms", Logging.OutTypes.COMPLETION);
        }
        System.exit(0);

    }

}