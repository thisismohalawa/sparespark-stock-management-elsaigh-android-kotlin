package sparespark.stock.management.core.result

import android.content.Context
import androidx.annotation.StringRes

sealed class UiResourceResult {

    class StringResource(
        @StringRes val resId: Int,
        vararg val args: Any
    ) : UiResourceResult()


    fun asString(context: Context?): String? {
        return when (this) {
            is StringResource -> context?.getString(resId, *args)
        }
    }
}
