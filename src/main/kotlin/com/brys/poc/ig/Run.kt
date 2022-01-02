package com.brys.poc.ig

import java.io.File
import java.util.concurrent.Executors
import kotlin.io.path.Path
import kotlin.system.measureTimeMillis

fun main(args: Array<String>) {
    val fallback = args.contains("--fallback") || args.contains("-F") || System.getenv("fallback").toBoolean()
    val debugLocal = args.contains("--debug") || args.contains("-D") || System.getenv("debug").toBoolean()
    val bradTemplate = args.contains("--bradgen") || args.contains("-BG") || System.getenv("bradgen").toBoolean()
    val path = if (System.getenv("cachepath") != null) System.getenv("cachepath") else "./cache"
    if (fallback) Logger.warn("Fallback is enabled for images, it might take longer to grab the image then expected.")
    if (debugLocal) Logger.warn("Debug local is enabled, the generated image will be saved to debug the program.")
    if (bradTemplate) Logger.warn("Brad's template is being used. This will take longer to generate an image, please be advised.")
    if (path != "./cache") Logger.warn("Using custom path (${path}) exact = ${Path(path).toAbsolutePath()}")
    Logger.info("Starting com.brys.poc.ig.Server")
    Logger.debug("Testing Directory status...")
    val ifDir = File(path)
    if (!ifDir.exists()) {
        Logger.warn("com.brys.poc.ig.Cache dir doesn't exist. Creating...")
        ifDir.mkdirs()
        Logger.success("Created com.brys.poc.ig.Cache dir")
    }
    val timing = measureTimeMillis {
        val asyncThreadPool = Executors.newCachedThreadPool()
        val cache = Cache(asyncThreadPool, path)
        val imageGen = ImageGenerator(cache, fallback)
        val server = Server(3002, imageGen, debugLocal, bradTemplate, asyncThreadPool)
        server.route()
    }

    Logger.success("[Main]: Operations started in ${timing}ms")

}