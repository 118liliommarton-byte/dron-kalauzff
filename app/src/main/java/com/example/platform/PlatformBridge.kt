package com.example.platform

enum class TargetPlatform {
    ANDROID,
    IOS
}

interface PlatformBridge {
    val platform: TargetPlatform
    val platformName: String
    val supportsExactSystemAlarms: Boolean
    val supportsBackgroundGpsTracking: Boolean
}

object CurrentPlatform : PlatformBridge {
    override val platform: TargetPlatform = TargetPlatform.ANDROID
    override val platformName: String = "Android"
    override val supportsExactSystemAlarms: Boolean = true
    override val supportsBackgroundGpsTracking: Boolean = true
}
