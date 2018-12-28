package com.innercirclesoftware.trimio.ui.main

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import butterknife.BindView
import butterknife.OnClick
import com.innercirclesoftware.trimio.R
import com.innercirclesoftware.trimio.ui.base.BaseActivity
import javax.inject.Inject

class MainActivity : BaseActivity() {

    @BindView(R.id.cache)
    lateinit var cache: CheckBox

    @BindView(R.id.data)
    lateinit var data: CheckBox

    @BindView(R.id.system)
    lateinit var system: CheckBox

    @BindView(R.id.trim_btn)
    lateinit var trim: Button

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    lateinit var viewModel: MainViewModel

    override val layout: Int = R.layout.activity_main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MainViewModel::class.java)
        viewModel.getTrimStatus().observe(this, Observer {
            trim.isEnabled = it is TrimStatus.Sleeping

            when (it) {
                is TrimStatus.Trimming -> Toast.makeText(this, "Trimming ${it.current}", Toast.LENGTH_LONG).show()
                is TrimStatus.Sleeping -> Toast.makeText(this, "Not trimming", Toast.LENGTH_LONG).show()
            }
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
