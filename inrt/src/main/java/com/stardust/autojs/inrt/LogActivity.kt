package com.stardust.autojs.inrt

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.stardust.autojs.core.console.ConsoleImpl
import com.stardust.autojs.core.console.ConsoleView
import com.stardust.autojs.inrt.autojs.AutoJs
import com.stardust.autojs.inrt.launch.GlobalProjectLauncher
import kotlin.system.exitProcess

class LogActivity : AppCompatActivity() {

    private lateinit var mConsoleImpl: ConsoleImpl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupView()
        if (intent.getBooleanExtra(EXTRA_LAUNCH_SCRIPT, false)) {
            GlobalProjectLauncher.launch(this)
        }
    }

    private fun setupView() {
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        mConsoleImpl = AutoJs.instance.globalConsole as ConsoleImpl

        val consoleView = findViewById<ConsoleView>(R.id.console)
        consoleView.setConsole(mConsoleImpl)
        consoleView.findViewById<View>(R.id.input_container).visibility = View.GONE
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_exit -> {
                exitProcess(0)   // 完全退出程序
            }
            R.id.action_clear_log -> {
                mConsoleImpl.clear()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        // 让三个点图标（即溢出菜单）的颜色和其他图标颜色一致
        val iconColor = ContextCompat.getColor(this, android.R.color.white)
        for (i in 0 until menu.size()) {
            val menuItem = menu.getItem(i)
            val drawable = menuItem.icon
            drawable?.let {
                it.setTint(iconColor)
                menuItem.icon = it
            }
        }
        return true
    }

    companion object {
        const val EXTRA_LAUNCH_SCRIPT = "launch_script"
    }
}
