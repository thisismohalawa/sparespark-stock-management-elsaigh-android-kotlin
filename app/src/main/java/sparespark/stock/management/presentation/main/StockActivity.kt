package sparespark.stock.management.presentation.main

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance
import sparespark.stock.management.R
import sparespark.stock.management.core.ADMIN_ROLE_ID
import sparespark.stock.management.core.COLOR_RED
import sparespark.stock.management.core.EMPLOYEE_ROLE_ID
import sparespark.stock.management.core.OWNER_ROLE_ID
import sparespark.stock.management.core.PM_ROLE_ID
import sparespark.stock.management.core.handlerPostDelayed
import sparespark.stock.management.core.result.UiResourceResult
import sparespark.stock.management.core.view.makeToast
import sparespark.stock.management.core.view.restartListActivity
import sparespark.stock.management.core.view.startLoginActivity
import sparespark.stock.management.data.receiver.DataBackupsService
import sparespark.stock.management.data.receiver.START_ACTION_BACKUPS
import sparespark.stock.management.data.receiver.START_ACTION_CLEAR_DATA
import sparespark.stock.management.databinding.ActivityStockBinding

private const val STORAGE_PERMISSION_CODE = 101
class StockActivity : AppCompatActivity(), StockActivityInteract.View,
    StockActivityInteract.Action,
    KodeinAware {
    override val kodein by closestKodein()
    private lateinit var mBinding: ActivityStockBinding
    private lateinit var navController: NavController
    lateinit var viewModel: StockActivityViewModel
    private val viewModelFactory: StockActivityViewModelFactory by instance()
    override fun moveToLoginView(): Unit = startLoginActivity()
    override fun restartActivity(): Unit = restartListActivity()
    override fun showLoadingProgress(): Unit = updateSwipeRefreshState(isRefresh = true)
    override fun hideLoadingProgress(): Unit = updateSwipeRefreshState(isRefresh = false)
    override fun updateMainActionStatusText(msg: UiResourceResult, isError: Boolean) {
        if (isError) mBinding.contentMain.txtActionStatus.setTextColor(Color.parseColor(COLOR_RED))
        mBinding.contentMain.txtActionStatus.text = msg.asString(this@StockActivity)
    }

    override fun actionOnSwipeLayoutRefreshed(rc: RecyclerView?, action: () -> Unit) {
        with(mBinding.contentMain.swipeContentLayout) {
            this.setOnChildScrollUpCallback(object : SwipeRefreshLayout.OnChildScrollUpCallback {
                override fun canChildScrollUp(parent: SwipeRefreshLayout, child: View?): Boolean {
                    if (rc != null) {
                        return rc.canScrollVertically(-1)
                    }
                    return false
                }
            })
            this.setOnRefreshListener {
                handlerPostDelayed(millisValue = 1000) {
                    this.isRefreshing = false
                    action()
                }
            }

        }
    }

    override fun deleteAllCompletedData() {
        val serviceIntent = Intent(this@StockActivity, DataBackupsService::class.java)
        serviceIntent.action = START_ACTION_CLEAR_DATA
        startService(serviceIntent)
    }

    override fun downloadDataBackupAsExcelFile() {
        val serviceIntent = Intent(this@StockActivity, DataBackupsService::class.java)
        serviceIntent.action = START_ACTION_BACKUPS
        startService(serviceIntent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this@StockActivity, R.layout.activity_stock)
        setupNavigationController()
        setupSwipeRefreshLayoutBehavioral()
        setupViewModel()
        viewModel.startObserving()
        actionOnSwipeLayoutRefreshed {
            restartActivity()
        }
    }

    private fun setupNavigationController() {
        val navHostFragment: NavHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        navController = navHostFragment.findNavController()
    }

    private fun setupSwipeRefreshLayoutBehavioral() {
        mBinding.contentMain.swipeContentLayout.setColorSchemeResources(
            android.R.color.holo_blue_dark, android.R.color.holo_green_light
        )
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(
            this@StockActivity, viewModelFactory
        )[StockActivityViewModel::class.java]
        viewModel.handleEvent(StockActivityEvent.OnStart)
    }

    private fun setupBottomNavigationMenu(roleId: Int?) {
        when (roleId) {
            OWNER_ROLE_ID, ADMIN_ROLE_ID, PM_ROLE_ID -> inflateBottomNavigation(R.menu.admin_btm_menu)

            EMPLOYEE_ROLE_ID -> inflateBottomNavigation(R.menu.employee_btm_menu)

            null -> inflateBottomNavigation(null)
            else -> inflateBottomNavigation(null)
        }
    }

    private fun inflateBottomNavigation(menuRes: Int?) {
        with(mBinding.contentMain.bottomNav) {
            if (menuRes == null) this.visibility = View.INVISIBLE
            else {
                this.menu.clear()
                this.inflateMenu(menuRes)
                this.setupWithNavController(navController)
            }
        }
    }

    private fun updateSwipeRefreshState(isRefresh: Boolean) {
        mBinding.contentMain.swipeContentLayout.isRefreshing = isRefresh
    }

    private fun StockActivityViewModel.startObserving() {
        loading.observe(this@StockActivity) {
            if (it) showLoadingProgress() else hideLoadingProgress()
        }
        actionEventTextStatus.observe(this@StockActivity) {
            updateMainActionStatusText(msg = it.uiResourceResult, isError = it.isError)
        }
        signAttempt.observe(this@StockActivity) {
            moveToLoginView()
        }
        userRole.observe(this@StockActivity) {
            setupBottomNavigationMenu(roleId = it)
            checkWriteStoragePermission()
        }
    }
    private fun checkWriteStoragePermission() {
        if (ContextCompat.checkSelfPermission(
                this@StockActivity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_DENIED
        ) ActivityCompat.requestPermissions(
            this@StockActivity,
            arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
            STORAGE_PERMISSION_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) makeToast(
                getString(R.string.permission_granted_storage)
            )
            else makeToast(getString(R.string.permission_denied_storage))
        }
    }
}
