
package in.anuj.upisoundbox

import android.content.Context
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.speech.tts.TextToSpeech
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar

class PaymentNotificationListener : NotificationListenerService() {

    private var tts: TextToSpeech? = null
    private var ttsReady = false

    private val supportedApps = mapOf(
        "com.google.android.apps.nbu.paisa.user" to "Google Pay",
        "com.phonepe.app" to "PhonePe",
        "net.one97.paytm" to "Paytm",
        "in.org.npci.upiapp" to "BHIM"
    )

    override fun onCreate() {
        super.onCreate()
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsReady = true
                tts?.language = Locale("hi", "IN")
            }
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val source = supportedApps[sbn.packageName] ?: return
        val extras = sbn.notification.extras

        val title = extras.getCharSequence("android.title")?.toString().orEmpty()
        val body = extras.getCharSequence("android.text")?.toString().orEmpty()
        val bigText = extras.getCharSequence("android.bigText")?.toString().orEmpty()

        val combined = "$title $body $bigText"
        val lower = combined.lowercase(Locale.ROOT)

        val receivedWords = listOf(
            "received", "credited", "you got", " पैसे मिले",
            "प्राप्त", "जमा", "credited to", "payment from"
        )

        val sentWords = listOf(
            "sent", "paid", "debited", "you paid",
            "भेजे", "भुगतान किया", "कटे"
        )

        if (!receivedWords.any { lower.contains(it) } ||
            sentWords.any { lower.contains(it) }) return

        val amountRegex = Regex(
            "(?:₹|rs\\.?\\s*|inr\\s*)([0-9,]+(?:\\.[0-9]{1,2})?)",
            RegexOption.IGNORE_CASE
        )

        val amountText =
            amountRegex.find(combined)?.groupValues?.getOrNull(1) ?: return

        val amount = amountText.replace(",", "").toFloatOrNull() ?: return
        if (amount <= 0f) return

        val prefs = getSharedPreferences("upi_box", Context.MODE_PRIVATE)
        val dedupeKey = "seen_${sbn.packageName}_${sbn.key}_${sbn.postTime}"

        if (prefs.getBoolean(dedupeKey, false)) return

        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        val nightSilent =
            prefs.getBoolean("night_silent", false) &&
            (hour >= 22 || hour < 7)

        val time = SimpleDateFormat(
            "dd-MM-yyyy HH:mm",
            Locale("en", "IN")
        ).format(Date())

        val line = "₹${String.format(Locale.US, "%.2f", amount)} — $source — $time"

        val old = prefs.getString("history", "").orEmpty()
        val oldTotal = prefs.getFloat("total_received", 0f)

        prefs.edit()
            .putBoolean(dedupeKey, true)
            .putFloat("total_received", oldTotal + amount)
            .putString("history", (line + "\n" + old).take(4000))
            .apply()

        if (!nightSilent && prefs.getBoolean("voice_on", true) && ttsReady) {
            tts?.speak(
                "${amountText.replace(",", "")} रुपये प्राप्त हुए।",
                TextToSpeech.QUEUE_ADD,
                null,
                "upi_${sbn.postTime}"
            )
        }

        if (!nightSilent && prefs.getBoolean("sound_on", true)) {
            try {
                val tone = android.media.ToneGenerator(
                    android.media.AudioManager.STREAM_NOTIFICATION, 70
                )
                tone.startTone(
                    android.media.ToneGenerator.TONE_PROP_BEEP, 180
                )
                tone.release()
            } catch (_: Exception) { }
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        super.onDestroy()
    }
}
