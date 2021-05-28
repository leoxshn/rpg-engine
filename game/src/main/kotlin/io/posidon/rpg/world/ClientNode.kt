package io.posidon.rpg.world

import io.posidon.game.netApi.Packet
import io.posidon.game.netApi.PacketTypes
import io.posidon.game.netApi.PacketTypes.INIT
import io.posidon.game.netApi.client.Client
import io.posidon.game.netApi.client.ClientApi
import io.posidon.rpgengine.scene.node.Node

class ClientNode(val world: World) : Node() {

    val client = Client("localhost", 2512)

    override fun init() {
        client.onResult = {
            if (it) {
                client.onClose = {
                    log.e("There was a connection error")
                }
                client.send(ClientApi.auth("leoxshn", "w04m58cyp49y59ti5ts9io3k"))
                val line = client.waitForPacket(INIT)
                val tokens = line.split('&')
                val x = tokens[1].toFloat()
                val y = tokens[2].toFloat()
                world.player.position.set(x, y)
                world.initWorld(tokens[3].toInt())
            } else log.e("Couldn't connect to server")
        }
        client.startAsync(onPacketReceived = ::onPacketReceived)
    }

    override fun destroy() {
        client.stop()
    }

    fun onPacketReceived(packet: Packet) {
        when (packet.type) {
            PacketTypes.CHUNK -> world.onChunkReceived(packet)
            else -> world.player.onPacketReceived(packet)
        }
    }
}