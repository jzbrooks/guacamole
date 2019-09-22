package com.jzbrooks.guacamole.vd

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import com.jzbrooks.guacamole.assertk.extensions.containsKey
import com.jzbrooks.guacamole.assertk.extensions.containsKeys
import com.jzbrooks.guacamole.assertk.extensions.doesNotContainKey
import com.jzbrooks.guacamole.graphic.Graphic
import com.jzbrooks.guacamole.graphic.Path
import com.jzbrooks.guacamole.graphic.command.*
import kotlin.test.Test

class VectorDrawableReaderTests {
    @Test
    fun testParseDimensions() {
        javaClass.getResourceAsStream("/vd_visibilitystrike.xml").use {
            val graphic: Graphic = parse(it)

            assertThat(graphic.attributes["android:width"]).isEqualTo("24dp")
            assertThat(graphic.attributes["android:height"]).isEqualTo("24dp")
        }
    }

    @Test
    fun testParseMetadataDoesNotContainPathData() {
        javaClass.getResourceAsStream("/vd_visibilitystrike.xml").use {
            val graphic: Graphic = parse(it)

            val path = graphic.elements.first() as Path

            assertThat(path.attributes).doesNotContainKey("android:pathData")
        }
    }

    @Test
    fun testParseMetadata() {
        javaClass.getResourceAsStream("/vd_visibilitystrike.xml").use {
            val graphic: Graphic = parse(it)

            val path = graphic.elements.first() as Path

            assertThat(path.attributes).containsKeys("android:name", "android:strokeWidth", "android:fillColor")
        }
    }

    @Test
    fun testParsePaths() {
        javaClass.getResourceAsStream("/vd_visibilitystrike.xml").use {
            val graphic: Graphic = parse(it)

            val path = graphic.elements.first() as Path
            assertThat(path.commands).isEqualTo(
                    listOf(
                            MoveTo(CommandVariant.ABSOLUTE, listOf(Point(2f, 4.27f))),
                            LineTo(CommandVariant.ABSOLUTE, listOf(Point(3.27f, 3f))),
                            LineTo(CommandVariant.ABSOLUTE, listOf(Point(3.27f, 3f))),
                            LineTo(CommandVariant.ABSOLUTE, listOf(Point(2f, 4.27f))),
                            ClosePath()
                    )
            )
            assertThat(graphic.elements).hasSize(3)
        }
    }

    @Test
    fun testStoreNameForPath() {
        javaClass.getResourceAsStream("/vd_visibilitystrike.xml").use {
            val graphic: Graphic = parse(it)

            val path = graphic.elements.first() as Path

            assertThat(path.attributes).containsKey("android:name")
            assertThat(path.attributes["android:name"]).isEqualTo("strike_thru_path")
        }
    }
}