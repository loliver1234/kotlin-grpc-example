package personal.kostera.kotlin.grpc.poc

import personal.kostera.kotlin.grpc.poc.client.PingPongClient
import personal.kostera.kotlin.grpc.poc.server.PingPongServer


private const val serverHost = "localhost"
private const val serverPort = 8080

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
            client.ping("Hi, I'm the client and I'm pinging you with my RPC ping call")
            client.getCharacters("This message should be split into characters, with each char as a response in a stream")
            client.getWord(listOf('H', 'e', 'l', 'l', 'o', ' ', 'W', 'o', 'r', 'l', 'd', '!'))
        }
    }
}
