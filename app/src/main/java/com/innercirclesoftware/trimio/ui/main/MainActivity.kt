package com.innercirclesoftware.trimio.ui.main

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Observer
import com.innercirclesoftware.trimio.R
import com.innercirclesoftware.trimio.ui.base.BaseActivity
import javax.inject.Inject

class MainActivity : BaseActivity() {

    @Inject
    lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel.getTrimStatus().observe(this, Observer {
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
}
