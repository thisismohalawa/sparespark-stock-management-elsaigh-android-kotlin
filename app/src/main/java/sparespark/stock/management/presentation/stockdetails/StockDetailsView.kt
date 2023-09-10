package sparespark.stock.management.presentation.stockdetails

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance
import sparespark.stock.management.R
import sparespark.stock.management.core.IS_BUY
import sparespark.stock.management.core.MAX_CLIENT_NAME
import sparespark.stock.management.core.MAX_GRAM_DIG
import sparespark.stock.management.core.MAX_TOTAL_DIG
import sparespark.stock.management.core.actionDisplayConfirmationDialog
import sparespark.stock.management.core.actionShareText
import sparespark.stock.management.core.binding.ViewBindingHolder
import sparespark.stock.management.core.binding.ViewBindingHolderImpl
import sparespark.stock.management.core.newStock
import sparespark.stock.management.core.toDoubleLimitation
import sparespark.stock.management.core.toEditable
import sparespark.stock.management.core.view.beginInputAndTextLayoutAssetWatcher
import sparespark.stock.management.core.view.getDividedRequiredAveValue
import sparespark.stock.management.core.view.isNumFormatValid
import sparespark.stock.management.core.view.setIconAction
import sparespark.stock.management.core.view.setUpClientAutoComplete
import sparespark.stock.management.core.view.setupViewOperationTypeBackground
import sparespark.stock.management.core.view.visible
import sparespark.stock.management.data.model.stock.Stock
import sparespark.stock.management.databinding.StockdetailsViewBinding
import sparespark.stock.management.presentation.base.BaseViewInteract
import sparespark.stock.management.presentation.main.StockActivityInteract
import sparespark.stock.management.presentation.stockdetails.viewmodel.StockDetailsViewModel
import sparespark.stock.management.presentation.stockdetails.viewmodel.StockDetailsViewModelFactory

