package io.posidon.rpg.world.tile

import io.posidon.game.shared.types.Vec2f
import io.posidon.rpg.world.tilemap.TileChunk
import io.posidon.rpg.world.tilemap.TileLevel
import io.posidon.rpgengine.gfx.Context
import io.posidon.rpgengine.gfx.assets.Mesh

class MarchingSquaresMeshGenerator {

    private val vertices = ArrayList<Vec2f>()
    private val triangles = ArrayList<Int>()

    fun generateMesh(
        context: Context,
        map: TileLevel,
        squareSize: Float,
    ): Mesh {
        val squareGrid = SquareGrid(map, squareSize)

        squareGrid.squares.forEach { it.forEach(::triangulateSquare) }

        return context.makeMesh(triangles.toTypedArray().toIntArray(), context.makeVBO(2, *Array(vertices.size * 2) {
            val v = vertices[it / 2]
            if (it % 2 == 0) v.x else v.y
        }.toFloatArray()))
    }

    private fun triangulateSquare(square: Square) {
        when (square.configuration) {
            0b0001 -> meshFromPoints(square.bottom, square.bottomLeft, square.left)
            0b0010 -> meshFromPoints(square.right, square.bottomRight, square.bottom)
            0b0100 -> meshFromPoints(square.top, square.topRight, square.right)
            0b1000 -> meshFromPoints(square.topLeft, square.top, square.left)

            0b0011 -> meshFromPoints(square.right, square.bottomRight, square.bottomLeft, square.left)
            0b0110 -> meshFromPoints(square.top, square.topRight, square.bottomRight, square.bottom)
            0b1100 -> meshFromPoints(square.topLeft, square.topRight, square.right, square.left)
            0b1001 -> meshFromPoints(square.topLeft, square.top, square.bottom, square.bottomLeft)

            0b0101 -> meshFromPoints(square.top, square.topRight, square.right, square.bottom, square.bottomLeft, square.left)
            0b1010 -> meshFromPoints(square.topLeft, square.top, square.right, square.bottomRight, square.bottom, square.left)

            0b0111 -> meshFromPoints(square.top, square.topRight, square.bottomRight, square.bottomLeft, square.left)
            0b1011 -> meshFromPoints(square.topLeft, square.top, square.right, square.bottomRight, square.bottomLeft)
            0b1101 -> meshFromPoints(square.topLeft, square.bottomLeft, square.right, square.bottom, square.bottomLeft)
            0b1110 -> meshFromPoints(square.topLeft, square.topRight, square.bottomRight, square.bottom, square.left)

            0b1111 -> meshFromPoints(square.topLeft, square.topRight, square.bottomRight, square.bottomLeft)
        }
    }

    private fun meshFromPoints(vararg points: Node) {
        assignVertices(points)
        if (points.size >= 3) createTriangle(points[0], points[1], points[2])
        if (points.size >= 4) createTriangle(points[0], points[2], points[3])
        if (points.size >= 5) createTriangle(points[0], points[3], points[4])
        if (points.size >= 6) createTriangle(points[0], points[4], points[5])
    }

    private fun assignVertices(points: Array<out Node>) {
        for (point in points) {
            if (point.vertexIndex == -1) {
                point.vertexIndex = vertices.size
                vertices.add(point.position)
            }
        }
    }

    private fun createTriangle(a: Node, b: Node, c: Node) {
        triangles.add(a.vertexIndex)
        triangles.add(b.vertexIndex)
        triangles.add(c.vertexIndex)
    }

    private class SquareGrid(
        map: TileLevel,
        squareSize: Float,
    ) {
        val squares: Array<Array<Square>>

        init {
            val mapWidth = TileChunk.SIZE * squareSize
            val controlNodes = Array(TileChunk.SIZE) { x ->
                Array(TileChunk.SIZE) { y ->
                    val pos = Vec2f(-mapWidth/2f + x * squareSize + squareSize / 2f, -mapWidth/2f + y * squareSize + squareSize / 2f)
                    ControlNode(pos, map[x, y] != null, squareSize)
                }
            }

            squares = Array(TileChunk.SIZE - 1) { x ->
                Array(TileChunk.SIZE - 1) { y ->
                    val movedY = y + 1
                    val movedX = x + 1
                    Square(
                        controlNodes[x][movedY],
                        controlNodes[movedX][movedY],
                        controlNodes[movedX][y],
                        controlNodes[x][y]
                    )
                }
            }
        }
    }

    private class Square(
        val topLeft: ControlNode,
        val topRight: ControlNode,
        val bottomLeft: ControlNode,
        val bottomRight: ControlNode,
    ) {
        val top: Node = topLeft.right
        val right: Node = bottomRight.above
        val bottom: Node = bottomLeft.right
        val left: Node = bottomLeft.above

        val configuration: Int = run {
            var c = 0
            if (topLeft     .active) c = c or 0b1000
            if (topRight    .active) c = c or 0b0100
            if (bottomRight .active) c = c or 0b0010
            if (bottomLeft  .active) c = c or 0b0001
            c
        }
    }

    private open class Node (
        val position: Vec2f,
    ) {
        var vertexIndex: Int = -1
    }

    private class ControlNode(
        position: Vec2f,
        val active: Boolean,
        squareSize: Float
    ) : Node(position) {
        val above: Node = Node(position + Vec2f(0f, 1f) * squareSize / 2f)
        val right: Node = Node(position + Vec2f(1f, 0f) * squareSize / 2f)
    }
}