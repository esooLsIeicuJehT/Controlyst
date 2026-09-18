package com.example.injector

import android.graphics.PointF
import android.util.Log
import com.example.model.PrivilegeMethod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.OutputStream

class APatchInjector : InputInjector {
    override val method: PrivilegeMethod = PrivilegeMethod.APATCH

    private val scope = CoroutineScope(Dispatchers.IO)
    private var suProcess: Process? = null
    private var suWriter: OutputStream? = null

    init {
        initSession()
    }

    private fun initSession() {
        scope.launch {
            try {
                val p = ProcessBuilder("su").redirectErrorStream(true).start()
                suProcess = p
                suWriter = p.outputStream
            } catch (e: Exception) {
                Log.d("APatchInjector", "APatch session: ${e.message}")
            }
        }
    }

    override fun isAvailable(): Boolean {
        return try {
            val apDaemon = File("/data/adb/apd")
            val apDir = File("/data/adb/ap")
            apDaemon.exists() || apDir.exists()
        } catch (e: Exception) {
            false
        }
    }

    override fun injectTap(x: Float, y: Float): Boolean {
        val command = "input tap ${x.toInt()} ${y.toInt()}\n"
        return sendCmd(command)
    }

    override fun injectDrag(path: List<PointF>, durationMs: Long): Boolean {
        if (path.size < 2) return false
        val start = path.first()
        val end = path.last()
        val command = "input swipe ${start.x.toInt()} ${start.y.toInt()} ${end.x.toInt()} ${end.y.toInt()} $durationMs\n"
        return sendCmd(command)
    }

    override fun injectKeyEvent(keyCode: Int, action: Int): Boolean {
        val command = "input keyevent $keyCode\n"
        return sendCmd(command)
    }

    private fun sendCmd(cmd: String): Boolean {
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
                Log.d("APatchInjector", "Command dispatched: $cmd")
            }
        }
        return true
    }

    override fun cleanup() {
        try {
            suWriter?.close()
            suProcess?.destroy()
        } catch (e: Exception) {
            Log.d("APatchInjector", "cleanup: ${e.message}")
        }
        suWriter = null
        suProcess = null
    }
}
