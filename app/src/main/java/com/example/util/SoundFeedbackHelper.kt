package com.example.util

import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log

/**
 * Utilitário para reprodução de sons do sistema:
 * - Bip rápido ao escanear código de barras (estilo leitor comercial/PDV)
 * - Som de sucesso ao cadastrar ou alterar produto
 * - Som de erro quando o produto não for localizado ou houver falha
 */
object SoundFeedbackHelper {
    private const val TAG = "SoundFeedbackHelper"

    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
        } catch (e: Exception) {
            Log.e(TAG, "Falha ao inicializar ToneGenerator: ${e.message}")
        }
    }

    /**
     * Bip curto de leitor de código de barras (EAN/QR)
     */
    fun playScanBeep() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 120)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao tocar beep: ${e.message}")
        }
    }

    /**
     * Som agradável de confirmação / sucesso ao salvar ou alterar
     */
    fun playSuccess() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 250)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao tocar som de sucesso: ${e.message}")
        }
    }

    /**
     * Som de erro / alerta quando produto não for encontrado ou falha
     */
    fun playError() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_SUP_ERROR, 350)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao tocar som de erro: ${e.message}")
        }
    }
}
