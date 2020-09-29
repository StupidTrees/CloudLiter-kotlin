package com.stupidtree.hichat.ui.widgets;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.StringRes;

import com.stupidtree.hichat.R;

import butterknife.BindView;

/**
 * 圆角的文本框底部弹窗
 */
public class PopUpEditText extends TransparentBottomSheetDialog {
    @BindView(R.id.title)
    TextView title;

    @BindView(R.id.text)
    EditText editText;

    @BindView(R.id.confirm)
    View confirm;

    @BindView(R.id.cancel)
    View cancel;

    @StringRes
    Integer init_title;
    @StringRes
    Integer init_hint;
    String init_text;


    OnConfirmListener onConfirmListener;
    public interface OnConfirmListener{
        void OnConfirm(String text);
    }

    @Override
    public void onStart() {
        super.onStart();
        editText.requestFocus();
    }

    public PopUpEditText setTitle(@StringRes int title) {
        this.init_title = title;
        return this;
    }

    public PopUpEditText setText(String text) {
        this.init_text = text;
        return this;
    }

    public PopUpEditText setHint(@StringRes int hint) {
        this.init_hint = hint;
        return this;
    }

    public PopUpEditText setOnConfirmListener(OnConfirmListener onConfirmListener){
        this.onConfirmListener = onConfirmListener;
        return this;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_bottom_edit_text;
    }

    @Override
    protected void initViews(View v) {
        if(init_title!=null){
            title.setText(init_title);
        }
        if(init_hint!=null){
            editText.setHint(init_hint);
        }
        if(init_text!=null){
            editText.setText(init_text);
        }
        cancel.setOnClickListener(view -> dismiss());
        confirm.setOnClickListener(view -> {
            if(onConfirmListener!=null){
                onConfirmListener.OnConfirm(editText.getText().toString());
            }
            dismiss();
        });
    }
}
