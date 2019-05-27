package personal.kostera.kotlin.grpc.poc.service

import io.grpc.stub.StreamObserver
import personal.kostera.kotlin.grpc.poc.PingRequest
import personal.kostera.kotlin.grpc.poc.PingResponse
import personal.kostera.kotlin.grpc.poc.PingServiceGrpc
import java.util.logging.Level
import java.util.logging.Logger

class PingServiceImpl: PingServiceGrpc.PingServiceImplBase() {
    private val logger = Logger.getLogger(PingServiceImpl::class.java.name)
    override fun ping(request: PingRequest, responseObserver: StreamObserver<PingResponse>) {
        val reply = PingResponse.newBuilder().setMessage("Pong: ${request.message}").build()
        responseObserver.onNext(reply)
        responseObserver.onCompleted()
        logger.log(Level.INFO, "Request no: ${request.counter}")
    }
}