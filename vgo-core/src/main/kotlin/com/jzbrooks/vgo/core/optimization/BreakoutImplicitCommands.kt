package com.jzbrooks.vgo.core.optimization

import com.jzbrooks.vgo.core.graphic.PathElement
import com.jzbrooks.vgo.core.graphic.command.Command
import com.jzbrooks.vgo.core.graphic.command.CommandVariant
import com.jzbrooks.vgo.core.graphic.command.CubicBezierCurve
import com.jzbrooks.vgo.core.graphic.command.EllipticalArcCurve
import com.jzbrooks.vgo.core.graphic.command.HorizontalLineTo
import com.jzbrooks.vgo.core.graphic.command.LineTo
import com.jzbrooks.vgo.core.graphic.command.MoveTo
import com.jzbrooks.vgo.core.graphic.command.ParameterizedCommand
import com.jzbrooks.vgo.core.graphic.command.QuadraticBezierCurve
import com.jzbrooks.vgo.core.graphic.command.SmoothCubicBezierCurve
import com.jzbrooks.vgo.core.graphic.command.SmoothQuadraticBezierCurve
import com.jzbrooks.vgo.core.graphic.command.VerticalLineTo

/**
 * Enables more resolution in the the other command
 * related optimizations like [CommandVariant] and [RemoveRedundantCommands]
 */
class BreakoutImplicitCommands : TopDownOptimization, PathElementVisitor {
    override fun visit(pathElement: PathElement) {
        val commands = mutableListOf<Command>()

        for (current in pathElement.commands) {
            if (current is ParameterizedCommand<*> && current.parameters.size > 1) {
                val splitCommands = divideParameters(current)
                commands.addAll(splitCommands)
            } else {
                commands.add(current)
            }
        }

        pathElement.commands = commands
    }

    private fun divideParameters(first: ParameterizedCommand<*>): List<Command> {
        return when (first) {
            is MoveTo -> first.parameters.mapIndexed { i, it ->
                if (i == 0) first.copy(parameters = listOf(it)) else LineTo(first.variant, listOf(it))
            }
            is LineTo -> first.parameters.map { first.copy(parameters = listOf(it)) }
            is SmoothQuadraticBezierCurve -> first.parameters.map { first.copy(parameters = listOf(it)) }
            is HorizontalLineTo -> first.parameters.map { first.copy(parameters = listOf(it)) }
            is VerticalLineTo -> first.parameters.map { first.copy(parameters = listOf(it)) }
            is QuadraticBezierCurve -> first.parameters.map { first.copy(parameters = listOf(it)) }
            is CubicBezierCurve -> first.parameters.map { first.copy(parameters = listOf(it)) }
            is SmoothCubicBezierCurve -> first.parameters.map { first.copy(parameters = listOf(it)) }
            is EllipticalArcCurve -> first.parameters.map { first.copy(parameters = listOf(it)) }
            else -> throw IllegalArgumentException("Cannot divide parameters for command type ${first::class}")
        }
    }
}
