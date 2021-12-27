package com.brys.poc.ig

import java.io.File
import java.net.URL
import java.util.concurrent.ExecutorService
import javax.imageio.ImageIO
import kotlin.system.measureTimeMillis

class Cache(private val executor: ExecutorService) {
    fun grabCacheThumb(id: String): ImageGenerator.BufferRes {
        val cached = File("cache/${id}.jpg")
        if (!cached.exists()) {
            Logger.warn("[com.brys.poc.ig.Cache -> Retrieve -> FileNotFound]: Cached image for ${id} doesn't exist.\nStreaming JPG and writing cache file.")
            val url = URL("https://img.youtube.com/vi/${id}/maxresdefault.jpg")
            val streamedURL = ImageIO.read(url)
             executor.submit {
                Logger.info("[ThreadPool -> CacheIMG]: Starting...")
                 val saveTime = measureTimeMillis {
                     ImageIO.write(streamedURL, "jpg", File("cache/${id}.jpg"))
                 }
                 Logger.success("[ThreadPool -> com.brys.poc.ig.Cache IMG]: Finished in ${saveTime}ms")
            }
            return ImageGenerator.BufferRes(streamedURL, false)
        }
        Logger.success("[com.brys.poc.ig.Cache -> Retrieve]: Cached file found for $id")
        return ImageGenerator.BufferRes(ImageIO.read(cached), true)
    }
    init {
        Logger.info("[ClassLoader -> com.brys.poc.ig.Cache]: Initialized")
    }

}