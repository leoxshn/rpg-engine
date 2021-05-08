package io.posidon.game.netApi.client

import io.posidon.game.netApi.Packet
import io.posidon.game.netApi.PacketTypes

object ClientApi {

    fun breakBlock(x: Int, y: Int): Packet = Packet.make(PacketTypes.BREAK_BLOCK, x, y)

    fun goto(x: Float, y: Float): Packet = Packet.make(PacketTypes.GOTO, x, y)

    fun auth(userName: String, accessCode: String): Packet = Packet.make(PacketTypes.AUTH, userName, accessCode)
}