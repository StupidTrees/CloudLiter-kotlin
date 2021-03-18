package com.stupidtree.cloudliter.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.stupidtree.cloudliter.data.model.*
import com.stupidtree.cloudliter.data.model.Conversation.Companion.fromNewMessage
import com.stupidtree.cloudliter.data.model.Conversation.Companion.fromUserRelationAndProfile
import com.stupidtree.cloudliter.ui.accessibility.AccessibilityActivity
import com.stupidtree.cloudliter.ui.chat.ChatActivity
import com.stupidtree.cloudliter.ui.conversation.ConversationActivity
import com.stupidtree.cloudliter.ui.face.MyFaceActivity
import com.stupidtree.cloudliter.ui.gallery.ScenesActivity
import com.stupidtree.cloudliter.ui.gallery.album.AlbumActivity
import com.stupidtree.cloudliter.ui.group.GroupEditorActivity
import com.stupidtree.cloudliter.ui.imagedetect.ImageDetectActivity
import com.stupidtree.cloudliter.ui.main.MainActivity
import com.stupidtree.cloudliter.ui.myprofile.MyProfileActivity
import com.stupidtree.cloudliter.ui.profile.ProfileActivity
import com.stupidtree.cloudliter.ui.qrcode.QRCodeActivity
import com.stupidtree.cloudliter.ui.relationevent.RelationEventActivity
import com.stupidtree.cloudliter.ui.search.SearchActivity
import com.stupidtree.cloudliter.ui.welcome.WelcomeActivity
import com.stupidtree.cloudliter.ui.wordcloud.WordCloudActivity
import com.stupidtree.cloudliter.ui.widgets.PhotoDetailActivity

/**
 * 此类整合了跳转到各个Activity的入口
 */
object ActivityUtils {
    /**
     * 启动欢迎页
     * @param from 上下文
     */
    fun startLoginActivity(from: Context) {
        val i = Intent(from, WelcomeActivity::class.java)
        from.startActivity(i)
    }

    /**
     * 启动搜索页
     * @param from 上下文
     */
    fun startSearchActivity(from: Context) {
        val i = Intent(from, SearchActivity::class.java)
        from.startActivity(i)
    }

    /**
     * 启动用户资料页（他人）
     * @param from 上下文
     * @param id 用户id
     */
    fun startProfileActivity(from: Context, id: String) {
        val i = Intent(from, ProfileActivity::class.java)
        i.putExtra("id", id)
        i.putExtra("showLogout",from is MainActivity)
        from.startActivity(i)
    }

    /**
     * 启动我的资料页
     * @param from 上下文
     */
    fun startMyProfileActivity(from: Context) {
        val i = Intent(from, MyProfileActivity::class.java)
        from.startActivity(i)
    }

    /**
     * 启动聊天Activity
     * @param from 调用者
     * @param friendProfile 朋友资料对象
     */
    fun startChatActivity(from: Context, friendProfile: UserProfile, userRelation: UserRelation, userLocal: UserLocal) {
        val i = Intent(from, ChatActivity::class.java)
        val b = Bundle()
        b.putSerializable("conversation", fromUserRelationAndProfile(friendProfile, userRelation, userLocal))
        i.putExtras(b)
        from.startActivity(i)
    }

    fun startChatActivity(from: Context, conversation: Conversation) {
        val i = Intent(from, ChatActivity::class.java)
        val b = Bundle()
        b.putSerializable("conversation", conversation)
        i.putExtras(b)
        from.startActivity(i)
    }

    fun getIntentForChatActivity(from: Context, message: ChatMessage): Intent {
        val conversation = fromNewMessage(message)
        val i = Intent(from, ChatActivity::class.java)
        val b = Bundle()
        b.putSerializable("conversation", conversation)
        i.putExtras(b)
        return i
    }

    /**
     * 启动对话设置页面
     * @param from 上下文
     * @param friendId 朋友id
     */
    fun startConversationActivity(from: Context, friendId: String) {
        val i = Intent(from, ConversationActivity::class.java)
        i.putExtra("friendId", friendId)
        from.startActivity(i)
    }

    /**
     * 启动好友关系事件页面
     * @param from 上下文
     */
    fun startRelationEventActivity(from: Context) {
        val i = Intent(from, RelationEventActivity::class.java)
        from.startActivity(i)
    }

    /**
     * 启动分组管理
     * @param from 上下文
     */
    fun startGroupEditorActivity(from: Context) {
        val i = Intent(from, GroupEditorActivity::class.java)
        from.startActivity(i)
    }

    /**
     * 圖片識別
     * @param from 上下文
     * @param imageId 图片id
     */
    fun startImageDetectionActivity(from: Context, imageId: String) {
        val it = Intent(from, ImageDetectActivity::class.java)
        it.putExtra("id", imageId)
        from.startActivity(it)
    }

    /**
     * 显示多张大图
     * @param from 上下文
     * @param urls 图片链接
     * @param index 初始显示的下必
     */
    fun showMultipleImages(from: Activity, urls: List<String?>, index: Int) {
        val it = Intent(from, PhotoDetailActivity::class.java)
        //  ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(from,view,"image");
        val urlsArr = arrayOfNulls<String>(urls.size)
        for (i in urlsArr.indices) urlsArr[i] = urls[i]
        it.putExtra("urls", urlsArr)
        it.putExtra("init_index", index)
        from.startActivity(it) //,activityOptionsCompat.toBundle());
    }

    /**
     * 进入二维码页面
     */
    fun startQRCodeActivity(from: Context) {
        val it = Intent(from, QRCodeActivity::class.java)
        from.startActivity(it)
    }


    /**
     * 进入词云详情
     */
    fun startWordCloudActivity(from:Context){
        val it = Intent(from, WordCloudActivity::class.java)
        from.startActivity(it)
    }

    /**
     * 进入人脸管理
     */
    fun startMyFaceActivity(from:Context){
        val it = Intent(from,MyFaceActivity::class.java)
        from.startActivity(it)
    }

    /**
     * 进入类别
     */
    fun startGalleryActivity(from: Context){
        val it = Intent(from,ScenesActivity::class.java)
        from.startActivity(it)
    }
    /**
     * 进入类别相册
     */
    fun startAlbumActivity(from: Context,key:String){
        val it = Intent(from,AlbumActivity::class.java)
        it.putExtra("key",key)
        from.startActivity(it)
    }

    /**
     * 进入无障碍管理页面
     */
    fun startAccessibilityActivity(from: Context){
        val it = Intent(from,AccessibilityActivity::class.java)
        from.startActivity(it)
    }
}