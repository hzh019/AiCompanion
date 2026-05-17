package com.aicompanion.audio

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioDeviceInfo
import android.media.AudioManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

enum class AudioRoute {
    SPEAKER, EARPIECE, BLUETOOTH_SCO, BLUETOOTH_A2DP, WIRED_HEADSET
}

@Singleton
class AudioDeviceRouter @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val _currentRoute = MutableStateFlow(AudioRoute.SPEAKER)
    val currentRoute: StateFlow<AudioRoute> = _currentRoute.asStateFlow()

    private var bluetoothHeadset: BluetoothHeadset? = null

    private val btProfileListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
            if (profile == BluetoothProfile.HEADSET) {
                bluetoothHeadset = proxy as? BluetoothHeadset
            }
        }
        override fun onServiceDisconnected(profile: Int) {
            if (profile == BluetoothProfile.HEADSET) {
                bluetoothHeadset = null
            }
        }
    }

    init {
        // Connect to Bluetooth headset profile
        BluetoothAdapter.getDefaultAdapter()?.let { adapter ->
            // Check BLUETOOTH_CONNECT permission before calling (requires API 31+)
            try {
                adapter.getProfileProxy(context, btProfileListener, BluetoothProfile.HEADSET)
            } catch (e: Exception) {
                // Permission not granted yet
            }
        }
    }

    fun routeToSpeaker() {
        audioManager.mode = AudioManager.MODE_NORMAL
        audioManager.isSpeakerphoneOn = true
        audioManager.stopBluetoothSco()
        _currentRoute.value = AudioRoute.SPEAKER
    }

    fun routeToEarpiece() {
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        audioManager.isSpeakerphoneOn = false
        audioManager.stopBluetoothSco()
        _currentRoute.value = AudioRoute.EARPIECE
    }

    fun startBluetoothSco(): Boolean {
        if (audioManager.isBluetoothScoAvailableOffCall) {
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
            audioManager.isBluetoothScoOn = true
            audioManager.startBluetoothSco()
            _currentRoute.value = AudioRoute.BLUETOOTH_SCO
            return true
        }
        return false
    }

    fun stopBluetoothSco() {
        audioManager.stopBluetoothSco()
        audioManager.isBluetoothScoOn = false
    }

    fun isBluetoothScoOn(): Boolean = audioManager.isBluetoothScoOn

    fun getConnectedBluetoothDevice(): BluetoothDevice? {
        return bluetoothHeadset?.connectedDevices?.firstOrNull()
    }

    fun hasBluetoothSco(): Boolean {
        return audioManager.isBluetoothScoAvailableOffCall
    }

    fun destroy() {
        bluetoothHeadset?.let { hs ->
            BluetoothAdapter.getDefaultAdapter()?.closeProfileProxy(BluetoothProfile.HEADSET, hs)
        }
        bluetoothHeadset = null
    }
}
