package sparespark.stock.management.core.view

import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.widget.EditText
import android.widget.TextView
import com.google.android.material.textfield.TextInputLayout
import sparespark.stock.management.core.toDoubleLimitation

// MAX
internal const val MAX_INPUT_LEN = 200


internal fun String.isPhoneNumberValid(): Boolean = Patterns.PHONE.matcher(this).matches()

internal fun String.isNumFormatValid(maxDig: Int): Boolean =
    (this.length in 1..maxDig &&
            this != "0.0" &&
            this != "0" &&
            this != "." &&
            !this.startsWith("00") &&
            !this.startsWith(".") &&
            !this.endsWith("."))

internal fun Double.toStringFullNumberFormat(): String = try {
    if (this > Int.MAX_VALUE) "MAX"
    else String.format("%,.2f", this)
} catch (e: Exception) {
    Double.toString()
}

internal fun getDividedRequiredAveValue(targetNum: Double, dividedBy: Double): Double = try {
    if (targetNum.equals(0.0) || dividedBy.equals(0.0)) 0.0
    else (targetNum / dividedBy).toDoubleLimitation()
} catch (e: Exception) {
    0.0
}

internal fun EditText.beginInputAndTextActionWatcher(actionText: TextView) {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun afterTextChanged(s: Editable) {
        }

        override fun onTextChanged(text: CharSequence, start: Int, count: Int, after: Int) =
            if (text.isNotEmpty() && text.length < MAX_INPUT_LEN)
                actionText.enable(true)
            else
                actionText.enable(false)
    })
}
internal fun EditText.beginInputAndTextLayoutAssetWatcher(
    inputLayout: TextInputLayout,
    maxDig: Int
) {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun afterTextChanged(s: Editable) {
        }

        override fun onTextChanged(text: CharSequence, start: Int, count: Int, after: Int): Unit {
            if (text.isNotEmpty())
                if (text.toString().isNumFormatValid(maxDig)) {
                    inputLayout.isErrorEnabled = false
                    inputLayout.error = null
                } else
                    inputLayout.error = "-"
        }

    })
}
