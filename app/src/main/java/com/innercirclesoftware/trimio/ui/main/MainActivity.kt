package com.innercirclesoftware.trimio.ui.main

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.innercirclesoftware.trimio.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        viewModel.getTrimStatus().observe(this, Observer {
            when (it) {
                is TrimStatus.Trimming -> Toast.makeText(this, "Trimming ${it.current}", Toast.LENGTH_LONG).show()
                is TrimStatus.Sleeping -> Toast.makeText(this, "Not trimming", Toast.LENGTH_LONG).show()
            }
        })
    }
}
