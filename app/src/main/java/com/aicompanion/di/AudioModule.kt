package com.aicompanion.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Audio module for Hilt DI.
 *
 * AudioCaptureManager, AudioPlayer, AudioDeviceRouter, and AudioFocusManager
 * are all provided via @Inject @Singleton on their respective classes in the
 * com.aicompanion.audio package.
 *
 * This module is a placeholder for any future audio-related bindings that
 * cannot be satisfied via constructor injection.
 */
@Module
@InstallIn(SingletonComponent::class)
object AudioModule {
    // All audio classes in com.aicompanion.audio use @Inject @Singleton.
    // No additional @Provides needed here.
}
