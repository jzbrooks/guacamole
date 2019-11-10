package com.jzbrooks.guacamole.svg

import com.jzbrooks.guacamole.core.graphic.Graphic
import com.jzbrooks.guacamole.core.optimization.*
import com.jzbrooks.guacamole.core.optimization.OptimizationRegistry
import com.jzbrooks.guacamole.svg.optimization.BakeTransformations

class OptimizationRegistry : OptimizationRegistry {
    override fun apply(graphic: Graphic) {
        for (optimization in optimizations) {
            optimization.optimize(graphic)
        }
    }

    companion object {
        private val optimizations = listOf(
                BakeTransformations(),
                CollapseGroups(),
                MergePaths(),
                BreakoutImplicitCommands(),
                CommandVariant(),
                RemoveRedundantCommands(),
                SimplifyLineCommands(0.00001f),
                RemoveEmptyGroups()
        )
    }
}