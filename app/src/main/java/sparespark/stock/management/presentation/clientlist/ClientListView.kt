package sparespark.stock.management.presentation.clientlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import androidx.activity.addCallback
import androidx.appcompat.widget.SearchView
import androidx.core.view.isNotEmpty
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance
import sparespark.stock.management.R
import sparespark.stock.management.core.binding.ViewBindingHolder
import sparespark.stock.management.core.binding.ViewBindingHolderImpl
import sparespark.stock.management.core.toEditable
import sparespark.stock.management.core.view.beginInputAndTextActionWatcher
import sparespark.stock.management.core.view.relaunchCurrentView
import sparespark.stock.management.core.view.setClickListenerWithViewDelayEnabled
import sparespark.stock.management.core.view.setUpCitySpinners
import sparespark.stock.management.core.view.setupListItemDecoration
import sparespark.stock.management.data.model.client.Client
import sparespark.stock.management.databinding.ClientlistViewBinding
import sparespark.stock.management.presentation.base.BaseViewInteract
import sparespark.stock.management.presentation.clientlist.adapter.ClientAdapter
import sparespark.stock.management.presentation.clientlist.viewmodel.ClientListViewModel
import sparespark.stock.management.presentation.clientlist.viewmodel.ClientListViewModelFactory
import sparespark.stock.management.presentation.main.StockActivityInteract
import java.util.Locale

class ClientListView : Fragment(), BaseViewInteract,
    ViewBindingHolder<ClientlistViewBinding> by ViewBindingHolderImpl(),
    KodeinAware {
    override val kodein by closestKodein()
    private lateinit var clientAdapter: ClientAdapter
    private lateinit var viewComm: StockActivityInteract.View
    private lateinit var viewModel: ClientListViewModel
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private val viewModelFactory: ClientListViewModelFactory by instance()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = initBinding(ClientlistViewBinding.inflate(layoutInflater), this) {
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
        viewModel.handleEvent(ClientListEvent.OnDestroy)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding?.clientRecList?.adapter = null
    }

    override fun initializeBaseViewModel() {
        viewModel =
            ViewModelProvider(
                this@ClientListView,
                viewModelFactory
            )[ClientListViewModel::class.java]
        viewModel.handleEvent(ClientListEvent.OnClientViewStart)
        viewModel.handleEvent(ClientListEvent.GetClientList)
        viewModel.handleEvent(ClientListEvent.GetCityList)
    }

    override fun initializeBaseViewInteract() {
        viewComm = activity as StockActivityInteract.View
        viewComm.actionOnSwipeLayoutRefreshed(
            binding?.clientRecList
        ) {
            relaunchCurrentView()
        }
    }

    private fun ClientListViewModel.startObserving() {
        loading.observe(viewLifecycleOwner) {
            if (it) viewComm.showLoadingProgress()
            else viewComm.hideLoadingProgress()
        }
        error.observe(viewLifecycleOwner) {
            if (it != null) {
                handleEvent(ClientListEvent.UpdateBottomSheetToHideState)
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
        client.observe(viewLifecycleOwner) {
            if (it != null)
                binding?.apply {
                    edClientName.text = it.name.toEditable()
                    edClientPhone.text = it.phoneNum.toEditable()
                    cityListSpinner.spinner.selectValue(it.cityName)
                }
        }
        clientList.observe(viewLifecycleOwner) {
            it?.let {
                clientAdapter.submitList(it)
                setupSearchViewListener(it)
            }
        }
        cityList.observe(viewLifecycleOwner) {
            if (it?.isNotEmpty() == true)
                binding?.cityListSpinner?.spinner?.setUpCitySpinners(
                    context = requireContext(),
                    allAsFirstItem = true,
                    cityList = it,
                    action = { cityName ->
                        viewModel.updateSelectedCity(cityName)
                    })
        }
    }

    private fun setupBottomSheetBehavioral() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding?.bottomSheetUpdateClient!!)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun setupRecyclerListBehavioral() {
        clientAdapter = ClientAdapter()
        binding?.clientRecList?.apply {
            adapter = clientAdapter
            setupListItemDecoration(context)
        }
    }

    private fun setupRecyclerEventAdapter() {
        clientAdapter.event.observe(
            viewLifecycleOwner
        ) {
            viewModel.handleEvent(it)
        }
    }

    private fun setupSearchViewListener(clientList: List<Client>) {
        binding?.itemSearch?.mtSearchView?.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { filterList(it, clientList) }
                return true
            }
        })
    }

    private fun filterList(query: String, clientList: List<Client>) {
        val filteredList: MutableList<Client> = mutableListOf()
        for (client: Client in clientList) if (client.name.lowercase(Locale.ROOT)
                .contains(query)
        ) filteredList.add(client)
        else if (client.cityName.lowercase(Locale.ROOT).contains(query)) filteredList.add(client)

        if (filteredList.isNotEmpty()) clientAdapter.submitList(filteredList)
    }

    private fun Spinner.selectValue(value: Any) {
        if (this.isNotEmpty()) for (i in 0 until this.count) {
            if (this.getItemAtPosition(i) == value) {
                this.setSelection(i)
                break
            }
        }
    }

    private fun setupViewClickListener() {
        binding?.apply {
            edClientName.beginInputAndTextActionWatcher(txtUpdateClient)
            txtUpdateClient.setClickListenerWithViewDelayEnabled {
                viewModel.handleEvent(
                    ClientListEvent.UpdateClient(
                        clientName = edClientName.text.toString().trim(),
                        phoneNum = edClientPhone.text.toString().trim()
                    )
                )
            }

            btnAddCity.setOnClickListener {
                navigateToCityList()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (viewModel.isBottomSheetAtExpandingState())
                viewModel.handleEvent(ClientListEvent.UpdateBottomSheetToHideState)
            else findNavController().popBackStack()
        }
    }

    private fun navigateToCityList() =
        if (findNavController().currentDestination?.id == R.id.clientListView)
            findNavController().navigate(
                ClientListViewDirections.navigateToCityView()
            )
        else Unit
}
