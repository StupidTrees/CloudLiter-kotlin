package com.stupidtree.hichat.ui.main.conversations;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.stupidtree.hichat.R;
import com.stupidtree.hichat.data.model.Conversation;
import com.stupidtree.hichat.ui.base.BaseFragment;
import com.stupidtree.hichat.ui.base.BaseListAdapter;
import com.stupidtree.hichat.ui.base.BaseViewHolder;
import com.stupidtree.hichat.ui.base.DataState;
import com.stupidtree.hichat.utils.ActivityUtils;
import com.stupidtree.hichat.utils.ImageUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;


/**
 * “消息”页面
 */
public class ConversationsFragment extends BaseFragment<ConversationsViewModel> {

    /**
     * View绑定区
     */
    @BindView(R.id.list)
    RecyclerView list;

    /**
     * 适配器区
     */
    CAdapter listAdapter;




    public ConversationsFragment() {
    }


    public static ConversationsFragment newInstance() {
        return new ConversationsFragment();
    }

    @Override
    protected Class<ConversationsViewModel> getViewModelClass() {
        return ConversationsViewModel.class;
    }




    @Override
    protected void initViews(View view) {
        listAdapter = new CAdapter(getContext(), new LinkedList<>());
        list.setAdapter(listAdapter);
        list.setLayoutManager(new LinearLayoutManager(getContext()));
        listAdapter.setOnItemClickListener((data, card, position) -> ActivityUtils.startChatActivity(requireContext(), data));
        viewModel.getListData().observe(this, listDataState -> {
            if (listDataState.getState() == DataState.STATE.SUCCESS) {
                List<Conversation> list = listDataState.getData();
                Collections.sort(list, (conversation, t1) -> t1.getUpdatedAt().compareTo(conversation.getUpdatedAt()));
                listAdapter.notifyItemChangedSmooth(list);
            } else {
                Toast.makeText(getContext(), "失败", Toast.LENGTH_SHORT).show();
            }

        });
        viewModel.getUnreadMessageState().observe(this, listDataState -> viewModel.startRefresh());
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_conversations;
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.startRefresh();
    }

    @Override
    public void onStart() {
        super.onStart();
        viewModel.bindService(getActivity());
        viewModel.callOnline();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        viewModel.unbindService(getActivity());
    }

    class CAdapter extends BaseListAdapter<Conversation, CAdapter.CHolder> {
        /**
         * 缓存每个对话的未读状态
         */
        HashMap<String,Integer> unreadMap;

        public CAdapter(Context mContext, List<Conversation> mBeans) {
            super(mContext, mBeans);
            unreadMap = new HashMap<>();
        }

        @Override
        protected int getLayoutId(int viewType) {
            return R.layout.fragment_conversations_list_item;
        }

        @Override
        public CHolder createViewHolder(View v, int viewType) {
            return new CHolder(v);
        }

        @Override
        protected void bindHolder(@NonNull CHolder holder, @Nullable Conversation data, int position) {
            if (data != null) {
                ImageUtils.loadAvatarInto(mContext, data.getFriendAvatar(), holder.avatar);
                holder.lastMessage.setText(data.getLastMessage());
                holder.name.setText(data.getFriendNickname());
                int unread = viewModel.getUnreadNumber(data);
                unreadMap.put(data.getId(),unread);
                if (unread > 0) {
                    holder.unread.setVisibility(View.VISIBLE);
                    holder.unread.setText(String.valueOf(unread));
                } else {
                    holder.unread.setVisibility(View.INVISIBLE);
                }
                holder.updatedAt.setText(data.getUpdatedAt().toString());
                if (mOnItemClickListener != null) {
                    holder.item.setOnClickListener(view -> mOnItemClickListener.onItemClick(data, view, position));
                }
            }

        }


        public void notifyItemChangedSmooth(List<Conversation> newL){
            super.notifyItemChangedSmooth(newL, (oldData, newData) -> {
                Integer newUnread = viewModel.getUnreadNumber(newData);
                Integer lastUnread = unreadMap.get(oldData.getId());
                return !Objects.equals(newData, oldData) || !Objects.equals(lastUnread, newUnread);
            }, (conversation, t1) -> {
                if(Objects.equals(t1.getId(),conversation.getId())) return 0;
                else return conversation.getUpdatedAt().compareTo(t1.getUpdatedAt());
            });
        }

        class CHolder extends BaseViewHolder {
            @BindView(R.id.name)
            TextView name;

            @BindView(R.id.last_message)
            TextView lastMessage;

            @BindView(R.id.avatar)
            ImageView avatar;

            @BindView(R.id.updated_at)
            TextView updatedAt;

            @BindView(R.id.item)
            ViewGroup item;

            @BindView(R.id.unread)
            TextView unread;

            public CHolder(@NonNull View itemView) {
                super(itemView);
            }
        }
    }

}