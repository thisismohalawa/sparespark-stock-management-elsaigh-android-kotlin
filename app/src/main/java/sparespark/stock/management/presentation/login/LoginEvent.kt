package sparespark.stock.management.presentation.login

/*
* Login event class represent different events..
* In some cases we want to return LoginResult which is a data wrapper,
* main some data like requestCode, token.
*
* */
sealed class LoginEvent<out T> {
    object OnStartGetAuthUser : LoginEvent<Nothing>()
    object OnAuthButtonClick : LoginEvent<Nothing>()
    data class OnGoogleSignInResult<out LoginResult>(val result: LoginResult) :
        LoginEvent<LoginResult>()
}
