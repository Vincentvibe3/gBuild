package com.gbuild.util;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.platform.win32.WinDef.DWORDByReference;

public class Logging {

    private static final char escape = 27;
    public static final String TASK = "[" + escape + "[36m TASK " + escape + "[0m]: ";
    public static final String COMPLETION = "[\u001B[92m TASKS COMPLETE \u001B[0m] ";
    public static final String ERROR = "[\u001B[31m FAILED \u001B[0m]: ";
    public static final String INFO = "[\u001B[33m INFO \u001B[0m]: ";
    public static final String USAGE = "Usage: [-v] [clean | compile | package | build]";
    public static final String REMOVE = "[\u001B[92m REMOVED \u001B[0m]: ";
    public static final String NOMODE = "Please specify a mode";
    public static final String INVALID_ARG = "Invalid argument";
    public static final String INVALID_ARG_COUNT = "Too many arguments were passed";
    public static final String ACTION = "[\u001B[33m ACTION \u001B[0m]: ";

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

