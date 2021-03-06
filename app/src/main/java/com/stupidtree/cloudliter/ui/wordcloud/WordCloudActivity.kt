package com.stupidtree.cloudliter.ui.wordcloud

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.stupidtree.cloudliter.databinding.ActivityWordCloudBinding
import com.stupidtree.cloudliter.ui.base.BaseActivity
import com.stupidtree.cloudliter.ui.base.DataState
import com.stupidtree.cloudliter.ui.profile.WordCloudListAdapter

class WordCloudActivity : BaseActivity<WordCloudViewModel, ActivityWordCloudBinding>() {

    var listAdapter: WordCloudListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setWindowParams(statusBar = true, darkColor = true, navi = false)
        setToolbarActionBack(binding.toolbar)
    }

    override fun initViewBinding(): ActivityWordCloudBinding {
        return ActivityWordCloudBinding.inflate(layoutInflater)
    }

    override fun getViewModelClass(): Class<WordCloudViewModel> {
        return WordCloudViewModel::class.java
    }

    override fun initViews() {
        listAdapter = WordCloudListAdapter(this, mutableListOf())
        binding.list.adapter = listAdapter
        binding.list.layoutManager = LinearLayoutManager(this)
        binding.refresh.setColorSchemeColors(getColorPrimary())
        viewModel.wordCloudLiveData.observe(this) {
            binding.refresh.isRefreshing = false
            if (it.state == DataState.STATE.SUCCESS) {
                val newList = mutableListOf<Pair<String, Float?>>()
                for (key in it.data!!.keys) {
                    newList.add(Pair(key, it.data!![key]))
                }
                listAdapter?.notifyItemChangedSmooth(newList)
            }
        }
        binding.refresh.setOnRefreshListener {
            refresh()
        }
    }

    fun refresh(){
        viewModel.startRefresh()
        binding.refresh.isRefreshing = true
    }
    override fun onStart() {
        super.onStart()
        refresh()
    }
}