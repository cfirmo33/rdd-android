package lv.rigadevday.android.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import lv.rigadevday.android.R
import lv.rigadevday.android.repository.Repository
import lv.rigadevday.android.ui.tabs.TabActivity
import lv.rigadevday.android.utils.BaseApp
import lv.rigadevday.android.utils.showMessage
import javax.inject.Inject


class SplashActivity : AppCompatActivity() {

    val TIME_TO_EXIT: Long = 2000

    @Inject lateinit var repo: Repository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BaseApp.graph.inject(this)

        // Cache and enrich data into memory
        repo.updateCache()
            .toCompletable()
            .subscribe(
                {
                    Intent(this, TabActivity::class.java)
                        .let { startActivity(it) }
                        .also { finish() }
                },
                {
                    showMessage(R.string.error_connection_message)
                    exitApp()
                }
            )
    }

    fun exitApp() = Handler().postDelayed(
        { finish() },
        TIME_TO_EXIT
    )
}
