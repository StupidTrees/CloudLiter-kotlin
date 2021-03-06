package com.stupidtree.cloudliter.data.source.websource.service

/**
 * api请求返回码，和后端保持一致
 */
object codes {
    const val SUCCESS = 2000
    const val USER_ALREADY_EXISTS = 3005
    const val REQUEST_ALREADY_SENT = 3006
    const val WRONG_USERNAME = 3000
    const val WRONG_PASSWORD = 3001
    const val GROUP_NAME_EXIST = 3017
    const val TOKEN_INVALID = 7000
    const val RELATION_NOT_EXIST = 3014
    const val WORD_CLOUD_PRIVATE = 3021
    const val IMAGE_NO_FACE = 3022
}