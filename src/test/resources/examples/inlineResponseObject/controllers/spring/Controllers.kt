package examples.inlineResponseObject.controllers

import examples.inlineResponseObject.models.WidgetResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.validation.`annotation`.Validated
import org.springframework.web.bind.`annotation`.PathVariable
import org.springframework.web.bind.`annotation`.RequestMapping
import org.springframework.web.bind.`annotation`.RequestMethod
import kotlin.String

@Controller
@Validated
@RequestMapping("")
public interface WidgetsController {
    /**
     *
     *
     * @param widgetId
     */
    @RequestMapping(
        value = ["/widgets/{widgetId}"],
        produces = ["application/json"],
        method = [RequestMethod.GET],
    )
    public fun getWidget(
        @PathVariable(value = "widgetId", required = true) widgetId: String,
    ): ResponseEntity<WidgetResponse>
}
