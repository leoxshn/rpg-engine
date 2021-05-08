package io.posidon.rpgengine.util

import org.lwjgl.system.MemoryStack
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

inline class Stack(val stack: MemoryStack) {

    @OptIn(ExperimentalContracts::class)
    inline fun push(fn: (Stack) -> Unit) {
        contract { callsInPlace(fn, InvocationKind.EXACTLY_ONCE) }
        fn(Stack(stack.push()))
        stack.pop()
    }

    fun callocInt(size: Int) = stack.callocInt(size)
    fun callocLong(size: Int) = stack.callocLong(size)
    fun callocFloat(size: Int) = stack.callocFloat(size)
    fun callocPointer(size: Int) = stack.callocPointer(size)

    fun mallocInt(size: Int) = stack.mallocInt(size)
    fun mallocLong(size: Int) = stack.mallocLong(size)
    fun mallocFloat(size: Int) = stack.mallocFloat(size)
    fun mallocPointer(size: Int) = stack.mallocPointer(size)

    companion object {
        inline fun get(): Stack = Stack(MemoryStack.stackGet())
        @OptIn(ExperimentalContracts::class)
        inline fun push(fn: (Stack) -> Unit) {
            contract { callsInPlace(fn, InvocationKind.EXACTLY_ONCE) }
            get().push(fn)
        }
    }
}