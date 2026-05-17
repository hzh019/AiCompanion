package com.aicompanion.personality

import javax.inject.Inject
import javax.inject.Singleton

enum class Emotion {
    HAPPY, SAD, ANGRY, ANXIOUS, EXCITED, TIRED, NEUTRAL, LOVING
}

data class EmotionContext(
    val userEmotion: Emotion = Emotion.NEUTRAL,
    val confidence: Float = 0f,
    val suggestedTone: String = "正常"
)

@Singleton
class EmotionInjector @Inject constructor() {

    fun detectEmotion(text: String): EmotionContext {
        // Simple keyword-based emotion detection for MVP
        val happyKeywords = listOf("哈哈", "开心", "高兴", "太好", "喜欢", "爱你", "棒", "不错", "谢谢", "😊", "😄", "哈哈", "嘻嘻")
        val sadKeywords = listOf("难过", "伤心", "哭", "失落", "失望", "孤独", "累", "烦", "😢", "😭", "唉", "郁闷")
        val angryKeywords = listOf("生气", "愤怒", "讨厌", "滚", "无语", "别烦", "够了", "😡", "操", "烦死了")
        val anxiousKeywords = listOf("担心", "紧张", "害怕", "焦虑", "不安", "怎么办", "要是")
        val excitedKeywords = listOf("哇", "天哪", "太棒了", "太好了", "超级", "终于", "!!", "！！")
        val tiredKeywords = listOf("好累", "困", "想睡", "疲惫", "没精神", "休息")
        val lovingKeywords = listOf("想你", "爱你", "抱抱", "亲亲", "宝贝", "亲爱的", "❤", "💕")

        var maxScore = 0
        var detectedEmotion = Emotion.NEUTRAL

        fun score(keywords: List<String>): Int {
            return keywords.count { text.contains(it) }
        }

        listOf(
            Emotion.HAPPY to happyKeywords,
            Emotion.SAD to sadKeywords,
            Emotion.ANGRY to angryKeywords,
            Emotion.ANXIOUS to anxiousKeywords,
            Emotion.EXCITED to excitedKeywords,
            Emotion.TIRED to tiredKeywords,
            Emotion.LOVING to lovingKeywords
        ).forEach { (emotion, keywords) ->
            val s = score(keywords)
            if (s > maxScore) {
                maxScore = s
                detectedEmotion = emotion
            }
        }

        val confidence = (maxScore / 5f).coerceIn(0f, 1f)

        val suggestedTone = when (detectedEmotion) {
            Emotion.SAD -> "温柔安慰"
            Emotion.ANGRY -> "平静理性"
            Emotion.ANXIOUS -> "安抚鼓励"
            Emotion.EXCITED -> "一起开心"
            Emotion.TIRED -> "轻声关心"
            Emotion.LOVING -> "甜蜜回应"
            Emotion.HAPPY -> "愉快回应"
            else -> "正常"
        }

        return EmotionContext(detectedEmotion, confidence, suggestedTone)
    }
}
