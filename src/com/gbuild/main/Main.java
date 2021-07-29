package com.gbuild.main;

import java.time.Instant;

import com.gbuild.tools.Cleaner;
import com.gbuild.tools.Compiler;
import com.gbuild.tools.ConfigBuilder;
import com.gbuild.tools.Defaults;
import com.gbuild.tools.Packager;
import com.gbuild.util.BuildConfig;
import com.gbuild.util.Logging;

public class Main {
    private static boolean verbose = false;
    private static String mode = null;

    public static void main(String[] args) throws Exception {
        long startTime = Instant.now().toEpochMilli();
        try{
            if (args.length > 2){
                System.out.println(Logging.INVALID_ARG_COUNT);
                System.out.println(Logging.USAGE);
                System.exit(1);
            }

            else if (args[0].equals("-v")){
                verbose = true;
                mode = args[1];
                
            } else if (args[0].equals("create") || args[0].equals("clean") || args[0].equals("build") || args[0].equals("package") || args[0].equals("compile")){
                mode = args[0];

            } else {
                System.out.println(Logging.INVALID_ARG);
                System.out.println(Logging.USAGE);
                System.exit(1);

            }

        } catch (IndexOutOfBoundsException indexfail){
            System.out.println(Logging.NOMODE);
            System.out.println(Logging.USAGE);
            System.exit(1);
        }
        ConfigBuilder builder = new ConfigBuilder(verbose);
        BuildConfig config = builder.read(mode);
        
        if (mode.equals("create")){
            Defaults.createProject();

        } else if (mode.equals("clean")){
            Cleaner cleaner = new Cleaner(verbose);
            cleaner.clean(config);

        } else if (mode.equals("compile")){
            Compiler compiler = new Compiler(verbose);
            Cleaner cleaner = new Cleaner(verbose);
            cleaner.clean(config);
            compiler.compile(config);

        } else if (mode.equals("package")){
            Packager packager = new Packager(verbose);
            packager.createJar(config);

        } else if (mode.equals("build")){
            Compiler compiler = new Compiler(verbose);
            Packager packager = new Packager(verbose);
            Cleaner cleaner = new Cleaner(verbose);
            cleaner.clean(config);
            compiler.compile(config);
            packager.createJar(config);

        } else {
            System.out.println(Logging.NOMODE);
            System.out.println(Logging.USAGE);
            System.exit(1);
        }

        long endTime = Instant.now().toEpochMilli();
        System.out.println(Logging.COMPLETION + "Completed in " + (endTime-startTime) + " ms");
        System.exit(0);

    }

}