package com.stupidtree.hichat.ui.relationevent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.stupidtree.hichat.R;
import com.stupidtree.hichat.data.model.RelationEvent;
import com.stupidtree.hichat.ui.base.BaseActivity;
import com.stupidtree.hichat.ui.base.BaseListAdapter;
import com.stupidtree.hichat.ui.base.BaseViewHolder;
import com.stupidtree.hichat.ui.base.DataState;
import com.stupidtree.hichat.ui.widgets.PopUpCheckableList;
import com.stupidtree.hichat.utils.ActivityUtils;
import com.stupidtree.hichat.utils.ImageUtils;
import com.stupidtree.hichat.utils.TextUtils;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;

/**
 * 好友关系页面
 */
public class RelationEventActivity extends BaseActivity<RelationEventViewModel> {

    /**
     * View绑定区
     */
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.list)
    RecyclerView list;

    /**
     * 适配器
     */
    RListAdapter listAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_relation_event;
    }

    @Override
    protected Class<RelationEventViewModel> getViewModelClass() {
        return RelationEventViewModel.class;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true,true,false);
        setToolbarActionBack(toolbar);
    }

    @Override
    protected void initViews() {
        listAdapter = new RListAdapter(this,new LinkedList<>());
        list.setAdapter(listAdapter);
        list.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        listAdapter.setOnButtonClickListener((data, card, position) -> {
            new PopUpCheckableList<RelationEvent.ACTION>().setTitle(R.string.select_action)
                    .setListData(
                            Arrays.asList(getString(R.string.prompt_accept),getString(R.string.prompt_reject)),
                            Arrays.asList(RelationEvent.ACTION.ACCEPT, RelationEvent.ACTION.REJECT))
                    .setOnConfirmListener((title, key) -> viewModel.responseFriendRequest(data.getId(), key))
            .show(getSupportFragmentManager(),"select");

        });
        listAdapter.setOnItemClickListener((data, card, position) -> ActivityUtils.startProfileActivity(getThis(),data.getOtherId()));

        viewModel.getListData().observe(this, listDataState -> {
            if(listDataState.getState()== DataState.STATE.SUCCESS){
                listAdapter.notifyItemChangedSmooth(listDataState.getData(), (a, b) -> !Objects.equals(a, b));
            }
        });
        viewModel.getResponseResult().observe(this, dataState -> {
            if(dataState.getState()== DataState.STATE.SUCCESS){
                Toast.makeText(getThis(),R.string.make_friends_success,Toast.LENGTH_SHORT).show();
            }
            viewModel.startRefresh();
        });


        viewModel.getMarkReadResult().observe(this, dataState -> {
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        viewModel.startRefresh();
        viewModel.startMarkRead();
    }

    class RListAdapter extends BaseListAdapter<RelationEvent, RListAdapter.RHolder>{

        OnItemClickListener<RelationEvent> mOnButtonClickListener;

        public void setOnButtonClickListener(OnItemClickListener<RelationEvent> mOnButtonClickListener) {
            this.mOnButtonClickListener = mOnButtonClickListener;
        }

        public RListAdapter(Context mContext, List<RelationEvent> mBeans) {
            super(mContext, mBeans);
        }

        @Override
        protected int getLayoutId(int viewType) {
            return R.layout.activity_relation_event_list_item;
        }

        @Override
        public RHolder createViewHolder(View v, int viewType) {
            return new RHolder(v);
        }

        @Override
        protected void bindHolder(@NonNull RHolder holder, @Nullable RelationEvent data, int position) {
            if(data!=null){
                holder.nickname.setText(data.getOtherNickname());
                holder.time.setText(TextUtils.getChatTimeText(mContext,data.getCreatedAt()));
                ImageUtils.loadAvatarNoCacheInto(mContext,data.getOtherAvatar(),holder.avatar);
                if(mOnItemClickListener!=null){
                    holder.item.setOnClickListener(view -> mOnItemClickListener.onItemClick(data,view,position));
                }
                if(data.isUnread()){
                    holder.unread.setVisibility(View.VISIBLE);
                }else{
                    holder.unread.setVisibility(View.GONE);
                }

                if(Objects.equals(data.getUserId(),viewModel.getLocalUserId())){ //我发出的
                    holder.accept.setVisibility(View.GONE);
                    holder.message.setVisibility(View.VISIBLE);
                    holder.icon.setImageResource(R.drawable.ic_sent);
                    if(data.getState()== RelationEvent.STATE.REQUESTING){
                        holder.message.setText(R.string.relation_requesting);
                    }else if(data.getState() == RelationEvent.STATE.REJECTED){
                        holder.message.setText(R.string.relation_rejected);
                    }else if(data.getState()== RelationEvent.STATE.ACCEPTED){
                        holder.message.setText(R.string.relation_accepted);
                    }else if(data.getState()== RelationEvent.STATE.DELETE){
                        holder.message.setText(R.string.relation_you_deleted);
                    }
                }else{ //我收到的
                    holder.icon.setImageResource(R.drawable.ic_received);
                    holder.accept.setVisibility(View.GONE);
                    holder.message.setVisibility(View.VISIBLE);
                    if(data.getState()== RelationEvent.STATE.DELETE){
                        holder.message.setText(R.string.relation_delete);
                    }else if(data.getState()== RelationEvent.STATE.ACCEPTED) {
                        holder.message.setText(R.string.relation_accepted);
                    } else if(data.getState() == RelationEvent.STATE.REJECTED){
                        holder.message.setText(R.string.relation_rejected);
                    }else if(data.getState()== RelationEvent.STATE.REQUESTING){
                        holder.accept.setVisibility(View.VISIBLE);
                        holder.message.setVisibility(View.GONE);
                        if(mOnButtonClickListener!=null){
                            holder.accept.setOnClickListener(view -> mOnButtonClickListener.onItemClick(data,view,position));
                        }
                    }
                }
                if(data.getState()== RelationEvent.STATE.REQUESTING){
                    holder.icon.setImageTintList(ColorStateList.valueOf(getColor(R.color.colorPrimary)));
                    holder.icon.setBackgroundResource(R.drawable.element_round_primary);
                }else{
                    holder.icon.setImageTintList(ColorStateList.valueOf(getColor(android.R.color.darker_gray)));
                    holder.icon.setBackgroundResource(R.drawable.element_round_grey);
                }
            }
        }

        class RHolder extends RecyclerView.ViewHolder {
            View item;
            ImageView avatar;
            TextView nickname;
            ImageView icon;
            TextView message;
            TextView time;
            View accept;
            ImageView unread;
            public RHolder(@NonNull View itemView) {
                super(itemView);
                item = itemView.findViewById(R.id.item);
                accept = itemView.findViewById(R.id.accept);
                avatar = itemView.findViewById(R.id.avatar);
                nickname = itemView.findViewById(R.id.nickname);
                icon = itemView.findViewById(R.id.icon);
                message = itemView.findViewById(R.id.message);
                time = itemView.findViewById(R.id.time);
                unread = itemView.findViewById(R.id.unread);
            }
        }
    }

}