package sparespark.stock.management.presentation.teamdetails

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance
import sparespark.stock.management.R
import sparespark.stock.management.core.ACTIVE
import sparespark.stock.management.core.ADMIN_ROLE_ID
import sparespark.stock.management.core.COLOR_GREEN
import sparespark.stock.management.core.COLOR_RED
import sparespark.stock.management.core.EMPLOYEE_ROLE_ID
import sparespark.stock.management.core.OWNER_ROLE_ID
import sparespark.stock.management.core.PM_ROLE_ID
import sparespark.stock.management.core.actionDisplayConfirmationDialog
import sparespark.stock.management.core.binding.ViewBindingHolder
import sparespark.stock.management.core.binding.ViewBindingHolderImpl
import sparespark.stock.management.core.view.displayAsLinedBoldStyle
import sparespark.stock.management.core.view.relaunchCurrentView
import sparespark.stock.management.data.model.login.User
import sparespark.stock.management.databinding.TeamdetailsViewBinding
import sparespark.stock.management.presentation.base.BaseViewInteract
import sparespark.stock.management.presentation.main.StockActivityInteract
import sparespark.stock.management.presentation.profile.ProfileViewDirections
import sparespark.stock.management.presentation.teamdetails.viewmodel.TeamDetailsViewModel
import sparespark.stock.management.presentation.teamdetails.viewmodel.TeamDetailsViewModelFactory

private val errorUser = User("0", "Error.", "", EMPLOYEE_ROLE_ID, ACTIVE)

class TeamDetailsView : Fragment(), BaseViewInteract,
    ViewBindingHolder<TeamdetailsViewBinding> by ViewBindingHolderImpl(), KodeinAware {
    private val args: TeamDetailsViewArgs? by navArgs()
    private val currentUser: User?
        get() = try {
            args?.user
        } catch (ex: Exception) {
            errorUser
        }
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var viewComm: StockActivityInteract.View
    private lateinit var viewModel: TeamDetailsViewModel
    private val viewModelFactory: TeamDetailsViewModelFactory by instance()
    override val kodein by closestKodein()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = initBinding(TeamdetailsViewBinding.inflate(layoutInflater), this) {
        setupBottomSheetBehavioral()
        initializeBaseViewModel()
        initializeBaseViewInteract()
        viewModel.startObserving()
        setupViewListener()
    }

    override fun initializeBaseViewModel() {
        viewModel = ViewModelProvider(
            this@TeamDetailsView, viewModelFactory
        )[TeamDetailsViewModel::class.java]
        viewModel.handleEvent(
            TeamDetailsViewEvent.OnTeamDetailsStartGetUser(
                user = currentUser ?: errorUser
            )
        )
    }

    override fun initializeBaseViewInteract() {
        viewComm = activity as StockActivityInteract.View
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.handleEvent(TeamDetailsViewEvent.OnDestroy)
    }

    private fun setupBottomSheetBehavioral() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding?.bottomSheetUpdateUser!!)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun TeamDetailsViewModel.startObserving() {
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
            if (it) navigateToTeamView()
        }
        bottomSheetViewState.observe(viewLifecycleOwner) {
            bottomSheetBehavior.state = it
        }
        user.observe(viewLifecycleOwner) {
            if (it != null) bindUser(it)
        }
    }

    private fun bindUser(user: User) {
        binding?.apply {
            itemUserInfo.userName.text = user.name
            itemUserInfo.userEmail.text = user.email
            pmSwitch.isChecked = user.roleId == PM_ROLE_ID
            adminSwitch.isChecked = user.roleId == OWNER_ROLE_ID || user.roleId == ADMIN_ROLE_ID
            if (user.activated) {
                itemUserInfo.userActive.displayAsLinedBoldStyle(getString(R.string.activated))
                itemUserInfo.userActive.setTextColor(Color.parseColor(COLOR_GREEN))
            } else {
                itemUserInfo.userActive.displayAsLinedBoldStyle(getString(R.string.deactivated))
                itemUserInfo.userActive.setTextColor(Color.parseColor(COLOR_RED))
            }
        }
    }

    private fun setupViewListener() {
        binding?.apply {
            adminSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
                if (buttonView?.isPressed == true) requireContext().actionDisplayConfirmationDialog(
                    R.string.update_user,
                ) {
                    viewModel.handleEvent(
                        TeamDetailsViewEvent.UpdateUserAdminStatus(
                            isAdmin = isChecked
                        )
                    )
                }
            }
            pmSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
                if (buttonView?.isPressed == true) requireContext().actionDisplayConfirmationDialog(
                    R.string.update_user,
                ) {
                    viewModel.handleEvent(
                        TeamDetailsViewEvent.UpdateUserPmStatus(
                            isPm = isChecked
                        )
                    )
                }
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (viewModel.isBottomSheetAtExpandingState()) viewModel.handleEvent(
                TeamDetailsViewEvent.UpdateBottomSheetToHideState
            )
            else findNavController().popBackStack()
        }
    }

    private fun navigateToTeamView() =
        if (findNavController().currentDestination?.id == R.id.teamDetailsView) findNavController().navigate(
            TeamDetailsViewDirections.navigateToTeamListView()
        )
        else Unit
}
