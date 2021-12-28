package com.brys.poc.ig

import java.io.File
import java.util.concurrent.Executors
import kotlin.system.measureTimeMillis

fun main() {
    Logger.info("Starting com.brys.poc.ig.Server")

    Logger.debug("Testing Directory status...")
    val ifDir = File("./cache")
    if (!ifDir.exists()) {
        Logger.warn("com.brys.poc.ig.Cache dir doesn't exist. Creating...")
        ifDir.mkdirs()
        Logger.success("Created com.brys.poc.ig.Cache dir")
    }
    val timing = measureTimeMillis {
        val asyncThreadPool = Executors.newCachedThreadPool()
        val cache = Cache(asyncThreadPool)
        val imageGen = ImageGenerator(cache)
        val server = Server(3002,  imageGen)
        server.route()
    }

    Logger.success("[Main]: Operations started in ${timing}ms")

}