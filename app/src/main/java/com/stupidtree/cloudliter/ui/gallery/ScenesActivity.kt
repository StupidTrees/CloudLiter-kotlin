package com.stupidtree.cloudliter.ui.gallery

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.stupidtree.cloudliter.databinding.ActivityScenesBinding
import com.stupidtree.cloudliter.ui.base.BaseActivity
import com.stupidtree.cloudliter.ui.base.BaseListAdapter
import com.stupidtree.cloudliter.ui.base.DataState
import com.stupidtree.cloudliter.utils.ActivityUtils

class ScenesActivity:BaseActivity<ScenesViewModel,ActivityScenesBinding>() {

    lateinit var listAdapter:ScenesListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setWindowParams(statusBar = true, darkColor = true, navi = false)
        setToolbarActionBack(binding.toolbar)
    }
    override fun onStart() {
        super.onStart()
        refresh()
    }
    override fun initViewBinding(): ActivityScenesBinding {
        return ActivityScenesBinding.inflate(layoutInflater)
    }

    override fun getViewModelClass(): Class<ScenesViewModel> {
        return ScenesViewModel::class.java
    }

    override fun initViews() {
        listAdapter = ScenesListAdapter(this, mutableListOf())
        listAdapter.setOnItemClickListener(object :BaseListAdapter.OnItemClickListener<SceneEntity>{
            override fun onItemClick(data: SceneEntity, card: View?, position: Int) {
                ActivityUtils.startAlbumActivity(getThis(),data.key)
            }

        })
        binding.list.adapter = listAdapter
        binding.list.layoutManager = LinearLayoutManager(this)
        binding.refresh.setOnRefreshListener {
            refresh()
        }
        binding.refresh.setColorSchemeColors(getColorPrimary())
        viewModel.imagesLiveData.observe(this){
            binding.refresh.isRefreshing = false
            if(it.state==DataState.STATE.SUCCESS){
                listAdapter.notifyItemChangedSmooth(it.data!!,false)
            }

        }
    }

    fun refresh(){
        viewModel.startRefresh()
    }

}