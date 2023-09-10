package sparespark.stock.management.core

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AlertDialog
import sparespark.stock.management.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

internal fun handlerPostDelayed(millisValue: Long, action: (() -> Unit)? = null) {
    Handler(Looper.getMainLooper()).postDelayed({
        action?.let { it() }
    }, millisValue)
}

internal fun isDeviceLanguageArabic(): Boolean =
    Locale.getDefault().language.equals("ar")

internal fun getCalendarDateTime(pattern: String = "d MMM yyyy HH:mm:ss"): String {
    val cal = Calendar.getInstance(TimeZone.getDefault())
    val sdf = SimpleDateFormat(
        pattern, Locale.US
    )
    sdf.timeZone = cal.timeZone
    return sdf.format(cal.time)
}

internal fun Context.actionDisplayConfirmationDialog(
    titleResId: Int,
    msgString: String? = null,
    actionResId: Int = R.string.confirm,
    subActionResId: Int? = null,
    subAction: (() -> Unit)? = null,
    action: (() -> Unit)?
) {
    AlertDialog.Builder(this)
        .setTitle(getString(titleResId))
        .setMessage(msgString)
        .setNegativeButton(
            subActionResId?.let { getString(it) }
        ) { _, _ ->
            subAction?.let { it() }
        }
        .setPositiveButton(
            getString(actionResId)
        ) { _, _ ->
            action?.let { it() }
        }.show()
}
