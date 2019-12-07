package com.jzbrooks.guacamole.core.graphic.command

data class HorizontalLineTo(
        override var variant: CommandVariant,
        override var parameters: List<Float>
) : ParameterizedCommand<Float>