package com.brys.poc.ig

import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.concurrent.ExecutorService
import javax.imageio.ImageIO
import kotlin.system.measureTimeMillis


class Cache(private val executor: ExecutorService, private val path: String) {
    fun grabCacheThumb(id: String?, fallback: Boolean, bradGen: Boolean): ImageGenerator.BufferRes {
        if (id == null) return ImageGenerator.BufferRes(BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB), false, 0)
        val cached = File("${path}/${id}.jpg")
        if (!cached.exists()) {
            Logger.warn("[com.brys.poc.ig.Cache -> Retrieve -> FileNotFound]: Cached image for $id doesn't exist.\nStreaming JPG and writing cache file.")
            var url = URL("https://img.youtube.com/vi/${id}/maxresdefault.jpg")
            if (fallback && !bradGen) {
                Logger.info("[Cache -> CacheThumb -> Fallback]: Fallback is enabled, testing HQDefault url")
                val code = getResponseCode("https://img.youtube.com/vi/${id}/maxresdefault.jpg")
                if (code == 404) {
                    Logger.error("[Cache -> CacheThumb -> Fallback]: HQDefault doesn't exist, falling back to MQDefault")
                    url = URL("https://img.youtube.com/vi/${id}/mqdefault.jpg")
                }
            } else if (bradGen) {url = URL("https://img.youtube.com/vi/${id}/hqdefault.jpg")}

            val streamedURL = ImageIO.read(url)

            executor.submit {
                Logger.info("[ThreadPool -> CacheIMG]: Starting...")
                val saveTime = measureTimeMillis {
                    ImageIO.write(streamedURL, "jpg", File("${path}/${id}.jpg"))
                }
                Logger.success("[ThreadPool -> com.brys.poc.ig.Cache IMG]: Finished in ${saveTime}ms")
            }
            return ImageGenerator.BufferRes(streamedURL, false, 0)
        }
        Logger.success("[com.brys.poc.ig.Cache -> Retrieve]: Cached file found for $id")
        return ImageGenerator.BufferRes(ImageIO.read(cached), true, 0)
    }

    init {
        Logger.info("[ClassLoader -> com.brys.poc.ig.Cache]: Initialized")
    }

    @Throws(MalformedURLException::class, IOException::class)
    fun getResponseCode(urlString: String?): Int {
        val u = URL(urlString)
        val huc: HttpURLConnection = u.openConnection() as HttpURLConnection
        huc.requestMethod = "GET"
        huc.connect()
        return huc.responseCode
    }
}
