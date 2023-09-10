package sparespark.stock.management.presentation.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance
import sparespark.stock.management.R
import sparespark.stock.management.core.SIGN_IN_REQUEST_CODE
import sparespark.stock.management.core.binding.ViewBindingHolder
import sparespark.stock.management.core.binding.ViewBindingHolderImpl
import sparespark.stock.management.core.view.makeToast
import sparespark.stock.management.core.view.setClickListenerWithViewDelayEnabled
import sparespark.stock.management.core.view.startListActivity
import sparespark.stock.management.core.view.visible
import sparespark.stock.management.data.model.login.LoginResult
import sparespark.stock.management.databinding.LoginViewBinding
import sparespark.stock.management.presentation.login.viewmodel.LoginViewModel
import sparespark.stock.management.presentation.login.viewmodel.LoginViewModelFactory

class LoginView : Fragment(),
    ViewBindingHolder<LoginViewBinding> by ViewBindingHolderImpl(), KodeinAware {
    override val kodein by closestKodein()
    private lateinit var loginViewModel: LoginViewModel
    private val viewModelFactory: LoginViewModelFactory by instance()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = initBinding(LoginViewBinding.inflate(layoutInflater), this) {
        initializeBaseViewModel()
        setUpViewListeners()
        loginViewModel.startObserving()
    }

    private fun initializeBaseViewModel() {
        loginViewModel =
            ViewModelProvider(this@LoginView, viewModelFactory)[LoginViewModel::class.java]
        loginViewModel.handleEvent(LoginEvent.OnStartGetAuthUser)
    }

    private fun setUpViewListeners() {
        binding?.btnLogin?.setClickListenerWithViewDelayEnabled {
            loginViewModel.handleEvent(LoginEvent.OnAuthButtonClick)
        }
    }

    private fun LoginViewModel.startObserving() {
        loading.observe(viewLifecycleOwner) {
            binding?.progressCircular?.visible(it)
        }
        error.observe(viewLifecycleOwner) {
            activity?.makeToast(it.asString(context))
        }
        signInStatusText.observe(viewLifecycleOwner) {
            binding?.txtSignStatus?.text = it.asString(context)
        }
        authButtonText.observe(viewLifecycleOwner) {
            binding?.btnLogin?.text = it.asString(context)
        }
        authAttempt.observe(viewLifecycleOwner) {
            startSignInFlow()
        }
        moveToMainViewAttempt.observe(viewLifecycleOwner) {
            (activity as LoginActivity).startListActivity()
        }
    }

    private fun startSignInFlow() {
        val gso: GoogleSignInOptions =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()

        val googleSignInClient: GoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        val signInIntent: Intent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, SIGN_IN_REQUEST_CODE)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)/*
        *
        *  +TO DO
        * Enable Google Sign in method
        *
        * Add a support email address to your project in project settings.
        * Open link https://console.firebase.google.com/
        *
        * */
        try {
            var userToken: String? = null
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)

            val account: GoogleSignInAccount? = task.getResult(ApiException::class.java)
            if (account != null) {
                userToken = account.idToken
                loginViewModel.handleEvent(
                    LoginEvent.OnGoogleSignInResult(
                        LoginResult(requestCode, userToken)
                    )
                )
            }
        } catch (ex: Exception) {
            activity?.makeToast(
                getString(R.string.unable_to_sign) + "\n" +
                        "${ex.message}"
            )
        }
    }

}
