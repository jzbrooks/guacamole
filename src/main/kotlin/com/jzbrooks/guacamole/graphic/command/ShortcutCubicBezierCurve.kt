package com.jzbrooks.guacamole.graphic.command

import com.jzbrooks.guacamole.util.math.Point

data class ShortcutCubicBezierCurve(override var variant: CommandVariant, var parameters: List<Parameter>) : VariantCommand {
    override fun toString(): String {
        val command = when (variant) {
            CommandVariant.ABSOLUTE -> 'S'
            CommandVariant.RELATIVE -> 's'
        }

        return "$command${parameters.joinToString(separator = " ")}"
    }

    data class Parameter(var endControl: Point, var end: Point) {
        override fun toString() = "$endControl $end"
    }
}