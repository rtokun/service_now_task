package com.example.servicenowstarterproject.sdk

class AlreadyInitializedError :
    RuntimeException("Already initialized SDK, you should call ServiceNowSDK.init() only once")