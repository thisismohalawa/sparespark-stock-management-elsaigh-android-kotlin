package sparespark.stock.management.presentation.teamlist

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
import sparespark.stock.management.core.binding.ViewBindingHolder
import sparespark.stock.management.core.binding.ViewBindingHolderImpl
import sparespark.stock.management.core.getMutableTeamFilteredList
import sparespark.stock.management.core.view.relaunchCurrentView
import sparespark.stock.management.data.model.login.User
import sparespark.stock.management.databinding.TeamlistViewBinding
import sparespark.stock.management.presentation.base.BaseViewInteract
import sparespark.stock.management.presentation.main.StockActivityInteract
import sparespark.stock.management.presentation.teamlist.adapter.MainTeamAdapter
import sparespark.stock.management.presentation.teamlist.viewmodel.TeamListViewModel
import sparespark.stock.management.presentation.teamlist.viewmodel.TeamListViewModelFactory

class TeamListView : Fragment(), BaseViewInteract,
    ViewBindingHolder<TeamlistViewBinding> by ViewBindingHolderImpl(), KodeinAware {
    private lateinit var viewComm: StockActivityInteract.View
    private lateinit var teamAdapter: MainTeamAdapter
    private lateinit var viewModel: TeamListViewModel
    private val viewModelFactory: TeamListViewModelFactory by instance()
    override val kodein by closestKodein()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = initBinding(TeamlistViewBinding.inflate(layoutInflater), this) {
        initializeBaseViewModel()
        initializeBaseViewInteract()
        setupRecyclerListBehavioral()
        viewModel.startObserving()
        setupRecyclerEventAdapter()
    }

    override fun initializeBaseViewModel() {
        viewModel =
            ViewModelProvider(this@TeamListView, viewModelFactory)[TeamListViewModel::class.java]
        viewModel.handleEvent(TeamListViewEvent.GetTeamList)
    }

    override fun initializeBaseViewInteract() {
        viewComm = activity as StockActivityInteract.View
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.handleEvent(TeamListViewEvent.OnDestroy)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding?.recTeamList?.adapter = null
    }

    private fun TeamListViewModel.startObserving() {
        loading.observe(viewLifecycleOwner) {
            if (it) viewComm.showLoadingProgress()
            else viewComm.hideLoadingProgress()
        }
        error.observe(viewLifecycleOwner) {
            if (it != null) viewComm.updateMainActionStatusText(
                msg = it, isError = true
            )
        }
        updated.observe(viewLifecycleOwner) {
            if (it) relaunchCurrentView()
        }
        userList.observe(viewLifecycleOwner) {
            teamAdapter.submitList(getMutableTeamFilteredList(it))
        }
        teamItemClickedNavigateAttempt.observe(viewLifecycleOwner) {
            navigateToTeamDetails(it)
        }
    }

    private fun setupRecyclerListBehavioral() {
        teamAdapter = MainTeamAdapter()
        binding?.recTeamList?.adapter = teamAdapter
    }

    private fun setupRecyclerEventAdapter() {
        teamAdapter.event.observe(
            viewLifecycleOwner
        ) {
            viewModel.handleEvent(it)
        }
    }

    private fun navigateToTeamDetails(user: User) =
        if (findNavController().currentDestination?.id == R.id.teamListView) findNavController().navigate(
            TeamListViewDirections.navigateToTeamDetailsView(user)
        )
        else Unit
}
