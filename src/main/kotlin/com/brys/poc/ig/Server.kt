package com.brys.poc.ig

import io.javalin.Javalin
import io.javalin.http.staticfiles.Location
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.ExecutorService
import javax.imageio.ImageIO
import kotlin.system.measureTimeMillis
import kotlin.time.measureTime


class Server(
    port: Int,
    val imgGen: ImageGenerator,
    val debugLocal: Boolean,
    val useBradTemplate: Boolean,
    val executor: ExecutorService,
    val path: String
) {
    val server = Javalin.create { config ->
        config.addStaticFiles { files ->
            files.hostedPath = "/"
            files.directory = path
            files.location = Location.EXTERNAL
        }
        config.enableCorsForAllOrigins()
    }
    val app = server.start(port)
    fun route() {
        app.post("/np") {
            val body = it.bodyAsClass<NPTrackPayload>()
            val generated = imgGen.generateNPTrack(
                ImageGenerator.Song(
                    body.title.substringBefore("-"),
                    body.title.substringAfter("-"), // TODO: Fix APOLLO to support split apart Author for song rather then combined
                    length = body.duration,
                    position = body.position
                ), body.author.toString(), if (scan(body.uri)) body.identifier else null
            )
            val baos = ByteArrayOutputStream()
            ImageIO.write(generated.image, "png", baos)
            val imgInBytes = baos.toByteArray()
            baos.flush()
            baos.close()
            it.contentType("image/png")
            it.res.contentType = "image/png"
            it.res.setContentLength(imgInBytes.size)
            it.res.addHeader("Cached", generated.cacheGrab.toString())
            it.res.addHeader("Generated", generated.timing.toString())
            it.result(imgInBytes)
            if (debugLocal) {
                executor.submit {
                    val timeDebugWrite = measureTimeMillis {
                        ImageIO.write(generated.image, "png", File("debugoutputnp.png"))
                    }
                    Logger.debug("[ThreadPool -> WriteDebugIMG]: Debug image for np wrote to disk in ${timeDebugWrite}ms")
                }
            }
        }
        app.get("/cache") {
            val files = File("./cache").listFiles()
            val filesID = mutableListOf<IDFiles>()
            files.forEach { f -> filesID.add(IDFiles(f.name, f.length())) }
            it.json(object {
                val files = filesID
                val total = files.size
            })
            return@get
        }
        app.post("/add") {
            val body = it.bodyAsClass<AddTrackPayload>()
            val generated = if (!useBradTemplate) imgGen.generateAddTrack(
                ImageGenerator.Song(
                    body.title.substringBefore("-"),
                    body.title.substringAfter("-"),
                    body.uri.toString(),
                    body.duration
                ), body.author, if (scan(body.uri.toString())) body.identifier else null
            ) else imgGen.generateAddTrackBradTemplate(
                ImageGenerator.Song(
                    body.title.substringBefore("-"),
                    body.title.substringAfter("-"),
                    body.uri.toString(),
                    body.duration
                ), body.author, if (scan(body.uri.toString())) body.identifier else null
            )
            val baos = ByteArrayOutputStream()
            ImageIO.write(generated.image, "png", baos)
            val imgInBytes = baos.toByteArray()
            baos.flush()
            baos.close()
            it.contentType("image/png")
            it.res.contentType = "image/png"
            it.res.setContentLength(imgInBytes.size)
            it.res.addHeader("Cached", generated.cacheGrab.toString())
            it.res.addHeader("Generated", generated.timing.toString())
            it.result(imgInBytes)
            if (debugLocal) {
                executor.submit {
                    val timeDebugWrite = measureTimeMillis {
                        ImageIO.write(generated.image, "png", File("debugoutput.png"))
                    }
                    Logger.debug("[ThreadPool -> WriteDebugIMG]: Debug image wrote to disk in ${timeDebugWrite}ms")
                }
            }
        }

    }



    init {
        app.before { p ->
            Logger.debug("Routing ${p.path()}")
        }
    }

    data class AddTrackPayload(
        val title: String,
        val author: String,
        val duration: Long,
        val uri: String?,
        val identifier: String?
    )

    data class NPTrackPayload(
        val title: String,
        val author: String? = "N/A",
        val uri: String,
        val duration: Long,
        val position: Long,
        val identifier: String?
    )

    data class IDFiles(val name: String, val size: Long)
}
