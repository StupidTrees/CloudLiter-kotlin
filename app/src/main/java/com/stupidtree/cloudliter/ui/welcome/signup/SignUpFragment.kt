package com.stupidtree.cloudliter.ui.welcome.signup

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.data.model.UserLocal
import com.stupidtree.cloudliter.databinding.FragmentSignUpBinding
import com.stupidtree.style.base.BaseFragment

class SignUpFragment : BaseFragment<SignUpViewModel, FragmentSignUpBinding>() {

    override fun getViewModelClass(): Class<SignUpViewModel> {
        return SignUpViewModel::class.java
    }

    override fun initViews(view: View) {
        viewModel.loginFormState.observe(this, { signUpFormState: SignUpFormState? ->
            //将表单合法性同步到注册按钮可用性
            if (signUpFormState != null) {
                binding?.signUp?.isEnabled = signUpFormState.isFormValid
                //若有表单上的错误，则通知View显示错误
                if (signUpFormState.usernameError != null) {
                    binding?.username?.error = getString(signUpFormState.usernameError!!)
                }
                if (signUpFormState.passwordError != null) {
                    binding?.password?.error = getString(signUpFormState.passwordError!!)
                }
                if (signUpFormState.passwordConfirmError != null) {
                    binding?.passwordConfirm?.error = getString(signUpFormState.passwordConfirmError!!)
                }
                if (signUpFormState.nicknameError != null) {
                    binding?.nickname?.error = getString(signUpFormState.nicknameError!!)
                }
            }

        })
        viewModel.signUpResult.observe(this, { signUpResult: SignUpResult? ->
            binding?.loading?.visibility = View.INVISIBLE
            if (signUpResult != null) {
                Toast.makeText(context, signUpResult.message, Toast.LENGTH_SHORT).show()
            }
            if (signUpResult != null) {
                if (signUpResult.state === SignUpResult.STATES.SUCCESS) {
                    requireActivity().finish()
                } else if (signUpResult.state === SignUpResult.STATES.USER_EXISTS) {
                    binding?.username?.error = getString(signUpResult.message)
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
                viewModel.signUpDataChanged(binding?.username?.text.toString(),
                        binding?.password?.text.toString(),
                        binding?.passwordConfirm?.text.toString(),
                        binding?.nickname?.text.toString())
            }
        }
        binding?.username?.addTextChangedListener(afterTextChangedListener)
        binding?.password?.addTextChangedListener(afterTextChangedListener)
        binding?.passwordConfirm?.addTextChangedListener(afterTextChangedListener)
        binding?.nickname?.addTextChangedListener(afterTextChangedListener)
        binding?.signUp?.setOnClickListener {
            binding?.loading?.visibility = View.VISIBLE
            viewModel.signUp(binding?.username?.text.toString(),
                    binding?.password?.text.toString(),
                    if (binding?.genderGroup?.checkedRadioButtonId == R.id.radioButtonMale) UserLocal.GENDER.MALE else UserLocal.GENDER.FEMALE,
                    binding?.nickname?.text.toString())
        }
    }

    companion object {
        fun newInstance(): SignUpFragment {
            return SignUpFragment()
        }
    }

    override fun initViewBinding(): FragmentSignUpBinding {
        return FragmentSignUpBinding.inflate(layoutInflater)
    }
}