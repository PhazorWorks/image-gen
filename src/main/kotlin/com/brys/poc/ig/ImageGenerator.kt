package com.brys.poc.ig

import java.awt.*
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.io.File
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO
import kotlin.system.measureTimeMillis


class ImageGenerator(private val cache: Cache, private val fallback: Boolean) {
    private val staticBase = ImageIO.read(File("assets/images/apollo-template.png"))
    private val bradTemplateStaticBase = ImageIO.read(File("assets/images/brad-template.png"))
    private val bradTemplateNPBase = ImageIO.read(File("assets/images/brad-template-np-poc.png"))
    private val apolloImage = ImageIO.read(File("assets/images/logo.png"))
    private val ubuntu = Font.createFont(Font.TRUETYPE_FONT, File("assets/fonts/Ubuntu-Regular.ttf"))
    private val noto = Font.createFont(Font.TRUETYPE_FONT, File("assets/fonts/NotoSansJP-Regular.otf"))
    private val kosugi = Font.createFont(Font.TRUETYPE_FONT, File("assets/fonts/KosugiMaru-Regular.ttf"))
    private val athiti = Font.createFont(Font.TRUETYPE_FONT, File("assets/fonts/Athiti.ttf"))
    fun generateAddTrack(song: Song, user: String, id: String?): BufferRes {
        val base = copyImage(staticBase)
        val thumbnail = cache.grabCacheThumb(id, fallback, false)
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
            Logger.debug("[com.brys.poc.ig.ImageGenerator -> AddTrack -> Render]: Graphics Disposed   ============ Render")
        }
        Logger.success("[Image Generator -> Add Track -> Render]: Finished in ${imageGenTime}ms")

