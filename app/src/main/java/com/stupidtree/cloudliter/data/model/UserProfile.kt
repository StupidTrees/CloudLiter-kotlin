package com.stupidtree.cloudliter.data.model

import android.content.Context
import androidx.annotation.StringRes
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.stupidtree.cloudliter.R
import com.stupidtree.cloudliter.data.model.UserLocal.GENDER

/**
 * 显示在用户资料页的用户资料Model
 * 和服务器返回数据匹配，无需适配函数
 */
@Entity(tableName = "profile")
class UserProfile {

    @PrimaryKey
    var NORMAL : Int = 0
    var VISUAL : Int = 1
    var HEARING : Int = 2
    var LIMB : Int = 4

    var id //用户id
            : String = ""
    var username //用户名
            : String? = null
    var nickname //昵称
            : String? = null
    var gender //性别
            : GENDER? = null
    var signature //签名
            : String? = null
    var avatar //头像
            : String? = null
    var accessibility //颜色
            : UserLocal.ACCESSIBILITY = UserLocal.ACCESSIBILITY.NO
    var type
            : Int = 0
    var subType
            : String? = null
    var typePermission
            : UserLocal.TYPEPERMISSION = UserLocal.TYPEPERMISSION.PRIVATE

    var wordCloudPrivate:Boolean = false//词云私密性


    fun getAccessibilityName():Int{
        return when (accessibility) {
            UserLocal.ACCESSIBILITY.NO -> {
                R.string.accessibility_off
            }
            UserLocal.ACCESSIBILITY.YES_PUBLIC -> {
                R.string.accessibility_on_public
            }
            else -> {
                R.string.accessibility_on_private
            }
        }
    }

    fun getTypeList(typeT:Int):List<Int>{
        return when (typeT) {
            1 -> { listOf(VISUAL) }
            2 -> { listOf(HEARING) }
            3 -> { listOf(VISUAL, HEARING) }
            4 -> { listOf(LIMB) }
            5 -> { listOf(VISUAL, LIMB) }
            6 -> { listOf(HEARING, LIMB) }
            7 -> { listOf(VISUAL, HEARING, LIMB) }
            else -> { listOf() }
        }
    }

    fun getTypeName():Int{
        return when (type) {
            1 -> { R.string.type_visual }
            2 -> { R.string.type_hearing }
            3 -> { R.string.type_visual_hearing }
            4 -> { R.string.type_limb }
            5 -> { R.string.type_visual_limb }
            6 -> { R.string.type_hearing_limb }
            7 -> { R.string.type_visual_hearing_limb }
            else -> { R.string.type_normal }
        }
    }

    fun getTypePermissionName():Int{
        return when (typePermission) {
            UserLocal.TYPEPERMISSION.PRIVATE -> {
                R.string.type_permission_private
            }
            UserLocal.TYPEPERMISSION.PROTECTED -> {
                R.string.type_permission_protected
            }
            UserLocal.TYPEPERMISSION.PUBLIC -> {
                R.string.type_permission_public
            }
        }
    }
}