class StockDetailsView : Fragment(), BaseViewInteract,
    ViewBindingHolder<StockdetailsViewBinding> by ViewBindingHolderImpl(),
    View.OnClickListener, KodeinAware {
    override val kodein by closestKodein()
    private val args: StockDetailsViewArgs? by navArgs()
    private val currentStockItem: Stock?
        get() = try {
            args?.stock
        } catch (ex: Exception) {
            newStock()
        }
    private lateinit var viewComm: StockActivityInteract.View
    private lateinit var viewModel: StockDetailsViewModel
    private val viewModelFactory: StockDetailsViewModelFactory by instance()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = initBinding(StockdetailsViewBinding.inflate(layoutInflater), this) {
        initializeBaseViewModel()
        initializeBaseViewInteract()
        viewModel.startObserving()
        setupViewInputActionAndBehavioral()
        setupViewClickListener()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.buy_layout -> viewModel.handleEvent(
                StockDetailsViewEvent.UpdateStockOperationType(
                    isBuying = IS_BUY
                )
            )

            R.id.sell_layout -> viewModel.handleEvent(
                StockDetailsViewEvent.UpdateStockOperationType(
                    isBuying = !IS_BUY
                )
            )

            R.id.btn_update_item -> if (isItemInputValid())
                activity?.actionDisplayConfirmationDialog(
                    titleResId = R.string.add_item,
                    msgString = getTotalInfoConfirmText(),
                    action = {
                        viewModel.handleEvent(
                            StockDetailsViewEvent.UpdateStock(
                                isTemp = false,
                                gramPrice = getInputAssetGramPriceDoubleValue(),
                                quantity = getInputAssetQuantityDoubleValue(),
                                details = getInputTextDetails()
                            )
                        )
                    })

            R.id.btn_store_temp -> if (isItemInputValid())
                activity?.actionDisplayConfirmationDialog(
                    titleResId = R.string.store_temp,
                    msgString = getTotalInfoConfirmText(),
                    action = {
                        viewModel.handleEvent(
                            StockDetailsViewEvent.UpdateStock(
                                isTemp = true,
                                gramPrice = getInputAssetGramPriceDoubleValue(),
                                quantity = getInputAssetQuantityDoubleValue(),
                                details = getInputTextDetails()
                            )
                        )
                    })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.handleEvent(StockDetailsViewEvent.OnDestroy)
    }

    override fun initializeBaseViewModel() {
        viewModel =
            ViewModelProvider(
                this@StockDetailsView,
                viewModelFactory
            )[StockDetailsViewModel::class.java]
        viewModel.handleEvent(
            StockDetailsViewEvent.OnStartGetStock(
                stock = currentStockItem ?: newStock()
            )
        )
        viewModel.handleEvent(StockDetailsViewEvent.GetClientList)
    }

    override fun initializeBaseViewInteract() {
        viewComm = activity as StockActivityInteract.View
    }

    private fun StockDetailsViewModel.startObserving() {
        loading.observe(viewLifecycleOwner) {
            if (it) viewComm.showLoadingProgress()
            else viewComm.hideLoadingProgress()
        }
        error.observe(viewLifecycleOwner) {
            if (it != null) viewComm.updateMainActionStatusText(
                msg = it, isError = true
            )
        }
        updateButtonTextStatus.observe(viewLifecycleOwner) {
            binding?.btnUpdateItem?.text = it.asString(context)
        }
        updateTypeVisibleStatus.observe(viewLifecycleOwner) {
            binding?.apply {
                btnStoreTemp.visible(!it)
            }
        }
        update.observe(viewLifecycleOwner) {
            if (it) findNavController().popBackStack()
        }
        actionSharingDialogStatus.observe(viewLifecycleOwner) {
            if (it?.isNotEmpty() == true) {
                activity?.actionDisplayConfirmationDialog(titleResId = R.string.updated_success,
                    actionResId = R.string.share_action,
                    action = {
                        activity?.actionShareText(it)
                    })
                navigateToListView()
            }
        }
        clientList.observe(viewLifecycleOwner) {
            if (it?.isNotEmpty() == true) binding?.inputClientAutocomplete?.etClient?.setUpClientAutoComplete(
                context = requireContext(),
                clientList = it,
                action = { client ->
                    handleEvent(
                        StockDetailsViewEvent.UpdateStockClientDetails(
                            clientName = client
                        )
                    )
                })
        }
        stock.observe(viewLifecycleOwner) {
            bindStock(it)
        }
    }

    private fun bindStock(stock: Stock) {
        binding?.apply {
            itemStockType.viewSell.setupViewOperationTypeBackground(
                isBuying = stock.operationType, reverseView = itemStockType.viewBuy
            )
            if (stock.assetGramPrice != 0.0) {
                inputClientAutocomplete.etClient.text = stock.client.toEditable()
                inputAssetGramPrice.editText.text = stock.assetGramPrice.toString().toEditable()
                inputAssetQuantity.editText.text = stock.assetQuantity.toString().toEditable()
                inputTotalPrice.editText.text =
                    (stock.assetGramPrice * stock.assetQuantity).toString().toEditable()
                inputTxtDetails.editText.text = stock.details.toEditable()
            }
        }
    }

    private fun setupViewInputActionAndBehavioral() {
        binding?.apply {
            inputAssetGramPrice.textInputLayout.hint = getString(R.string.asset_price)
            inputAssetQuantity.textInputLayout.hint = getString(R.string.asset_quantity)
            inputTotalPrice.textInputLayout.hint = getString(R.string.asset_total_price)
            inputClientAutocomplete.etClient.beginInputAndTextLayoutAssetWatcher(
                inputLayout = inputClientAutocomplete.textInputLayout, maxDig = MAX_CLIENT_NAME
            )
            inputAssetGramPrice.editText.beginInputAndTextLayoutAssetWatcher(
                inputLayout = inputAssetGramPrice.textInputLayout, maxDig = MAX_GRAM_DIG
            )
            inputAssetQuantity.editText.beginInputAndTextLayoutAssetWatcher(
                inputLayout = inputAssetQuantity.textInputLayout, maxDig = MAX_TOTAL_DIG
            )
            inputTotalPrice.editText.beginInputAndTextLayoutAssetWatcher(
                inputLayout = inputTotalPrice.textInputLayout, maxDig = MAX_TOTAL_DIG
            )
            /*
            * Action on clicked.
            *
            * */
            inputTotalPrice.textInputLayout.setIconAction(
                icon = R.drawable.ic_refresh,
                action = {
                    if (isInputAssetGramValid() && isInputAssetQuantityValid()) inputTotalPrice.editText.text =
                        getTotalPrice().toEditable()
                }
            )
            inputAssetQuantity.textInputLayout.setIconAction(
                icon = R.drawable.ic_refresh,
                action = {
                    if (isInputAssetGramValid() && isInputTotalPriceValid()) inputAssetQuantity.editText.text =
                        getRequiredQuantity().toEditable()
                }
            )
        }
    }

    private fun isInputClientNameValid(): Boolean =
        binding != null && !(binding.inputClientAutocomplete.textInputLayout.isErrorEnabled) && !TextUtils.isEmpty(
            binding.inputClientAutocomplete.etClient.text.toString()
        ) && viewModel.isClientNameValid()

    private fun isInputAssetGramValid(): Boolean =
        binding != null && !(binding.inputAssetGramPrice.textInputLayout.isErrorEnabled) && binding.inputAssetGramPrice.editText.text.toString()
            .isNumFormatValid(MAX_GRAM_DIG)

    private fun isInputAssetQuantityValid(): Boolean =
        binding != null && !(binding.inputAssetQuantity.textInputLayout.isErrorEnabled) && binding.inputAssetQuantity.editText.text.toString()
            .isNumFormatValid(MAX_TOTAL_DIG) && binding.inputAssetQuantity.editText.text.toString()
            .toDouble() < Int.MAX_VALUE

    private fun isInputTotalPriceValid(): Boolean =
        binding != null && !(binding.inputTotalPrice.textInputLayout.isErrorEnabled) && binding.inputTotalPrice.editText.text.toString()
            .isNumFormatValid(MAX_TOTAL_DIG) && binding.inputTotalPrice.editText.text.toString()
            .toDouble() < Int.MAX_VALUE

    private fun getInputAssetGramPriceDoubleValue(): Double =
        binding?.inputAssetGramPrice?.editText?.text.toString().toDouble().toDoubleLimitation()

    private fun getInputAssetQuantityDoubleValue(): Double =
        binding?.inputAssetQuantity?.editText?.text.toString().toDouble().toDoubleLimitation()

    private fun getInputTotalPriceDoubleValue(): Double =
        binding?.inputTotalPrice?.editText?.text.toString().toDouble().toDoubleLimitation()

    private fun getInputTextDetails(): String =
        binding?.inputTxtDetails?.editText?.text.toString()

    private fun getTotalInfoConfirmText(): String =
        getString(R.string.asset_price) + " = ${binding?.inputAssetGramPrice?.editText?.text}\n" + getString(
            R.string.asset_quantity
        ) + " = ${binding?.inputAssetQuantity?.editText?.text}\n" + "__________________\n\n= ${getTotalPrice()}"

    private fun getTotalPrice(): String =
        (getInputAssetGramPriceDoubleValue() * getInputAssetQuantityDoubleValue()).toString()

    private fun getRequiredQuantity(): String =
        (getDividedRequiredAveValue(
            targetNum = getInputTotalPriceDoubleValue(),
            dividedBy = getInputAssetGramPriceDoubleValue()
        )).toDoubleLimitation()
            .toString()

    private fun isItemInputValid(): Boolean =
        if (!isInputClientNameValid()) {
            binding?.inputClientAutocomplete?.textInputLayout?.error =
                "-"
            false
        } else if (!isInputAssetGramValid()) {
            binding?.inputAssetGramPrice?.textInputLayout?.error =
                "-"
            false

        } else if (!isInputAssetQuantityValid()) {
            binding?.inputAssetQuantity?.textInputLayout?.error =
                "-"
            false
        } else true

    private fun navigateToListView() =
        if (findNavController().currentDestination?.id == R.id.stockDetailsView)
            findNavController().navigate(
                StockDetailsViewDirections.navigateToStockListView()
            )
        else Unit

    private fun setupViewClickListener() {
        binding?.apply {
            itemStockType.sellLayout.setOnClickListener(this@StockDetailsView)
            itemStockType.buyLayout.setOnClickListener(this@StockDetailsView)
            btnUpdateItem.setOnClickListener(this@StockDetailsView)
            btnStoreTemp.setOnClickListener(this@StockDetailsView)
        }
    }
}
