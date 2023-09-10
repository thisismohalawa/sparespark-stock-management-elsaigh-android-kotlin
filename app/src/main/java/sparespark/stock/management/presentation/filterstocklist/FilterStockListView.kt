package sparespark.stock.management.presentation.filterstocklist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance
import sparespark.stock.management.R
import sparespark.stock.management.core.IS_BUY
import sparespark.stock.management.core.IS_PENDING
import sparespark.stock.management.core.binding.ViewBindingHolder
import sparespark.stock.management.core.binding.ViewBindingHolderImpl
import sparespark.stock.management.core.view.relaunchCurrentView
import sparespark.stock.management.core.view.setUpCitySpinners
import sparespark.stock.management.core.view.setUpClientSpinners
import sparespark.stock.management.core.view.setUpSearchViewListenerByQuery
import sparespark.stock.management.core.view.setupListItemDecoration
import sparespark.stock.management.core.view.toStringFullNumberFormat
import sparespark.stock.management.core.view.visible
import sparespark.stock.management.data.model.stock.Stock
import sparespark.stock.management.databinding.FilterStocklistViewBinding
import sparespark.stock.management.presentation.base.BaseViewInteract
import sparespark.stock.management.presentation.filterstocklist.viewmodel.FilterStockListViewModel
import sparespark.stock.management.presentation.filterstocklist.viewmodel.FilterStockListViewModelFactory
import sparespark.stock.management.presentation.main.StockActivityInteract
import sparespark.stock.management.presentation.stocklist.StockListEvent
import sparespark.stock.management.presentation.stocklist.adapter.StockAdapter

