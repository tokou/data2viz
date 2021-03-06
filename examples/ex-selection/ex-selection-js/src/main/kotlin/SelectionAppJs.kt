@file:Suppress("DEPRECATION")

import io.data2viz.viz.ParentElement
import io.data2viz.viz.selectOrCreateSvg
import kotlin.browser.window
import kotlin.js.Date


fun main(args: Array<String>) {
    val svg = selectOrCreateSvg().apply { 
        setAttribute("width", widthHeight.toString())
        setAttribute("height", widthHeight.toString())
    }
    svg.append((root as ParentElement).domElement)
}

actual fun random(): Double  = kotlin.js.Math.random()
