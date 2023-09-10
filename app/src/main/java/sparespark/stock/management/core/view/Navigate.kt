package sparespark.stock.management.core.view

import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.findNavController
import sparespark.stock.management.presentation.main.StockActivity
import sparespark.stock.management.presentation.login.LoginActivity

internal fun attachFragment(
    manager: FragmentManager,
    containerId: Int,
    view: Fragment,
    tag: String
) {
    manager.beginTransaction()
        .replace(containerId, view, tag)
        .commitNowAllowingStateLoss()
}

internal fun StockActivity.startLoginActivity() {
    startActivity(
        Intent(
            this@startLoginActivity, LoginActivity::class.java
        )
    ).also {
        this@startLoginActivity.finish()
    }
}
internal fun LoginActivity.startListActivity() {
    startActivity(
        Intent(
            this@startListActivity, StockActivity::class.java
        )
    ).also { this@startListActivity.finish() }
}

internal fun StockActivity.restartListActivity() {
    val intent: Intent? = applicationContext.packageManager
        .getLaunchIntentForPackage(applicationContext.packageName)
    intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
}
internal fun Fragment.relaunchCurrentView() {
    view?.post {
        findNavController().apply {
            val id = currentDestination?.id
            id?.let {
                popBackStack(it, true)
                navigate(it)
            }
        }
    }
}
