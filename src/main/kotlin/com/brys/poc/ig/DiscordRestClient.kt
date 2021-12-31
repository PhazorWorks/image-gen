package com.brys.poc.ig

import dev.kord.common.entity.DiscordMessageReference
import dev.kord.common.entity.Snowflake
import dev.kord.common.entity.optional.Optional
import dev.kord.common.entity.optional.OptionalBoolean
import dev.kord.common.entity.optional.OptionalSnowflake
import dev.kord.rest.NamedFile
import dev.kord.rest.json.request.MessageCreateRequest
import dev.kord.rest.json.request.MultipartMessageCreateRequest
import dev.kord.rest.service.RestClient
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.concurrent.ExecutorService
import javax.imageio.ImageIO
import kotlin.system.measureTimeMillis


class DiscordRestClient(val verifyToken: String, val auth: String, private val executor: ExecutorService) {
    private val rest = RestClient(auth)
    fun replyMessage(channel: String, reply: MessageReplyRef, type: String, image: BufferedImage) {
        executor.submit {

            val messageTiming = measureTimeMillis {
                val os = ByteArrayOutputStream()
                ImageIO.write(image, "png", os)
                val bufferedInputStream: InputStream = ByteArrayInputStream(os.toByteArray())
                suspend {
                    rest.channel.createMessage(
                        Snowflake(channel),
                        MultipartMessageCreateRequest(
                            MessageCreateRequest(
                                messageReference = Optional.invoke(
                                    DiscordMessageReference(
                                        OptionalSnowflake.Value(Snowflake(reply.messageID)),
                                        OptionalSnowflake.Value(Snowflake(reply.channel)),
                                        OptionalSnowflake.Value(Snowflake(reply.guild)),
                                        OptionalBoolean.Value(false)
                                    )
                                )
                            ),
                            listOf(NamedFile(type, bufferedInputStream))
                        )
                    )
                }
            }
            Logger.success("[ThreadPool -> SendDirect -> WithReply]: Sent in ${messageTiming}ms")
        }
    }
    fun replyMessage(channel: String, type: String, image: BufferedImage) {
        executor.submit {
            val messageTiming = measureTimeMillis {
                val os = ByteArrayOutputStream()
                ImageIO.write(image, "png", os)
                val bufferedInputStream: InputStream = ByteArrayInputStream(os.toByteArray())
                suspend {
                    rest.channel.createMessage(
                        Snowflake(channel),
                        MultipartMessageCreateRequest(
                            MessageCreateRequest(),
                            listOf(NamedFile(type, bufferedInputStream))
                        )
                    )
                }
            }
            Logger.success("[ThreadPool -> SendDirect -> WithoutReply]: Sent in ${messageTiming}ms")
        }
    }
    init {
        Logger.debug("[ClassLoader -> com.brys.poc.ig.DiscordRestClient]: Initalized")
    }

    data class MessageReplyRef(val messageID: String, val channel: String, val guild: String)
}