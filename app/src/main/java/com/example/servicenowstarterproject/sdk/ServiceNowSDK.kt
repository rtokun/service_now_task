package com.example.servicenowstarterproject.sdk

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.gson.Gson

class ServiceNowSDK {

    companion object {

        private var initiated: Boolean = false

        var isLoggable: Boolean = true

        private lateinit var context: Application

        private val gson = Gson()

        private val fileManager: FileManager = FileManagerImpl()

        private val snapshotManager: SnapshotManager = PixelCopySnapshotManager()

        private lateinit var sessionActivities: MutableList<Pair<String, Long>>

        @Throws(AlreadyInitializedError::class)
        fun init(context: Application, isLoggable: Boolean = true) {

            if (initiated) {
                throw AlreadyInitializedError()
            }

            this.context = context
            this.isLoggable = isLoggable

            registerSessionObserver()
            registerActivitiesObserver()
            this.initiated = true
        }

        private fun registerActivitiesObserver() {
            context.registerActivityLifecycleCallbacks(object : EmptyLifeCycleCallbacks() {

                override fun onActivityResumed(p0: Activity) {
                    super.onActivityResumed(p0)
                    persistActivityResumed(p0)
                    takeSnapshot(p0)
                }
            })
        }

        private fun takeSnapshot(activity: Activity) {
            Handler().postDelayed(Runnable {
                activity.window.peekDecorView()?.let { rootView ->
                    snapshotManager.getScreenShot(rootView, activity) {
                        val screenShotId = getScreenShotId(activity)
                        fileManager.storeScreenShot(it, screenShotId, context)
                    }
                }
            }, 100)
        }

        private fun getScreenShotId(activity: Activity): String {
            return "${activity}_${System.currentTimeMillis()}"
        }

        private fun persistActivityResumed(activity: Activity) {
            sessionActivities.add(Pair(activity.toString(), System.currentTimeMillis()))
        }

        private fun registerSessionObserver() {
            ProcessLifecycleOwner.get().lifecycle.addObserver(object : LifecycleObserver {

                @OnLifecycleEvent(Lifecycle.Event.ON_START)
                fun onMoveToForeground() { // app moved to foreground
                    if (isLoggable) Log.i(ServiceNowSDKTag, "Session started")

                    createSession()
                }

                @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
                fun onMoveToBackground() { // app moved to background
                    if (isLoggable) Log.i(ServiceNowSDKTag, "Session ended")

                    persistSession()
                }
            })
        }

        private fun persistSession() {
            val jsonString = convertSessionToJson()
            saveToFile(jsonString, getSessionId())
        }

        private fun getSessionId(): String {
            return System.currentTimeMillis().toString()
        }

        private fun saveToFile(jsonString: String, sessionId: String) {
            fileManager.saveSessionToFile(jsonString, sessionId, context)
        }

        private fun convertSessionToJson(): String {
            return gson.toJson(sessionActivities)
        }

        private fun createSession() {
            sessionActivities = mutableListOf()
        }
    }
}

private open class EmptyLifeCycleCallbacks() : ActivityLifecycleCallbacks {

    override fun onActivityPaused(p0: Activity) {
    }

    override fun onActivityStarted(p0: Activity) {
    }

    override fun onActivityDestroyed(p0: Activity) {
    }

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
    }

    override fun onActivityStopped(p0: Activity) {
    }

    override fun onActivityCreated(p0: Activity, p1: Bundle?) {
    }

    override fun onActivityResumed(p0: Activity) {
    }
}