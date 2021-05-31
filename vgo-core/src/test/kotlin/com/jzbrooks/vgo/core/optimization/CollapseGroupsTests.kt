package com.jzbrooks.vgo.core.optimization

import assertk.assertThat
import assertk.assertions.containsExactly
import com.jzbrooks.vgo.core.graphic.ContainerElement
import com.jzbrooks.vgo.core.graphic.Element
import com.jzbrooks.vgo.core.graphic.Graphic
import com.jzbrooks.vgo.core.graphic.Group
import com.jzbrooks.vgo.core.graphic.command.CommandVariant
import com.jzbrooks.vgo.core.graphic.command.MoveTo
import com.jzbrooks.vgo.core.util.element.createPath
import com.jzbrooks.vgo.core.util.element.traverseBottomUp
import com.jzbrooks.vgo.core.util.math.Matrix3
import com.jzbrooks.vgo.core.util.math.Point
import org.junit.jupiter.api.Test

class CollapseGroupsTests {
    @Test
    fun testCollapseSingleUnnecessaryGroup() {
        val innerPath = createPath(listOf(MoveTo(CommandVariant.ABSOLUTE, listOf(Point(10f, 15f)))))
        val group = Group(listOf(innerPath))

        val graphic = object : Graphic {
            override var elements: List<Element> = listOf(group)
            override val id: String? = null
            override val foreign: MutableMap<String, String> = mutableMapOf()
        }

        val groupCollapser = CollapseGroups()
        traverseBottomUp(graphic) {
            if (it is ContainerElement) groupCollapser.visit(it)
        }

        assertThat(graphic::elements).containsExactly(innerPath)
    }

    @Test
    fun testCollapseSingleUnnecessaryNestedGroups() {
        val innerPath = createPath(listOf(MoveTo(CommandVariant.ABSOLUTE, listOf(Point(10f, 15f)))))
        val group = Group(listOf(Group(listOf(innerPath))))
        val graphic = object : Graphic {
            override var elements: List<Element> = listOf(group)
            override val id: String? = null
            override val foreign: MutableMap<String, String> = mutableMapOf()
        }

        val groupCollapser = CollapseGroups()
        traverseBottomUp(graphic) {
            if (it is ContainerElement) groupCollapser.visit(it)
        }

        assertThat(graphic::elements).containsExactly(innerPath)
    }

    @Test
    fun testRetainNestedGroupWithAttributes() {
        val scale = Matrix3.from(floatArrayOf(20f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f))

        val innerPath = createPath(listOf(MoveTo(CommandVariant.ABSOLUTE, listOf(Point(10f, 15f)))))
        val innerGroupWithAttributes = Group(listOf(innerPath), null, mutableMapOf(), scale)
        val group = Group(listOf(innerGroupWithAttributes))

        val graphic = object : Graphic {
            override var elements: List<Element> = listOf(group)
            override val id: String? = null
            override val foreign: MutableMap<String, String> = mutableMapOf()
        }

        val groupCollapser = CollapseGroups()
        traverseBottomUp(graphic) {
            if (it is ContainerElement) groupCollapser.visit(it)
        }

        assertThat(graphic::elements).containsExactly(innerGroupWithAttributes)
    }
}
