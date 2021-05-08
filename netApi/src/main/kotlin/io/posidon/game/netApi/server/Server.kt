package io.posidon.game.netApi.server

import io.posidon.game.netApi.Packet
import io.posidon.game.netApi.server.player.PlayerManager
import io.posidon.game.netApi.server.player.ServerPlayer
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import java.net.URL
import kotlin.concurrent.thread

class Server <Player : ServerPlayer<Player>> (val port: Int) {

    val players = PlayerManager<Player>()

    private val socket = ServerSocket(port)

    var onException: (Exception) -> Unit = { it.printStackTrace() }

    fun start(onSocketAccepted: (Socket) -> Player) = thread (isDaemon = true) {
        try {
            while (true) {
                val p = onSocketAccepted(socket.accept())
                p.serverInit(this)
                players.add(p)
            }
        }
        catch (e: SocketException) {}
        catch (e: Exception) { onException(e) }
    }

    fun close() {
        for (player in players) {
            player.disconnect()
        }
        socket.close()
    }

    fun getExtIP(): String? {
        var out: String? = null
        try {
            val ipUrl = URL("http://checkip.amazonaws.com")
            var input: BufferedReader? = null
            try {
                input = BufferedReader(InputStreamReader(ipUrl.openStream()))
                out = input.readLine()
            } catch (e: Exception) {}
            input?.close()
        } catch (e: Exception) {}
        return out
    }

    fun sendToAllPlayers(packet: Packet) {
        for (p in players) {
            p.send(packet)
        }
    }
}