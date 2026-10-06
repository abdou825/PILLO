package com.example.alarm

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

class SoundPlayer(private val context: Context) {
    private var isPlaying = false
    private var audioTrack: AudioTrack? = null
    private var playJob: Job? = null
    private var escalationJob: Job? = null
    private var vibrator: Vibrator? = null
    private var currentVolume = 0.6f
    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    /**
     * Raises system ALARM stream volume to max, then starts sound and vibration.
     * Sound escalates from 60% to 100% over 60 seconds.
     */
    fun startAlarm(soundStyle: Int = 0) {
        if (isPlaying) return
        isPlaying = true

        // Maximize ALARM stream volume
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxVol, 0)
        } catch (_: Exception) {}

        currentVolume = 0.6f

        // Start synthesised loud sound loop
        startToneLoop(soundStyle)

        // Start repeating vibration
        startVibration()

        // Volume escalation over 60 seconds: 0.6 -> 1.0
        escalationJob = scope.launch {
            val steps = 30
            val stepDelayMs = 2000L
            val volumeIncrement = (1.0f - 0.6f) / steps
            for (i in 1..steps) {
                delay(stepDelayMs)
                if (!isActive || !isPlaying) break
                currentVolume = (currentVolume + volumeIncrement).coerceAtMost(1.0f)
                audioTrack?.setVolume(currentVolume)
            }
        }
    }

    private fun startToneLoop(soundStyle: Int) {
        playJob = scope.launch {
            val sampleRate = 44100
            val numSamplesPerChunk = sampleRate / 2
            val buffer = ShortArray(numSamplesPerChunk)

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val audioFormat = AudioFormat.Builder()
                .setSampleRate(sampleRate)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()

            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            try {
                audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(audioAttributes)
                    .setAudioFormat(audioFormat)
                    .setBufferSizeInBytes(maxOf(minBufferSize, numSamplesPerChunk * 2))
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                audioTrack?.setVolume(currentVolume)
                audioTrack?.play()

                var phase = 0.0
                var iteration = 0

                while (isActive && isPlaying) {
                    // 3 Distinct alarm sound styles
                    val freq1: Double
                    val freq2: Double
                    when (soundStyle) {
                        1 -> {
                            // Style 1: Urgent Two-Tone Staccato (880Hz / 1760Hz)
                            val isFirstHalf = (iteration % 4) < 2
                            freq1 = if (isFirstHalf) 880.0 else 1760.0
                            freq2 = freq1 * 1.5
                        }
                        2 -> {
                            // Style 2: Rapid Tri-tone Chime
                            val step = (iteration % 3)
                            freq1 = when (step) {
                                0 -> 784.0 // G5
                                1 -> 987.7 // B5
                                else -> 1318.5 // E6
                            }
                            freq2 = freq1 * 2.0
                        }
                        else -> {
                            // Style 0 (Default): Intense Egyptian Alert Pulsing Chord (1000Hz + 1500Hz)
                            val isBeep = (iteration % 2 == 0)
                            freq1 = if (isBeep) 950.0 else 0.0
                            freq2 = if (isBeep) 1425.0 else 0.0
                        }
                    }

                    for (i in 0 until numSamplesPerChunk) {
                        if (freq1 > 0.0) {
                            val sample = (sin(phase * freq1 * 2 * Math.PI / sampleRate) * 0.6 +
                                    sin(phase * freq2 * 2 * Math.PI / sampleRate) * 0.4)
                            buffer[i] = (sample * Short.MAX_VALUE * 0.85).toInt().toShort()
                        } else {
                            buffer[i] = 0
                        }
                        phase += 1.0
                    }

                    audioTrack?.write(buffer, 0, numSamplesPerChunk)
                    iteration++
                    delay(120L)
                }
            } catch (_: Exception) {
                // AudioTrack fallback
            }
        }
    }

    private fun startVibration() {
        val pattern = longArrayOf(0, 800, 300, 800, 300, 1000, 500)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0)
        }
    }

    fun stopAlarm() {
        isPlaying = false
        escalationJob?.cancel()
        playJob?.cancel()
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
        try {
            vibrator?.cancel()
        } catch (_: Exception) {}
    }
}
