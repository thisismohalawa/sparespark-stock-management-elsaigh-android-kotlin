package sparespark.stock.management

import android.app.Application
import com.google.firebase.FirebaseApp
import com.jakewharton.threetenabp.AndroidThreeTen
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton
import sparespark.stock.management.data.implementation.CityRepositoryImpl
import sparespark.stock.management.data.implementation.ClientRepositoryImpl
import sparespark.stock.management.data.implementation.StockRepositoryImpl
import sparespark.stock.management.data.implementation.TeamRepositoryImpl
import sparespark.stock.management.data.implementation.UserRepositoryImpl
import sparespark.stock.management.data.local.StockDatabase
import sparespark.stock.management.data.local.preference.util.UtilPreference
import sparespark.stock.management.data.local.preference.util.UtilPreferenceImpl
import sparespark.stock.management.data.network.connectivity.ConnectivityInterceptor
import sparespark.stock.management.data.network.connectivity.ConnectivityInterceptorImpl
import sparespark.stock.management.data.reminder.ReminderAPI
import sparespark.stock.management.data.reminder.ReminderApiImpl
import sparespark.stock.management.data.repository.CityRepository
import sparespark.stock.management.data.repository.ClientRepository
import sparespark.stock.management.data.repository.StockRepository
import sparespark.stock.management.data.repository.TeamRepository
import sparespark.stock.management.data.repository.UserRepository
import sparespark.stock.management.presentation.citylist.viewmodel.CityListViewModelFactory
import sparespark.stock.management.presentation.clientlist.viewmodel.ClientListViewModelFactory
import sparespark.stock.management.presentation.filterstocklist.viewmodel.FilterStockListViewModelFactory
import sparespark.stock.management.presentation.login.viewmodel.LoginViewModelFactory
import sparespark.stock.management.presentation.main.StockActivityViewModelFactory
import sparespark.stock.management.presentation.stockdetails.viewmodel.StockDetailsViewModelFactory
import sparespark.stock.management.presentation.stocklist.viewmodel.StockListViewModelFactory
import sparespark.stock.management.presentation.teamdetails.viewmodel.TeamDetailsViewModelFactory
import sparespark.stock.management.presentation.teamlist.viewmodel.TeamListViewModelFactory

class StockApplication : Application(), KodeinAware {
    override val kodein = Kodein.lazy {
        import(androidXModule(this@StockApplication))

        // Network
        bind<ConnectivityInterceptor>() with singleton { ConnectivityInterceptorImpl(instance()) }

        // Room
        bind() from singleton { StockDatabase(instance()) }
        bind() from singleton { instance<StockDatabase>().userDao() }
        bind() from singleton { instance<StockDatabase>().clientDao() }
        bind() from singleton { instance<StockDatabase>().cityDao() }
        bind() from singleton { instance<StockDatabase>().stockDao() }

        // Preference
        bind<UtilPreference>() with singleton { UtilPreferenceImpl(instance()) }

        // Alarm
        bind<ReminderAPI>() with singleton { ReminderApiImpl(instance(), instance()) }

        // Repository
        bind<StockRepository>() with singleton {
            StockRepositoryImpl(
                instance(),
                instance(),
                instance(),
                instance()
            )
        }
        bind<ClientRepository>() with singleton {
            ClientRepositoryImpl(
                instance(),
                instance(),
                instance(),
                instance()
            )
        }
        bind<CityRepository>() with singleton {
            CityRepositoryImpl(
                instance(),
                instance(),
                instance(),
                instance()
            )
        }
        bind<UserRepository>() with singleton {
            UserRepositoryImpl(
                instance(),
                instance(),
                instance()
            )
        }
        bind<TeamRepository>() with singleton {
            TeamRepositoryImpl(
                instance(),
                instance(),
                instance()
            )
        }

        // ViewModel
        bind() from provider { LoginViewModelFactory(instance()) }
        bind() from provider { ClientListViewModelFactory(instance(), instance()) }
        bind() from provider { CityListViewModelFactory(instance()) }
        bind() from provider { TeamListViewModelFactory(instance()) }
        bind() from provider { TeamDetailsViewModelFactory(instance()) }
        bind() from provider { StockListViewModelFactory(instance()) }
        bind() from provider { StockDetailsViewModelFactory(instance(), instance()) }
        bind() from provider { FilterStockListViewModelFactory(instance(), instance(), instance()) }
        bind() from provider { StockActivityViewModelFactory(instance(), instance()) }

    }

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this@StockApplication)
        AndroidThreeTen.init(this@StockApplication)
    }
}
