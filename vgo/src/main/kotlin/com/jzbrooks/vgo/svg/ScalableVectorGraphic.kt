package com.jzbrooks.vgo.svg

import com.jzbrooks.vgo.core.graphic.Element
import com.jzbrooks.vgo.core.graphic.Graphic

data class ScalableVectorGraphic(
    override var elements: List<Element>,
    override val id: String?,
    override val foreign: MutableMap<String, String>,
) : Graphic
