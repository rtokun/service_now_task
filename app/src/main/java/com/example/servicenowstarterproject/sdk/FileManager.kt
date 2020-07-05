package com.example.servicenowstarterproject.sdk

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.IOException

interface FileManager {
    fun saveSessionToFile(session: String, sessionId: String, context: Context)
    fun storeScreenShot(bm: Bitmap?, screenshotId: String, context: Context): File
}

class FileManagerImpl : FileManager {

    override fun saveSessionToFile(session: String, sessionId: String, context: Context) {
        try {
            val folder = File(context.applicationInfo.dataDir + "/service_now_sessions/")
            if (!folder.exists()) {
                folder.mkdir()
            }

            val file = FileWriter(folder.absolutePath + "/session_$sessionId.txt")
            file.write(session)
            file.flush()
            file.close()
        } catch (ioException: IOException) {
            Log.e(ServiceNowSDKTag, "Failed to write session into file", ioException)
        }
    }

    override fun storeScreenShot(bm: Bitmap?, screenshotId: String, context: Context): File {
        val dir = File(context.applicationInfo.dataDir + "/service_now_screenshots/")
        if (!dir.exists())
            dir.mkdirs()

        val file = File(context.filesDir?.absolutePath, "$screenshotId.jpeg")

        try {
            val fOut = FileOutputStream(file)
            bm?.compress(Bitmap.CompressFormat.JPEG, 10, fOut)
            fOut.flush()
            fOut.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return file
    }

}