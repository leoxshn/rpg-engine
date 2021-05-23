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
                val defs = tokens[1].split(',')
                for (def in defs) {
                    val eqI = def.indexOf('=')
                    world.blockDictionary[def.substring(0, eqI).toInt()] = def.substring(eqI + 1)
                }
                val x = tokens[2].toFloat()
                val y = tokens[3].toFloat()
                val h = tokens[4].toInt()
                world.player.position.set(x, y, h)
                world.initWorld(tokens[5].toInt())
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
            PacketTypes.SET_BLOCK -> world.onSetBlockReceived(packet)
            PacketTypes.TIME -> world.updateTime(packet.parseTime())
            else -> world.player.onPacketReceived(packet)
        }
    }
}