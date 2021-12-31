package com.brys.poc.ig

import io.javalin.Javalin
import io.javalin.http.staticfiles.Location
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*
import java.util.concurrent.ExecutorService
import javax.imageio.ImageIO
import kotlin.system.measureTimeMillis


class Server(
    port: Int,
    private val imgGen: ImageGenerator,
    private val debugLocal: Boolean,
    private val useBradTemplate: Boolean,
    private val executor: ExecutorService
) {
    private val server: Javalin = Javalin.create { config ->
        config.addStaticFiles { files ->
            files.hostedPath = "/"
            files.directory = "./cache"
            files.location = Location.EXTERNAL
        }
    }
    private val registeredClients = mutableListOf<DiscordRestClient>()
    val app: Javalin = server.start(port)
    fun route() {
        app.post("/register") {
            val register = it.bodyAsClass<Register>()
            println(registeredClients.find { c -> c.auth == register.token })
            if (registeredClients.find { c -> c.auth == register.token } != null) {
                it.status(403).json(object {val message = "Client already exists (403)"})
                return@post
            }
            val verifier = UUID.randomUUID().toString()
            registeredClients.add(DiscordRestClient(verifier, register.token, executor))
            it.status(201).json(object {val verifyToken = verifier})
            Logger.success("[Server -> Register]: New client registered (Verify = ${verifier})")
            return@post
        }
        app.get("/registered/{token}") {

            val register = it.pathParam("token")
            registeredClients.forEach { c -> println(c.verifyToken) }
            if (registeredClients.find { c -> c.verifyToken == register } != null) {
                it.status(200)
                return@get
            } else {
                it.status(404)
                return@get
            }
        }
        app.delete("/unregister") {
            val register = it.bodyAsClass<Unregister>()
            if (registeredClients.find { c -> c.verifyToken == register.verifyToken } == null) {
                it.status(403)
                return@delete
            } else {
                registeredClients.removeAll { c -> c.verifyToken == register.verifyToken && c.auth == register.token }
                it.status(410)
                Logger.error("[Server -> Register]: Client removed (Verify = ${register.verifyToken})")
                return@delete
            }
        }
        app.post("/np/direct") {

        }
        app.post("/add/direct") {

        }
        app.post("/np") {
            val body = it.bodyAsClass<NPTrackPayload>()
            val direct = it.queryParam("direct").toBoolean()
            val generated = imgGen.generateNPTrack(
                ImageGenerator.Song(
                    body.title.substringBefore("-"),
                    body.title.substringAfter("-"),
                    length = body.duration,
                    position = body.position
                ), body.author.toString(), body.identifier
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
        val author: String? = "N/A",
        val duration: Long,
        val position: Long,
        val identifier: String?
    )

    data class IDFiles(val name: String, val size: Long)
    data class Register(val token: String)
    data class Unregister(val token: String, val verifyToken: String)
    data class CheckRegister(val verifyToken: String)
}