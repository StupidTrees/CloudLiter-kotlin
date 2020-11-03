package com.stupidtree.cloudliter.ui.welcome.login

import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.lifecycle.Observer
import butterknife.BindView
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.ui.base.BaseFragment

/**
 * 登录页面Fragment
 */
class LoginFragment : BaseFragment<LoginViewModel>() {
    /**
     * View绑定区
     */
    @JvmField
    @BindView(R.id.username)
    var usernameEditText: EditText? = null

    @JvmField
    @BindView(R.id.password)
    var passwordEditText: EditText? = null

    @JvmField
    @BindView(R.id.login)
    var loginButton: Button? = null

    @JvmField
    @BindView(R.id.loading)
    var loadingProgressBar: ProgressBar? = null
    override fun getViewModelClass(): Class<LoginViewModel> {
        return LoginViewModel::class.java
    }

    override fun initViews(view: View) {
        //登录表单的数据变更监听器
        viewModel!!.loginFormState.observe(this, Observer { loginFormState: LoginFormState ->
            //将表单合法性同步到登录按钮可用性
            loginButton!!.isEnabled = loginFormState.isDataValid
            //若有表单上的错误，则通知View显示错误
            if (loginFormState.usernameError != null) {
                usernameEditText!!.error = getString(loginFormState.usernameError!!)
            }
            if (loginFormState.passwordError != null) {
                passwordEditText!!.error = getString(loginFormState.passwordError!!)
            }
        })

        //登录结果的数据变更监听
        viewModel!!.loginResult.observe(this, Observer { loginResult ->
            loadingProgressBar!!.visibility = View.INVISIBLE
            if (loginResult != null) {
                Toast.makeText(context, loginResult.message, Toast.LENGTH_SHORT).show()
            }
            if (loginResult != null) {
                if (loginResult.state == LoginResult.STATES.SUCCESS) {
                    requireActivity().finish()
                } else if (loginResult.state == LoginResult.STATES.WRONG_USERNAME) {
                    usernameEditText!!.error = getString(loginResult.message)
                } else if (loginResult.state == LoginResult.STATES.WRONG_PASSWORD) {
                    passwordEditText!!.error = getString(loginResult.message)
                }
            }
        })

        // 登录表单的文本监视器
        val afterTextChangedListener: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // ignore
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // ignore
            }

            override fun afterTextChanged(s: Editable) {
                //将文本信息改变通知给ViewModel
                viewModel!!.loginDataChanged(usernameEditText!!.text.toString(),
                        passwordEditText!!.text.toString())
            }
        }
        usernameEditText!!.addTextChangedListener(afterTextChangedListener)
        passwordEditText!!.addTextChangedListener(afterTextChangedListener)

        //使得手机输入法上”完成“按钮映射到登录动作
        passwordEditText!!.setOnEditorActionListener { v: TextView?, actionId: Int, event: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel!!.login(usernameEditText!!.text.toString(),
                        passwordEditText!!.text.toString())
            }
            false
        }
        loginButton!!.setOnClickListener { v: View? ->
            loadingProgressBar!!.visibility = View.VISIBLE
            viewModel!!.login(usernameEditText!!.text.toString(),
                    passwordEditText!!.text.toString())
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_login
    }

    companion object {
        fun newInstance(): LoginFragment {
            return LoginFragment()
        }
    }
}