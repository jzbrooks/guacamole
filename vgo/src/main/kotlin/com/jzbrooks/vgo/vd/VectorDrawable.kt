package com.jzbrooks.vgo.vd

import com.jzbrooks.vgo.core.graphic.Element
import com.jzbrooks.vgo.core.graphic.Graphic
import com.jzbrooks.vgo.core.graphic.Attributes as CoreAttributes

data class VectorDrawable(
    override var elements: List<Element>,
    override var attributes: Attributes = Attributes(null, mutableMapOf()),
) : Graphic {

    data class Attributes(override val id: String?, override val foreign: MutableMap<String, String>) : CoreAttributes
}
