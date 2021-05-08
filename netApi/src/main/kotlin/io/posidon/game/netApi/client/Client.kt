package io.posidon.game.netApi.client

import io.posidon.game.netApi.Packet
import java.io.*
import java.net.Socket
import java.net.SocketException
import kotlin.concurrent.thread

class Client(
    val ip: String,
    val port: Int
) {

    private lateinit var socket: Socket
    private lateinit var output: OutputStream
    private lateinit var input: InputStream
    private lateinit var writer: Writer

    var onResult: (Boolean) -> Unit = {}
    var onClose: () -> Unit = {}

    fun startAsync(isDaemon: Boolean = true, onPacketReceived: (Packet) -> Unit): Thread = thread (isDaemon = isDaemon) {
        start(onPacketReceived)
    }

    fun start(onPacketReceived: (Packet) -> Unit) {
        try {
            socket = Socket(ip, port)
            output = socket.getOutputStream()
            input = socket.getInputStream()
            writer = OutputStreamWriter(output, Charsets.UTF_8).buffered()

            onResult(true)

            try { input.reader(Charsets.UTF_8).forEachLine {
                onPacketReceived(Packet(it))
            }}
            catch (e: EOFException) { stop() }
            catch (e: SocketException) { stop() }
            catch (e: StreamCorruptedException) { stop() }
            stop()
        } catch (e: Exception) {
            onResult(false)
        }
    }

    fun send(packet: Packet) {
        try {
            writer.write(packet.string)
            writer.write(0x0a)
            writer.flush()
        }
        catch (e: SocketException) { stop() }
        catch (e: Exception) { e.printStackTrace() }
    }

    fun stop() {
        try {
            output.close()
            input.close()
            socket.close()
        } catch (ignore: Exception) {}
        onClose()
    }

    fun waitForPacket(name: String): String {
        var line: String
        do line = input.bufferedReader(Charsets.UTF_8).readLine()
        while (!line.startsWith("$name&"))
        return line
    }
}