package com.jzbrooks.vgo.vd

import com.jzbrooks.vgo.core.optimization.*
import com.jzbrooks.vgo.vd.optimization.BakeTransformations

class VectorDrawableOptimizationRegistry : OptimizationRegistry(topDownOptimizations, emptyList(), wholeGraphicOptimizations) {

    companion object {
        private val topDownOptimizations: List<TopDownOptimization> = listOf(
                BakeTransformations(),
                BreakoutImplicitCommands(),
                SimplifyLineCommands(0.00001f),
                RemoveRedundantCommands(),
                CommandVariant(VectorDrawableCommandPrinter(3))
        )

        private val wholeGraphicOptimizations = listOf(
                CollapseGroups(),
                MergePaths(),
                RemoveEmptyGroups()
        )
    }
}