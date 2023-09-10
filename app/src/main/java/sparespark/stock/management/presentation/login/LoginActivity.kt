package sparespark.stock.management.presentation.login

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import sparespark.stock.management.R
import sparespark.stock.management.core.view.attachFragment

private const val LOGIN_VIEW = "LOGIN_VIEW"

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val view: LoginView =
            supportFragmentManager.findFragmentByTag(LOGIN_VIEW) as LoginView? ?: LoginView()
        attachFragment(supportFragmentManager, R.id.root_activity_login, view, LOGIN_VIEW)
    }
}
