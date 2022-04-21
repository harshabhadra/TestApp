package com.harshabhadra.testapp.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import com.harshabhadra.testapp.Loading
import com.harshabhadra.testapp.MainViewModel
import com.harshabhadra.testapp.Result
import com.harshabhadra.testapp.ViewModelFactory
import com.harshabhadra.testapp.common.BaseActivity
import com.harshabhadra.testapp.databinding.ActivityMainBinding


class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var entryListAdapter: EntryListAdapter

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val viewModelFactory = ViewModelFactory(this, apiService)
        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java].apply {
            getEntries()
        }

        //Adding divider to recyclerView
        val dividerItemDecoration = DividerItemDecoration(
            binding.entryRecyclerView.context,
            DividerItemDecoration.VERTICAL
        )
        binding.entryRecyclerView.addItemDecoration(dividerItemDecoration)

        //Setting up adapter
        entryListAdapter = EntryListAdapter()
        binding.entryRecyclerView.adapter = entryListAdapter

        registerObservers()
        binding.fab.setOnClickListener {
            startActivity(Intent(this, LocationActivity::class.java))
        }
    }

    private fun registerObservers() {
        viewModel.entriesLiveData.observe(this) {
            it?.let {
                when (it) {
                    is Result.Success -> {
                        Log.e(TAG, "no. of entries: ${it.entries.size}")
                        if (it.entries.isNotEmpty()) entryListAdapter.submitList(it.entries)
                    }
                    else -> {
                        showErrorDialog()
                    }
                }

            }
        }

        viewModel.loadingLiveData.observe(this) {
            it?.let {
                binding.loading.isVisible = it == Loading.LOADING
            }
        }
    }

    private fun showErrorDialog() {
        Toast.makeText(this, "Failed to load data", Toast.LENGTH_SHORT).show()
    }
}