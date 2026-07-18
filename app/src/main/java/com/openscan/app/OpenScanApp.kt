package com.openscan.app

import android.app.Application
import com.openscan.app.image.OpenCvSupport
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class OpenScanApp : Application() {
    override fun onCreate() {
        super.onCreate()
        OpenCvSupport.init()
    }
}
