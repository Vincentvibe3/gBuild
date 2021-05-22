# gBuild
Build Tool for Java


[![Build main](https://github.com/Vincentvibe3/gBuild/actions/workflows/Build.yml/badge.svg?branch=main)](https://github.com/Vincentvibe3/gBuild/actions/workflows/Build.yml)
[![Build releases](https://github.com/Vincentvibe3/gBuild/actions/workflows/Releases.yml/badge.svg)](https://github.com/Vincentvibe3/gBuild/actions/workflows/Releases.yml)
[![MIT license](https://img.shields.io/badge/License-MIT-blue.svg)](https://github.com/Vincentvibe3/gBuild/blob/main/LICENSE)


## Dependencies
[JSON-java(package org.json)](https://github.com/stleary/JSON-java)
  
## Installation
 Download a zip from the [releases page](https://github.com/Vincentvibe3/gBuild/releases) and set the ```bin``` directory to ```PATH```

 Make sure the ```JAVA_HOME``` environment variable is set on Windows or if ```which java``` does not return any output on UNIX systems

## How to use
  ```gBuild [-v] [clean | compile | build]```

  or 

  ```gBuild.cmd [-v] [clean | compile | build]``` on Windows
  
  A ```build.json``` file in the root of the project is used to configure the build. An example can be found [here](https://github.com/Vincentvibe3/gBuild/blob/main/build.json)
