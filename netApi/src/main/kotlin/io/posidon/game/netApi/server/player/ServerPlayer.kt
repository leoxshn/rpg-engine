package io.posidon.game.netApi.server.player

import io.posidon.game.netApi.Packet
import io.posidon.game.netApi.Packet.Companion.SEPARATOR
import io.posidon.game.netApi.PacketTypes
import io.posidon.game.netApi.server.Server
import java.io.InputStream
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.net.Socket
import java.net.SocketException
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread

abstract class ServerPlayer <SELF : ServerPlayer<SELF>> (
    private val socket: Socket
) {
    internal var server: Server<SELF>? = null

    private val output: OutputStream = socket.getOutputStream()
    private val input: InputStream = socket.getInputStream()
    private val writer = OutputStreamWriter(output, Charsets.UTF_8)

    protected var running = true

    var name: String = socket.inetAddress.hostAddress
        private set
    var id = 0
        private set

    fun init() {
        val packet = waitForPacket().split("&")
        name = packet[1]
        id = packet[2].hashCode()
    }

    internal fun serverInit(server: Server<SELF>) {
        this.server = server
    }

    val sendLock = ReentrantLock()

    fun send(packet: Packet) {
        if (running) try {
            sendLock.lock()
            writer.write(packet.string)
            writer.write(0x0a)
            sendLock.unlock()
            writer.flush()
        }
        catch (e: SocketException) { disconnect() }
    }

    fun waitForPacket(): String {
        var tmp = ""
        do {
            try {
                tmp = input.bufferedReader(Charsets.UTF_8).readLine()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } while (!tmp.startsWith(PacketTypes.AUTH + SEPARATOR) && running)
        return tmp
    }

    fun startListenForPackets() {
        thread(name = socket.inetAddress.hostAddress) {
            while (running) {
                var string: String? = null
                try {
                    string = input.bufferedReader(Charsets.UTF_8).readLine()
                } catch (e: SocketException) {
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                if (string.isNullOrEmpty() && running) {
                    disconnect()
                } else if (string != null) onPacketReceived(Packet(string))
            }
        }
    }

    abstract fun onPacketReceived(packet: Packet)
    abstract fun onDisconnect()

    fun disconnect() {
        server!!.players.remove(id)
        destroy()
        onDisconnect()
    }

    fun destroy() {
        running = false
        try {
            output.close()
            input.close()
            socket.close()
        } catch (ignore: Exception) {}
    }
}