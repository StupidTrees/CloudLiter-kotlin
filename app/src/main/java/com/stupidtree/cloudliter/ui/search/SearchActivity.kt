package com.stupidtree.cloudliter.ui.search

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.data.model.UserSearched
import com.stupidtree.cloudliter.databinding.ActivitySearchBinding
import com.stupidtree.cloudliter.databinding.ActivitySearchListItemBinding
import com.stupidtree.cloudliter.ui.base.BaseActivity
import com.stupidtree.cloudliter.ui.base.BaseListAdapter
import com.stupidtree.cloudliter.ui.base.BaseListAdapter.OnItemClickListener
import com.stupidtree.cloudliter.ui.base.BaseViewHolder
import com.stupidtree.cloudliter.ui.base.DataState
import com.stupidtree.cloudliter.ui.search.SearchActivity.SListAdapter.SViewHolder
import com.stupidtree.cloudliter.utils.ActivityUtils
import com.stupidtree.cloudliter.utils.AnimationUtils
import com.stupidtree.cloudliter.utils.ImageUtils
import kotlinx.android.synthetic.main.activity_search.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * "搜索"页面Activity
 */
class SearchActivity : BaseActivity<SearchViewModel, ActivitySearchBinding>() {
    /**
     * 适配器区
     */
    var listAdapter //搜索结果列表的Adapter
            : SListAdapter? = null
    private var wordCloudListAdapter: MyWordCloudListAdapter? = null

    override fun getViewModelClass(): Class<SearchViewModel> {
        return SearchViewModel::class.java
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setToolbarActionBack(toolbar!!)
        setWindowParams(statusBar = true, darkColor = true, navi = false)

    }

    private fun onSearchModeSwitched(wordCloudSearch: Boolean) {
        if (wordCloudSearch) {
            binding.modeButton.setImageResource(R.drawable.ic_bt_wordcloud)
            binding.searchview.setHint(R.string.search_hint_word_cloud)
        } else {
            binding.modeButton.setImageResource(R.drawable.ic_bt_id)
            binding.searchview.setHint(R.string.search_hint)
        }
        AnimationUtils.rotate(binding.modeButton)
        binding.searchview.setText("")
        binding.hint.visibility = View.VISIBLE
        list.visibility = View.GONE
    }

    override fun initViews() {
        //设置列表
        listAdapter = SListAdapter(getThis(), LinkedList())
        list.adapter = listAdapter
        list.layoutManager = LinearLayoutManager(getThis())
        listAdapter!!.setOnItemClickListener(object : OnItemClickListener<UserSearched> {
            override fun onItemClick(data: UserSearched, card: View?, position: Int) {
                //点击要跳转到用户资料页
                ActivityUtils.startProfileActivity(getThis(), data.id.toString())
            }
        })

        wordCloudListAdapter = MyWordCloudListAdapter(this, ArrayList())

        binding.wordcloud.adapter = wordCloudListAdapter
        binding.wordcloud.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        //为viewModel的各种data设置监听
        viewModel.searchListStateLiveData?.observe(this, { searchListState: DataState<List<UserSearched>?> ->
            binding.loading.visibility = View.GONE
            binding.hint.visibility = View.GONE
            list.visibility = View.VISIBLE
            if (searchListState.state == DataState.STATE.SUCCESS) {
                searchListState.data.let {
                    searchListState.data?.let { listAdapter?.notifyItemChangedSmooth(it, false) }
                }
            }
            if (searchListState.state != DataState.STATE.SUCCESS) {
                Toast.makeText(applicationContext, "搜索失败！", Toast.LENGTH_SHORT).show()
            }
        })
        viewModel.searchMode.observe(this, {
            onSearchModeSwitched(it)
        })
        viewModel.myWordCloudLiveData?.observe(this, {
            if (it.state == DataState.STATE.SUCCESS && it.data != null) {
                val list = ArrayList<String>()
                for (s in it.data!!.keys) {
                    list.add(s)
                }
                wordCloudListAdapter?.notifyItemChangedSmooth(list)
                if (it.data!!.isEmpty()) {
                    binding.wordcloudLayout.visibility = View.GONE
                } else {
                    binding.wordcloudLayout.visibility = View.VISIBLE
                }

            }
        })

        //搜索输入框选择”搜索“的动作监听
        binding.searchview.setOnEditorActionListener { textView: TextView, i: Int, _: KeyEvent? ->
            if (TextUtils.isEmpty(textView.text.toString())) return@setOnEditorActionListener false
            if (i == EditorInfo.IME_ACTION_GO || i == EditorInfo.IME_ACTION_SEARCH) {
                startSearch()
                return@setOnEditorActionListener true
            }
            false
        }

        cancel.visibility = View.GONE
        cancel.setOnClickListener {
            binding.searchview.text = null
            binding.hint.visibility = View.VISIBLE
            list.visibility = View.GONE
            cancel.visibility = View.GONE
        }
        binding.searchview.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) {
                    binding.hint.visibility = View.VISIBLE
                    list.visibility = View.GONE
                    cancel.visibility = View.GONE
                } else {
                    cancel.visibility = View.VISIBLE
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })

        wordCloudListAdapter?.setOnItemClickListener(object : OnItemClickListener<String> {
            override fun onItemClick(data: String, card: View?, position: Int) {
                viewModel.setSearchMode(true)
                binding.searchview.setText(data)
                startSearch()
            }
        })

        binding.modeButton.setOnClickListener {
            viewModel.switchSearchMode()
        }
    }

    private fun startSearch() {
        // 隐藏软键盘
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(window.decorView.windowToken, 0)
        //通知ViewModel进行搜索，传入搜索框的文本作为检索语句
        viewModel.beginSearch(binding.searchview.text.toString())
        binding.loading.visibility = View.VISIBLE
    }


    override fun onStart() {
        super.onStart()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        binding.searchview.requestFocus()
        imm.showSoftInput(binding.searchview, 0)
    }

    override fun onResume() {
        super.onResume()
        viewModel.beginRefresh()

    }

    /**
     * 本页面搜索结果列表的适配器
     */
    class SListAdapter(mContext: Context, mBeans: MutableList<UserSearched>) : BaseListAdapter<UserSearched, SViewHolder>(mContext, mBeans) {

        override fun bindHolder(holder: SViewHolder, data: UserSearched?, position: Int) {
            if (data != null) {
                //显示头像
                ImageUtils.loadAvatarInto(mContext, data.avatar, holder.binding.avatar)
                //显示各种其他信息
                holder.binding.username.text = data.username
                holder.binding.nickname.text = data.nickname
                //点击事件
                holder.binding.item.setOnClickListener { view: View? ->
                    mOnItemClickListener?.onItemClick(data, view, position)
                }
            }
        }


        class SViewHolder(itemView: ActivitySearchListItemBinding) : BaseViewHolder<ActivitySearchListItemBinding>(itemView)

        override fun getViewBinding(parent: ViewGroup, viewType: Int): ViewBinding {
            return ActivitySearchListItemBinding.inflate(mInflater, parent, false)
        }

        override fun createViewHolder(viewBinding: ViewBinding, viewType: Int): SViewHolder {
            return SViewHolder(viewBinding as ActivitySearchListItemBinding)
        }

    }

    override fun initViewBinding(): ActivitySearchBinding {
        return ActivitySearchBinding.inflate(layoutInflater)
    }
}