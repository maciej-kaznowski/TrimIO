package com.innercirclesoftware.trimio.ui.main

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import butterknife.OnClick
import com.innercirclesoftware.trimio.R
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
        observeViewModel()
    }

    private fun observeViewModel() {
        observeCacheState()
        observeDataState()
        observeSystemState()
        observeTrimState()
    }

    private fun observeCacheState() {
        viewModel.cacheState.observe(this, Observer {
            Toast.makeText(this, "Cache: $it", Toast.LENGTH_SHORT).show()
        })
    }

    private fun observeDataState() {
        viewModel.dataState.observe(this, Observer {
            Toast.makeText(this, "Data: $it", Toast.LENGTH_SHORT).show()
        })
    }

    private fun observeSystemState() {
        viewModel.systemState.observe(this, Observer {
            Toast.makeText(this, "System: $it", Toast.LENGTH_SHORT).show()
        })
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