class FilterStockListView : Fragment(), BaseViewInteract,
    ViewBindingHolder<FilterStocklistViewBinding> by ViewBindingHolderImpl(), KodeinAware {
    override val kodein by closestKodein()
    private lateinit var viewModel: FilterStockListViewModel
    private lateinit var stockAdapter: StockAdapter
    private lateinit var viewComm: StockActivityInteract.View
    private val viewModelFactory: FilterStockListViewModelFactory by instance()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = initBinding(FilterStocklistViewBinding.inflate(layoutInflater), this) {
        initializeBaseViewModel()
        initializeBaseViewInteract()
        setupListRecyclerAdapterAndBehavioral()
        setupViewInputsBehavioral()
        viewModel.startObserving()
        setUpRecyclerViewModelEvent()
        setupViewClickListener()
    }

    override fun initializeBaseViewModel() {
        viewModel = ViewModelProvider(
            this@FilterStockListView, viewModelFactory
        )[FilterStockListViewModel::class.java]
        viewModel.handleEvent(StockListEvent.GetCityList)
        viewModel.handleEvent(StockListEvent.GetClientList)
    }

    override fun initializeBaseViewInteract() {
        viewComm = activity as StockActivityInteract.View
    }


    override fun onDestroyView() {
        super.onDestroyView()
        binding?.recStockList?.adapter = null
    }

    private fun FilterStockListViewModel.startObserving() {
        loading.observe(viewLifecycleOwner) {
            if (it) viewComm.showLoadingProgress()
            else viewComm.hideLoadingProgress()
        }
        error.observe(viewLifecycleOwner) {
            viewComm.updateMainActionStatusText(
                msg = it, isError = true
            )
        }
        updated.observe(viewLifecycleOwner) {
            if (it) navigateToStockListView()
        }
        cityList.observe(viewLifecycleOwner) {
            if (it?.isNotEmpty() == true) binding?.itemCitySpinner?.spinner?.setUpCitySpinners(
                context = requireContext(),
                cityList = it,
                allAsFirstItem = true,
                action = { city ->
                    unCheckRadioTypeGroupValue()
                    unCheckRadioActiveGroupValue()
                    binding.itemClientSpinner.spinner.setBackgroundResource(R.drawable.item_rounded_layout_gray)
                    binding.itemCitySpinner.spinner.setBackgroundResource(R.drawable.item_rounded_layout_green)
                    handleEvent(StockListEvent.FilterPayListByQuery(query = city))
                })
        }
        clientList.observe(viewLifecycleOwner) {
            if (it?.isNotEmpty() == true) binding?.itemClientSpinner?.spinner?.setUpClientSpinners(
                context = requireContext(),
                clientList = it,
                action = { client ->
                    unCheckRadioTypeGroupValue()
                    unCheckRadioActiveGroupValue()
                    binding.itemCitySpinner.spinner.setBackgroundResource(R.drawable.item_rounded_layout_gray)
                    binding.itemClientSpinner.spinner.setBackgroundResource(R.drawable.item_rounded_layout_green)
                    handleEvent(StockListEvent.FilterPayListByQuery(query = client))
                })
        }
        filteredList.observe(viewLifecycleOwner) {
            if (it != null) stockAdapter.submitList(it)
        }
        filteredListDetails.observe(viewLifecycleOwner) {
            binding?.txtTotalInfo?.text = getTotalInfoText(
                totalCost = it.totalCostAmount, quantity = it.totalAssetQuantity
            )
        }
        stockItemClickedNavigateAttempt.observe(viewLifecycleOwner) {
            navigateToStockDetailsView(stock = it)
        }
    }

    private fun setupListRecyclerAdapterAndBehavioral() {
        stockAdapter = StockAdapter()
        binding?.recStockList?.apply {
            adapter = stockAdapter
            setupListItemDecoration(context)
        }
    }

    private fun setUpRecyclerViewModelEvent() {
        stockAdapter.event.observe(
            viewLifecycleOwner
        ) {
            viewModel.handleEvent(it)
        }
    }

    private fun setupViewInputsBehavioral() {
        binding?.apply {
            itemSearch.mtSearchView.queryHint = getString(R.string.search_by)
            itemCitySpinner.txtInfo.text = getString(R.string.select_city)
            itemClientSpinner.txtInfo.text = getString(R.string.select_client)
            itemRadioGroupPayType.radioBtn1.text = getString(R.string.buy)
            itemRadioGroupPayType.radioBtn2.text = getString(R.string.sell)
            itemRadioGroupPayActivate.radioBtn1.text = getString(R.string.pending)
            itemRadioGroupPayActivate.radioBtn2.text = getString(R.string.completed)
        }
    }

    private fun setupViewClickListener() {
        binding?.apply {
            itemSearch.mtSearchView.setUpSearchViewListenerByQuery(action = {
                viewModel.handleEvent(StockListEvent.FilterPayListByQuery(query = it))
            })
            itemRadioGroupPayType.radioGroup.setOnCheckedChangeListener { _, checkedId ->
                if (checkedId == R.id.radio_btn1) {
                    viewModel.handleEvent(
                        StockListEvent.UpdatePayOperationType(
                            isBuying = IS_BUY
                        )
                    )
                    itemRadioGroupPayType.radioBtn2.visibility = View.INVISIBLE
                } else {
                    viewModel.handleEvent(
                        StockListEvent.UpdatePayOperationType(
                            isBuying = !IS_BUY
                        )
                    )
                    itemRadioGroupPayType.radioBtn1.visibility = View.INVISIBLE
                }
            }
            itemRadioGroupPayActivate.radioGroup.setOnCheckedChangeListener { _, checkedId ->
                if (checkedId == R.id.radio_btn1) {
                    viewModel.handleEvent(
                        StockListEvent.UpdatePayPendingType(
                            isPending = IS_PENDING
                        )
                    )
                    itemRadioGroupPayActivate.radioBtn2.visibility = View.INVISIBLE
                } else {
                    viewModel.handleEvent(
                        StockListEvent.UpdatePayPendingType(
                            isPending = !IS_PENDING
                        )
                    )
                    itemRadioGroupPayActivate.radioBtn1.visibility = View.INVISIBLE
                }
            }

        }
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (viewModel.isOperationTypeFiltered() || viewModel.isOperationPendingTypeFiltered()) relaunchCurrentView()
            else navigateToStockListView()
        }
    }

    private fun unCheckRadioTypeGroupValue() {
        if (viewModel.isOperationTypeFiltered()) binding?.apply {
            itemRadioGroupPayType.radioBtn1.isChecked = false
            itemRadioGroupPayType.radioBtn2.isChecked = false
            itemRadioGroupPayType.radioBtn1.visible(true)
            itemRadioGroupPayType.radioBtn2.visible(true)
        }
    }

    private fun unCheckRadioActiveGroupValue() {
        if (viewModel.isOperationTypeFiltered()) binding?.apply {
            itemRadioGroupPayActivate.radioBtn1.isChecked = false
            itemRadioGroupPayActivate.radioBtn2.isChecked = false
            itemRadioGroupPayActivate.radioBtn1.visible(true)
            itemRadioGroupPayActivate.radioBtn2.visible(true)
        }
    }

    private fun getTotalInfoText(totalCost: Double, quantity: Double): String =
        "${getString(R.string.total_balance)}: ${totalCost.toStringFullNumberFormat()} ${
            getString(
                R.string.le
            )
        }\n${getString(R.string.asset_quantity)}: ${quantity.toStringFullNumberFormat()} ${
            getString(
                R.string.g
            )
        }"

    private fun navigateToStockListView() =
        if (findNavController().currentDestination?.id == R.id.filterStockListView) findNavController().navigate(
            FilterStockListViewDirections.navigateToStockListView()
        ) else Unit

    private fun navigateToStockDetailsView(stock: Stock) =
        if (findNavController().currentDestination?.id == R.id.filterStockListView)
            findNavController().navigate(
                FilterStockListViewDirections.navigateToStockDetailsView(stock = stock)
            )
        else Unit
}
