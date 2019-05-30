package personal.kostera.kotlin.grpc.poc.client

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.StatusRuntimeException
import io.grpc.stub.StreamObserver
import personal.kostera.kotlin.grpc.poc.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.Logger

class PingPongClient(private val channel: ManagedChannel) {
    private val logger = Logger.getLogger(PingPongClient::class.java.name)
    private val blockingStub: PingServiceGrpc.PingServiceBlockingStub = PingServiceGrpc.newBlockingStub(channel)
    private val asyncStub: PingServiceGrpc.PingServiceStub = PingServiceGrpc.newStub(channel)
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

    fun getCharacters(message: String) {
        logger.log(Level.INFO, "Will try to get characters from message: ${message}")
        val request = StringRequest.newBuilder().setMessage(message).build()
        val response = try {
            blockingStub.getCharacters(request)
        } catch (e: StatusRuntimeException) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.status)
            return
        }
        for (item in response) {
            logger.log(Level.INFO, "Got character: ${item.message}")
        }
    }

    fun getWord(charactersList: List<Char>): String {
        logger.log(Level.INFO, "Will try to get full word while streaming characters: ${charactersList}")
        var result = ""
        val finishLatch = CountDownLatch(1);

        val responseObserver = object : StreamObserver<StringResponse> {
            override fun onNext(value: StringResponse) {
                logger.log(Level.INFO, "Got word: ${value.message}")
                result = value.message
            }

            override fun onError(t: Throwable?) {
                logger.log(Level.SEVERE, "Exception occurred while streaming response", t)
                finishLatch.countDown();
            }

            override fun onCompleted() {
                logger.log(Level.INFO, "Response ended")
                finishLatch.countDown();
            }

        }
        val requestObserver = try {
            asyncStub.getWord(responseObserver)
        } catch (e: StatusRuntimeException) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.status)
            return ""
        }
        for (item in charactersList) {
            requestObserver.onNext(StringRequest.newBuilder().setMessage(item.toString()).build())
        }
        requestObserver.onCompleted()

        // Receiving happens asynchronously
        finishLatch.await(1, TimeUnit.MINUTES);
        return result
    }
}