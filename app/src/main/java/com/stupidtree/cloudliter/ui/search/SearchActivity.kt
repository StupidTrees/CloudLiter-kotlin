package com.stupidtree.cloudliter.ui.search

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.data.model.UserSearched
import com.stupidtree.cloudliter.ui.base.BaseActivity
import com.stupidtree.cloudliter.ui.base.BaseListAdapter
import com.stupidtree.cloudliter.ui.base.BaseListAdapter.OnItemClickListener
import com.stupidtree.cloudliter.ui.base.BaseViewHolder
import com.stupidtree.cloudliter.ui.base.DataState
import com.stupidtree.cloudliter.ui.search.SearchActivity.SListAdapter.SViewHolder
import com.stupidtree.cloudliter.utils.ActivityUtils
import com.stupidtree.cloudliter.utils.ImageUtils
import java.util.*

/**
 * "搜索"页面Activity
 */
@SuppressLint("NonConstantResourceId")
class SearchActivity : BaseActivity<SearchViewModel>() {
    /**
     * View绑定区
     */
    @JvmField
    @BindView(R.id.toolbar)
    var toolbar: Toolbar? = null

    @JvmField
    @BindView(R.id.searchview)
    var searchText: EditText? = null

    @JvmField
    @BindView(R.id.loading)
    var progressLoading: ProgressBar? = null

    @JvmField
    @BindView(R.id.list)
    var list: RecyclerView? = null

    /**
     * 适配器区
     */
    var listAdapter //搜索结果列表的Adapter
            : SListAdapter? = null

    override fun getViewModelClass(): Class<SearchViewModel>? {
        return SearchViewModel::class.java
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setToolbarActionBack(toolbar!!)
        setWindowParams(true, true, false)
    }

    override fun initViews() {
        //设置列表
        listAdapter = SListAdapter(getThis(), LinkedList())
        list!!.adapter = listAdapter
        list!!.layoutManager = LinearLayoutManager(getThis())
        listAdapter!!.setOnItemClickListener(object : OnItemClickListener<UserSearched> {
            override fun onItemClick(data: UserSearched, card: View?, position: Int) {
                //点击要跳转到用户资料页
                ActivityUtils.startProfileActivity(getThis(), data.id.toString())
            }
        })

        //为viewModel的各种data设置监听
        viewModel!!.searchListStateLiveData?.observe(this, Observer { searchListState: DataState<List<UserSearched>?> ->
            Log.e("refresh", searchListState.data.toString())
            progressLoading!!.visibility = View.GONE
            if (searchListState.state == DataState.STATE.SUCCESS) {
                searchListState.data.let{
                    searchListState.data?.let { listAdapter?.notifyItemChangedSmooth(it,false) }
                }

            }
            if (searchListState.state != DataState.STATE.SUCCESS) {
                Toast.makeText(applicationContext, "搜索失败！", Toast.LENGTH_SHORT).show()
            }
        })

        //搜索输入框选择”搜索“的动作监听
        searchText!!.setOnEditorActionListener { textView: TextView, i: Int, _: KeyEvent? ->
            if (TextUtils.isEmpty(textView.text.toString())) return@setOnEditorActionListener false
            if (i == EditorInfo.IME_ACTION_GO || i == EditorInfo.IME_ACTION_SEARCH) {
                // 隐藏软键盘
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(window.decorView.windowToken, 0)
                //通知ViewModel进行搜索，传入搜索框的文本作为检索语句
                viewModel?.beginSearch(searchText!!.text.toString())
                progressLoading!!.visibility = View.VISIBLE
                return@setOnEditorActionListener true
            }
            false
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_search
    }

    /**
     * 本页面搜索结果列表的适配器
     */
    class SListAdapter(mContext: Context, mBeans: MutableList<UserSearched>) : BaseListAdapter<UserSearched, SViewHolder>(mContext, mBeans) {
        override fun getLayoutId(viewType: Int): Int {
            return R.layout.activity_search_list_item
        }

        override fun bindHolder(holder: SViewHolder, data: UserSearched?, position: Int) {
            if (data != null) {
                //显示头像
                ImageUtils.loadAvatarInto(mContext, data.avatar, holder.avatar!!)
                //显示各种其他信息
                holder.username!!.text = data.username
                holder.nickname!!.text = data.nickname
                //点击事件
                holder.item!!.setOnClickListener { view: View? ->
                    mOnItemClickListener?.onItemClick(data,view,position)
                }
            }
        }

        override fun createViewHolder(v: View, viewType: Int): SViewHolder {
            return SViewHolder(v)
        }

        class SViewHolder(itemView: View) : BaseViewHolder(itemView) {
            @JvmField
            @BindView(R.id.nickname)
            var nickname: TextView? = null

            @JvmField
            @BindView(R.id.username)
            var username: TextView? = null

            @JvmField
            @BindView(R.id.item)
            var item: ViewGroup? = null

            @JvmField
            @BindView(R.id.avatar)
            var avatar: ImageView? = null
        }




    }
}