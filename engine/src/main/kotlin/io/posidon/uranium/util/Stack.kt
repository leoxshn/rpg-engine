package io.posidon.uranium.util

import org.lwjgl.system.MemoryStack
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@JvmInline
value class Stack(val stack: MemoryStack) {

    @OptIn(ExperimentalContracts::class)
    inline fun <T> push(fn: (Stack) -> T): T {
        contract { callsInPlace(fn, InvocationKind.EXACTLY_ONCE) }
        val r = fn(Stack(stack.push()))
        stack.pop()
        return r
    }

    inline fun callocInt(size: Int) = stack.callocInt(size)
    inline fun callocLong(size: Int) = stack.callocLong(size)
    inline fun callocFloat(size: Int) = stack.callocFloat(size)
    inline fun callocPointer(size: Int) = stack.callocPointer(size)

    inline fun mallocInt(size: Int) = stack.mallocInt(size)
    inline fun mallocLong(size: Int) = stack.mallocLong(size)
    inline fun mallocFloat(size: Int) = stack.mallocFloat(size)
    inline fun mallocPointer(size: Int) = stack.mallocPointer(size)

    inline fun int(vararg v: Int) = mallocInt(v.size).put(v).flip()
    inline fun long(vararg v: Long) = mallocLong(v.size).put(v).flip()
    inline fun float(vararg v: Float) = mallocFloat(v.size).put(v).flip()

    companion object {
        inline fun get(): Stack = Stack(MemoryStack.stackGet())
        @OptIn(ExperimentalContracts::class)
        inline fun <T> push(fn: (Stack) -> T): T {
            contract { callsInPlace(fn, InvocationKind.EXACTLY_ONCE) }
            return get().push(fn)
        }
    }
}