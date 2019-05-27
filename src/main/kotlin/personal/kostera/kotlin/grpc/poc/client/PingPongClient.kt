package personal.kostera.kotlin.grpc.poc.client

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.StatusRuntimeException
import personal.kostera.kotlin.grpc.poc.PingRequest
import personal.kostera.kotlin.grpc.poc.PingResponse
import personal.kostera.kotlin.grpc.poc.PingServiceGrpc
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.Logger

class PingPongClient(private val channel: ManagedChannel) {
    private val logger = Logger.getLogger(PingPongClient::class.java.name)
    private val blockingStub: PingServiceGrpc.PingServiceBlockingStub = PingServiceGrpc.newBlockingStub(channel)
    private var counter = 0

    constructor(host: String, port: Int) : this(
        ManagedChannelBuilder.forAddress(host, port)
            .usePlaintext()
            .build()
    )

    @Throws(InterruptedException::class)
    fun shutdown() {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS)
    }

    /** Ping server with message */
    fun ping(message: String) {
        logger.log(Level.INFO, "Will try to ping with message: ${message}")
        val request = PingRequest.newBuilder().setMessage(message).setCounter(counter++).build()
        val response: PingResponse = try {
            blockingStub.ping(request)
        } catch (e: StatusRuntimeException) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.status)
            return
        }

        logger.info("Got pong response: ${response.message}")
    }
}