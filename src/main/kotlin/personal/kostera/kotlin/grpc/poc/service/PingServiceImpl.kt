package personal.kostera.kotlin.grpc.poc.service

import io.grpc.stub.StreamObserver
import personal.kostera.kotlin.grpc.poc.*
import java.util.logging.Level
import java.util.logging.Logger

class PingServiceImpl : PingServiceGrpc.PingServiceImplBase() {

    private val logger = Logger.getLogger(PingServiceImpl::class.java.name)

    override fun ping(request: PingRequest, responseObserver: StreamObserver<PingResponse>) {
        val reply = PingResponse.newBuilder().setMessage("Pong: ${request.message}").build()
        responseObserver.onNext(reply)
        responseObserver.onCompleted()
        logger.log(Level.INFO, "Request no: ${request.counter}")
    }

    override fun getCharacters(request: StringRequest, responseObserver: StreamObserver<StringResponse>) {
        for (char in request.message) {
            responseObserver.onNext(StringResponse.newBuilder().setMessage(char.toString()).build())
        }
        responseObserver.onCompleted();
    }

    override fun getWord(responseObserver: StreamObserver<StringResponse>): StreamObserver<StringRequest> {
        return object : StreamObserver<StringRequest> {
            private var wordResult = ""

            override fun onNext(value: StringRequest) {
                logger.log(Level.INFO, "onNext: Got message: ${value.message}")
                wordResult += value.message
            }

            override fun onError(t: Throwable) {
                logger.log(Level.SEVERE, "Exception occurred while streaming response", t)
            }

            override fun onCompleted() {
                responseObserver.onNext(StringResponse.newBuilder().setMessage(wordResult).build())
            }
        }
    }
}