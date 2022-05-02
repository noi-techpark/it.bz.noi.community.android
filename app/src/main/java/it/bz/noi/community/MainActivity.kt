package it.bz.noi.community

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.asLiveData
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.MaterialToolbar
import it.bz.noi.community.data.api.ApiHelper
import it.bz.noi.community.data.api.RetrofitBuilder
import it.bz.noi.community.data.repository.JsonFilterRepository
import it.bz.noi.community.databinding.ActivityMainBinding
import it.bz.noi.community.oauth.AuthManager
import it.bz.noi.community.oauth.AuthStateStatus
import it.bz.noi.community.ui.MainViewModel
import it.bz.noi.community.ui.ViewModelFactory
import it.bz.noi.community.ui.WebViewFragment
import kotlinx.coroutines.Dispatchers
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.EndSessionResponse

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val navController: NavController by lazy {
        findNavController(R.id.nav_host_fragment)
    }

    private val mainViewModel: MainViewModel by viewModels(factoryProducer = {
        ViewModelFactory(ApiHelper(RetrofitBuilder.apiService), JsonFilterRepository(application))
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            window.navigationBarColor =
                resources.getColor(R.color.background_color, theme)
        } else {
            window.navigationBarColor = resources.getColor(R.color.background_color)
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(findViewById(R.id.toolbar))

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_today,
                R.id.navigation_orientate,
                R.id.navigation_meet,
                R.id.navigation_eat,
                R.id.navigation_more
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            when (destination.id) {
                R.id.navigation_more -> {
                    supportActionBar?.hide()
                }
				R.id.webViewFragment -> {
					supportActionBar?.show()
					(findViewById<MaterialToolbar>(R.id.toolbar).getChildAt(0) as TextView).textSize =
						18f
					if (destination.id == R.id.webViewFragment) {
						arguments?.let {
							supportActionBar?.title = arguments.getString(WebViewFragment.TITLE_ARG)
						}
					}
				}
                R.id.eventDetailsFragment, R.id.filtersFragment, R.id.myAccount -> {
					supportActionBar?.show()
                    (findViewById<MaterialToolbar>(R.id.toolbar).getChildAt(0) as TextView).textSize =
                        18f
                }
                else -> {
                    (findViewById<MaterialToolbar>(R.id.toolbar).getChildAt(0) as TextView).textSize =
                        26f
                    supportActionBar?.show()
                }
            }
        }


		AuthManager.status.asLiveData(Dispatchers.Main).observe(this) { status ->
			when (status) {
//				is AuthStateStatus.Authorized ->
				is AuthStateStatus.Error,
				AuthStateStatus.Unauthorized.UserAuthRequired -> {
					goToOnboardingActivity()
				}
//				AuthStateStatus.Unauthorized.NotValidRole -> TODO()
//				AuthStateStatus.Unauthorized.PendingToken -> TODO()

			}
		}
    }

    override fun onSupportNavigateUp(): Boolean {
        if (navController.currentBackStackEntry?.destination?.id == R.id.filtersFragment) {
        	if (!mainViewModel.isFiltersSameAsCached()) {
				mainViewModel.restoreCachedFilters()
				mainViewModel.refresh()
			}
        }
        navController.popBackStack()
        return super.onSupportNavigateUp()
    }

	private fun goToOnboardingActivity() {
		startActivity(Intent(this, OnboardingActivity::class.java))
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		Log.d(TAG, "onActivityResult")
		when (requestCode) {
			END_SESSION_REQUEST_CODE -> {
				val exception: AuthorizationException? = data?.let {
					AuthorizationException.fromIntent(it)
				}
				if (exception != null) {
					// TODO
					Toast.makeText(this, "Logout error", Toast.LENGTH_SHORT).show()
				} else {
					AuthManager.onEndSession()
				}
			}
			else -> {
				super.onActivityResult(requestCode, resultCode, data)
			}
		}
	}

	companion object {
		private const val TAG = "MainActivity"
		const val END_SESSION_REQUEST_CODE = 911
	}

}
