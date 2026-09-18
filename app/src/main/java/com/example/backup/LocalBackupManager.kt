package com.example.backup

import android.content.Context
import com.example.model.MappingConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

data class BackupMetadata(
    val appVersion: String = "1.0.0",
    val backupSchemaVersion: Int = 3,
    val timestamp: Long = System.currentTimeMillis(),
    val totalProfilesCount: Int = 0,
    val deviceModel: String = android.os.Build.MODEL ?: "Android Device"
)

object LocalBackupManager {

    suspend fun exportFullBackupZip(
        context: Context,
        configs: List<MappingConfig>
    ): File = withContext(Dispatchers.IO) {
        val backupDir = File(context.cacheDir, "backups").apply { mkdirs() }
        val backupZip = File(backupDir, "controlyst_backup_${System.currentTimeMillis()}.zip")
        if (backupZip.exists()) backupZip.delete()

        ZipOutputStream(BufferedOutputStream(FileOutputStream(backupZip))).use { zos ->
            // 1. metadata.json
            val metaJson = JSONObject().apply {
                put("appVersion", "1.0.0")
                put("backupSchemaVersion", 3)
                put("timestamp", System.currentTimeMillis())
                put("totalProfilesCount", configs.size)
                put("deviceModel", android.os.Build.MODEL)
                put("deviceManufacturer", android.os.Build.MANUFACTURER)
            }
            writeStringToZip(zos, "metadata.json", metaJson.toString(2))

            // 2. profiles/
            configs.forEach { cfg ->
                val cfgJson = JSONObject().apply {
                    put("id", cfg.id)
                    put("profileName", cfg.profileName)
                    put("gamePackage", cfg.gamePackage)
                    put("gameTitle", cfg.gameTitle)
                    put("controllerType", cfg.controllerType.name)
                    put("targetAspectRatio", cfg.targetAspectRatio)
                    put("buttonCount", cfg.buttons.size)
                }
                writeStringToZip(zos, "profiles/${cfg.id}.json", cfgJson.toString(2))
            }

            // 3. controllers/
            val controllersJson = JSONObject().apply {
                put("calibratedTypes", JSONArray(listOf("XBOX", "DUALSENSE", "STADIA", "SWITCH_PRO")))
                put("defaultInnerDeadzone", 0.15)
                put("defaultOuterDeadzone", 0.95)
            }
            writeStringToZip(zos, "controllers/calibrations.json", controllersJson.toString(2))

            // 4. macros/
            val macrosJson = JSONObject().apply {
                put("savedMacros", JSONArray())
                put("macroSafetyNotice", "Automated rapid-fire loops may violate online game TOS.")
            }
            writeStringToZip(zos, "macros/macros_library.json", macrosJson.toString(2))

            // 5. settings/
            val settingsJson = JSONObject().apply {
                put("overlayOpacity", 0.85)
                put("hapticsEnabled", true)
                put("floatingHudSize", "MEDIUM")
            }
            writeStringToZip(zos, "settings/app_preferences.json", settingsJson.toString(2))

            // 6. game_profiles/
            val gameProfilesJson = JSONObject().apply {
                put("linkedGameProfilesCount", configs.size)
            }
            writeStringToZip(zos, "game_profiles/game_bindings.json", gameProfilesJson.toString(2))

            // 7. performance_profiles/
            val perfProfilesJson = JSONObject().apply {
                put("activeMode", "GAMING")
                put("thermalLimitC", 42.0)
                put("autoGameSwitchingEnabled", true)
            }
            writeStringToZip(zos, "performance_profiles/root_performance.json", perfProfilesJson.toString(2))
        }

        backupZip
    }

    suspend fun validateAndInspectBackupZip(file: File): BackupMetadata? = withContext(Dispatchers.IO) {
        return@withContext try {
            ZipFile(file).use { zip ->
                val metaEntry = zip.getEntry("metadata.json") ?: return@withContext null
                val content = zip.getInputStream(metaEntry).bufferedReader().use { it.readText() }
                val json = JSONObject(content)
                BackupMetadata(
                    appVersion = json.optString("appVersion", "1.0.0"),
                    backupSchemaVersion = json.optInt("backupSchemaVersion", 1),
                    timestamp = json.optLong("timestamp", System.currentTimeMillis()),
                    totalProfilesCount = json.optInt("totalProfilesCount", 0),
                    deviceModel = json.optString("deviceModel", "Unknown")
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun writeStringToZip(zos: ZipOutputStream, path: String, content: String) {
        val entry = ZipEntry(path)
        zos.putNextEntry(entry)
        zos.write(content.toByteArray(Charsets.UTF_8))
        zos.closeEntry()
    }
}
