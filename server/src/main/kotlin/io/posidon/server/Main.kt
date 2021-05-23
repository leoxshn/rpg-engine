package io.posidon.server

import io.posidon.game.netApi.server.Server
import io.posidon.game.netApi.server.ServerApi
import io.posidon.game.shared.ConsoleColors
import io.posidon.server.cli.Console
import io.posidon.server.net.Player
import io.posidon.game.netApi.world.Block
import io.posidon.server.world.World
import java.io.IOException

var running = true
inline fun loop(methods: () -> Unit) { while (running) methods() }

val Server = Server<Player>(2512)

fun main(args: Array<String>) {
	Thread(Console()).start()
	Server.onException = { it.print() }
	Server.start {
		Player(it).also { player ->
			player.init()
			player.send(ServerApi.init(0f, World.getDefaultSpawnPosition().toFloat(), 0, buildString {
				for (value in Block.values())
					append(value.ordinal).append('=').append(value.id).append(',')
				deleteCharAt(lastIndex)
			}, World.sizeInChunks))
			Console.beforeCmdLine {
				Console.printInfo(player.name, " joined the server")
			}
			player.start()
		}
	}
	World.init(7480135)

	var lastTime: Long = System.nanoTime()
	var delta = 0.0
	loop {
		val now: Long = System.nanoTime()
		delta += (now - lastTime) / 1000000000.0
		while (delta >= 0.001) {
			Globals.tick(delta)
			delta = 0.0
		}
		lastTime = now
	}
}

fun stop() {
	running = false
	Console.println("Stopping server...")
	try { Server.close() }
	catch (e: IOException) { e.print() }
}

fun Throwable.print() = Console.beforeCmdLine {
	print(ConsoleColors.RED)
	printStackTrace()
	print(ConsoleColors.RESET)
}