# gBuild
Build Tool for Java


[![Build main](https://github.com/Vincentvibe3/gBuild/actions/workflows/Build.yml/badge.svg?branch=main)](https://github.com/Vincentvibe3/gBuild/actions/workflows/Build.yml)
[![Build releases](https://github.com/Vincentvibe3/gBuild/actions/workflows/Releases.yml/badge.svg)](https://github.com/Vincentvibe3/gBuild/actions/workflows/Releases.yml)
[![MIT license](https://img.shields.io/badge/License-MIT-blue.svg)](https://github.com/Vincentvibe3/gBuild/blob/main/LICENSE)


## Dependencies
[JSON-java(org.json)](https://github.com/stleary/JSON-java)

[JNA(org.sun.jna)](https://github.com/java-native-access/jna)
  
## Installation
 Download a zip from the [releases page](https://github.com/Vincentvibe3/gBuild/releases) and set the ```bin``` directory to ```PATH```
 
 On unix systems make the gBuild script executable with ```chmod +x```.

 Make sure the ```JAVA_HOME``` environment variable is set on Windows or if ```which java``` does not return any output on UNIX systems

## How to use
  ```gBuild [-v] [clean | compile | package | build | create]```

  or 

  ```gBuild.cmd [-v] [clean | compile | package | build | create]``` on Windows
  
## Configuration
  A ```build.json``` file in the root of the project is used to configure the build. An example can be found [here](https://github.com/Vincentvibe3/gBuild/blob/main/examples/build.json)

## Creating Projects
  A ```project.json``` file is used as a template from the project structure. An example can be found [here](https://github.com/Vincentvibe3/gBuild/blob/main/examples/project.json)
  
  All ```.java``` files defined in ```project.json``` will be replaced by a formatted ```template.java```. An example can be found [here](https://github.com/Vincentvibe3/gBuild/blob/main/examples/template.java)
  
  To create a new project template create a directory in ```gBuild/templates``` with the name of the template, then add ```template.java``` and ```project.json``` to it.
