package io.posidon.game.netApi.client

import io.posidon.game.netApi.Packet
import io.posidon.game.netApi.PacketTypes

object ClientApi {

    fun goto(x: Float, y: Float): Packet = Packet.make(PacketTypes.GOTO, x, y)

    fun auth(userName: String, accessCode: String): Packet = Packet.make(PacketTypes.AUTH, userName, accessCode)
}