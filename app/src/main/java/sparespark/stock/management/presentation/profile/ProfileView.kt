package sparespark.stock.management.presentation.profile

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import sparespark.stock.management.R
import sparespark.stock.management.core.COLOR_GREEN
import sparespark.stock.management.core.COLOR_RED
import sparespark.stock.management.core.actionDisplayConfirmationDialog
import sparespark.stock.management.core.binding.ViewBindingHolder
import sparespark.stock.management.core.binding.ViewBindingHolderImpl
import sparespark.stock.management.core.view.displayAsLinedBoldStyle
import sparespark.stock.management.core.view.setupListItemDecoration
import sparespark.stock.management.data.model.login.User
import sparespark.stock.management.databinding.ProfileViewBinding
import sparespark.stock.management.presentation.base.BaseViewInteract
import sparespark.stock.management.presentation.main.StockActivity
import sparespark.stock.management.presentation.main.StockActivityEvent
import sparespark.stock.management.presentation.main.StockActivityInteract
import sparespark.stock.management.presentation.main.StockActivityViewModel
import sparespark.stock.management.presentation.profile.adapter.SettingsAdapter

class ProfileView : Fragment(), BaseViewInteract,
    ViewBindingHolder<ProfileViewBinding> by ViewBindingHolderImpl() {
    private lateinit var viewModel: StockActivityViewModel
    private lateinit var viewComm: StockActivityInteract.View
    private lateinit var viewAction: StockActivityInteract.Action
    private lateinit var settingsAdapter: SettingsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = initBinding(ProfileViewBinding.inflate(layoutInflater), this) {
        initializeBaseViewModel()
        initializeBaseViewInteract()
        setUpSettingsListRecyclerBehavioral()
        viewModel.startObserving()
        setupRecyclerEventAdapter()
    }

    override fun initializeBaseViewModel() {
        viewModel = (activity as StockActivity).viewModel
        viewModel.handleEvent(StockActivityEvent.GetProfileUserInfo)
        viewModel.handleEvent(StockActivityEvent.GetProfileSettingsList)
    }

    override fun initializeBaseViewInteract() {
        viewComm = activity as StockActivityInteract.View
        viewAction = activity as StockActivityInteract.Action
    }

    private fun StockActivityViewModel.startObserving() {
        user.observe(viewLifecycleOwner) {
            if (it == null) viewComm.moveToLoginView()
            else bindUser(it)
        }
        settingsList.observe(viewLifecycleOwner) {
            settingsAdapter.submitList(it)
        }
        updated.observe(viewLifecycleOwner) {
            viewComm.restartActivity()
        }
        navigateToTeamAttempt.observe(viewLifecycleOwner) {
            navigateToTeamView()
        }
        navigateTempDBAttempt.observe(viewLifecycleOwner) {
            navigateToTempDBView()
        }
        backupActionDialogAttempt.observe(viewLifecycleOwner) {
            activity?.actionDisplayConfirmationDialog(
                titleResId = R.string.delete_completed_item,
                actionResId = R.string.delete_permanently,
                subActionResId = R.string.backup_first,
                subAction = {
                    viewAction.downloadDataBackupAsExcelFile()
                },
                action = {
                    viewAction.deleteAllCompletedData()
                }
            )
        }
    }

    private fun setUpSettingsListRecyclerBehavioral() {
        settingsAdapter = SettingsAdapter()
        binding?.settingRecList?.apply {
            adapter = settingsAdapter
            setupListItemDecoration(context)
        }
    }

    private fun setupRecyclerEventAdapter() {
        settingsAdapter.event.observe(
            viewLifecycleOwner
        ) {
            viewModel.handleEvent(it)
        }
    }

    private fun bindUser(user: User) {
        binding?.itemUserInfo?.apply {
            userName.text = user.name
            userEmail.text = user.email
            if (user.activated) {
                userActive.displayAsLinedBoldStyle(getString(R.string.activated))
                userActive.setTextColor(Color.parseColor(COLOR_GREEN))
            } else {
                userActive.displayAsLinedBoldStyle(getString(R.string.deactivated))
                userActive.setTextColor(Color.parseColor(COLOR_RED))
            }
        }
    }

    private fun navigateToTeamView() =
        if (findNavController().currentDestination?.id == R.id.profileView)
            findNavController().navigate(ProfileViewDirections.navigateToTeamListView())
        else Unit

    private fun navigateToTempDBView() =
        if (findNavController().currentDestination?.id == R.id.profileView)
            findNavController().navigate(ProfileViewDirections.navigateToTempListView())
        else Unit
}
