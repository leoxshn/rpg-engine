package io.posidon.game.shared

import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.InputStreamReader

object Resources {
    fun loadAsString(path: String): String {
        val result = StringBuilder()
        val s = Resources::class.java.getResourceAsStream(path) ?: throw FileNotFoundException("Couldn't find the file $path")
        BufferedReader(InputStreamReader(s)).use { reader ->
            var line: String?
            while (reader.readLine().also { line = it } != null) result.append(line).append("\n")
        }
        return result.toString()
    }

    fun loadAsByteBuffer(path: String, bufferSize: Int): ByteArray {
        val buffer = ByteArray(bufferSize)
        val s = Resources::class.java.getResourceAsStream(path) ?: throw FileNotFoundException("Couldn't find the file $path")
        s.use { it.read(buffer) }
        return buffer
    }

    fun getRealPath(path: String): String {
        return Resources::class.java.getResource(path)?.file ?: throw FileNotFoundException("No resource at \"$path\"")
    }
}