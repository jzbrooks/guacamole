package com.jzbrooks.vgo.core.optimization

import assertk.assertThat
import assertk.assertions.hasSize
import com.jzbrooks.vgo.core.graphic.Path
import com.jzbrooks.vgo.core.graphic.command.ClosePath
import com.jzbrooks.vgo.core.graphic.command.CommandVariant
import com.jzbrooks.vgo.core.graphic.command.LineTo
import com.jzbrooks.vgo.core.graphic.command.MoveTo
import com.jzbrooks.vgo.core.util.math.Point
import org.junit.jupiter.api.Test

class RemoveRedundantCommandsTests {
    @Test
    fun testRedundantCommandsAreRemoved() {
        val path = Path(
                listOf(
                        MoveTo(CommandVariant.ABSOLUTE, listOf(Point(100f, 1f))),
                        LineTo(CommandVariant.ABSOLUTE, listOf(Point(103f, 6f))),
                        LineTo(CommandVariant.ABSOLUTE, listOf(Point(103f, 6f))),
                        ClosePath()
                )
        )

        RemoveRedundantCommands().visit(path)

        assertThat(path.commands.filterIsInstance<LineTo>()).hasSize(1)
        assertThat(path.commands).hasSize(3)
    }

    @Test
    fun testUniqueCommandsAreNotModified() {
        val path = Path(
                listOf(
                        MoveTo(CommandVariant.ABSOLUTE, listOf(Point(100f, 1f))),
                        LineTo(CommandVariant.ABSOLUTE, listOf(Point(103f, 6f))),
                        LineTo(CommandVariant.ABSOLUTE, listOf(Point(106f, 7f), Point(93f, 10f))),
                        ClosePath()
                )
        )

        RemoveRedundantCommands().visit(path)

        assertThat(path.commands).hasSize(4)
    }
}