package sparespark.stock.management.presentation.stocklist

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance
import sparespark.stock.management.R
import sparespark.stock.management.core.COLOR_GREEN
import sparespark.stock.management.core.actionDisplayConfirmationDialog
import sparespark.stock.management.core.binding.ViewBindingHolder
import sparespark.stock.management.core.binding.ViewBindingHolderImpl
import sparespark.stock.management.core.view.makeToast
import sparespark.stock.management.core.view.relaunchCurrentView
import sparespark.stock.management.core.view.setupListItemDecoration
import sparespark.stock.management.core.view.visible
import sparespark.stock.management.data.model.stock.Stock
import sparespark.stock.management.databinding.StocklistViewBinding
import sparespark.stock.management.presentation.base.BaseViewInteract
import sparespark.stock.management.presentation.main.StockActivityInteract
import sparespark.stock.management.presentation.stocklist.adapter.StockAdapter
import sparespark.stock.management.presentation.stocklist.viewmodel.StockListViewModel
import sparespark.stock.management.presentation.stocklist.viewmodel.StockListViewModelFactory

class TempStockListView : Fragment(),
    View.OnClickListener,
    BaseViewInteract,
    ViewBindingHolder<StocklistViewBinding> by ViewBindingHolderImpl(), KodeinAware {
    override val kodein by closestKodein()
    private lateinit var stockAdapter: StockAdapter
    private lateinit var viewModel: StockListViewModel
    private lateinit var viewComm: StockActivityInteract.View
    private val viewModelFactory: StockListViewModelFactory by instance()
    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btn_update_remote -> activity?.actionDisplayConfirmationDialog(
                titleResId = R.string.send_data_to_server,
                action = {
                    viewModel.handleEvent(StockListEvent.PushTempDataToServer)
                }
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = initBinding(StocklistViewBinding.inflate(layoutInflater), this) {
        setupTempView()
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
    }

    override fun initializeBaseViewModel() {
        viewModel =
            ViewModelProvider(
                this@TempStockListView,
                viewModelFactory
            )[StockListViewModel::class.java]
        viewModel.handleEvent(StockListEvent.GetTempStockList)
    }

    override fun initializeBaseViewInteract() {
        viewComm = activity as StockActivityInteract.View
    }

    private fun setupTempView() {
        binding?.apply {
            itemStockInfo.contentLayout.visible(false)
            txtTotalBalance.visible(false)
            txtExchangeList.text = getString(R.string.temp_database)
            txtExchangeList.setTextColor(Color.parseColor(COLOR_GREEN))
        }
    }

    private fun setupViewClickListener() {
        binding?.apply {
            btnUpdateRemote.setOnClickListener(this@TempStockListView)
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
        stocklist.observe(viewLifecycleOwner) {
            if (it != null) {
                stockAdapter.submitList(it)
                binding?.btnUpdateRemote?.visible(true)
            }
        }
        updated.observe(viewLifecycleOwner) {
            if (it) navigateToStockList()
        }
        stockItemClickedNavigateAttempt.observe(viewLifecycleOwner) {
            navigateToStockDetails(stock = it)
        }
    }

    private fun setupListRecyclerAdapterAndBehavioral() {
        stockAdapter = StockAdapter(isTempItem = true)
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

    private fun navigateToStockDetails(stock: Stock) =
        findNavController().navigate(TempStockListViewDirections.navigateToStockDetailsView(stock))

    private fun navigateToStockList() =
        findNavController().navigate(TempStockListViewDirections.navigateToStockListView())
}
