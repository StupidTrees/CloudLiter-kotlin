package com.stupidtree.cloudliter.ui.gallery.album

import android.nfc.tech.MifareUltralight.PAGE_SIZE
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.stupidtree.cloudliter.databinding.ActivityAlbumBinding
import com.stupidtree.style.base.BaseActivity
import com.stupidtree.style.base.BaseListAdapter
import com.stupidtree.component.data.DataState
import com.stupidtree.cloudliter.utils.ActivityUtils
import com.stupidtree.cloudliter.utils.PlacesUtils

class AlbumActivity : BaseActivity<AlbumViewModel, ActivityAlbumBinding>() {

    lateinit var listAdapter: AlbumListAdapter
    override fun initViewBinding(): ActivityAlbumBinding {
        return ActivityAlbumBinding.inflate(layoutInflater)
    }

    override fun getViewModelClass(): Class<AlbumViewModel> {
        return AlbumViewModel::class.java
    }

    private fun refreshAll() {
        binding.refresh.isRefreshing = true
        intent.getStringExtra("key")?.let { key ->
            intent.getStringExtra("mode")?.let {
                val type = AlbumQuery.QType.valueOf(it)
                if(type==AlbumQuery.QType.SCENE){
                    binding.collapse.title = PlacesUtils.getNameForSceneKey(this, key)
                }
                viewModel.refreshAll(type, key)
            }

        }
    }

    fun nextPage() {
        binding.refresh.isRefreshing = true
        viewModel.nextPage()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent.getStringExtra("title")?.let {
            if (it.isNotEmpty()) {
                binding.toolbar.title = it
                binding.collapse.title = it
            }
        }
        setToolbarActionBack(binding.toolbar)
    }


    var firstStart = true
    override fun onStart() {
        super.onStart()
        if (firstStart) {
            refreshAll()
            firstStart = false
        }

    }

    override fun initViews() {
        listAdapter = AlbumListAdapter(this, mutableListOf())
        listAdapter.setOnItemClickListener(object : BaseListAdapter.OnItemClickListener<String> {
            override fun onItemClick(data: String, card: View?, position: Int) {
                ActivityUtils.showMultipleImages(getThis(),listAdapter.beans,position)
            }

        })
        binding.list.adapter = listAdapter
        binding.list.layoutManager = GridLayoutManager(this, 3)
        binding.refresh.setOnRefreshListener {
            refreshAll()
        }
        binding.list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (!binding.list.canScrollVertically(1) && listAdapter.itemCount >= PAGE_SIZE) {
                        nextPage()
                    }
                }
            }
        })
        viewModel.imagesLiveData.observe(this) {
            binding.refresh.isRefreshing = false
            if (it.state == DataState.STATE.SUCCESS) {
                Log.e("state", it.toString())
                if (it.listAction == DataState.LIST_ACTION.APPEND) {
                    listAdapter.notifyItemsAppended(it.data!!)
                } else {
                    listAdapter.notifyItemChangedSmooth(it.data!!)
                }
            }
        }

    }
}