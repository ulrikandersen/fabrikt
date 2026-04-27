package views.layout

import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.style

fun FlowContent.columnPanel(flexSizes: List<Double> = listOf(), topOffset: Int = 0, vararg content: DIV.() -> Unit) {
    div {
        val height = if (topOffset > 0) "calc(100vh - ${topOffset}px)" else "100vh"
        val top = if (topOffset > 0) "top: ${topOffset}px; " else ""
        style = "display: flex; flex-direction: row; height: $height; overflow: hidden; position: fixed; width: 100%; ${top}"
        content.forEachIndexed { index, it ->
            div("panel") {
                style = "flex: ${flexSizes.getOrElse(index){ 1 }}; padding: 10px; overflow-y: scroll; overflow-x: hidden;"
                it()
            }
        }
    }
}
