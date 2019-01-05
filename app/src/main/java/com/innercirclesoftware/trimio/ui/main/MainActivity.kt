package com.innercirclesoftware.trimio.ui.main

import android.content.Intent
import android.os.Bundle
import android.text.format.Formatter
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.transition.TransitionManager
import butterknife.OnClick
import com.innercirclesoftware.trimio.R
import com.innercirclesoftware.trimio.settings.SettingsActivity
import com.innercirclesoftware.trimio.trim.TrimResult
import com.innercirclesoftware.trimio.ui.base.BaseActivity
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : BaseActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: MainViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(MainViewModel::class.java)
    }

    override val layout: Int = R.layout.activity_main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initToolbar()
        observeViewModel()
    }

    private fun initToolbar() {
        toolbar.inflateMenu(R.menu.menu_main)
        toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.menu_item_settings) {
                Intent(this, SettingsActivity::class.java).also { startActivity(it) }
                return@setOnMenuItemClickListener true
            }
            false
        }
    }

    private fun observeViewModel() {
        observeCacheState()
        observeDataState()
        observeSystemState()
        observeTrimState()
    }

    private fun observeCacheState() {
        viewModel.cacheState.observe(this, Observer {
            onPartitionStateChanged(it, cache_summary, cache_progress_bar)
        })
    }

    private fun observeDataState() {
        viewModel.dataState.observe(this, Observer {
            onPartitionStateChanged(it, data_summary, data_progress_bar)
        })
    }

    private fun observeSystemState() {
        viewModel.systemState.observe(this, Observer {
            onPartitionStateChanged(it, system_summary, system_progress_bar)
        })
    }

    private fun onPartitionStateChanged(status: TrimStatus, summary: TextView, progressBar: ProgressBar) {
        TransitionManager.beginDelayedTransition(constraintLayout)
        when (status) {
            is TrimStatus.Completed -> {
                summary.text = when (status.result) {
                    is TrimResult.Success -> getString(R.string.main_trim_successful, Formatter.formatShortFileSize(this, status.result.trimmedBytes))
                    is TrimResult.Failure -> status.result.throwable.message
                }

                progressBar.visibility = View.GONE
                summary.visibility = View.VISIBLE
            }
            is TrimStatus.Trimming -> {
                progressBar.visibility = View.VISIBLE
                summary.visibility = View.GONE
            }
            is TrimStatus.Sleeping -> {
                progressBar.visibility = View.GONE
                summary.visibility = View.GONE
            }
        }
    }

    private fun observeTrimState() {
        viewModel.trimming.observe(this, Observer { trimming ->
            trim_btn.isEnabled = trimming.not()
        })
    }

    override fun inject() {
        DaggerMainActivityComponent.builder()
            .activityComponent(activityComponent)
            .mainActivityModule(MainActivityModule(this))
            .build()
            .inject(this)
    }

    @OnClick(R.id.trim_btn)
    fun onTrimClicked() {
        viewModel.trim(
            TrimRequest(
                trimCache = cache.isChecked,
                trimData = data.isChecked,
                trimSystem = system.isChecked
            )
        )
    }
}
