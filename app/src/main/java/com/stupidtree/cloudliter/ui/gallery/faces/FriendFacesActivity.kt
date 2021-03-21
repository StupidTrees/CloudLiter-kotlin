package com.stupidtree.cloudliter.ui.gallery.faces

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.stupidtree.cloudliter.databinding.ActivityFriendFacesBinding
import com.stupidtree.cloudliter.databinding.ActivityScenesBinding
import com.stupidtree.cloudliter.ui.gallery.album.AlbumQuery
import com.stupidtree.cloudliter.utils.ActivityUtils
import com.stupidtree.component.data.DataState
import com.stupidtree.style.base.BaseActivity
import com.stupidtree.style.base.BaseListAdapter

class FriendFacesActivity: BaseActivity<FriendFacesViewModel, ActivityFriendFacesBinding>() {

    lateinit var listAdapter: FriendFacesListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setWindowParams(statusBar = true, darkColor = true, navi = false)
        setToolbarActionBack(binding.toolbar)
    }
    override fun onStart() {
        super.onStart()
        refresh()
    }
    override fun initViewBinding(): ActivityFriendFacesBinding {
        return ActivityFriendFacesBinding.inflate(layoutInflater)
    }

    override fun getViewModelClass(): Class<FriendFacesViewModel> {
        return FriendFacesViewModel::class.java
    }

    override fun initViews() {
        listAdapter = FriendFacesListAdapter(this, mutableListOf())
        listAdapter.setOnItemClickListener(object :BaseListAdapter.OnItemClickListener<FriendFaceEntity>{
            override fun onItemClick(data: FriendFaceEntity, card: View?, position: Int) {
                ActivityUtils.startAlbumActivity(getThis(),AlbumQuery.QType.FRIEND,data.userId,data.userName)
            }

        })
        binding.list.adapter = listAdapter
        binding.list.layoutManager = GridLayoutManager(this,3)
        binding.refresh.setOnRefreshListener {
            refresh()
        }
        binding.refresh.setColorSchemeColors(getColorPrimary())
        viewModel.imagesLiveData.observe(this){
            binding.refresh.isRefreshing = false
            if(it.state== DataState.STATE.SUCCESS){
                listAdapter.notifyItemChangedSmooth(it.data!!,false)
            }

        }
    }

    fun refresh(){
        viewModel.startRefresh()
    }

}