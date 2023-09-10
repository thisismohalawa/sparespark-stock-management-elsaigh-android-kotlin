package sparespark.stock.management.presentation.citylist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance
import sparespark.stock.management.core.binding.ViewBindingHolder
import sparespark.stock.management.core.binding.ViewBindingHolderImpl
import sparespark.stock.management.core.toEditable
import sparespark.stock.management.core.view.beginInputAndTextActionWatcher
import sparespark.stock.management.core.view.relaunchCurrentView
import sparespark.stock.management.core.view.setClickListenerWithViewDelayEnabled
import sparespark.stock.management.core.view.setupListItemDecoration
import sparespark.stock.management.data.model.city.City
import sparespark.stock.management.databinding.CitylistViewBinding
import sparespark.stock.management.presentation.base.BaseViewInteract
import sparespark.stock.management.presentation.citylist.adapter.CityAdapter
import sparespark.stock.management.presentation.citylist.viewmodel.CityListViewModel
import sparespark.stock.management.presentation.citylist.viewmodel.CityListViewModelFactory
import sparespark.stock.management.presentation.main.StockActivityInteract
import java.util.Locale

class CityListView : Fragment(), BaseViewInteract,
    ViewBindingHolder<CitylistViewBinding> by ViewBindingHolderImpl(),
    KodeinAware {
    override val kodein by closestKodein()
    private lateinit var cityAdapter: CityAdapter
    private lateinit var viewComm: StockActivityInteract.View
    private lateinit var viewModel: CityListViewModel
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private val viewModelFactory: CityListViewModelFactory by instance()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = initBinding(CitylistViewBinding.inflate(layoutInflater), this) {
        setupBottomSheetBehavioral()
        initializeBaseViewModel()
        initializeBaseViewInteract()
        setupRecyclerListBehavioral()
        viewModel.startObserving()
        setupRecyclerEventAdapter()
        setupViewClickListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.handleEvent(CityListEvent.OnDestroy)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding?.cityRecList?.adapter = null
    }

    override fun initializeBaseViewModel() {
        viewModel =
            ViewModelProvider(
                this@CityListView,
                viewModelFactory
            )[CityListViewModel::class.java]
        viewModel.handleEvent(CityListEvent.OnCityViewStart)
        viewModel.handleEvent(CityListEvent.GetCityList)
    }

    override fun initializeBaseViewInteract() {
        viewComm = activity as StockActivityInteract.View
        viewComm.actionOnSwipeLayoutRefreshed(
            binding?.cityRecList
        ) {
            relaunchCurrentView()
        }
    }

    private fun setupBottomSheetBehavioral() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding?.bottomSheetUpdateClient!!)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun setupRecyclerListBehavioral() {
        cityAdapter = CityAdapter()
        binding?.cityRecList?.apply {
            adapter = cityAdapter
            setupListItemDecoration(context)
        }
    }

    private fun setupRecyclerEventAdapter() {
        cityAdapter.event.observe(
            viewLifecycleOwner
        ) {
            viewModel.handleEvent(it)
        }
    }

    private fun CityListViewModel.startObserving() {
        loading.observe(viewLifecycleOwner) {
            if (it) viewComm.showLoadingProgress()
            else viewComm.hideLoadingProgress()
        }
        error.observe(viewLifecycleOwner) {
            if (it != null) {
                handleEvent(CityListEvent.UpdateBottomSheetToHideState)
                viewComm.updateMainActionStatusText(
                    msg = it, isError = true
                )
            }
        }
        bottomSheetViewState.observe(viewLifecycleOwner) {
            bottomSheetBehavior.state = it
        }
        updated.observe(viewLifecycleOwner) {
            if (it) relaunchCurrentView()
        }
        deleted.observe(viewLifecycleOwner) {
            if (it) relaunchCurrentView()
        }
        city.observe(viewLifecycleOwner) {
            it?.let {
                binding?.edCityName?.text = it.name.toEditable()
            }
        }
        cityList.observe(viewLifecycleOwner) {
            if (it?.isNotEmpty() == true) {
                cityAdapter.submitList(it)
                setUpSearchViewListener(it)
            }
        }
    }

    private fun setUpSearchViewListener(cityList: List<City>) {
        binding?.itemSearch?.mtSearchView?.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { filterList(it, cityList) }
                return true
            }
        })
    }

    private fun filterList(query: String, cityList: List<City>) {
        val filteredList: MutableList<City> = mutableListOf()
        for (city: City in cityList)
            if (city.name.lowercase(Locale.ROOT).contains(query))
                filteredList.add(city)

        if (filteredList.isNotEmpty())
            cityAdapter.submitList(filteredList)
    }

    private fun setupViewClickListener() {
        binding?.apply {
            edCityName.beginInputAndTextActionWatcher(txtUpdateCity)
            txtUpdateCity.setClickListenerWithViewDelayEnabled {
                viewModel.handleEvent(
                    CityListEvent.UpdateCity(
                        edCityName.text.toString().trim()
                    )
                )
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (viewModel.isBottomSheetAtExpandingState())
                viewModel.handleEvent(CityListEvent.UpdateBottomSheetToHideState)
            else findNavController().popBackStack()
        }
    }
}
