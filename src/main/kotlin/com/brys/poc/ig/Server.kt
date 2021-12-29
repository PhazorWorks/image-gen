package com.brys.poc.ig

import io.javalin.Javalin
import io.javalin.http.staticfiles.Location
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.ExecutorService
import javax.imageio.ImageIO
import kotlin.system.measureTimeMillis


class Server(
    port: Int,
    val imgGen: ImageGenerator,
    val debugLocal: Boolean,
    val useBradTemplate: Boolean,
    val executor: ExecutorService
) {
    val server = Javalin.create { config ->
        config.addStaticFiles { files ->
            files.hostedPath = "/"
            files.directory = "./cache"
            files.location = Location.EXTERNAL
        }
    }
    val app = server.start(port)
    fun route() {

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
                    body.uri,
                    body.duration
                ), body.author, body.identifier
            ) else imgGen.generateAddTrackBradTemplate(
                ImageGenerator.Song(
                    body.title.substringBefore("-"),
                    body.title.substringAfter("-"),
                    body.uri,
                    body.duration
                ), body.author, body.identifier
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
            app.post("/np") {
                val body = it.bodyAsClass<NPTrackPayload>()
                val generated =  imgGen.generateNPTrack(
                    ImageGenerator.Song(
                        body.title.substringBefore("-"),
                        body.title.substringAfter("-"),
                        length = body.duration,
                        position = body.position
                    ), body.author, body.identifier
                )
                val baos = ByteArrayOutputStream()
                ImageIO.write(generated!!.image, "png", baos)
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
        val uri: String,
        val identifier: String
    )

    data class NPTrackPayload(
        val title: String,
        val author: String = "N/A",
        val duration: Long,
        val position: Long,
        val identifier: String
    )

    data class IDFiles(val name: String, val size: Long)
}