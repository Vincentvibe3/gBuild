package com.gbuild.util;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinNT.HANDLE;

import com.sun.jna.platform.win32.WinDef.DWORDByReference;

public class Logging {

    private static final char escape = 27;
    private static final int BLUE = 36;
    private static final int GREEN = 92;
    private static final int RED = 31;
    private static final int YELLOW = 33;
    private static final int RESET = 0;

    private static final String NOMODE = "Please specify a mode";
    private static final String INVALID_ARG = "Invalid argument";
    private static final String INVALID_ARG_COUNT = "Too many arguments were passed";
    private static final String USAGE = "Usage: [-v] [clean | compile | package | build]";

    public enum OutTypes{
        TASK,
        COMPLETION,
        ERROR,
        INFO,
        REMOVE, 
        ACTION

    }

    public enum UsageErrors{
        NOMODE,
        INVALID_ARG,
        INVALID_ARG_COUNT
    }

    public static void usage(UsageErrors error){
        switch (error){
            case NOMODE:
                System.out.println(NOMODE);
                break;
            case INVALID_ARG:
                System.out.println(INVALID_ARG);
                break;
            case INVALID_ARG_COUNT:
                System.out.println(INVALID_ARG_COUNT);
                break;
        }
        System.out.println(USAGE);
    }

    public static void print(String message, OutTypes type){
        String color="";
        String reset = escape + "[" + RESET + "m";
        String prefix = "";
        switch (type){
            case TASK:
                color = escape + "[" + BLUE + "m";
                
                break;
            case COMPLETION:
                color = escape + "[" + GREEN+ "m";
                break;
            case ERROR: case REMOVE:
                color = escape + "[" + RED + "m";
                break;
            case INFO: case ACTION:
                color = escape + "[" + YELLOW + "m";
                break;

        }
        prefix = "[ " + color + type + reset + " ]: ";
        if (type != Logging.OutTypes.ERROR){
            System.out.println(prefix+message);
        } else {
            System.err.println(prefix+message);
        }
        

    }

    public interface Kernel32 extends Library{
        public HANDLE GetStdHandle(DWORD STD_OUTPUT_HANDLE);
        public boolean GetConsoleMode(HANDLE hConsoleHandle, DWORDByReference lpMode);
        public boolean SetConsoleMode(HANDLE hConsoleHandle, DWORD dwMode);
    }

    public static void enableColors(){
        if (System.getProperty("os.name").equals("Windows 10")){
            Kernel32 k32 = Native.load("kernel32", Kernel32.class);
            DWORDByReference lpMode = new DWORDByReference(new DWORD(0));
            DWORD STD_OUTPUT_HANDLE = new DWORD(-11);
            int ENABLE_VIRTUAL_TERMINAL_PROCESSING = 4;
            HANDLE hConsoleHandle = k32.GetStdHandle(STD_OUTPUT_HANDLE);
            k32.GetConsoleMode(hConsoleHandle, lpMode);
            DWORD dwMode = lpMode.getValue();
            dwMode.setValue(dwMode.intValue() | ENABLE_VIRTUAL_TERMINAL_PROCESSING);
            k32.SetConsoleMode(hConsoleHandle, dwMode);
        }
    }
}

