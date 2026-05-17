package com.aicompanion.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object LlmModule {
    // DeepSeekClient in com.aicompanion.llm is provided via @Inject @Singleton
    // on its class directly. No additional bindings needed here.
}
