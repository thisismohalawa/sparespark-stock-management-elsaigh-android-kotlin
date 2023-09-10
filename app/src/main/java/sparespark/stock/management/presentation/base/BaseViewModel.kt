package sparespark.stock.management.presentation.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import sparespark.stock.management.R
import sparespark.stock.management.core.DEACTIVATED
import sparespark.stock.management.core.NOT_PERMITTED
import sparespark.stock.management.core.NO_INTERNET_CONNECTION
import sparespark.stock.management.core.result.DataResult
import sparespark.stock.management.core.result.UiResourceResult
import sparespark.stock.management.data.model.other.ErrorUiResource
import kotlin.coroutines.CoroutineContext

abstract class BaseViewModel<T>(
    private val uiContext: CoroutineContext
) : ViewModel(), CoroutineScope {

    internal val actionEventTextStatus = MutableLiveData<ErrorUiResource>()

    private val errorState = MutableLiveData<UiResourceResult>()
    val error: LiveData<UiResourceResult> get() = errorState

    private val loadingState = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = loadingState

    abstract fun handleEvent(event: T)

    protected var jobTracker: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = uiContext + jobTracker

    protected fun updateActionStatusText(resMsg: Int, isError: Boolean = false): Job = launch {
        actionEventTextStatus.value = ErrorUiResource(
            UiResourceResult.StringResource(resMsg), isError
        )
    }

    protected fun hideActionEventText() {
        updateActionStatusText(R.string.nothing)
    }

    protected fun showError(stringRes: Int) {
        errorState.value =
            UiResourceResult.StringResource(stringRes)
    }

    protected fun String?.resultErrorMsgTrigger() = when (this) {
        NO_INTERNET_CONNECTION -> showError(R.string.no_internet)
        DEACTIVATED -> showError(R.string.deactivated)
        NOT_PERMITTED -> showError(R.string.not_permitted)
        else -> showError(R.string.error_get_data)
    }


    protected fun showLoading() {
        loadingState.value = true
    }

    protected fun hideLoading() {
        loadingState.value = false
    }
}
