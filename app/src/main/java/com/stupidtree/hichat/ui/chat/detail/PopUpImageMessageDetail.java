package com.stupidtree.hichat.ui.chat.detail;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.stupidtree.hichat.R;
import com.stupidtree.hichat.data.model.ChatMessage;
import com.stupidtree.hichat.ui.base.BaseListAdapter;
import com.stupidtree.hichat.ui.base.BaseViewHolder;
import com.stupidtree.hichat.ui.widgets.TransparentBottomSheetDialog;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;

/**
 * 圆角的文本框底部弹窗
 */
@SuppressLint("NonConstantResourceId")
public class PopUpImageMessageDetail extends TransparentBottomSheetDialog {
    /**
     * View绑定区
     */
    @BindView(R.id.neutral)
    TextView neutral;

    @BindView(R.id.drawing)
    TextView drawing;

    @BindView(R.id.hentai)
    TextView hentai;

    @BindView(R.id.porn)
    TextView porn;

    @BindView(R.id.sexy)
    TextView sexy;

    /**
     * 不得已放在UI里的数据
     */
    @NotNull
    ChatMessage chatMessage;



    public PopUpImageMessageDetail setChatMessage(@NotNull ChatMessage chatMessage) {
        this.chatMessage = chatMessage;
        return this;
    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_chat_popup_image_message_detail;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void initViews(View v) {
        DecimalFormat df = new DecimalFormat("#.####");
        HashMap<String,Float> map = chatMessage.getExtraAsImageAnalyse();
        neutral.setText(df.format(map.get("Neutral")));
        drawing.setText(df.format(map.get("Drawing")));
        porn.setText(df.format(map.get("Porn")));
        hentai.setText(df.format(map.get("Hentai")));
        sexy.setText(df.format(map.get("Sexy")));

    }



}
