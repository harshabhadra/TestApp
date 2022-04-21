package com.harshabhadra.testapp.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import com.harshabhadra.testapp.MainViewModel
import com.harshabhadra.testapp.Repository
import com.harshabhadra.testapp.ViewModelFactory
import com.harshabhadra.testapp.common.BaseActivity
import com.harshabhadra.testapp.database.EntryDatabase
import com.harshabhadra.testapp.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch


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

        val database = EntryDatabase.getDatabase(this)
        val viewModelFactory =
            ViewModelFactory(this, Repository(apiService, database))
        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]

        //Adding divider to recyclerView
        val dividerItemDecoration = DividerItemDecoration(
            binding.entryRecyclerView.context,
            DividerItemDecoration.VERTICAL
        )
        binding.entryRecyclerView.addItemDecoration(dividerItemDecoration)

        setupAdapter()
        registerObservers()
        initEntry()

        binding.fab.setOnClickListener {
            startActivity(Intent(this, LocationActivity::class.java))
        }
    }

    private fun setupAdapter() {
        //Setting up adapter
        entryListAdapter = EntryListAdapter()
        binding.entryRecyclerView.adapter = entryListAdapter
        entryListAdapter.addLoadStateListener { loadState ->
            val isEmptyList =
                loadState.refresh is LoadState.NotLoading && entryListAdapter.itemCount == 0
            binding.emptyTextView.isVisible = isEmptyList
            binding.loading.isVisible = loadState.refresh is LoadState.Loading
        }
    }

    private fun registerObservers() {
        lifecycleScope.launch {
            viewModel.getEntries().collectLatest {
                entryListAdapter.submitData(it)
            }
        }
    }

    private fun initEntry() {
        lifecycleScope.launch {
            entryListAdapter.loadStateFlow
                .distinctUntilChangedBy { it.refresh }
                .filter { it.refresh is LoadState.NotLoading }
                .collect {
                    val snapShot = entryListAdapter.snapshot()
                    Log.e(TAG, "no. if items: ${snapShot.items.size}")
                    if (snapShot.items.isNotEmpty()) binding.entryRecyclerView.scrollToPosition(0)
                }
        }
    }
}