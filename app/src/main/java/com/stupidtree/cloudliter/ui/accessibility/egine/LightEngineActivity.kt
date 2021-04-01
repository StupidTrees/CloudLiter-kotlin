package com.stupidtree.cloudliter.ui.accessibility.egine

import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import com.stupidtree.cloudliter.databinding.ActivityLightEngineBinding
import com.stupidtree.style.base.BaseActivity

class LightEngineActivity : BaseActivity<LightEngineViewModel, ActivityLightEngineBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setToolbarActionBack(binding.toolbar)
    }

    override fun initViewBinding(): ActivityLightEngineBinding {
        return ActivityLightEngineBinding.inflate(layoutInflater)
    }

    override fun getViewModelClass(): Class<LightEngineViewModel> {
        return LightEngineViewModel::class.java
    }

    override fun onStart() {
        super.onStart()
        viewModel.startRefresh()
    }

    override fun initViews() {
        val urls = mutableListOf("https://findicons.com/files/icons/2777/sound_and_audio_for_android/96/5_favorite_star.png",
                "https://findicons.com/files/icons/2777/sound_and_audio_for_android/96/1_speaker_minus.png",
                "https://findicons.com/files/icons/2777/sound_and_audio_for_android/96/4_audio_play.png",
                "https://findicons.com/files/icons/2777/sound_and_audio_for_android/96/2_audio_simple_repeat_2.png",
                "https://findicons.com/files/icons/2777/sound_and_audio_for_android/96/3_audio_hardware_2.png",
                "https://findicons.com/files/icons/2777/sound_and_audio_for_android/96/4_audio_rewind.png",
                "https://findicons.com/files/icons/2777/sound_and_audio_for_android/96/1_speaker_medium.png",
                "https://findicons.com/files/icons/2777/sound_and_audio_for_android/96/1_speaker_plus.png",
                "https://findicons.com/files/icons/2777/sound_and_audio_for_android/96/4_audio_step_back.png",
                "https://findicons.com/files/icons/2777/sound_and_audio_for_android/96/8_audio_micro.png",
                "https://findicons.com/files/icons/2777/sound_and_audio_for_android/96/1_speaker_loud.png",
                "https://findicons.com/files/icons/2777/sound_and_audio_for_android/96/4_audio_title_forward.png",
                "https://findicons.com/files/icons/2777/sound_and_audio_for_android/96/1_speaker_off.png",
                "https://findicons.com/files/icons/2777/sound_and_audio_for_android/96/2_audio_simple_repeat.png",
                "https://findicons.com/files/icons/2777/sound_and_audio_for_android/96/3_audio_hardware.png",
                "https://findicons.com/files/icons/2777/sound_and_audio_for_android/96/4_audio_pause.png",
                "https://findicons.com/files/icons/2777/sound_and_audio_for_android/96/6_micro_sing.png",
                "https://findicons.com/files/icons/2777/sound_and_audio_for_android/96/7_music_node.png",
                "https://findicons.com/files/icons/2777/sound_and_audio_for_android/96/2_audio_repeat.png",
                "https://findicons.com/files/icons/2777/sound_and_audio_for_android/96/2_audio_repeat_unlimit.png",
                "https://findicons.com/files/icons/2777/sound_and_audio_for_android/96/2_audio_simple_random.png",
                "https://findicons.com/files/icons/2777/sound_and_audio_for_android/96/4_audio_step_forward.png",
                "https://findicons.com/files/icons/2777/sound_and_audio_for_android/96/7_music_nodes.png",
                "https://findicons.com/files/icons/2777/sound_and_audio_for_android/96/4_audio_stop.png",
                "https://findicons.com/files/icons/2777/sound_and_audio_for_android/96/4_audio_title_back.png",
                "https://findicons.com/files/icons/2777/sound_and_audio_for_android/96/8_audio_enhancement.png",
                "https://findicons.com/files/icons/2777/sound_and_audio_for_android/96/2_audio_simple_repeat_all.png",
                "https://findicons.com/files/icons/2777/sound_and_audio_for_android/96/5_favorite_cricle_star.png",
                "https://findicons.com/files/icons/2777/sound_and_audio_for_android/96/8_audio_headphones.png",
                "https://findicons.com/files/icons/2777/sound_and_audio_for_android/96/5_favorite_half_star.png",
                "https://cdn4.iconfinder.com/data/icons/multimedia-75/512/multimedia-01-128.png",
                "https://cdn4.iconfinder.com/data/icons/multimedia-75/512/multimedia-02-128.png",
                "https://cdn4.iconfinder.com/data/icons/multimedia-75/512/multimedia-10-128.png",
                "https://cdn4.iconfinder.com/data/icons/multimedia-75/512/multimedia-11-128.png",
                "https://cdn4.iconfinder.com/data/icons/multimedia-75/512/multimedia-06-128.png",
                "https://cdn4.iconfinder.com/data/icons/multimedia-75/512/multimedia-25-128.png",
                "https://cdn4.iconfinder.com/data/icons/multimedia-75/512/multimedia-22-128.png",
                "https://cdn4.iconfinder.com/data/icons/multimedia-75/512/multimedia-09-128.png",
                "https://cdn4.iconfinder.com/data/icons/multimedia-75/512/multimedia-26-128.png",
                "https://cdn4.iconfinder.com/data/icons/multimedia-75/512/multimedia-42-128.png",
                "https://cdn4.iconfinder.com/data/icons/multimedia-75/512/multimedia-05-128.png",
                "https://cdn4.iconfinder.com/data/icons/multimedia-75/512/multimedia-50-128.png",
                "https://cdn4.iconfinder.com/data/icons/multimedia-75/512/multimedia-29-128.png",
                "https://cdn4.iconfinder.com/data/icons/multimedia-75/512/multimedia-07-128.png",
                "https://cdn4.iconfinder.com/data/icons/multimedia-75/512/multimedia-35-128.png",
                "https://cdn4.iconfinder.com/data/icons/multimedia-75/512/multimedia-12-128.png",
                "https://cdn4.iconfinder.com/data/icons/multimedia-75/512/multimedia-41-128.png",
                "https://cdn4.iconfinder.com/data/icons/multimedia-75/512/multimedia-31-128.png",
                "https://cdn4.iconfinder.com/data/icons/multimedia-75/512/multimedia-49-128.png",
                "https://cdn4.iconfinder.com/data/icons/multimedia-75/512/multimedia-37-128.png",
                "https://cdn4.iconfinder.com/data/icons/multimedia-75/512/multimedia-28-128.png",
                "https://cdn4.iconfinder.com/data/icons/multimedia-75/512/multimedia-03-128.png",
                "https://cdn4.iconfinder.com/data/icons/multimedia-75/512/multimedia-20-128.png",
                "https://cdn4.iconfinder.com/data/icons/multimedia-75/512/multimedia-27-128.png",
                "https://cdn4.iconfinder.com/data/icons/multimedia-75/512/multimedia-33-128.png",
                "https://cdn4.iconfinder.com/data/icons/multimedia-75/512/multimedia-44-128.png",
                "https://cdn4.iconfinder.com/data/icons/multimedia-75/512/multimedia-16-128.png",
                "https://cdn4.iconfinder.com/data/icons/multimedia-75/512/multimedia-13-128.png",
                "https://cdn4.iconfinder.com/data/icons/multimedia-75/512/multimedia-08-128.png",
                "https://cdn4.iconfinder.com/data/icons/multimedia-75/512/multimedia-38-128.png",
                "https://cdn4.iconfinder.com/data/icons/multimedia-75/512/multimedia-15-128.png",
                "https://cdn4.iconfinder.com/data/icons/multimedia-75/512/multimedia-18-128.png",
                "https://cdn4.iconfinder.com/data/icons/multimedia-75/512/multimedia-17-128.png",
                "https://cdn4.iconfinder.com/data/icons/multimedia-75/512/multimedia-43-128.png",
                "https://cdn4.iconfinder.com/data/icons/multimedia-75/512/multimedia-24-128.png",
                "https://cdn4.iconfinder.com/data/icons/multimedia-75/512/multimedia-04-128.png",
                "https://cdn4.iconfinder.com/data/icons/multimedia-75/512/multimedia-30-128.png",
                "https://cdn4.iconfinder.com/data/icons/multimedia-75/512/multimedia-23-128.png",
                "https://cdn4.iconfinder.com/data/icons/multimedia-75/512/multimedia-39-128.png",
                "https://cdn4.iconfinder.com/data/icons/multimedia-75/512/multimedia-14-128.png",
                "https://cdn4.iconfinder.com/data/icons/multimedia-75/512/multimedia-48-128.png",
                "https://cdn4.iconfinder.com/data/icons/multimedia-75/512/multimedia-46-128.png",
                "https://cdn4.iconfinder.com/data/icons/multimedia-75/512/multimedia-36-128.png",
                "https://cdn4.iconfinder.com/data/icons/multimedia-75/512/multimedia-32-128.png",
                "https://cdn4.iconfinder.com/data/icons/multimedia-75/512/multimedia-40-128.png",
                "https://cdn4.iconfinder.com/data/icons/multimedia-75/512/multimedia-45-128.png",
                "https://cdn4.iconfinder.com/data/icons/multimedia-75/512/multimedia-19-128.png",
                "https://cdn4.iconfinder.com/data/icons/multimedia-75/512/multimedia-47-128.png",
                "https://cdn4.iconfinder.com/data/icons/multimedia-75/512/multimedia-21-128.png",
                "https://cdn4.iconfinder.com/data/icons/multimedia-75/512/multimedia-34-128.png"
        )
        val adapter = IconTestListAdapter(this, urls)
        binding.iconList.adapter = adapter
        binding.iconList.layoutManager = GridLayoutManager(this, 4)
    }
}