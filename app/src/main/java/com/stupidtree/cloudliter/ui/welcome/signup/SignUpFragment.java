package com.stupidtree.cloudliter.ui.welcome.signup;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.stupidtree.cloudliter.data.model.UserLocal;
import com.stupidtree.cloudliter.ui.base.BaseFragment;
import com.stupidtree.cloudliter.R;

import butterknife.BindView;

public class SignUpFragment extends BaseFragment<SignUpViewModel> {
    @BindView(R.id.username)
    EditText usernameEditText;
    @BindView(R.id.password)
    EditText passwordEditText;
    @BindView(R.id.password_confirm)
    EditText confirmPasswordEditText;
    @BindView(R.id.nickname)
    EditText nicknameEditText;
    @BindView(R.id.gender_group)
    RadioGroup genderRadioGroup;
    @BindView(R.id.sign_up)
    Button signUpButton;
    @BindView(R.id.loading)
    ProgressBar loadingProgressBar;


    public SignUpFragment() {
    }

    public static SignUpFragment newInstance() {
        return new SignUpFragment();
    }

//    @Override
//    protected void initViewModel() {
//        viewModel = new ViewModelProvider(this, new SignUpViewModelFactory())
//                .get(SignUpViewModel.class);
//    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_sign_up;
    }

    @Override
    protected Class<SignUpViewModel> getViewModelClass() {
        return SignUpViewModel.class;
    }

    @Override
    protected void initViews(View view) {
        viewModel.getLoginFormState().observe(this, signUpFormState -> {
            if (signUpFormState == null) {
                return;
            }
            //将表单合法性同步到注册按钮可用性
            signUpButton.setEnabled(signUpFormState.isFormValid());
            //若有表单上的错误，则通知View显示错误
            if (signUpFormState.getUsernameError() != null) {
                usernameEditText.setError(getString(signUpFormState.getUsernameError()));
            }
            if (signUpFormState.getPasswordError() != null) {
                passwordEditText.setError(getString(signUpFormState.getPasswordError()));
            }
            if (signUpFormState.getPasswordConfirmError() != null) {
                confirmPasswordEditText.setError(getString(signUpFormState.getPasswordConfirmError()));
            }
            if (signUpFormState.getNicknameError() != null) {
                nicknameEditText.setError(getString(signUpFormState.getNicknameError()));
            }

        });

        viewModel.getSignUpResult().observe(this, signUpResult -> {
            loadingProgressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(getContext(), signUpResult.getMessage(), Toast.LENGTH_SHORT).show();
            if(signUpResult.getState()== SignUpResult.STATES.SUCCESS){
                requireActivity().finish();
            }else if(signUpResult.getState()== SignUpResult.STATES.USER_EXISTS){
                usernameEditText.setError(getString(signUpResult.getMessage()));
            }
        });
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
                viewModel.signUpDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString(),
                        confirmPasswordEditText.getText().toString(),
                        nicknameEditText.getText().toString());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        confirmPasswordEditText.addTextChangedListener(afterTextChangedListener);
        nicknameEditText.addTextChangedListener(afterTextChangedListener);

        signUpButton.setOnClickListener(v -> {
            loadingProgressBar.setVisibility(View.VISIBLE);
            viewModel.signUp(usernameEditText.getText().toString(),
                    passwordEditText.getText().toString(),
                    genderRadioGroup.getCheckedRadioButtonId()==R.id.radioButtonMale? UserLocal.GENDER.MALE : UserLocal.GENDER.FEMALE,
                    nicknameEditText.getText().toString());
        });
    }


}
