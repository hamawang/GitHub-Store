package zed.rainxch.core.data.services

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import co.touchlab.kermit.Logger
import zed.rainxch.core.domain.system.AggressiveOemDetector

class AndroidAggressiveOemDetector(
    private val context: Context,
) : AggressiveOemDetector {
    override fun isAggressiveOem(): Boolean {
        val brand = (Build.BRAND ?: "").lowercase()
        val manufacturer = (Build.MANUFACTURER ?: "").lowercase()
        return AGGRESSIVE_OEMS.any { it in brand || it in manufacturer }
    }

    override fun isBatteryOptimizationIgnored(): Boolean {
        val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return false
        return pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    override fun openBatteryOptimizationSettings(): Boolean =
        // The targeted intent (`ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`)
        // bypasses Play Store policy on standalone APKs and lands the user
        // directly on the system whitelist toggle — but it's gated by the
        // `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` permission, which Play
        // restricts. Fall back to the per-app battery-optimization screen
        // if the targeted action throws on a packaged build.
        runCatching {
            val intent =
                Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            context.startActivity(intent)
            true
        }.getOrElse {
            Logger.w(it) {
                "AggressiveOemDetector: targeted battery-optimization intent failed; opening generic screen"
            }
            runCatching {
                val fallback =
                    Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                context.startActivity(fallback)
                true
            }.getOrElse { fallbackError ->
                Logger.w(fallbackError) {
                    "AggressiveOemDetector: fallback battery-optimization screen also failed"
                }
                false
            }
        }

    private companion object {
        // Substring matches against `Build.BRAND` / `Build.MANUFACTURER` —
        // covers vendor sub-brands (BBK group: Oppo, OnePlus, Realme,
        // vivo, iQOO; Xiaomi: Redmi, Poco; Huawei: Honor).
        private val AGGRESSIVE_OEMS =
            listOf(
                "oppo",
                "oneplus",
                "realme",
                "xiaomi",
                "redmi",
                "poco",
                "vivo",
                "iqoo",
                "huawei",
                "honor",
                "meizu",
            )
    }
}
