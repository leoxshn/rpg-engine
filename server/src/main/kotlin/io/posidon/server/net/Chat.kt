package io.posidon.server.net

import io.posidon.server.Server
import io.posidon.game.netApi.server.ServerApi

object Chat {

    fun post(sender: String, message: String) {
        Server.sendToAllPlayers(ServerApi.chat(sender, message, false))
    }

    fun privateMessage(sender: String, message: String, receiver: Player) {
        receiver.send(ServerApi.chat(sender, message, true))
    }
}