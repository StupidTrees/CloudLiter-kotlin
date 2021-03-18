package com.stupidtree.cloudliter.utils

import java.io.*

object Base64Utils {
    /**
     * 功能：编码字符串
     * @param data
     * 源字符串
     * @return String
     */
    fun encode(data: String): String {
        return String(encode(data.toByteArray()))
    }

    /**
     * 功能：解码字符串
     *
     * @author jiangshuai
     * @date 2016年10月03日
     * @param data
     * 源字符串
     * @return String
     */
    fun decode(data: String): String {
        return String(decode(data.toCharArray()))
    }

    /**
     * 功能：编码byte[]
     * @param data
     * 源
     * @return char[]
     */
    fun encode(data: ByteArray?): CharArray {
        val out = CharArray((data!!.size + 2) / 3 * 4)
        var i = 0
        var index = 0
        while (i < data.size) {
            var quad = false
            var trip = false
            var `val` = 0xFF and data[i].toInt()
            `val` = `val` shl 8
            if (i + 1 < data.size) {
                `val` = `val` or (0xFF and data[i + 1].toInt())
                trip = true
            }
            `val` = `val` shl 8
            if (i + 2 < data.size) {
                `val` = `val` or (0xFF and data[i + 2].toInt())
                quad = true
            }
            out[index + 3] = alphabet[if (quad) `val` and 0x3F else 64]
            `val` = `val` shr 6
            out[index + 2] = alphabet[if (trip) `val` and 0x3F else 64]
            `val` = `val` shr 6
            out[index + 1] = alphabet[`val` and 0x3F]
            `val` = `val` shr 6
            out[index + 0] = alphabet[`val` and 0x3F]
            i += 3
            index += 4
        }
        return out
    }

    /**
     * 功能：解码
     * @param data
     * 编码后的字符数组
     * @return byte[]
     */
    fun decode(data: CharArray): ByteArray {
        var tempLen = data.size
        for (ix in data.indices) {
            if (data[ix] > 255.toChar() || codes[data[ix].toInt()] < 0) {
                --tempLen // ignore non-valid chars and padding
            }
        }
        // calculate required length:
        // -- 3 bytes for every 4 valid base64 chars
        // -- plus 2 bytes if there are 3 extra base64 chars,
        // or plus 1 byte if there are 2 extra.
        var len = tempLen / 4 * 3
        if (tempLen % 4 == 3) {
            len += 2
        }
        if (tempLen % 4 == 2) {
            len += 1
        }
        val out = ByteArray(len)
        var shift = 0 // # of excess bits stored in accum
        var accum = 0 // excess bits
        var index = 0

        // we now go through the entire array (NOT using the 'tempLen' value)
        for (ix in data.indices) {
            val value = if (data[ix] > 255.toChar()) -1 else codes[data[ix].toInt()].toInt()
            if (value >= 0) { // skip over non-code
                accum = accum shl 6 // bits shift up by 6 each time thru
                shift += 6 // loop, with new bits being put in
                accum = accum or value // at the bottom.
                if (shift >= 8) { // whenever there are 8 or more shifted in,
                    shift -= 8 // write them out (from the top, leaving any
                    out[index++] =  // excess at the bottom for next iteration.
                            (accum shr shift and 0xff).toByte()
                }
            }
        }

        // if there is STILL something wrong we just have to throw up now!
        if (index != out.size) {
            throw Error("Miscalculated data length (wrote " + index
                    + " instead of " + out.size + ")")
        }
        return out
    }

    /**
     * 功能：编码文件
     *
     * @param file
     * 源文件
     */
    @Throws(IOException::class)
    fun encode(file: File?) {
        var file = file
        if (!file!!.exists()) {
            System.exit(0)
        } else {
            val decoded = readBytes(file)
            val encoded = encode(decoded)
            writeChars(file, encoded)
        }
        file = null
    }

    /**
     * 功能：解码文件。
     *
     * @param file
     * 源文件
     * @throws IOException
     */
    @Throws(IOException::class)
    fun decode(file: File?) {
        var file = file
        if (!file!!.exists()) {
            System.exit(0)
        } else {
            val encoded = readChars(file)
            val decoded = decode(encoded)
            writeBytes(file, decoded)
        }
        file = null
    }

    //
    // code characters for values 0..63
    //
    private val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/="
            .toCharArray()

    //
    // lookup table for converting base64 characters to value in range 0..63
    //
    private val codes = ByteArray(256)
    @Throws(IOException::class)
    private fun readBytes(file: File?): ByteArray? {
        val baos = ByteArrayOutputStream()
        var b: ByteArray? = null
        var fis: InputStream? = null
        var `is`: InputStream? = null
        try {
            fis = FileInputStream(file)
            `is` = BufferedInputStream(fis)
            var count = 0
            val buf = ByteArray(16384)
            while (`is`.read(buf).also { count = it } != -1) {
                if (count > 0) {
                    baos.write(buf, 0, count)
                }
            }
            b = baos.toByteArray()
        } finally {
            try {
                fis?.close()
                `is`?.close()
                baos.close()
            } catch (e: Exception) {
                println(e)
            }
        }
        return b
    }

    @Throws(IOException::class)
    private fun readChars(file: File?): CharArray {
        val caw = CharArrayWriter()
        var fr: Reader? = null
        var `in`: Reader? = null
        try {
            fr = FileReader(file)
            `in` = BufferedReader(fr)
            var count: Int
            val buf = CharArray(16384)
            while (`in`.read(buf).also { count = it } != -1) {
                if (count > 0) {
                    caw.write(buf, 0, count)
                }
            }
        } finally {
            try {
                caw.close()
                `in`?.close()
                fr?.close()
            } catch (e: Exception) {
                println(e)
            }
        }
        return caw.toCharArray()
    }

    @Throws(IOException::class)
    private fun writeBytes(file: File?, data: ByteArray) {
        var fos: OutputStream? = null
        var os: OutputStream? = null
        try {
            fos = FileOutputStream(file)
            os = BufferedOutputStream(fos)
            os.write(data)
        } finally {
            try {
                os?.close()
                fos?.close()
            } catch (e: Exception) {
                println(e)
            }
        }
    }

    @Throws(IOException::class)
    private fun writeChars(file: File?, data: CharArray) {
        var fos: Writer? = null
        var os: Writer? = null
        try {
            fos = FileWriter(file)
            os = BufferedWriter(fos)
            os.write(data)
        } finally {
            try {
                os?.close()
                fos?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    init {
        for (i in 0..255) {
            codes[i] = -1
            // LoggerUtil.debug(i + "&" + codes[i] + " ");
        }
        run {
            var i = 'A'.toInt()
            while (i <= 'Z'.toInt()) {
                codes[i] = (i - 'A'.toInt()).toByte()
                i++
            }
        }
        run {
            var i = 'a'.toInt()
            while (i <= 'z'.toInt()) {
                codes[i] = (26 + i - 'a'.toInt()).toByte()
                i++
            }
        }
        var i = '0'.toInt()
        while (i <= '9'.toInt()) {
            codes[i] = (52 + i - '0'.toInt()).toByte()
            i++
        }
        codes['+'.toInt()] = 62
        codes['/'.toInt()] = 63
    }
}