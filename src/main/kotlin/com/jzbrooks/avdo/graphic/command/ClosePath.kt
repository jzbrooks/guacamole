package com.jzbrooks.avdo.graphic.command

class ClosePath : Command {
    override fun equals(other: Any?) = other is ClosePath
    override fun hashCode() = javaClass.hashCode()
    override fun toString(): String = "Z"
}