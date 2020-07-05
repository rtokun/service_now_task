package com.example.servicenowstarterproject.sdk

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.util.Log
import android.view.PixelCopy
import android.view.View

interface SnapshotManager {
    fun getScreenShot(view: View, activity: Activity, callback: (Bitmap) -> Unit)
}

class PixelCopySnapshotManager : SnapshotManager {

    override fun getScreenShot(view: View, activity: Activity, callback: (Bitmap) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getScreenShotFromView(view, activity) {
                callback(it)
            }
        } else {
            callback(getScreenShot(view))
        }
    }

    // for api level 28
    private fun getScreenShotFromView(view: View, activity: Activity, callback: (Bitmap) -> Unit) {
        activity.window?.let { window ->
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val locationOfViewInWindow = IntArray(2)
            view.getLocationInWindow(locationOfViewInWindow)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    PixelCopy.request(
                        window,
                        Rect(
                            locationOfViewInWindow[0],
                            locationOfViewInWindow[1],
                            locationOfViewInWindow[0] + view.width,
                            locationOfViewInWindow[1] + view.height
                        ), bitmap, { copyResult ->
                            if (copyResult == PixelCopy.SUCCESS) {
                                callback(bitmap)
                            } else {
                                Log.e(ServiceNowSDKTag, "Failed to create snapshot: $copyResult")
                            }
                        },
                        Handler()
                    )
                }
            } catch (e: IllegalArgumentException) {
                // PixelCopy may throw IllegalArgumentException, make sure to handle it
                e.printStackTrace()
            }
        }
    }

    // deprecated version
    /*  Method which will return Bitmap after taking screenshot. We have to pass the view which we want to take screenshot.  */
    private fun getScreenShot(view: View): Bitmap {
        val screenView = view.rootView
        screenView.isDrawingCacheEnabled = true
        val bitmap = Bitmap.createBitmap(screenView.drawingCache)
        screenView.isDrawingCacheEnabled = false
        return bitmap
    }
}