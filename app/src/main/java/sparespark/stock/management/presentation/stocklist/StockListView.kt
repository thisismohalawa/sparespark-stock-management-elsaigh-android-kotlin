package sparespark.stock.management.presentation.stocklist

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
import sparespark.stock.management.core.binding.ViewBindingHolder
import sparespark.stock.management.core.binding.ViewBindingHolderImpl
import sparespark.stock.management.core.view.makeToast
import sparespark.stock.management.core.view.relaunchCurrentView
import sparespark.stock.management.core.view.setUpTextInfoTitle
import sparespark.stock.management.core.view.setupListItemDecoration
import sparespark.stock.management.data.model.stock.Stock
import sparespark.stock.management.databinding.StocklistViewBinding
import sparespark.stock.management.presentation.base.BaseViewInteract
import sparespark.stock.management.presentation.main.StockActivity
import sparespark.stock.management.presentation.main.StockActivityInteract
import sparespark.stock.management.presentation.stocklist.adapter.StockAdapter
import sparespark.stock.management.presentation.stocklist.viewmodel.StockListViewModel
import sparespark.stock.management.presentation.stocklist.viewmodel.StockListViewModelFactory

class StockListView : Fragment(), BaseViewInteract,
    ViewBindingHolder<StocklistViewBinding> by ViewBindingHolderImpl(), KodeinAware {
    override val kodein by closestKodein()
    private lateinit var stockAdapter: StockAdapter
    private lateinit var viewModel: StockListViewModel
    private lateinit var viewComm: StockActivityInteract.View
    private val viewModelFactory: StockListViewModelFactory by instance()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = initBinding(StocklistViewBinding.inflate(layoutInflater), this) {
        initializeBaseViewModel()
        initializeBaseViewInteract()
        setupListRecyclerAdapterAndBehavioral()
        viewModel.startObserving()
        setUpRecyclerViewModelEvent()
        setupViewClickListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.handleEvent(StockListEvent.OnDestroy)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding?.recStockList?.adapter = null
        StockAdapter.clearSelectedStockIds()
    }

    override fun initializeBaseViewModel() {
        viewModel =
            ViewModelProvider(this@StockListView, viewModelFactory)[StockListViewModel::class.java]
        viewModel.handleEvent(StockListEvent.GetStockList)
    }

    override fun initializeBaseViewInteract() {
        viewComm = activity as StockActivityInteract.View
    }

    private fun setupViewClickListener() {
        binding?.apply {
            itemStockInfo.contentLayout.setOnClickListener {
                navigateToSettingView()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            (activity as StockActivity).finish()
        }
    }

    private fun StockListViewModel.startObserving() {
        loading.observe(viewLifecycleOwner) {
            if (it) viewComm.showLoadingProgress()
            else viewComm.hideLoadingProgress()
        }
        error.observe(viewLifecycleOwner) {
            activity?.makeToast(it.asString(requireContext()))
        }
        updated.observe(viewLifecycleOwner) {
            if (it) relaunchCurrentView()
        }
        stocklist.observe(viewLifecycleOwner) {
            if (it != null) stockAdapter.submitList(it)
        }
        stockListDetails.observe(viewLifecycleOwner) {
            binding?.itemStockInfo?.apply {
                it?.let {
                    txtTotalPrice.setUpTextInfoTitle(it.totalCostAmount, getString(R.string.le))
                    txtTotalQuantity.setUpTextInfoTitle(
                        it.totalAssetQuantity, getString(R.string.g)
                    )
                    txtAverage.setUpTextInfoTitle(
                        it.average, getString(R.string.avg)
                    )
                }
            }
        }
        stockItemClickedNavigateAttempt.observe(viewLifecycleOwner) {
            navigateToStockDetails(stock = it)
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

    private fun navigateToSettingView() =
        if (findNavController().currentDestination?.id == R.id.stockListView) findNavController().navigate(
            StockListViewDirections.navigateToSettingsView()
        ) else Unit

    private fun navigateToStockDetails(stock: Stock) =
        if (findNavController().currentDestination?.id == R.id.stockListView) findNavController().navigate(
            StockListViewDirections.navigateToStockDetailsView(stock)
        ) else Unit
}
