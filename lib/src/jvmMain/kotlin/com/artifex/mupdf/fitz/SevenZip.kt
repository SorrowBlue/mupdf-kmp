//package com.artifex.mupdf.fitz
//
//import com.sorrowblue.mupdf.kmp.BuildKonfig
//import java.io.File
//import java.io.FileInputStream
//import java.io.FileOutputStream
//import java.io.IOException
//import java.io.InputStream
//import java.security.MessageDigest
//import java.security.NoSuchAlgorithmException
//import java.security.PrivilegedAction
//import java.util.Locale
//import java.util.Properties
//import kotlin.io.path.Path
//import kotlin.io.path.absolutePathString
//import kotlin.io.path.createDirectory
//import kotlin.io.path.exists
//import kotlin.io.path.isDirectory
//import kotlin.io.path.isWritable
//import kotlin.io.path.notExists
//import kotlin.io.path.pathString
//import kotlin.random.Random
//
//object SevenZip {
//    val sevenZipJBindingVersion = BuildKonfig.version
//
//    private const val SYSTEM_PROPERTY_TMP = "java.io.tmpdir"
//    private const val SYSTEM_PROPERTY_SEVEN_ZIP_NO_DO_PRIVILEGED_INITIALIZATION =
//        "sevenzip.no_doprivileged_initialization"
//    private const val LIB_NAME = "lib.%s.name"
//    private const val LIB_HASH = "lib.%s.hash"
//    private const val PROPERTY_BUILD_REF = "build.ref"
//    private const val LIB_PROPERTIES_FILENAME =
//        "mupdf-lib.properties"
//    private const val MUPDF_PLATFORMS_PROPRETIES_FILENAME =
//        "/mupdf-platforms.properties"
//
//    @get:Synchronized
//    var isAutoInitializationWillOccur: Boolean = true
//        private set
//
//    @get:Synchronized
//    var isInitializedSuccessfully: Boolean = false
//        private set
//    private var availablePlatforms: MutableList<String?>? = null
//
//    @get:Synchronized
//    var usedPlatform: String? = null
//        private set
//
//    @get:Synchronized
//    val platformList: List<String> by lazy {
//        val propertiesInputStream = SevenZip::class.java
//            .getResourceAsStream(MUPDF_PLATFORMS_PROPRETIES_FILENAME)
//        if (propertiesInputStream == null) {
//            throw IllegalStateException("Can not find MuPDF platform property file $MUPDF_PLATFORMS_PROPRETIES_FILENAME.")
//        }
//        val properties = Properties()
//        try {
//            properties.load(propertiesInputStream)
//        } catch (e: IOException) {
//            throwInitException(
//                e,
//                "Error loading existing property file $MUPDF_PLATFORMS_PROPRETIES_FILENAME"
//            )
//        }
//        properties.mapNotNull { (key, value) ->
//            if (key.toString().contains("platform.")) {
//                value.toString()
//            } else {
//                null
//            }
//        }
//    }
//
//    @Synchronized
//    fun initPlatformJar() {
//        try {
//            if (isInitializedSuccessfully) {
//                return
//            }
//            usedPlatform = platformBestMatch
//            val properties = loadLibProperties()
//            val tmpDirFile = createOrVerifyTmpDir()
//            val muPDFTmpDir = getOrCreateMuPDFTmpDir(tmpDirFile, properties)
//            val nativeLibraries = copyOrSkipLibraries(properties, muPDFTmpDir)
//            loadNativeLibraries(nativeLibraries)
//            nativeInitialization()
//        } catch (exception: Exception) {
//            lastInitializationException = exception
//            throw exception
//        }
//    }
//
//    private var lastInitializationException: Exception? = null
//
//    private fun loadLibProperties(): Properties {
//        val pathInJAR = "/$usedPlatform/"
//
//        val sevenZipJBindingLibProperties = SevenZip::class.java
//            .getResourceAsStream(pathInJAR + LIB_PROPERTIES_FILENAME)
//        if (sevenZipJBindingLibProperties == null) {
//            throwInitException(
//                "error loading property file '$pathInJAR$LIB_PROPERTIES_FILENAME' from a jar-file 'sevenzipjbinding-<Platform>.jar'. Is the platform jar-file not on the class path?"
//            )
//        }
//
//        val properties = Properties()
//        try {
//            properties.load(sevenZipJBindingLibProperties)
//        } catch (e: IOException) {
//            throwInitException(
//                ("error loading property file '" + LIB_PROPERTIES_FILENAME
//                        + "' from a jar-file 'sevenzipjbinding-<Platform>.jar'")
//            )
//        }
//        return properties
//    }
//
//    private fun createOrVerifyTmpDir(): java.nio.file.Path {
//        val systemPropertyTmp = System.getProperty(SYSTEM_PROPERTY_TMP)
//        if (systemPropertyTmp == null) {
//            throwInitException(
//                ("can't determinte tmp directory. Use may use -D$SYSTEM_PROPERTY_TMP=<path to tmp dir> parameter for jvm to fix this.")
//            )
//        }
//        val tmpDir = Path(systemPropertyTmp)
//        if (tmpDir.notExists() || !tmpDir.isDirectory()) {
//            throwInitException("invalid tmp directory '${tmpDir.pathString}'")
//        }
//
//        if (!tmpDir.isWritable()) {
//            throwInitException("can't create files in '${tmpDir.pathString}'")
//        }
//        return tmpDir
//    }
//
//    private fun getOrCreateMuPDFTmpDir(
//        tmpDir: java.nio.file.Path,
//        properties: Properties,
//    ): java.nio.file.Path {
//        val buildRef = getOrGenerateBuildRef(properties)
//        val tmpSubdir = tmpDir.resolve("MuPDF-$buildRef")
//        if (!tmpSubdir.exists()) {
//            runCatching {
//                tmpSubdir.createDirectory()
//            }.onFailure {
//                throwInitException("Directory '" + tmpDir.absolutePathString() + "' couldn't be created")
//            }
//        }
//        return tmpSubdir
//    }
//
//    private fun getOrGenerateBuildRef(properties: Properties): String {
//        var buildRef = properties.getProperty(PROPERTY_BUILD_REF)
//        if (buildRef == null) {
//            buildRef = Random.nextInt(10000000).toString()
//        }
//        return buildRef
//    }
//
//    private fun copyOrSkipLibraries(
//        properties: Properties,
//        muPDFTmpDir: java.nio.file.Path,
//    ): List<File> {
//        val nativeLibraries: MutableList<File?> = ArrayList(5)
//        var i = 1
//        while (true) {
//            val propertyName = String.format(LIB_NAME, i)
//            val propertyHash = String.format(LIB_HASH, i)
//            val libName = properties.getProperty(propertyName)
//            val libHash = properties.getProperty(propertyHash)
//            if (libName == null) {
//                if (nativeLibraries.size == 0) {
//                    throwInitException(
//                        ("property file '" + LIB_PROPERTIES_FILENAME
//                                + "' from 'sevenzipjbinding-<Platform>.jar' missing property '" + propertyName + "'")
//                    )
//                } else {
//                    break
//                }
//            }
//            if (libHash == null) {
//                throwInitException(
//                    ("property file '" + LIB_PROPERTIES_FILENAME
//                            + "' from 'sevenzipjbinding-<Platform>.jar' missing property " + propertyHash
//                            + " containing the hash for the library '" + libName + "'")
//                )
//            }
//            val libTmpFile =
//                File(muPDFTmpDir.absolutePathString() + File.separatorChar + libName)
//
//            if (!libTmpFile.exists() || !hashMatched(libTmpFile, libHash!!)) {
//                val libInputStream =
//                    SevenZip::class.java.getResourceAsStream("/$usedPlatform/$libName")
//                if (libInputStream == null) {
//                    throwInitException(
//                        ("error loading native library '" + libName
//                                + "' from a jar-file 'sevenzipjbinding-<Platform>.jar'.")
//                    )
//                }
//
//                copyLibraryToFS(libTmpFile, libInputStream!!)
//            }
//            nativeLibraries.add(libTmpFile)
//            i++
//        }
//        return nativeLibraries
//    }
//
//    private fun hashMatched(libTmpFile: File, libHash: String): Boolean {
//        val digest: MessageDigest = try {
//            MessageDigest.getInstance("SHA-256")
//        } catch (e: NoSuchAlgorithmException) {
//            throwInitException(e, "Error initializing SHA-256 algorithm")
//            return false
//        }
//
//        var fileInputStream: FileInputStream? = null
//        return try {
//            fileInputStream = FileInputStream(libTmpFile)
//            val buffer = ByteArray(128 * 1024)
//            var bytesRead: Int
//            while (fileInputStream.read(buffer).also { bytesRead = it } != -1) {
//                digest.update(buffer, 0, bytesRead)
//            }
//            val fileHash = byteArrayToHex(digest.digest())
//            fileHash.equals(
//                libHash.trim { it <= ' ' }.lowercase(Locale.getDefault()),
//                ignoreCase = true
//            )
//        } catch (e: IOException) {
//            throwInitException(
//                e,
//                "Error reading library file from the temp directory: '${libTmpFile.absolutePath}'"
//            )
//            false
//        } finally {
//            try {
//                fileInputStream?.close()
//            } catch (e: IOException) {
//                // 無視
//            }
//        }
//    }
//
//    private fun byteArrayToHex(byteArray: ByteArray): String {
//        val stringBuilder = StringBuilder()
//        for (i in byteArray.indices) {
//            stringBuilder.append(String.format("%1$02x", 0xFF and byteArray[i].toInt()))
//        }
//        return stringBuilder.toString()
//    }
//
//    private fun loadNativeLibraries(libraryList: MutableList<File?>) {
//        // Load native libraries in to reverse order
//        for (i in libraryList.size - 1 downTo -1 + 1) {
//            val libraryFileName = libraryList.get(i)!!.absolutePath
//            try {
//                System.load(libraryFileName)
//            } catch (t: Throwable) {
//                throw Exception(
//                    "7-Zip-JBinding initialization failed: Error loading native library: '" + libraryFileName + "'",
//                    t
//                )
//            }
//        }
//    }
//
//    @Synchronized
//    fun initLoadedLibraries() {
//        if (isInitializedSuccessfully) {
//            return
//        }
//        isAutoInitializationWillOccur = false
//        nativeInitialization()
//    }
//
//    private fun nativeInitialization() {
//        val doPrivileged = System.getProperty(
//            SYSTEM_PROPERTY_SEVEN_ZIP_NO_DO_PRIVILEGED_INITIALIZATION
//        )
//        val errorMessage: Array<String?>? = arrayOfNulls<String>(1)
//        val throwable: Array<Throwable?>? = arrayOfNulls<Throwable>(1)
//        if (doPrivileged == null || doPrivileged.trim { it <= ' ' } == "0") {
//            AccessController.doPrivileged<Void?>(object : PrivilegedAction<Void?> {
//                override fun run(): Void? {
//                    try {
//                        errorMessage!![0] = nativeInitSevenZipLibrary()
//                    } catch (e: Throwable) {
//                        throwable!![0] = e
//                    }
//                    return null
//                }
//            })
//        } else {
//            errorMessage!![0] = nativeInitSevenZipLibrary()
//        }
//        if (errorMessage!![0] != null || throwable!![0] != null) {
//            var message = errorMessage[0]
//            if (message == null) {
//                message = "No message"
//            }
//            val lastInitializationException = Exception(
//                "Error initializing 7-Zip-JBinding: " + message, throwable!![0]
//            )
//            throw lastInitializationException
//        }
//        isInitializedSuccessfully = true
//    }
//
//    private fun throwInitException(message: String?) {
//        throwInitException(null, message)
//    }
//
//    private fun throwInitException(exception: java.lang.Exception?, message: String?) {
//        throw Exception(
//            ("Error loading SevenZipJBinding native library into JVM: "
//                    + message + " [You may also try different SevenZipJBinding initialization methods "
//                    + "'net.sf.sevenzipjbinding.SevenZip.init*()' in order to solve this problem] "),
//            exception
//        )
//    }
//
//    private fun copyLibraryToFS(toLibTmpFile: File, fromLibInputStream: InputStream) {
//        var libTmpOutputStream: FileOutputStream? = null
//        try {
//            libTmpOutputStream = FileOutputStream(toLibTmpFile)
//            val buffer = ByteArray(65536)
//            while (true) {
//                val read = fromLibInputStream.read(buffer)
//                if (read > 0) {
//                    libTmpOutputStream.write(buffer, 0, read)
//                } else {
//                    break
//                }
//            }
//        } catch (e: java.lang.Exception) {
//            throw RuntimeException(
//                ("Error initializing SevenZipJBinding native library: "
//                        + "can't copy native library out of a resource file to the temporary location: '"
//                        + toLibTmpFile.absolutePath + "'"), e
//            )
//        } finally {
//            try {
//                fromLibInputStream.close()
//            } catch (e: IOException) {
//                // Ignore errors here
//            }
//            try {
//                if (libTmpOutputStream != null) {
//                    libTmpOutputStream.close()
//                }
//            } catch (e: IOException) {
//                // Ignore errors here
//            }
//        }
//    }
//
//    val platformBestMatch: String
//        get() {
//            val availablePlatform = platformList
//            if (availablePlatform.size == 1) {
//                return availablePlatform[0]
//            }
//
//            val arch = System.getProperty("os.arch")
//            val system: String? = System.getProperty("os.name").split(" ".toRegex())
//                .dropLastWhile { it.isEmpty() }.toTypedArray()[0]
//            if (availablePlatform.contains("$system-$arch")) {
//                return "$system-$arch"
//            }
//
//            // TODO allow fuzzy matches
//            val stringBuilder =
//                StringBuilder("Can't find suited platform for os.arch=")
//            stringBuilder.append(arch)
//            stringBuilder.append(", os.name=")
//            stringBuilder.append(system)
//            stringBuilder.append("... Available list of platforms: ")
//
//            for (platform in availablePlatform) {
//                stringBuilder.append(platform)
//                stringBuilder.append(", ")
//            }
//            stringBuilder.setLength(stringBuilder.length - 2)
//            throw Exception(stringBuilder.toString())
//        }
//}
