package com.jzbrooks.vgo.core.graphic.command

import com.jzbrooks.vgo.core.util.math.Point

data class LineTo(
    override var variant: CommandVariant,
    override var parameters: List<Point>
) : ParameterizedCommand<Point>
