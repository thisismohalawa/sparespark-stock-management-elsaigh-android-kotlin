package sparespark.stock.management.core.view

import android.R
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.core.text.parseAsHtml
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import sparespark.stock.management.core.COLOR_BLUE
import sparespark.stock.management.core.COLOR_GRAY
import sparespark.stock.management.core.COLOR_RED
import sparespark.stock.management.core.isDeviceLanguageArabic
import sparespark.stock.management.data.model.client.Client
import java.util.Locale

internal fun Activity.makeToast(strValue: String?) {
    strValue?.apply {
        Toast.makeText(this@makeToast, strValue, Toast.LENGTH_SHORT).show()
    }
}

internal fun View.visible(isVisible: Boolean) {
    visibility = if (isVisible) View.VISIBLE else View.GONE
}

internal fun View.setViewColor(hexColor: String) {
    this.setBackgroundColor(Color.parseColor(hexColor))
}

internal fun View.enable(enabled: Boolean) {
    isEnabled = enabled
    alpha = if (enabled) 1f else 0.5f
}

internal fun View.setClickListenerWithViewDelayEnabled(action: () -> Unit) {
    setOnClickListener {
        this.isEnabled = false
        action()
        postDelayed({ isEnabled = true }, 2000)
    }
}

internal fun RecyclerView.setupListItemDecoration(context: Context) {
    addItemDecoration(
        DividerItemDecoration(
            context, DividerItemDecoration.VERTICAL
        )
    )
}

internal fun TextView.displayAsLinedBoldStyle(content: String) = try {
    val spanString = SpannableString(content)
    spanString.setSpan(UnderlineSpan(), 0, spanString.length, 0)
    spanString.setSpan(StyleSpan(Typeface.BOLD), 0, spanString.length, 0)
    this.setText(spanString)
} catch (ex: Exception) {
    ex.printStackTrace()
}

internal fun MenuItem.setTitleColor(color: Int) {
    val hexColor = Integer.toHexString(color).toUpperCase(Locale.ROOT).substring(2)
    val html = "<font color='#$hexColor'>$title</font>"
    this.title = html.parseAsHtml()
}

internal fun ImageView.updateImageDependOnExpandingLayout(expandingLayout: LinearLayout) {
    if (expandingLayout.visibility == View.VISIBLE) this.setImageResource(sparespark.stock.management.R.drawable.ic_arrow_down)
    else this.setImageResource(sparespark.stock.management.R.drawable.ic_arrow_up)
}

internal fun View.onViewedClickedUpdateExpanding(
    contentLayout: ViewGroup, expandingLayout: ViewGroup, followedAction: () -> Unit
) {
    this.setOnClickListener {
        if (expandingLayout.visibility == View.GONE) {
            TransitionManager.beginDelayedTransition(contentLayout, AutoTransition())
            expandingLayout.visibility = View.VISIBLE
        } else {
            TransitionManager.beginDelayedTransition(contentLayout, AutoTransition())
            expandingLayout.visibility = View.GONE
        }
        followedAction()
    }
}

internal fun TextView.leftDrawable(
    @DrawableRes drawableRes: Int = 0,
    colorHex: String = COLOR_GRAY,
) {
    if (isDeviceLanguageArabic()) {
        this.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawableRes, 0)
        this.compoundDrawables[2].setTint(Color.parseColor(colorHex))
    } else {
        this.setCompoundDrawablesWithIntrinsicBounds(drawableRes, 0, 0, 0)
        this.compoundDrawables[0].setTint(Color.parseColor(colorHex))
    }
    this.setTextColor(Color.parseColor(colorHex))
}

@SuppressLint("SetTextI18n")
internal fun TextView.setUpTextInfoTitle(totalValue: Double, helper: String = "") {
    this.text = totalValue.toStringFullNumberFormat() + "\t" + helper
    if (totalValue < 0) this.leftDrawable(
        drawableRes = sparespark.stock.management.R.drawable.ic_arrow_down, colorHex = COLOR_RED
    )
    else this.leftDrawable(
        drawableRes = sparespark.stock.management.R.drawable.ic_arrow_up, colorHex = COLOR_BLUE
    )
}

internal fun TextView.markAsCompleted() {
    this.paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
}

internal fun TextView.removeMarkFlag() {
    this.paintFlags = this.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
}

internal fun View.setupViewOperationTypeBackground(
    isBuying: Boolean, reverseView: View
) {
    if (isBuying) {
        reverseView.setBackgroundResource(sparespark.stock.management.R.color.blue)
        setViewColor(COLOR_GRAY)
    } else {
        setBackgroundResource(sparespark.stock.management.R.color.red)
        reverseView.setViewColor(COLOR_GRAY)
    }
}
