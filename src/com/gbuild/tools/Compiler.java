package com.gbuild.tools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import javax.tools.ToolProvider;

import com.gbuild.util.BuildConfig;
import com.gbuild.util.Logging;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;

public class Compiler {

    boolean verbose = false;

    public Compiler(boolean verbosity){
        verbose = verbosity;
    }

    public void compile(BuildConfig config){
        Path[] files = getFilesToCompile(config.getSource());
        Logging.print("Compiling", Logging.OutTypes.TASK);
        JavaCompiler compiler =  ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager filemanager = compiler.getStandardFileManager(null, null, null);
        Iterable<? extends JavaFileObject> toCompile = filemanager.getJavaFileObjectsFromPaths(Arrays.asList(files));
        List<String> options = getOptions(config);
        if (verbose){
            Logging.print(options.toString(), Logging.OutTypes.INFO);
        }
        compiler.getTask(null, filemanager, null, options, null, toCompile).call();

        try{
            filemanager.close();

        } catch(IOException iofail){
            Logging.print("An error occurred while closing the file manager", Logging.OutTypes.ERROR);
            iofail.printStackTrace();
            System.exit(1);
        }
        
    }

    public List<String> getOptions(BuildConfig config){
        List<String> options = new ArrayList<>();
        String dependencies = config.getDependencies();
        String classPath = getClassPath(dependencies);
        String buildDir = config.getBuildDir();
        options.addAll(Arrays.asList("-classpath", classPath));
        options.addAll(Arrays.asList("-d", "./"+buildDir));
        return options;
    }
    
    public String getClassPath(String libPath){
        String classPath = ".;";
        StringBuilder builder = new StringBuilder();
        try {
            Stream<Path> paths = Files.walk(Paths.get(libPath));
            Path[] allFiles = paths.filter(item -> item.toFile().isFile()).toArray(Path[]::new);
            for (Path filename: allFiles){
                if (verbose){
                    Logging.print("Adding " + filename.toString() + " to ClassPath", Logging.OutTypes.INFO);
                }
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
            Logging.print("An error occurred while finding files", Logging.OutTypes.ERROR);
            iofail.printStackTrace();
            System.exit(1);
        }
        return classPath;
    }

    public static Path[] getFilesToCompile(String basePath){
        Logging.print("Fetching Files", Logging.OutTypes.TASK);
        Path[] allFiles = null;
        try {
            
            Stream<Path> paths = Files.walk(Paths.get(basePath));
            allFiles = paths.filter(item -> item.toFile().isFile() && item.toFile().getName().endsWith(".java")).toArray(Path[]::new);
            paths.close();

        } catch (IOException iofail) {
            Logging.print("An error occurred while fetching files", Logging.OutTypes.ERROR);
            iofail.printStackTrace();
            System.exit(1);
        }     
        return allFiles;
    }
}
