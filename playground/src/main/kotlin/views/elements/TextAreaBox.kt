package views.elements

import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.label
import kotlinx.html.textArea

/** Multi-line text input with a short instruction rendered under the label. */
fun FlowContent.textAreaBox(name: String, instruction: String, placeholder: String = "", currentValue: String = "") {
    div("mb2") {
        label("block mb1 bold") {
            htmlFor = name
            +name
        }
        div("mb1 h6 hint") { +instruction }
        textArea(classes = "block col-12 border p2 rounded") {
            this.name = name
            this.placeholder = placeholder
            rows = "3"
            +currentValue
        }
    }
}
