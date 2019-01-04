package com.innercirclesoftware.trimio.settings

import android.content.Intent
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import butterknife.OnClick
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItems
import com.afollestad.materialdialogs.list.listItemsMultiChoice
import com.innercirclesoftware.trimio.R
import com.innercirclesoftware.trimio.trim.Partition
import com.innercirclesoftware.trimio.trim.periodic.Frequency
import com.innercirclesoftware.trimio.trim.toPartition
import com.innercirclesoftware.trimio.ui.base.BaseActivity
import kotlinx.android.synthetic.main.activity_settings.*
import javax.inject.Inject

class SettingsActivity : BaseActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(SettingsViewModel::class.java)
    }

    override val layout: Int = R.layout.activity_settings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initToolbar()
        observeViewModel()
    }

    private fun initToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.navigationIcon?.setColorFilter(
            ContextCompat.getColor(this, R.color.colorPrimary),
            PorterDuff.Mode.SRC_ATOP
        )
        toolbar.setNavigationOnClickListener { supportFinishAfterTransition() }
    }

    private fun observeViewModel() {
        observeFrequency()
        observePartitions()
    }

    private fun observeFrequency() {
        viewModel.frequency.observe(this, Observer { it ->
            partitions.isEnabled = it != Frequency.NEVER
            frequency_summary.text = when (it!!) {
                //TODO localize
                Frequency.NEVER -> "Never"
                Frequency.DAILY -> "Daily"
                Frequency.WEEKLY -> "Weekly"
                Frequency.FORTNIGHTLY -> "Fortnightly"
                Frequency.MONTHLY -> "Monthly"
            }
        })
    }

    private fun observePartitions() {
        viewModel.partitions.observe(this, Observer { partitions ->
            partitions_summary.text = partitions.joinToString(separator = ", ") { it.directory }
        })
    }

    override fun inject() {
        DaggerSettingsActivityComponent.builder()
            .settingsActivityModule(SettingsActivityModule(this))
            .activityComponent(activityComponent)
            .build()
            .inject(this)
    }

    @OnClick(R.id.github)
    fun onGitHubClicked() {
        val githubUrl = getString(R.string.github_url)
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(githubUrl))
        startActivity(browserIntent)
    }

    @OnClick(R.id.frequency)
    fun onFrequencyClicked() {
        MaterialDialog(this).show {
            title(
                res = R.string.periodic_trim_frequency_dialog_title
            )
            listItems(
                res = R.array.periodic_trim_frequencies,
                waitForPositiveButton = false,
                selection = { dialog, index, _ ->
                    val selectedFrequency = when (index) {
                        0 -> Frequency.NEVER
                        1 -> Frequency.DAILY
                        2 -> Frequency.WEEKLY
                        3 -> Frequency.FORTNIGHTLY
                        4 -> Frequency.MONTHLY
                        else -> throw IllegalArgumentException("Unexpected frequency index=$index")
                    }

                    viewModel.onFrequencySelected(selectedFrequency)
                    dialog.dismiss()
                }
            )
            negativeButton(
                res = R.string.btn_cancel
            )
        }
    }

    @OnClick(R.id.partitions)
    fun onPartitionsClicked() {
        MaterialDialog(this).show {
            title(
                res = R.string.periodic_trim_partitions_dialog_title
            )
            listItemsMultiChoice(
                items = listOf(Partition.Data, Partition.Cache, Partition.System).map { it.directory },
                waitForPositiveButton = true,
                allowEmptySelection = false,
                selection = { dialog: MaterialDialog, _: IntArray, items: List<String> ->
                    val partitions = items.map { it.toPartition()!! }

                    viewModel.onPartitionsSelected(partitions)
                    dialog.dismiss()
                }
            )
            negativeButton(
                res = R.string.btn_cancel
            )
            positiveButton(
                res = R.string.btn_select
            )
        }
    }
}