package com.aicompanion.di

import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import com.aicompanion.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlin.time.Duration.Companion.seconds
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LlmModule {

    /**
     * Note: DeepSeekClient in com.aicompanion.llm is provided via @Inject @Singleton
     * on its class directly. This module provides the raw OpenAI client for other
     * components that may need direct API access.
     */
    private const val DEEPSEEK_HOST = "https://api.deepseek.com/v1"

    @Provides
    @Singleton
    fun provideOpenAI(): OpenAI {
        val apiKey = BuildConfig.DEEPSEEK_API_KEY.ifEmpty {
            // TODO: Replace with BuildConfig field or secure key retrieval
            "DEEPSEEK_API_KEY_PLACEHOLDER"
        }

        val config = OpenAIConfig(
            token = apiKey,
            host = DEEPSEEK_HOST,
            timeout = 60.seconds,
        )

        return OpenAI(config)
    }
}
