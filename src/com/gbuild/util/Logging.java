package com.gbuild.util;

public class Logging {
    public final static String TASK = "[\u001B[36m TASK \u001B[0m]: ";
    public final static String COMPLETION = "[\u001B[92m TASKS COMPLETE \u001B[0m] ";
    public final static String ERROR = "[\u001B[31m FAILED \u001B[0m]: ";
    public final static String INFO = "[\u001B[33m INFO \u001B[0m]: ";
    public final static String USAGE = "Usage: [-v] [clean | compile | package | build]";
    public final static String REMOVE = "[\u001B[92m REMOVED \u001B[0m]: ";
    public final static String NOMODE = "Please specify a mode";
    public final static String INVALID_ARG = "Invalid argument";
    public final static String INVALID_ARG_COUNT = "Too many arguments were passed";
    public final static String ACTION = "[\u001B[33m ACTION \u001B[0m]: ";
}
