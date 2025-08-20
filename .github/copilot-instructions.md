# MuPDF Kotlin Multiplatform (mupdf-kmp)

MuPDF Kotlin Multiplatform is a library that brings MuPDF functionality to Kotlin Multiplatform projects, supporting JVM, iOS, and Web (WASM) targets. The project provides PDF rendering capabilities across multiple platforms using native MuPDF bindings.

Always reference these instructions first and fallback to search or bash commands only when you encounter unexpected information that does not match the info here.

## Working Effectively

### Bootstrap and Dependencies
- Install system dependencies: `sudo apt-get update && sudo apt-get install -y libfreetype-dev build-essential openjdk-21-jdk`
- Set up Java 21 environment: 
  ```bash
  sudo update-alternatives --set java /usr/lib/jvm/java-21-openjdk-amd64/bin/java
  sudo update-alternatives --set javac /usr/lib/jvm/java-21-openjdk-amd64/bin/javac
  export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
  ```
- Initialize git submodules: `git submodule update --init --recursive` -- takes 3-5 minutes. NEVER CANCEL.
- Make gradlew executable: `chmod +x ./gradlew`

### Build Commands and Timing
- **CRITICAL**: All builds require Java 21, not Java 17. The project will fail with Java 17.
- List available tasks: `./gradlew tasks` -- takes 5-10 seconds after setup
- Build library module: `./gradlew :lib:build` -- takes 3-5 seconds (incremental) or 15-20 seconds (clean build). NEVER CANCEL.
- Build native MuPDF library: `./gradlew :lib:buildLibmupdfLinuxAMD64` -- takes 3.5-4 minutes. NEVER CANCEL. Set timeout to 10+ minutes.
- Copy native library to resources: `./gradlew :lib:copyLibmupdfJava64So` -- takes 1-2 seconds
- Clean all builds: `./gradlew clean` -- takes 1-2 seconds

### Platform Support Limitations
- **NETWORK RESTRICTIONS**: Cannot access Google Maven repository (dl.google.com), which prevents Android builds
- **WORKING PLATFORMS**: JVM/desktop and native library builds work correctly
- **BLOCKED FEATURES**: Android target, some Compose dependencies, androidx libraries
- To work around network restrictions, comment out Android plugins in build files when testing

### Native Library Build Process
- Native library compilation is required for JVM functionality
- Linux x64 library builds successfully and produces ~45MB `.so` file
- Build process includes Java compilation, C compilation, and shared library linking
- Generated library: `./lib/src/jvmMain/resources/Linux-amd64/mupdf_java.so`

## Validation and Testing

### Manual Validation Steps
- Always run native library build, copy, and library build as separate steps due to task dependencies:
  1. `./gradlew :lib:buildLibmupdfLinuxAMD64` -- builds native library (3.5 minutes)
  2. `./gradlew :lib:copyLibmupdfJava64So` -- copies to resources (1-2 seconds)  
  3. `./gradlew :lib:build` -- builds library module (3-5 seconds)
- Verify native library exists: `ls -la ./lib/src/jvmMain/resources/Linux-amd64/mupdf_java.so`
- Library should be approximately 45MB and executable
- Test MuPDF Java examples in `./mupdf/build/java/release/` directory
- **SCENARIO VALIDATION**: Cannot perform full end-to-end UI validation due to network restrictions, but native library builds and JVM compilation work correctly

### Build Validation Checklist
1. Java 21 is active: `java -version` should show version 21
2. Submodules initialized: `./mupdf/` directory should contain source files  
3. System dependencies installed: `pkg-config --exists freetype2` should succeed
4. Gradle tasks complete: `./gradlew tasks` should list available tasks
5. Native library builds: `./gradlew :lib:buildLibmupdfLinuxAMD64` completes without errors
6. Library module builds: `./gradlew :lib:build` completes with warnings but no errors

## Repository Structure
- `/lib/` - Main Kotlin Multiplatform library module with MuPDF bindings
- `/composeApp/` - Sample Compose Multiplatform application (Android support disabled due to network restrictions)
- `/build-logic/` - Custom Gradle plugins for MsBuild integration and version management
- `/mupdf/` - Git submodule containing MuPDF source code
- `/.github/workflows/` - CI/CD workflows for Windows and Linux native library builds

## Common Tasks and Output Reference

### Repository Root Contents
```
.
├── README.md
├── build.gradle.kts
├── settings.gradle.kts  
├── gradle.properties
├── gradlew
├── lib/
├── composeApp/
├── build-logic/
├── mupdf/           # Git submodule - initially empty
├── .github/
└── .gitmodules
```

### Available Gradle Tasks (Key Ones)
```
Build tasks:
- build - Assembles and tests this project
- buildLibmupdfLinuxAMD64 - Builds libmupdf_java64.so (3.5 minutes)
- copyLibmupdfJava64So - Copies library to resources folder
- desktopJar - Assembles desktop application JAR

Publishing tasks:
- publishToMavenLocal - Publishes to local Maven repository

Verification tasks:
- check - Runs all checks
```

### Known Limitations and Workarounds
- **Android builds fail**: Comment out `alias(libs.plugins.androidApplication)` and `alias(libs.plugins.androidLibrary)` in build files
- **External dependencies fail**: Remove or comment androidx and filekit dependencies in build.gradle.kts files
- **Configuration cache issues**: Use `--no-configuration-cache` flag when encountering serialization errors
- **Java version errors**: Ensure both `java` and `javac` are set to Java 21 using `update-alternatives`

## Development Workflow
1. Always ensure Java 21 environment is active before any Gradle commands
2. Initialize submodules on fresh clone: `git submodule update --init --recursive`
3. Build native library: `./gradlew :lib:buildLibmupdfLinuxAMD64`
4. Copy library to resources: `./gradlew :lib:copyLibmupdfJava64So`  
5. Build and test: `./gradlew :lib:build`
6. For publishing: `./gradlew :lib:publishToMavenLocal`

**IMPORTANT**: Run build steps separately due to Gradle task dependency issues. Do not combine them in a single command.

Always run builds with adequate timeouts and never cancel long-running native compilation tasks.