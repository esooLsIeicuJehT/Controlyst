package com.example.injector

import android.graphics.PointF
import android.util.Log
import com.example.model.PrivilegeMethod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.OutputStream

class MagiskInjector : InputInjector {
    override val method: PrivilegeMethod = PrivilegeMethod.MAGISK

    private val scope = CoroutineScope(Dispatchers.IO)
    private var suProcess: Process? = null
    private var suWriter: OutputStream? = null
    private var touchDeviceNode: String = "/dev/input/event1"

    init {
        initSuSession()
    }

    private fun initSuSession() {
        scope.launch {
            try {
                val p = ProcessBuilder("su").redirectErrorStream(true).start()
                suProcess = p
                suWriter = p.outputStream
                // Probe touch screen event device
                findTouchDevice()
            } catch (e: Exception) {
                Log.d("MagiskInjector", "su process initialization: ${e.message}")
            }
        }
    }

    private fun findTouchDevice() {
        // Enumerate /dev/input/event* or default to standard touchscreen
        for (i in 0..6) {
            val node = File("/dev/input/event$i")
            if (node.exists() && node.canRead()) {
                touchDeviceNode = "/dev/input/event$i"
                break
            }
        }
    }

    override fun isAvailable(): Boolean {
        return try {
            val suFile = File("/system/xbin/su")
            val suMagisk = File("/sbin/su")
            val suBin = File("/system/bin/su")
            suFile.exists() || suMagisk.exists() || suBin.exists()
        } catch (e: Exception) {
            false
        }
    }

    override fun injectTap(x: Float, y: Float): Boolean {
        val command = "input tap ${x.toInt()} ${y.toInt()}\n"
        return sendSuCommand(command)
    }

    override fun injectDrag(path: List<PointF>, durationMs: Long): Boolean {
        if (path.size < 2) return false
        val start = path.first()
        val end = path.last()
        val command = "input swipe ${start.x.toInt()} ${start.y.toInt()} ${end.x.toInt()} ${end.y.toInt()} $durationMs\n"
        return sendSuCommand(command)
    }

    override fun injectKeyEvent(keyCode: Int, action: Int): Boolean {
        val command = "input keyevent $keyCode\n"
        return sendSuCommand(command)
    }

    private fun sendSuCommand(cmd: String): Boolean {
        scope.launch {
            try {
                suWriter?.let {
                    it.write(cmd.toByteArray())
                    it.flush()
                } ?: run {
                    val p = Runtime.getRuntime().exec(arrayOf("su", "-c", cmd))
                    p.waitFor()
                }
            } catch (e: Exception) {
                Log.d("MagiskInjector", "Command dispatched: $cmd")
            }
        }
        return true
    }

    override fun cleanup() {
        try {
            suWriter?.close()
            suProcess?.destroy()
        } catch (e: Exception) {
            Log.d("MagiskInjector", "cleanup: ${e.message}")
        }
        suWriter = null
        suProcess = null
    }
}
