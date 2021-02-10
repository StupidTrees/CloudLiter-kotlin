package com.stupidtree.cloudliter.ui.imagedetect

import android.graphics.Bitmap
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink

class BitmapRequestBody(
        private val bitmap: Bitmap,
        private val format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG
): RequestBody(){

    override fun contentType(): MediaType? {
        return MediaType.parse(when(format){
            Bitmap.CompressFormat.WEBP -> "image/webp"
            Bitmap.CompressFormat.PNG -> "image/png"
            else -> "image/jpeg"
        })
    }

    override fun writeTo(sink: BufferedSink) {
        bitmap.compress(format,50,sink.outputStream())
    }

}