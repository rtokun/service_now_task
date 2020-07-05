package com.example.servicenowstarterproject

import android.app.Application
import com.example.servicenowstarterproject.sdk.ServiceNowSDK

class ServiceNowStarterApplication : Application(){

    override fun onCreate() {
        super.onCreate()
        ServiceNowSDK.init(this, isLoggable = true)
    }

}