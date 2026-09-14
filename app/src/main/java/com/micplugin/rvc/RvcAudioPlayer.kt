package com.micplugin.rvc

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.math.minOf

class RvcAudioPlayer {
    companion object {
        private const val SAMPLE_RATE = 44100
        private const val CHANNELS = AudioFormat.CHANNEL_OUT_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    }

    private var audioTrack: AudioTrack? = null
    private val bufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, CHANNELS, AUDIO_FORMAT)

    fun play(audioData: FloatArray) {
        if (audioTrack == null) {
            audioTrack = AudioTrack(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build(),
                AudioFormat.Builder()
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(CHANNELS)
                    .setEncoding(AUDIO_FORMAT)
                    .build(),
                bufferSize,
                AudioTrack.MODE_STREAM
            )
            audioTrack?.play()
        }

        // Convert float to 16-bit PCM
        val pcmData = ShortArray(audioData.size) { i ->
            (audioData[i] * 32767f).toInt().toShort()
        }

        audioTrack?.write(pcmData, 0, minOf(pcmData.size, bufferSize / 2), AudioTrack.WRITE_NON_BLOCKING)
    }

    fun stop() {
        audioTrack?.stop()
        audioTrack?.release()
        audioTrack = null
    }
}
