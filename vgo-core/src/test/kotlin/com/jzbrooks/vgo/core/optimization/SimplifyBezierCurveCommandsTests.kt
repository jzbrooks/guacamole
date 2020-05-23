package com.jzbrooks.vgo.core.optimization

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jzbrooks.vgo.core.graphic.Path
import com.jzbrooks.vgo.core.graphic.command.*
import com.jzbrooks.vgo.core.graphic.command.CommandVariant
import com.jzbrooks.vgo.core.util.math.Point
import org.junit.jupiter.api.Test

class SimplifyBezierCurveCommandsTests {
    @Test
    fun testCurrentCubicAndPreviousCubicUsesShorthand() {
        val path = Path(
                listOf(
                        MoveTo(CommandVariant.RELATIVE, listOf(Point(100f, 200f))),
                        CubicBezierCurve(CommandVariant.RELATIVE, listOf(
                                CubicBezierCurve.Parameter(Point(100f, 100f), Point(450f, 100f), Point(250f, 200f)))
                        ),
                        CubicBezierCurve(CommandVariant.RELATIVE, listOf(
                                CubicBezierCurve.Parameter(Point(-200f, 100f), Point(400f, 300f), Point(250f, 200f)))
                        )
                )
        )

        SimplifyBezierCurveCommands(0.00001f).visit(path)

        assertThat(path.commands.last()::class).isEqualTo(ShortcutCubicBezierCurve::class)
    }

    @Test
    fun testCurrentCubicAndPreviousShortcutUsesShorthand() {
        val path = Path(
                listOf(
                        MoveTo(CommandVariant.RELATIVE, listOf(Point(100f, 200f))),
                        ShortcutCubicBezierCurve(CommandVariant.RELATIVE, listOf(
                                ShortcutCubicBezierCurve.Parameter(Point(450f, 100f), Point(250f, 200f)))
                        ),
                        CubicBezierCurve(CommandVariant.RELATIVE, listOf(
                                CubicBezierCurve.Parameter(Point(-200f, 100f), Point(400f, 300f), Point(250f, 200f)))
                        )
                )
        )

        SimplifyBezierCurveCommands(0.00001f).visit(path)

        assertThat(path.commands.last()::class).isEqualTo(ShortcutCubicBezierCurve::class)
    }

    @Test
    fun testCurrentNonCubicNonShorthandUsesShorthand() {
        val path = Path(
                listOf(
                        MoveTo(CommandVariant.RELATIVE, listOf(Point(0f, 0f))),
                        CubicBezierCurve(CommandVariant.RELATIVE, listOf(
                                CubicBezierCurve.Parameter(Point(0f, 0f), Point(400f, 300f), Point(250f, 200f)))
                        )
                )
        )

        SimplifyBezierCurveCommands(0.00001f).visit(path)

        assertThat(path.commands.last()::class).isEqualTo(ShortcutCubicBezierCurve::class)
    }

    @Test
    fun testCurrentQuadraticAndPreviousQuadraticUsesShorthand() {
        val path = Path(
                listOf(
                        MoveTo(CommandVariant.RELATIVE, listOf(Point(100f, 200f))),
                        QuadraticBezierCurve(CommandVariant.RELATIVE, listOf(
                                QuadraticBezierCurve.Parameter(Point(450f, 100f), Point(250f, 200f)))
                        ),
                        QuadraticBezierCurve(CommandVariant.RELATIVE, listOf(
                                QuadraticBezierCurve.Parameter(Point(-200f, 100f), Point(400f, 300f)))
                        )
                )
        )

        SimplifyBezierCurveCommands(0.00001f).visit(path)

        assertThat(path.commands.last()::class).isEqualTo(ShortcutQuadraticBezierCurve::class)
    }

    @Test
    fun testCurrentQuadraticAndPreviousShorthandUsesShorthand() {
        val path = Path(
                listOf(
                        MoveTo(CommandVariant.RELATIVE, listOf(Point(100f, 200f))),
                        QuadraticBezierCurve(CommandVariant.RELATIVE, listOf(
                                QuadraticBezierCurve.Parameter(Point(-300f, 100f), Point(150f, 50f)))
                        ),
                        ShortcutQuadraticBezierCurve(CommandVariant.RELATIVE, listOf(
                                Point(250f, 200f))
                        ),
                        QuadraticBezierCurve(CommandVariant.RELATIVE, listOf(
                                QuadraticBezierCurve.Parameter(Point(-200f, 100f), Point(250f, 200f)))
                        )
                )
        )

        SimplifyBezierCurveCommands(0.00001f).visit(path)

        assertThat(path.commands.last()::class).isEqualTo(ShortcutQuadraticBezierCurve::class)
    }

    @Test
    fun testStraightCubicBezierCurveIsConvertedToLineTo() {
        val path = Path(
                listOf(
                        MoveTo(CommandVariant.RELATIVE, listOf(Point(100f, 200f))),
                        CubicBezierCurve(CommandVariant.RELATIVE, listOf(
                                CubicBezierCurve.Parameter(Point(20f, 20f), Point(20f, 20f), Point(20f, 20f)))
                        )
                )
        )

        SimplifyBezierCurveCommands(0.00001f).visit(path)

        assertThat(path.commands.last()::class).isEqualTo(LineTo::class)
    }

    @Test
    fun testShortcutCubicIsExpandedIfPreviousIsStraightened() {
        val path = Path(
                listOf(
                        MoveTo(CommandVariant.RELATIVE, listOf(Point(100f, 200f))),
                        CubicBezierCurve(CommandVariant.RELATIVE, listOf(
                                CubicBezierCurve.Parameter(Point(20f, 20f), Point(20f, 20f), Point(20f, 20f)))
                        ),
                        ShortcutCubicBezierCurve(CommandVariant.RELATIVE, listOf(
                                ShortcutCubicBezierCurve.Parameter(Point(450f, 100f), Point(250f, 200f)))
                        )
                )
        )

        SimplifyBezierCurveCommands(0.00001f).visit(path)

        assertThat(path.commands.last()::class).isEqualTo(CubicBezierCurve::class)
        assertThat((path.commands.last() as CubicBezierCurve).parameters.last().startControl).isEqualTo(Point.ZERO)
    }
}