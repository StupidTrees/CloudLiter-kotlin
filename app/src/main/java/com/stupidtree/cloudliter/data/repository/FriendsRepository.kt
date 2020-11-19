package com.stupidtree.cloudliter.data.repository

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.stupidtree.cloudliter.data.AppDatabase
import com.stupidtree.cloudliter.data.model.UserRelation
import com.stupidtree.cloudliter.data.source.websource.RelationWebSource
import com.stupidtree.cloudliter.ui.base.DataState

/**
 * 层次：Repository层
 * 好友Repository
 */
class FriendsRepository(application: Application) {
    //数据源：网络类型数据原
    private val relationWebSource: RelationWebSource = RelationWebSource.instance!!
    private val relationDao = AppDatabase.getDatabase(application).userRelationDao()

    /**
     * 获取所有好友
     * 直接调用数据源的相应函数
     * @param token 用户登陆状态token
     * @param id 用户id
     * @return 转发自数据源的LiveData
     */
    val friendsLiveData: MediatorLiveData<DataState<List<UserRelation>?>> = MediatorLiveData()
    private var friendsWebData: LiveData<DataState<List<UserRelation>?>>? = null
    private var friendsLocalData: LiveData<List<UserRelation>?>? = null
    fun actionGetFriends(token: String) {
        friendsLocalData?.let { friendsLiveData.removeSource(it) }
        friendsLocalData = relationDao.getFriends()
        friendsLiveData.addSource(friendsLocalData!!) { data ->
            data?.let {
                friendsLiveData.value = DataState(it)
            }
            friendsWebData?.let { friendsLiveData.removeSource(it) }
            friendsWebData = relationWebSource.getFriends(token)
            friendsLiveData.addSource(friendsWebData!!) { webData ->
                if (webData.state == DataState.STATE.SUCCESS&&webData.data!=null) {
                    friendsLocalData?.let { friendsLiveData.removeSource(it) }
                    //找到本地上多余的，删除
                    val redundant = mutableListOf<UserRelation>()
                    data?.let {
                        for(o in it){
                            var contains = false
                            for(d in webData.data!!){
                                if(d.friendId==o.friendId){
                                    contains = true
                                    break
                                }
                            }
                            if(!contains){
                                redundant.add(o)
                            }
                        }
                    }
                    Thread {
                        webData.data?.let {
                            relationDao.deleteRelations(redundant)
                            relationDao.saveRelations(it)
                        }
                    }.start()
                    friendsLiveData.value = webData
                }
            }
        }

    }


    companion object {
        //单例模式
        @JvmStatic
        @Volatile
        private var instance: FriendsRepository? = null

        fun getInstance(application: Application): FriendsRepository {
            if (instance == null) {
                instance = FriendsRepository(application)
            }
            return instance!!
        }

    }

}