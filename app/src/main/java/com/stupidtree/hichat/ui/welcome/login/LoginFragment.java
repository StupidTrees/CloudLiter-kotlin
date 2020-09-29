package com.stupidtree.hichat.ui.welcome.login;

import androidx.lifecycle.Observer;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.stupidtree.hichat.ui.base.BaseFragment;
import com.stupidtree.hichat.R;

import butterknife.BindView;

/**
 * 登录页面Fragment
 */
public class LoginFragment extends BaseFragment<LoginViewModel> {

    /**
     * View绑定区
     */
    @BindView(R.id.username)
    EditText usernameEditText;
    @BindView(R.id.password)
    EditText passwordEditText;
    @BindView(R.id.login)
    Button loginButton;
    @BindView(R.id.loading)
    ProgressBar loadingProgressBar;

    public LoginFragment() {
    }

    public static LoginFragment newInstance(){
        return new LoginFragment();
    }


    @Override
    protected Class<LoginViewModel> getViewModelClass() {
        return LoginViewModel.class;
    }

    @Override
    protected void initViews(View view) {
        //登录表单的数据变更监听器
        viewModel.getLoginFormState().observe(this, loginFormState -> {
            if (loginFormState == null) {
                return;
            }
            //将表单合法性同步到登录按钮可用性
            loginButton.setEnabled(loginFormState.isDataValid());
            //若有表单上的错误，则通知View显示错误
            if (loginFormState.getUsernameError() != null) {
                usernameEditText.setError(getString(loginFormState.getUsernameError()));
            }
            if (loginFormState.getPasswordError() != null) {
                passwordEditText.setError(getString(loginFormState.getPasswordError()));
            }
        });

        //登录结果的数据变更监听
        viewModel.getLoginResult().observe(this, new Observer<LoginResult>() {
            @Override
            public void onChanged(LoginResult loginResult) {
                loadingProgressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(getContext(),loginResult.getMessage(),Toast.LENGTH_SHORT).show();
                if(loginResult.getState()== LoginResult.STATES.SUCCESS){
                    requireActivity().finish();
                }else if(loginResult.getState()== LoginResult.STATES.WRONG_USERNAME) {
                    usernameEditText.setError(getString(loginResult.getMessage()));
                }else if(loginResult.getState()== LoginResult.STATES.WRONG_PASSWORD){
                    passwordEditText.setError(getString(loginResult.getMessage()));
                }
            }
        });

        // 登录表单的文本监视器
        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                //将文本信息改变通知给ViewModel
                viewModel.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);

        //使得手机输入法上”完成“按钮映射到登录动作
        passwordEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.login(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
            return false;
        });

        loginButton.setOnClickListener(v -> {
            loadingProgressBar.setVisibility(View.VISIBLE);
            viewModel.login(usernameEditText.getText().toString(),
                    passwordEditText.getText().toString());
        });
    }


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_login;
    }
}