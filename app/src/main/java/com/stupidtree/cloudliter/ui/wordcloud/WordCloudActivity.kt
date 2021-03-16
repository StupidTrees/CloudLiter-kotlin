package com.stupidtree.cloudliter.ui.wordcloud

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.databinding.ActivityWordCloudBinding
import com.stupidtree.cloudliter.ui.base.BaseActivity
import com.stupidtree.cloudliter.ui.base.BaseListAdapter
import com.stupidtree.cloudliter.ui.base.DataState
import com.stupidtree.cloudliter.ui.profile.WordCloudListAdapter
import com.stupidtree.cloudliter.ui.widgets.PopUpText

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
        listAdapter?.setOnItemClickListener(object : BaseListAdapter.OnItemClickListener<Pair<String, Float?>> {
            override fun onItemClick(data: Pair<String, Float?>, card: View?, position: Int) {
                PopUpText().setTitle(R.string.ensure_delete_word_cloud)
                        .setOnConfirmListener(object : PopUpText.OnConfirmListener {
                            override fun OnConfirm() {
                                data.let { viewModel.deleteWordCloud(it.first) }//这里使用删除函数（viewmodel中的）
                            }
                        }).show(supportFragmentManager, "delete")
            }

        })

        viewModel.deleteResult.observe(this) {
            if (it.state == DataState.STATE.SUCCESS) {
                Toast.makeText(getThis(), R.string.delete_word_cloud_ok, Toast.LENGTH_SHORT).show()
                viewModel.startRefresh()
            } else {
                Toast.makeText(getThis(), R.string.fail, Toast.LENGTH_SHORT).show()
            }
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