package com.example.injector

import android.graphics.PointF
import android.util.Log
import com.example.model.PrivilegeMethod
import java.io.File

class APatchInjector : InputInjector {
    override val method: PrivilegeMethod = PrivilegeMethod.APATCH

    override fun isAvailable(): Boolean {
        return try {
            val apDaemon = File("/data/adb/apd")
            val apDir = File("/data/adb/ap")
            if (!apDaemon.exists() && !apDir.exists()) return false

            val p = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
            p.waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }

    override fun injectTap(x: Float, y: Float): Boolean {
        val cmd = "input tap ${x.toInt()} ${y.toInt()}"
        return executeAPatchCommand(cmd)
    }

    override fun injectDrag(path: List<PointF>, durationMs: Long): Boolean {
        if (path.size < 2) return false
        val start = path.first()
        val end = path.last()
        val cmd = "input swipe ${start.x.toInt()} ${start.y.toInt()} ${end.x.toInt()} ${end.y.toInt()} $durationMs"
        return executeAPatchCommand(cmd)
    }

    override fun injectKeyEvent(keyCode: Int, action: Int): Boolean {
        val cmd = "input keyevent $keyCode"
        return executeAPatchCommand(cmd)
    }

    private fun executeAPatchCommand(command: String): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", command))
            process.waitFor() == 0
        } catch (e: Exception) {
            Log.e("APatchInjector", "APatch command execution failed: ${e.message}", e)
            false
        }
    }

    override fun cleanup() {
        Log.d("APatchInjector", "APatch injector cleaned up")
    }
}
