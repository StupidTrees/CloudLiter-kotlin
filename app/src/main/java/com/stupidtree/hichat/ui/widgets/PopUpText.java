package com.stupidtree.hichat.ui.widgets;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.StringRes;

import com.stupidtree.hichat.R;
import com.stupidtree.hichat.utils.TextUtils;

import butterknife.BindView;

/**
 * 圆角的文本框底部弹窗
 */
public class PopUpText extends TransparentBottomSheetDialog {
    @BindView(R.id.title)
    TextView title;

    @BindView(R.id.text)
    TextView textView;

    @BindView(R.id.confirm)
    View confirm;

    @BindView(R.id.cancel)
    View cancel;

    @StringRes
    Integer init_title;

    String init_text;


    OnConfirmListener onConfirmListener;

    public interface OnConfirmListener {
        void OnConfirm();
    }

    @Override
    public void onStart() {
        super.onStart();
        textView.requestFocus();
    }

    public PopUpText setTitle(@StringRes int title) {
        this.init_title = title;
        return this;
    }

    public PopUpText setDialogCancelable(boolean cancelable) {
        setCancelable(cancelable);
        return this;
    }

    public PopUpText setText(String text) {
        this.init_text = text;
        return this;
    }


    public PopUpText setOnConfirmListener(OnConfirmListener onConfirmListener) {
        this.onConfirmListener = onConfirmListener;
        return this;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_bottom_text;
    }

    @Override
    protected void initViews(View v) {
        if (init_title != null) {
            title.setText(init_title);
        }
        if (!TextUtils.isEmpty(init_text)) {
            textView.setText(init_text);
            textView.setVisibility(View.VISIBLE);
        }else{
            textView.setVisibility(View.GONE);
        }
        if (isCancelable()) {
            cancel.setVisibility(View.VISIBLE);
        } else {
            cancel.setVisibility(View.GONE);
        }
        cancel.setOnClickListener(view -> dismiss());
        confirm.setOnClickListener(view -> {
            if (onConfirmListener != null) {
                onConfirmListener.OnConfirm();
            }
            dismiss();
        });
    }
}
