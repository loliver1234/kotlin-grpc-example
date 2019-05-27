package personal.kostera.kotlin.grpc.poc

import personal.kostera.kotlin.grpc.poc.client.PingPongClient
import personal.kostera.kotlin.grpc.poc.server.PingPongServer


private const val serverHost = "localhost"
private const val serverPort = 8080
private val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Please provide --server or --client as command-line argument")
        return
    }
    when (args[0]) {
        "--server" -> {
            val server = PingPongServer()
            server.start(serverPort)
            server.blockUntilShutdown()
        }
        "--client" -> {
            val client = PingPongClient(serverHost, serverPort)
            while (true) {
                val message = (1..10)
                    .map { _ -> kotlin.random.Random.nextInt(0, charPool.size) }
                    .map(charPool::get)
                    .joinToString("")
                client.ping(message)
                Thread.sleep(1000)
            }
        }
    }
}
