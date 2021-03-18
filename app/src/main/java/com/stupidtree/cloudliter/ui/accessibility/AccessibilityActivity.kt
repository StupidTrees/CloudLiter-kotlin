package com.stupidtree.cloudliter.ui.accessibility

import android.os.Bundle
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.data.model.UserLocal
import com.stupidtree.cloudliter.data.model.UserProfile
import com.stupidtree.cloudliter.databinding.ActivityAccessibilityBinding
import com.stupidtree.style.base.BaseActivity
import com.stupidtree.component.data.DataState
import com.stupidtree.style.widgets.PopUpMultipleCheckableList
import com.stupidtree.style.widgets.PopUpSelectableList
import com.stupidtree.cloudliter.utils.ActivityUtils

class AccessibilityActivity : BaseActivity<AccessibilityViewModel, ActivityAccessibilityBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setWindowParams(statusBar = true, darkColor = true, navi = false)
        setToolbarActionBack(binding.toolbar)
    }

    override fun initViewBinding(): ActivityAccessibilityBinding {
        return ActivityAccessibilityBinding.inflate(layoutInflater)
    }

    override fun getViewModelClass(): Class<AccessibilityViewModel> {
        return AccessibilityViewModel::class.java
    }

    override fun onStart() {
        super.onStart()
        viewModel.startRefresh()
    }

    override fun initViews() {
        binding.accessibilityTypeLayout.setOnClickListener { view ->
            viewModel.myProfileLiveData.value?.data?.let {
                PopUpMultipleCheckableList<Int>(R.string.type, 0, 0)
                        .setInitValues(it.getTypeList(it.type))
                        .setListData(
                                listOf(getString(R.string.type_visual),
                                        getString(R.string.type_hearing),
                                        getString(R.string.type_limb)),
                                listOf(UserProfile.VISUAL,
                                        UserProfile.HEARING,
                                        UserProfile.LIMB)
                        ).setOnConfirmListener(object : PopUpMultipleCheckableList.OnConfirmListener<Int> {
                            override fun onConfirm(titles: List<String?>, data: List<Int>) {
                                var key = 0
                                for (d in data) {
                                    key = d xor key
                                }
                                viewModel.startChangeType(key, it.subType, it.typePermission)
                            }
                        }).show(supportFragmentManager, "select")
            }
        }


        // 点击更换无障碍隐私类型，弹出选择框
        binding.accessibilityPermissionLayout.setOnClickListener {
            viewModel.myProfileLiveData.value?.data?.let {
                PopUpSelectableList<UserLocal.TYPEPERMISSION>()
                        .setTitle(R.string.type_permission)
                        .setInitValue(it.typePermission)
                        .setListData(
                                listOf(getString(R.string.type_permission_public),
                                        getString(R.string.type_permission_protected),
                                        getString(R.string.type_permission_private)),
                                listOf(UserLocal.TYPEPERMISSION.PUBLIC,
                                        UserLocal.TYPEPERMISSION.PROTECTED,
                                        UserLocal.TYPEPERMISSION.PRIVATE)
                        ).setOnConfirmListener(object : PopUpSelectableList.OnConfirmListener<UserLocal.TYPEPERMISSION> {
                            override fun onConfirm(title: String?, key: UserLocal.TYPEPERMISSION) {
                                viewModel.startChangeType(it.type, it.subType, key)
                            }
                        }).show(supportFragmentManager, "select")
            }

        }
        binding.myFaceLayout.setOnClickListener {
            ActivityUtils.startMyFaceActivity(this)
        }
        viewModel.changeTypeResult.observe(this) {
            viewModel.startRefresh()
        }
        viewModel.myProfileLiveData.observe(this) {
            if (it.state == DataState.STATE.SUCCESS) {
                if (it.data?.isType(UserProfile.VISUAL) == true) {
                    binding.visualIcon.setBackgroundResource(R.drawable.element_round_primary_light)
                    binding.visualIcon.setColorFilter(getColorPrimary())
                } else {
                    binding.visualIcon.setBackgroundResource(R.drawable.element_round_grey_light)
                    binding.visualIcon.clearColorFilter()
                }

                if (it.data?.isType(UserProfile.HEARING) == true) {
                    binding.hearingIcon.setBackgroundResource(R.drawable.element_round_primary_light)
                    binding.hearingIcon.setColorFilter(getColorPrimary())
                } else {
                    binding.hearingIcon.setBackgroundResource(R.drawable.element_round_grey_light)
                    binding.hearingIcon.clearColorFilter()
                }

                if (it.data?.isType(UserProfile.LIMB) == true) {
                    binding.limbIcon.setBackgroundResource(R.drawable.element_round_primary_light)
                    binding.limbIcon.setColorFilter(getColorPrimary())
                } else {
                    binding.limbIcon.setBackgroundResource(R.drawable.element_round_grey_light)
                    binding.limbIcon.clearColorFilter()
                }
            }
        }
    }
}