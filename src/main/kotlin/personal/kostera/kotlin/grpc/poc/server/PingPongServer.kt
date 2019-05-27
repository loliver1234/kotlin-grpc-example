package personal.kostera.kotlin.grpc.poc.server

import io.grpc.Server
import io.grpc.ServerBuilder
import personal.kostera.kotlin.grpc.poc.service.PingServiceImpl
import java.util.logging.Level
import java.util.logging.Logger

class PingPongServer {
    private val logger = Logger.getLogger(PingPongServer::class.java.name)
    private var server: Server? = null

    fun start(port: Int) {
        server = ServerBuilder.forPort(port)
            .addService(PingServiceImpl())
            .build()
            .start()

        logger.log(Level.INFO, "Server started, listening on {0}", port.toString())

        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down")
                this@PingPongServer.stop()
                System.err.println("*** server shut down")
            }
        })
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    @Throws(InterruptedException::class)
    fun blockUntilShutdown() {
        server?.awaitTermination()
    }

    fun stop() {
        server?.shutdown()
    }
}