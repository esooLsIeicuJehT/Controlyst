package com.example.injector

import android.content.pm.PackageManager
import android.graphics.PointF
import android.util.Log
import com.example.model.PrivilegeMethod
import rikka.shizuku.Shizuku

class ShizukuInjector : InputInjector {
    override val method: PrivilegeMethod = PrivilegeMethod.SHIZUKU

    override fun isAvailable(): Boolean {
        return try {
            if (!Shizuku.pingBinder()) return false
            if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) return false
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun injectTap(x: Float, y: Float): Boolean {
        if (!isAvailable()) {
            throw IllegalStateException("Shizuku service is not running or permission not granted.")
        }
        val cmd = "input tap ${x.toInt()} ${y.toInt()}"
        return executeRishCommand(cmd)
    }

    override fun injectDrag(path: List<PointF>, durationMs: Long): Boolean {
        if (!isAvailable()) {
            throw IllegalStateException("Shizuku service is not running or permission not granted.")
        }
        if (path.size < 2) return false
        val start = path.first()
        val end = path.last()
        val cmd = "input swipe ${start.x.toInt()} ${start.y.toInt()} ${end.x.toInt()} ${end.y.toInt()} $durationMs"
        return executeRishCommand(cmd)
    }

    override fun injectKeyEvent(keyCode: Int, action: Int): Boolean {
        if (!isAvailable()) {
            throw IllegalStateException("Shizuku service is not running or permission not granted.")
        }
        val cmd = "input keyevent $keyCode"
        return executeRishCommand(cmd)
    }

    private fun executeRishCommand(command: String): Boolean {
        return try {
            // Using Shizuku rish ADB shell bridge
            val process = Runtime.getRuntime().exec(arrayOf("rish", "-c", command))
            val exitCode = process.waitFor()
            exitCode == 0
        } catch (e: Exception) {
            Log.e("ShizukuInjector", "Failed to execute rish command: ${e.message}", e)
            false
        }
    }

    override fun cleanup() {
        Log.d("ShizukuInjector", "Shizuku rish injector session closed")
    }
}
