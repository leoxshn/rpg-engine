package io.posidon.game.netApi.server

import io.posidon.game.netApi.util.Compressor
import io.posidon.game.netApi.Packet
import io.posidon.game.netApi.Packet.Companion.SEPARATOR
import io.posidon.game.netApi.PacketTypes

object ServerApi {

    fun init(x: Float, y: Float, sizeInChunks: Int): Packet = Packet.make(PacketTypes.INIT, x, y, sizeInChunks)

    fun position(x: Int, y: Int): Packet = Packet.make(PacketTypes.POSITION, x, y)

    fun time(time: Double): Packet = Packet.make(PacketTypes.TIME, time)

    fun chat(sender: String, message: String, private: Boolean): Packet = Packet.make(
        PacketTypes.CHAT, if (private) '1' else '0',
        if (private) Compressor.compressString("$sender$SEPARATOR$message", 2048)
        else "$sender$SEPARATOR$message")

    fun chunk(x: Int, y: Int, dataString: String): Packet = Packet.make(PacketTypes.CHUNK, x, y, dataString)
}