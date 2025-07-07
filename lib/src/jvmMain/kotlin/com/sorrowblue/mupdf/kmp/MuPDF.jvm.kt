package com.sorrowblue.mupdf.kmp

import com.artifex.mupdf.fitz.Context
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.util.Properties
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectory
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.isWritable
import kotlin.io.path.notExists
import kotlin.io.path.pathString
import kotlin.random.Random

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual object MuPDF {

    actual fun init() {
        println("platformBestMatch=$platformBestMatch")
        val usedPlatform = platformBestMatch
        val libProperties = loadLibProperties(usedPlatform)
        val tmpDir = createOrVerifyTmpDir()
        val muPDFTmpDir = getOrCreateMuPDFTmpDir(tmpDir, libProperties)
        val nativeLibraries = copyOrSkipLibraries(usedPlatform, muPDFTmpDir, libProperties)
        println("nativeLibraries=$nativeLibraries")
        @Suppress("UnsafeDynamicallyLoadedCode")
        System.load(nativeLibraries.absolutePathString())

        if (Context.initNative() < 0)
            throw RuntimeException("cannot initialize mupdf library")
        isInitializedSuccessfully = true
    }

    val platformList: List<String> by lazy {
        val propertiesInputStream =
            MuPDF::class.java.getResourceAsStream(
                MUPDF_PLATFORMS_PROPERTIES_FILENAME
            )
        if (propertiesInputStream == null) {
            throw IllegalStateException("Can not find MuPDF platform property file $MUPDF_PLATFORMS_PROPERTIES_FILENAME.")
        }
        val properties = Properties()
        try {
            properties.load(propertiesInputStream)
        } catch (e: Exception) {
            throw Exception(
                "Error loading existing property file $MUPDF_PLATFORMS_PROPERTIES_FILENAME",
                e
            )
        }
        properties.mapNotNull { (key, value) ->
            if (key.toString().contains("platform.")) {
                value.toString()
            } else {
                null
            }
        }
    }

    @get:Synchronized
    var isInitializedSuccessfully = false
        private set

    val platformBestMatch: String
        get() {
            val availablePlatform = platformList
            if (availablePlatform.size == 1) {
                return availablePlatform[0]
            }

            val arch = System.getProperty("os.arch")
            val system: String? = System.getProperty("os.name").split(" ".toRegex())
                .dropLastWhile { it.isEmpty() }.toTypedArray()[0]
            if (availablePlatform.contains("$system-$arch")) {
                return "$system-$arch"
            }

            val stringBuilder =
                StringBuilder("Can't find suited platform for os.arch=")
            stringBuilder.append(arch)
            stringBuilder.append(", os.name=")
            stringBuilder.append(system)
            stringBuilder.append("... Available list of platforms: ")

            for (platform in availablePlatform) {
                stringBuilder.append(platform)
                stringBuilder.append(", ")
            }
            stringBuilder.setLength(stringBuilder.length - 2)
            throw Exception(stringBuilder.toString())
        }
    private const val SYSTEM_PROPERTY_TMP = "java.io.tmpdir"
    private const val PROPERTY_BUILD_REF = "build.ref"
    private const val LIB_PROPERTIES_FILENAME = "mupdf-lib.properties"
    private const val MUPDF_PLATFORMS_PROPERTIES_FILENAME = "/mupdf-platforms.properties"
    private const val LIB_NAME = "lib.name"
    private const val LIB_HASH = "lib.hash"

    private fun loadLibProperties(usedPlatform: String): Properties {
        val pathInJAR = "/$usedPlatform/"
        val sevenZipJBindingLibProperties = MuPDF::class.java
            .getResourceAsStream(pathInJAR + LIB_PROPERTIES_FILENAME)
        if (sevenZipJBindingLibProperties == null) {
            throw Exception("error loading property file '$pathInJAR$LIB_PROPERTIES_FILENAME' from a jar-file 'sevenzipjbinding-<Platform>.jar'. Is the platform jar-file not on the class path?")
        }
        val properties = Properties()
        try {
            properties.load(sevenZipJBindingLibProperties)
        } catch (e: Exception) {
            throw Exception(
                "error loading property file '$LIB_PROPERTIES_FILENAME' from a jar-file 'sevenzipjbinding-<Platform>.jar'",
                e
            )
        }
        return properties
    }

    private fun createOrVerifyTmpDir(): Path {
        val systemPropertyTmp = System.getProperty(SYSTEM_PROPERTY_TMP)
        if (systemPropertyTmp == null) {
            throw Exception("can't determinte tmp directory. Use may use -D$SYSTEM_PROPERTY_TMP=<path to tmp dir> parameter for jvm to fix this.")
        }
        val tmpDir = Path(systemPropertyTmp)
        if (tmpDir.notExists() || !tmpDir.isDirectory()) {
            throw Exception("invalid tmp directory '${tmpDir.pathString}'")
        }

        if (!tmpDir.isWritable()) {
            throw Exception("can't create files in '${tmpDir.pathString}'")
        }
        return tmpDir
    }

    private fun getOrCreateMuPDFTmpDir(tmpDir: Path, properties: Properties): Path {
        val buildRef = getOrGenerateBuildRef(properties)
        val tmpSubdir = tmpDir.resolve("MuPDF-$buildRef")
        if (!tmpSubdir.exists()) {
            runCatching {
                tmpSubdir.createDirectory()
            }.onFailure {
                throw Exception(
                    "Directory '" + tmpDir.absolutePathString() + "' couldn't be created",
                    it
                )
            }
        }
        return tmpSubdir
    }

    private fun getOrGenerateBuildRef(properties: Properties): String {
        var buildRef = properties.getProperty(PROPERTY_BUILD_REF)
        if (buildRef == null) {
            buildRef = Random.nextInt(10000000).toString()
        }
        return buildRef
    }

    private fun copyOrSkipLibraries(
        usedPlatform: String,
        tmpDir: Path,
        properties: Properties,
    ): Path {
        val libName = properties.getProperty(LIB_NAME)
        val libHash = properties.getProperty(LIB_HASH)
        if (libName == null) {
            throw Exception("property file '$LIB_PROPERTIES_FILENAME' from 'mupdf.jar' missing property '$LIB_NAME'")
        }
        if (libHash == null) {
            throw Exception("property file '$LIB_PROPERTIES_FILENAME' from 'mupdf.jar' missing property $LIB_HASH containing the hash for the library '$libName'")
        }
        val libTmpFile = tmpDir.resolve(libName)

        if (!libTmpFile.exists() || !hashMatched(libTmpFile, libHash)) {
            val libInputStream =
                MuPDF::class.java.getResourceAsStream("/$usedPlatform/$libName")
            if (libInputStream == null) {
                throw Exception("error loading native library '$libName' from a jar-file mupdf.jar'.")
            }
            copyLibraryToFS(libTmpFile, libInputStream)
        }
        return libTmpFile
    }

    private fun copyLibraryToFS(toLibTmp: Path, fromLibInputStream: InputStream) {
        var libTmpOutputStream: OutputStream? = null
        try {
            libTmpOutputStream = Files.newOutputStream(toLibTmp)
            val buffer = ByteArray(65536)
            while (true) {
                val read = fromLibInputStream.read(buffer)
                if (read > 0) {
                    libTmpOutputStream.write(buffer, 0, read)
                } else {
                    break
                }
            }
        } catch (e: java.lang.Exception) {
            throw RuntimeException(
                "Error initializing MuPDF native library: can't copy native library out of a resource file to the temporary location: '$toLibTmp'",
                e
            )
        } finally {
            try {
                fromLibInputStream.close()
            } catch (e: Exception) {
                // Ignore errors here
            }
            try {
                libTmpOutputStream?.close()
            } catch (e: Exception) {
                // Ignore errors here
            }
        }
    }

    private fun hashMatched(libTmpFile: Path, libHash: String): Boolean {
        val digest = try {
            MessageDigest.getInstance("SHA-256")
        } catch (e: Exception) {
            throw Exception("Error initializing SHA-256 algorithm", e)
        }
        var fileInputStream: InputStream? = null
        return try {
            fileInputStream = Files.newInputStream(libTmpFile)
            val buffer = ByteArray(128 * 1024)
            var bytesRead: Int
            while (fileInputStream.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
            @OptIn(ExperimentalStdlibApi::class)
            val fileHash = digest.digest().toHexString()
            fileHash.equals(libHash.trim { it <= ' ' }.lowercase(), ignoreCase = true)
        } catch (e: Exception) {
            throw Exception("Error reading library file from the temp directory: '$libTmpFile'", e)
        } finally {
            try {
                fileInputStream?.close()
            } catch (e: Exception) {
                // 無視
            }
        }
    }
}
