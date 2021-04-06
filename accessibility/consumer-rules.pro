-keep class jackmego.com.jieba_android.RequestCallback { *; }
-keep class jackmego.com.jieba_android.JiebaSegmenter { *; }
-keep class jackmego.com.jieba_android.JiebaSegmenter$** {   # keep enum
    **[] $VALUES;
    public *;
}
-keep class jackmego.com.jieba_android.SegToken { *; }