package com.jzbrooks.vgo.vd

import com.jzbrooks.vgo.core.graphic.*
import com.jzbrooks.vgo.core.util.math.Matrix3
import com.jzbrooks.vgo.svg.ScalableVectorGraphic
import com.jzbrooks.vgo.vd.graphic.ClipPath
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val hexWithAlpha = Regex("#[a-fA-F\\d]{8}")

fun VectorDrawable.toSvg(): ScalableVectorGraphic {
    val graphic = traverse(this) as ContainerElement
    return ScalableVectorGraphic(graphic.elements, convertTopLevelAttributes(attributes))
}

private fun traverse(element: Element): Element {
    return when (element) {
        is ContainerElement -> process(element)
        is PathElement -> process(element)
        else -> element
    }
}

private fun process(containerElement: ContainerElement): Element {
    val newElements = mutableListOf<Element>()
    var defs: Extra? = null

    for ((index, element) in containerElement.elements.withIndex()) {
        if (element !is ClipPath) {
            if (defs != null) {
                element.attributes.foreign["clip-path"] = "url(#${defs.attributes.name})"
            }
            newElements.add(traverse(element))
        } else {
            defs = Extra("defs", listOf(Path(element.commands)), Extra.Attributes("clip_$index", mutableMapOf()))
        }
    }

    if (defs != null) {
        newElements.add(defs)
    }

    val newAttributes = convertContainerElementAttributes(containerElement.attributes.foreign.toMutableMap())
    containerElement.attributes.foreign.clear()
    containerElement.attributes.foreign.putAll(newAttributes)

    return containerElement.apply { elements = newElements }
}

private fun process(pathElement: PathElement): Element {
    return pathElement.apply {
        val newElements = convertPathElementAttributes(attributes.foreign.toMutableMap())
        attributes.foreign.clear()
        attributes.foreign.putAll(newElements)
    }
}

private fun convertPathElementAttributes(attributes: MutableMap<String, String>): MutableMap<String, String> {
    val svgPathElementAttributes = mutableMapOf<String, String>()

    for ((key, value) in attributes) {

        var newValue = value
        if (hexWithAlpha.matches(value)) {
            newValue = '#' + value.substring(3)
        }

        val svgKey = when (key) {
            "android:fillColor" -> "fill"
            "android:fillType" -> "fill-rule"
            "android:fillAlpha" -> "fill-opacity"
            // todo: figure out what to do with these
            // "android:trimPathStart" -> ""
            // "android:trimPathEnd" -> ""
            // "android:trimPathOffset" -> ""
            "android:strokeColor" -> "stroke"
            "android:strokeWidth" -> "stroke-width"
            "android:strokeAlpha" -> "stroke-opacity"
            "android:strokeLineCap" -> "stroke-linecap"
            "android:strokeLineJoin" -> "stroke-linejoin"
            "android:strokeMiterLimit" -> "stroke-miterlimit"
            else -> key
        }

        svgPathElementAttributes[svgKey] = newValue
    }

    // We've mangled the map at this point...
    attributes.clear()

    return svgPathElementAttributes
}

private fun convertContainerElementAttributes(attributes: MutableMap<String, String>): MutableMap<String, String> {
    val svgPathElementAttributes = mutableMapOf<String, String>()

    val transform = computeTransformationMatrix(attributes)
    if (transform != Matrix3.IDENTITY) {
        val matrixStringBuilder = StringBuilder("matrix(").apply {
            append(transform[0,0])
            append(", ")
            append(transform[1,0])
            append(", ")
            append(transform[0,1])
            append(", ")
            append(transform[1,1])
            append(", ")
            append(transform[0,2])
            append(", ")
            append(transform[1,2])
            append(")")
        }

        svgPathElementAttributes["transform"] = matrixStringBuilder.toString()
    }

    return svgPathElementAttributes
}

private fun convertTopLevelAttributes(attributes: VectorDrawable.Attributes): ScalableVectorGraphic.Attributes {
    val foreignAttributes = attributes.foreign

    val viewportHeight = foreignAttributes.remove("android:viewportHeight") ?: "24"
    val viewportWidth = foreignAttributes.remove("android:viewportWidth") ?: "24"
    foreignAttributes.remove("xmlns:android")

    val svgElementAttributes = mutableMapOf(
        "xmlns" to "http://www.w3.org/2000/svg",
        "viewPort" to "0 0 $viewportWidth $viewportHeight"
    )

    svgElementAttributes["width"] = "100%"
    svgElementAttributes["height"] = "100%"

    return ScalableVectorGraphic.Attributes(attributes.name, svgElementAttributes)
}

// Duplicated from vd.BakeTransform
private val transformationPropertyNames = setOf(
        "android:scaleX",
        "android:scaleY",
        "android:translateX",
        "android:translateY",
        "android:pivotX",
        "android:pivotY",
        "android:rotation"
)

private fun computeTransformationMatrix(groupAttributes: Map<String, String>): Matrix3 {
    val scaleX = groupAttributes["android:scaleX"]?.toFloat()
    val scaleY = groupAttributes["android:scaleY"]?.toFloat()

    val translationX = groupAttributes["android:translateX"]?.toFloat()
    val translationY = groupAttributes["android:translateY"]?.toFloat()

    val pivotX = groupAttributes["android:pivotX"]?.toFloat()
    val pivotY = groupAttributes["android:pivotY"]?.toFloat()

    val rotation = groupAttributes["android:rotation"]?.toFloat()

    val scale = Matrix3.from(arrayOf(
            floatArrayOf(scaleX ?: 1f, 0f, 0f),
            floatArrayOf(0f, scaleY ?: 1f, 0f),
            floatArrayOf(0f, 0f, 1f)
    ))

    val translation = Matrix3.from(arrayOf(
            floatArrayOf(1f, 0f, translationX ?: 0f),
            floatArrayOf(0f, 1f, translationY ?: 0f),
            floatArrayOf(0f, 0f, 1f)
    ))

    val pivot = Matrix3.from(arrayOf(
            floatArrayOf(1f, 0f, pivotX ?: 0f),
            floatArrayOf(0f, 1f, pivotY ?: 0f),
            floatArrayOf(0f, 0f, 1f)
    ))

    val pivotInverse = Matrix3.from(arrayOf(
            floatArrayOf(1f, 0f, (pivotX ?: 0f) * -1),
            floatArrayOf(0f, 1f, (pivotY ?: 0f) * -1),
            floatArrayOf(0f, 0f, 1f)
    ))

    val rotate = rotation?.let {
        val radians = it * PI.toFloat() / 180f
        Matrix3.from(arrayOf(
                floatArrayOf(cos(radians), -sin(radians), 0f),
                floatArrayOf(sin(radians), cos(radians), 0f),
                floatArrayOf(0f, 0f, 1f)
        ))
    } ?: Matrix3.IDENTITY

    return listOf(pivot, translation, rotate, scale, pivotInverse).reduce(Matrix3::times)
}
