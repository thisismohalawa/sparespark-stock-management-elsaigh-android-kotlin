package sparespark.stock.management.presentation.login.viewmodel

import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import sparespark.stock.management.R
import sparespark.stock.management.core.NO_INTERNET_CONNECTION
import sparespark.stock.management.core.SIGN_IN_REQUEST_CODE
import sparespark.stock.management.core.result.DataResult
import sparespark.stock.management.core.result.UiResourceResult
import sparespark.stock.management.core.view.SingleLiveData
import sparespark.stock.management.data.model.login.LoginResult
import sparespark.stock.management.data.model.login.User
import sparespark.stock.management.data.repository.UserRepository
import sparespark.stock.management.presentation.base.BaseViewModel
import sparespark.stock.management.presentation.login.LoginEvent
import kotlin.coroutines.CoroutineContext

class LoginViewModel(
    private val userRepo: UserRepository, uiContext: CoroutineContext
) : BaseViewModel<LoginEvent<LoginResult>>(uiContext) {

    // UI binding
    internal val signInStatusText = MutableLiveData<UiResourceResult>()
    internal val authButtonText = MutableLiveData<UiResourceResult>()

    // control Logic,
    // communicate with view based on what happen in particular state.
    // trigger an event, does not care about any value has been passed in that event.
    internal val authAttempt = MutableLiveData<Unit>()
    internal val moveToMainViewAttempt = MutableLiveData<Unit>()
    private val userAuthState = SingleLiveData<User?>()

    override fun handleEvent(event: LoginEvent<LoginResult>) {
        showLoading()
        when (event) {
            is LoginEvent.OnStartGetAuthUser -> getAuthRemoteUser()
            is LoginEvent.OnAuthButtonClick -> onAuthButtonClicked()
            is LoginEvent.OnGoogleSignInResult -> onSignInResult(event.result)
        }
    }

    private fun getAuthRemoteUser(): Job = launch {
        when (val result: DataResult<Exception, User?> = userRepo.getAuthUser()) {
            is DataResult.Error -> showError(R.string.error_try_again)
            is DataResult.Value -> if (result.value != null) {
                userAuthState.value = result.value
                updateRemoteUser(result.value)
                showSignedInState()
            } else showSignedOutState()
        }
        hideLoading()
    }

    private fun onAuthButtonClicked(): Job = launch {
        if (userAuthState.value == null) authAttempt.value = Unit
        else signOutUser()
    }

    private fun updateRemoteUser(user: User): Job = launch {
        when (val result: DataResult<Exception, Boolean> =
            userRepo.createARemoteFirebaseUser(user)) {
            is DataResult.Error -> if (result.error.message == NO_INTERNET_CONNECTION)
                showError(R.string.no_internet) else showError(R.string.cannot_update_user)
            is DataResult.Value -> if (result.value)
                updateLocalUserEntries(remoteUser = user)
        }
    }

    private fun updateLocalUserEntries(remoteUser: User): Job = launch {
        when (userRepo.updateLocalUserEntry(user = remoteUser)) {
            is DataResult.Error -> showError(R.string.cannot_update_user)
            else -> moveToMainViewAttempt.value = Unit
        }
    }


    private fun signOutUser(): Job = launch {
        when (userRepo.signOutCurrentUser()) {
            is DataResult.Error -> showError(R.string.error_try_again)
            is DataResult.Value -> {
                userAuthState.value = null
                showSignedOutState()
                hideLoading()
            }
        }
    }

    private fun onSignInResult(result: LoginResult): Job = launch {
        if (result.requestCode == SIGN_IN_REQUEST_CODE && result.userToken != null) {
            val createGoogleUserResult: DataResult<Exception, Unit> =
                userRepo.signInGoogleUser(result.userToken)
            if (createGoogleUserResult is DataResult.Value) getAuthRemoteUser()
            else showError(R.string.cannot_sign_in)
        } else showError(R.string.cannot_sign_in)
    }


    private fun showSignedInState() {
        signInStatusText.value = UiResourceResult.StringResource(R.string.signed_in_successfully)
        authButtonText.value = UiResourceResult.StringResource(R.string.sign_out_)
    }

    private fun showSignedOutState() {
        signInStatusText.value = UiResourceResult.StringResource(R.string.signed_out_title)
        authButtonText.value = UiResourceResult.StringResource(R.string.sign_in_)
    }
}
