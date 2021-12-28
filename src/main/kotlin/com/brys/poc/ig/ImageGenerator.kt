package com.brys.poc.ig

import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO
import kotlin.system.measureTimeMillis


class ImageGenerator(private val cache: Cache, private val fallback: Boolean) {
        private val staticBase = ImageIO.read(File("assets/images/apollo-template.png"))
        private val ubuntu = Font.createFont(Font.TRUETYPE_FONT, File("assets/fonts/Ubuntu-Regular.ttf"))
        private val kosugi = Font.createFont(Font.TRUETYPE_FONT, File("assets/fonts/KosugiMaru-Regular.ttf"))
        fun generateAddTrack(song: Song, user: String, id: String): BufferRes {
            val base = copyImage(staticBase)!!
            val thumbnail = cache.grabCacheThumb(id, fallback)
            val imageGenTime = measureTimeMillis {
                val g = base.createGraphics()
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
                Logger.debug("[com.brys.poc.ig.ImageGenerator -> AddTrack -> Render]: Rendering hints set ============ Render")
                g.drawImage(thumbnail.image, 450, 80, 340, 200, null)
                Logger.debug("[com.brys.poc.ig.ImageGenerator -> AddTrack -> Render]: Thumbnail drawn")
                g.font = ubuntu.deriveFont(Font.BOLD, 32f)
                g.drawString(song.name, 6, 30)
                g.font = ubuntu.deriveFont(Font.BOLD, 25f)
                g.color = Color.decode("#5e5e5e")
                g.drawString("By: ${song.author}", 6, 68)
                Logger.debug("[com.brys.poc.ig.ImageGenerator -> AddTrack -> Render]: Song data drawn")
                g.font = ubuntu.deriveFont(Font.BOLD, 55f)
                g.drawString(formatToDigitalClock(song.length), 136, 200)
                g.font = kosugi.deriveFont(Font.PLAIN, 20f)
                g.color = Color.decode("#29a7e4")
                g.drawString("Added by: $user", 7, 386)
                Logger.debug("[com.brys.poc.ig.ImageGenerator -> AddTrack -> Render]: User data drawn. Disposing...")
                g.dispose()
                Logger.debug("[com.brys.poc.ig.ImageGenerator -> AddTrack -> Render]: Graphics Disposed  ============ Render")
            }
            Logger.success("[Image Generator -> Add Track -> Render]: Finished in ${imageGenTime}ms")

            return BufferRes(base, thumbnail.cacheGrab)
        }

    init {
        Logger.info("[ClassLoader -> com.brys.poc.ig.ImageGenerator]: Initialized")
    }
    private fun formatToDigitalClock(miliSeconds: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(miliSeconds).toInt() % 24
        val minutes = TimeUnit.MILLISECONDS.toMinutes(miliSeconds).toInt() % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(miliSeconds).toInt() % 60
        return when {
            hours > 0 -> String.format("%d:%02d:%02d", hours, minutes, seconds)
            minutes > 0 -> String.format("%02d:%02d", minutes, seconds)
            seconds > 0 -> String.format("00:%02d", seconds)
            else -> {
                "00:00"
            }
        }
    }
    private fun copyImage(source: BufferedImage): BufferedImage {
        val bi = BufferedImage(source.width, source.height, source.type)
        val sourceData = (source.raster.dataBuffer as DataBufferByte).data
        val biData = (bi.raster.dataBuffer as DataBufferByte).data
        System.arraycopy(sourceData, 0, biData, 0, sourceData.size)
        return bi
    }
    data class Song(val name: String, val author: String, val uri: String, val length: Long)
    data class BufferRes(val image: BufferedImage, val cacheGrab: Boolean)
}