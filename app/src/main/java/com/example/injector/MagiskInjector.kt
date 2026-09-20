package com.example.injector

import android.graphics.PointF
import android.util.Log
import com.example.model.PrivilegeMethod
import java.io.File

class MagiskInjector : InputInjector {
    override val method: PrivilegeMethod = PrivilegeMethod.MAGISK

    override fun isAvailable(): Boolean {
        return try {
            val paths = listOf(
                "/system/xbin/su",
                "/system/bin/su",
                "/sbin/su",
                "/data/local/xbin/su",
                "/data/local/bin/su"
            )
            val suFound = paths.any { File(it).exists() }
            if (!suFound) return false
            // Verify su binary actually executes successfully
            val p = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
            val exitCode = p.waitFor()
            exitCode == 0
        } catch (e: Exception) {
            false
        }
    }

    override fun injectTap(x: Float, y: Float): Boolean {
        val cmd = "input tap ${x.toInt()} ${y.toInt()}"
        return executeSu(cmd)
    }

    override fun injectDrag(path: List<PointF>, durationMs: Long): Boolean {
        if (path.size < 2) return false
        val start = path.first()
        val end = path.last()
        val cmd = "input swipe ${start.x.toInt()} ${start.y.toInt()} ${end.x.toInt()} ${end.y.toInt()} $durationMs"
        return executeSu(cmd)
    }

    override fun injectKeyEvent(keyCode: Int, action: Int): Boolean {
        val cmd = "input keyevent $keyCode"
        return executeSu(cmd)
    }

    private fun executeSu(command: String): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", command))
            val exitCode = process.waitFor()
            exitCode == 0
        } catch (e: Exception) {
            Log.e("MagiskInjector", "Magisk su execution failed: ${e.message}", e)
            false
        }
    }

    override fun cleanup() {
        Log.d("MagiskInjector", "Magisk injector cleaned up")
    }
}