        return BufferRes(base, thumbnail.cacheGrab, imageGenTime)
    }

    fun generateAddTrackBradTemplate(song: Song, user: String, id: String?): BufferRes {
        val base = BufferedImage(500, 250, BufferedImage.TYPE_INT_ARGB)
        val thumbnail = cache.grabCacheThumb(id, fallback, true)
        var shortned = "${song.author} - ${song.name}".take(58)
        val imageGenTime = measureTimeMillis {
            val g = base.createGraphics()
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
            Logger.debug("[com.brys.poc.ig.ImageGenerator -> AddTrack -> Render]: Rendering hints set ============ Render")
            g.drawImage(thumbnail.image, -200, -150, 700, 420, null)
            Logger.debug("[com.brys.poc.ig.ImageGenerator -> AddTrack -> Render]: Thumbnail drawn. Disposing...")
            g.drawImage(bradTemplateStaticBase, 0, 0, 500, 250, null)
            g.font = athiti.deriveFont(Font.BOLD, 18f)
            g.color = Color.decode("#E2E2E2")
            g.drawString(shortned, 15, 205)
            Logger.debug("[com.brys.poc.ig.ImageGenerator -> AddTrack -> Render]: Song data drawn")
            g.font = athiti.deriveFont(Font.PLAIN, 14f)
            g.color = Color.decode("#239CDF")
            g.drawString(user.take(40), 97, 241)
            Logger.debug("[com.brys.poc.ig.ImageGenerator -> AddTrack -> Render]: User data drawn")
            g.drawString(formatToDigitalClock(song.length), 455, 241)
            g.drawImage(apolloImage, 15, 15, 32, 32, null)
            Logger.debug("[com.brys.poc.ig.ImageGenerator -> AddTrack -> Render]: Logo drawn. Disposing...")
            g.dispose()
            Logger.debug("[com.brys.poc.ig.ImageGenerator -> AddTrack -> Render]: Graphics Disposed   ============ Render")
        }
        Logger.success("[Image Generator -> Add Track -> Render]: Finished in ${imageGenTime}ms")
        return BufferRes(base, thumbnail.cacheGrab, imageGenTime)
    }

    fun containsHANCharacters(input: String): Boolean {
        return input.codePoints().anyMatch { codepoint -> Character.UnicodeScript.of(codepoint) == Character.UnicodeScript.HAN }
    }

    fun generateNPTrack(song: Song, user: String = "N/A", id: String?): BufferRes {
        val base = BufferedImage(500, 250, BufferedImage.TYPE_INT_ARGB)
        val thumbnail = id?.let { cache.grabCacheThumb(it, fallback, true) }
        val startColor = Color.decode("#4568dc")
        val endColor = Color.decode("#b06ab3")
        val authorAndSongName = "${song.author} - ${song.name}"
        val shortened:String = if (containsHANCharacters(authorAndSongName)) {
            authorAndSongName.take(50)
        } else {
            if (authorAndSongName.length > 65) {
                authorAndSongName.take(65)
            } else {
                authorAndSongName
            }
        }

        val imageGenTime = measureTimeMillis {
            val g = base.createGraphics()
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
            Logger.debug("[com.brys.poc.ig.ImageGenerator -> AddTrack -> Render]: Rendering hints set ============ Render")
            if (thumbnail != null) {
                g.drawImage(thumbnail.image, -200, -150, 700, 420, null)
            }
            Logger.debug("[com.brys.poc.ig.ImageGenerator -> AddTrack -> Render]: Thumbnail drawn. Disposing...")
            g.drawImage(bradTemplateNPBase, 0, 0, 500, 250, null)
//            g.font = athiti.deriveFont(Font.BOLD, 18f)
            g.font = noto.deriveFont(Font.BOLD, 14f)
            g.color = Color.decode("#E2E2E2")
            g.drawString(shortened, 15, 205)
//            g.drawString(formatToDigitalClock(song.position), 400, 205)
            Logger.debug("[com.brys.poc.ig.ImageGenerator -> AddTrack -> Render]: Song data drawn")
            g.font = noto.deriveFont(Font.PLAIN, 14f)
            g.color = Color.decode("#239CDF")
            g.drawString(user.take(32), 97, 241)
            Logger.debug("[com.brys.poc.ig.ImageGenerator -> AddTrack -> Render]: User data drawn")
            g.drawString(formatToDigitalClock(song.length), 455, 241)
            val timeGrad = measureTimeMillis {
                g.paint = GradientPaint(
                    167F, 18F, startColor,
                    318F, 25F, endColor
                )
                g.fill(Rectangle(167, 18, calcNPBar(song.position, song.length).toInt(), 25))
            }
            Logger.debug("[com.brys.poc.ig.ImageGenerator -> AddTrack -> Render]: NP bar drawn (took ${timeGrad}ms)")
            g.drawImage(apolloImage, 15, 15, 32, 32, null)
            Logger.debug("[com.brys.poc.ig.ImageGenerator -> AddTrack -> Render]: Logo drawn. Disposing...")
            g.dispose()
            Logger.debug("[com.brys.poc.ig.ImageGenerator -> AddTrack -> Render]: Graphics Disposed   ============ Render")
        }
        Logger.success("[Image Generator -> Add Track -> Render]: Finished in ${imageGenTime}ms")
        return BufferRes(base, false, imageGenTime)
    }
    fun createCacheQueue(songs: MutableList<Song>, queueID: String) {
        TODO("Create Pre Cache")
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
    private fun calcNPBar(pos: Long, dur: Long): Long {
        val width = 319
        val percent = dur / pos
        return width / percent
    }
    private fun copyImage(source: BufferedImage): BufferedImage {
        val bi = BufferedImage(source.width, source.height, source.type)
        val sourceData = (source.raster.dataBuffer as DataBufferByte).data
        val biData = (bi.raster.dataBuffer as DataBufferByte).data
        System.arraycopy(sourceData, 0, biData, 0, sourceData.size)
        return bi
    }

    data class Song(var name: String, var author: String = "N/A", val uri: String = "N/A", val length: Long, val position: Long = 0)
    data class BufferRes(val image: BufferedImage, val cacheGrab: Boolean, val timing: Long)
}
