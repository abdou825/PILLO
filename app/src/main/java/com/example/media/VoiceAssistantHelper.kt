package com.example.media

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import java.util.Locale

class VoiceAssistantHelper(
    private val context: Context,
    private val onCommandRecognized: (VoiceCommand) -> Unit,
    private val onError: (String) -> Unit
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false

    sealed interface VoiceCommand {
        data object TakeDose : VoiceCommand
        data class Snooze(val minutes: Int = 15) : VoiceCommand
        data object RemainingQuantityQuery : VoiceCommand
        data class Unrecognized(val rawText: String) : VoiceCommand
    }

    init {
        initTts()
    }

    private fun initTts() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech?.setLanguage(Locale("ar", "EG"))
                isTtsReady = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
            }
        }
    }

    fun speakArabic(text: String) {
        if (isTtsReady) {
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "PilloTTS")
        }
    }

    fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError("التعرف على الصوت غير مدعوم على هذا الجهاز أو يلزم تثبيت حزمة الصوت العربية.")
            return
        }

        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onError(error: Int) {
                    val msg = when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH -> "لم يتم فهم الكلام، حاول مرة ثانية بصوت واضح."
                        SpeechRecognizer.ERROR_NETWORK -> "جهازك يحتاج حزمة اللغة العربية دون إنترنت من إعدادات جوجل."
                        else -> "حدث خطأ أثناء الاستماع، يمكنك استخدام الأزرار العادية."
                    }
                    onError(msg)
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull() ?: ""
                    parseVoiceCommand(text)
                }

                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-EG")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ar-EG")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "تحدث الآن: 'أخدت الدوا' أو 'فكرني بعد ربع ساعة'")
        }

        try {
            speechRecognizer?.startListening(intent)
        } catch (_: Exception) {
            onError("تعذر تشغيل الميكروفون حالياً.")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (_: Exception) {}
    }

    fun destroy() {
        try {
            speechRecognizer?.destroy()
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        } catch (_: Exception) {}
    }

    private fun parseVoiceCommand(text: String) {
        val normalized = text.trim()
        when {
            normalized.contains("اخدت") || normalized.contains("أخدت") || normalized.contains("شربت") || normalized.contains("تم") -> {
                speakArabic("ألف سلامة عليك يا بطل! تم تسجيل الجرعة.")
                onCommandRecognized(VoiceCommand.TakeDose)
            }
            normalized.contains("فكرني") || normalized.contains("تأجيل") || normalized.contains("غفوة") || normalized.contains("ربع ساعة") -> {
                speakArabic("حاضر، هفكرك تاني بعد ربع ساعة.")
                onCommandRecognized(VoiceCommand.Snooze(15))
            }
            normalized.contains("فاضل") || normalized.contains("باقي") || normalized.contains("كام") -> {
                onCommandRecognized(VoiceCommand.RemainingQuantityQuery)
            }
            else -> {
                onCommandRecognized(VoiceCommand.Unrecognized(normalized))
            }
        }
    }

    companion object {
        private var sharedTts: TextToSpeech? = null
        private var isSharedReady = false

        fun speak(context: Context, text: String) {
            if (sharedTts == null) {
                sharedTts = TextToSpeech(context.applicationContext) { status ->
                    if (status == TextToSpeech.SUCCESS) {
                        val result = sharedTts?.setLanguage(Locale("ar", "EG"))
                        isSharedReady = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
                        if (isSharedReady) {
                            sharedTts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "PilloTTS")
                        }
                    }
                }
            } else if (isSharedReady) {
                sharedTts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "PilloTTS")
            }
        }
    }
}
