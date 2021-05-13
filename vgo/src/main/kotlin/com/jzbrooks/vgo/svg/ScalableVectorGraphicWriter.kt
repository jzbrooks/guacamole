package com.jzbrooks.vgo.svg

import com.jzbrooks.vgo.core.Writer
import com.jzbrooks.vgo.core.graphic.Element
import com.jzbrooks.vgo.core.graphic.Extra
import com.jzbrooks.vgo.core.graphic.Group
import com.jzbrooks.vgo.core.graphic.Path
import com.jzbrooks.vgo.core.util.math.Matrix3
import com.jzbrooks.vgo.svg.graphic.ClipPath
import org.w3c.dom.Document
import java.io.OutputStream
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.Collections.emptySet
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class ScalableVectorGraphicWriter(
    override val options: Set<Writer.Option> = emptySet(),
) : Writer<ScalableVectorGraphic> {

    private val commandPrinter = ScalableVectorGraphicCommandPrinter(3)

    private val formatter = DecimalFormat().apply {
        maximumFractionDigits = 2 // todo: parameterize?
        isDecimalSeparatorAlwaysShown = false
        roundingMode = RoundingMode.HALF_UP
    }

    override fun write(graphic: ScalableVectorGraphic, stream: OutputStream) {
        val builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val document = builder.newDocument()

        val root = document.createElement("svg")
        val elementName = graphic.id
        if (elementName != null) {
            root.setAttribute("id", elementName)
        }
        for (item in graphic.foreign) {
            root.setAttribute(item.key, item.value)
        }
        document.appendChild(root)

        for (element in graphic.elements) {
            write(root, element, document)
        }

        write(document, stream)
    }

    private fun write(parent: org.w3c.dom.Element, element: Element, document: Document) {
        val node = when (element) {
            is Path -> {
                document.createElement("path").apply {
                    val data = element.commands.joinToString(separator = "", transform = commandPrinter::print)
                    setAttribute("d", data)
                }
            }
            is Group -> {
                document.createElement("g").also { node ->
                    if (element.transform !== Matrix3.IDENTITY) {
                        val matrix = element.transform
                        val matrixElements = listOf(
                            formatter.format(matrix[0, 0]),
                            formatter.format(matrix[1, 0]),
                            formatter.format(matrix[0, 1]),
                            formatter.format(matrix[1, 1]),
                            formatter.format(matrix[0, 2]),
                            formatter.format(matrix[1, 2]),
                        ).joinToString(separator = ",")

                        node.setAttribute("transform", "matrix($matrixElements)")
                    }

                    for (child in element.elements) {
                        write(node, child, document)
                    }
                }
            }
            is ClipPath -> {
                document.createElement("clipPath").also {
                    for (child in element.elements) {
                        write(it, child, document)
                    }
                }
            }
            is Extra -> {
                document.createElement(element.name).also {
                    for (child in element.elements) {
                        write(it, child, document)
                    }
                }
            }
            else -> null
        }

        if (node != null) {
            val elementName = element.id
            if (elementName != null) {
                node.setAttribute("id", elementName)
            }
            for (item in element.foreign) {
                node.setAttribute(item.key, item.value)
            }
            parent.appendChild(node)
        }
    }

    private fun write(document: Document, outputStream: OutputStream) {
        val transformer = TransformerFactory.newInstance().newTransformer()
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")

        val indent = options.filterIsInstance<Writer.Option.Indent>().singleOrNull()?.columns
        if (indent != null) {
            transformer.setOutputProperty(OutputKeys.INDENT, "yes")
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", indent.toString())
        }

        val source = DOMSource(document)
        val result = StreamResult(outputStream)
        transformer.transform(source, result)
    }
}
