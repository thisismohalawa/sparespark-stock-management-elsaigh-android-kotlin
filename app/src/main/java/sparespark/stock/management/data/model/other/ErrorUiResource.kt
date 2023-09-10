package sparespark.stock.management.data.model.other

import sparespark.stock.management.core.result.UiResourceResult

data class ErrorUiResource(
    val uiResourceResult: UiResourceResult,
    val isError: Boolean
)
