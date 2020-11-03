package com.stupidtree.cloudliter.ui.welcome.signup

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.lifecycle.Observer
import butterknife.BindView
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.data.model.UserLocal
import com.stupidtree.cloudliter.ui.base.BaseFragment

class SignUpFragment : BaseFragment<SignUpViewModel>() {
    @JvmField
    @BindView(R.id.username)
    var usernameEditText: EditText? = null

    @JvmField
    @BindView(R.id.password)
    var passwordEditText: EditText? = null

    @JvmField
    @BindView(R.id.password_confirm)
    var confirmPasswordEditText: EditText? = null

    @JvmField
    @BindView(R.id.nickname)
    var nicknameEditText: EditText? = null

    @JvmField
    @BindView(R.id.gender_group)
    var genderRadioGroup: RadioGroup? = null

    @JvmField
    @BindView(R.id.sign_up)
    var signUpButton: Button? = null

    @JvmField
    @BindView(R.id.loading)
    var loadingProgressBar: ProgressBar? = null

    //    @Override
    //    protected void initViewModel() {
    //        viewModel = new ViewModelProvider(this, new SignUpViewModelFactory())
    //                .get(SignUpViewModel.class);
    //    }
    override fun getLayoutId(): Int {
        return R.layout.fragment_sign_up
    }

    override fun getViewModelClass(): Class<SignUpViewModel> {
        return SignUpViewModel::class.java
    }

    override fun initViews(view: View) {
        viewModel!!.loginFormState.observe(this, Observer { signUpFormState: SignUpFormState? ->
            //将表单合法性同步到注册按钮可用性
            if (signUpFormState != null) {
                signUpButton!!.isEnabled = signUpFormState.isFormValid
                //若有表单上的错误，则通知View显示错误
                if (signUpFormState.usernameError != null) {
                    usernameEditText!!.error = getString(signUpFormState.usernameError!!)
                }
                if (signUpFormState.passwordError != null) {
                    passwordEditText!!.error = getString(signUpFormState.passwordError!!)
                }
                if (signUpFormState.passwordConfirmError != null) {
                    confirmPasswordEditText!!.error = getString(signUpFormState.passwordConfirmError!!)
                }
                if (signUpFormState.nicknameError != null) {
                    nicknameEditText!!.error = getString(signUpFormState.nicknameError!!)
                }
            }

        })
        viewModel!!.signUpResult.observe(this, Observer { signUpResult: SignUpResult? ->
            loadingProgressBar!!.visibility = View.INVISIBLE
            if (signUpResult != null) {
                Toast.makeText(context, signUpResult.message, Toast.LENGTH_SHORT).show()
            }
            if (signUpResult != null) {
                if (signUpResult.state === SignUpResult.STATES.SUCCESS) {
                    requireActivity().finish()
                } else if (signUpResult.state === SignUpResult.STATES.USER_EXISTS) {
                    usernameEditText!!.error = getString(signUpResult.message)
                }
            }
        })
        val afterTextChangedListener: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // ignore
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // ignore
            }

            override fun afterTextChanged(s: Editable) {
                //将文本信息改变通知给ViewModel
                viewModel!!.signUpDataChanged(usernameEditText!!.text.toString(),
                        passwordEditText!!.text.toString(),
                        confirmPasswordEditText!!.text.toString(),
                        nicknameEditText!!.text.toString())
            }
        }
        usernameEditText!!.addTextChangedListener(afterTextChangedListener)
        passwordEditText!!.addTextChangedListener(afterTextChangedListener)
        confirmPasswordEditText!!.addTextChangedListener(afterTextChangedListener)
        nicknameEditText!!.addTextChangedListener(afterTextChangedListener)
        signUpButton!!.setOnClickListener { v: View? ->
            loadingProgressBar!!.visibility = View.VISIBLE
            viewModel!!.signUp(usernameEditText!!.text.toString(),
                    passwordEditText!!.text.toString(),
                    if (genderRadioGroup!!.checkedRadioButtonId == R.id.radioButtonMale) UserLocal.GENDER.MALE else UserLocal.GENDER.FEMALE,
                    nicknameEditText!!.text.toString())
        }
    }

    companion object {
        fun newInstance(): SignUpFragment {
            return SignUpFragment()
        }
    }
}