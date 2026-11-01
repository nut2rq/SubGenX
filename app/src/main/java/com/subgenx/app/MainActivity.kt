package com.subgenx.app

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.content.Intent
import android.net.Uri
import android.graphics.Typeface
import android.view.View
import android.widget.*
import android.util.Base64
import org.json.JSONObject
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

class MainActivity : Activity() {
    private var selectedAudio: Uri? = null
    private lateinit var fileLabel: TextView
    private lateinit var language: Spinner
    private lateinit var status: TextView
    private lateinit var resultText: TextView
    private lateinit var keyButton: Button
    private lateinit var openRouterKeyButton: Button
    private lateinit var translationProvider: Spinner
    private lateinit var openRouterModel: Spinner
    private val pickAudio = 101
    private val executor = Executors.newSingleThreadExecutor()
    private val prefs by lazy { getSharedPreferences("subgenx_settings", MODE_PRIVATE) }
    private val apiKey get() = prefs.getString("gemini_api_key", "") ?: ""
    private val openRouterApiKey get() = prefs.getString("openrouter_api_key", "") ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = 0xFFF7F8FC.toInt()
        window.navigationBarColor = 0xFFF7F8FC.toInt()
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(22), dp(24), dp(22), dp(18)); setBackgroundColor(0xFFF7F8FC.toInt()) }
        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, 0, 0, dp(20)) }
        content.addView(TextView(this).apply { text = "SubGenX"; textSize = 30f; setTextColor(0xFF172033.toInt()); setTypeface(null, Typeface.BOLD) })
        content.addView(TextView(this).apply { text = "Audio → subtitles, made simple"; textSize = 15f; setTextColor(0xFF687386.toInt()); setPadding(0, dp(4), 0, dp(20)) })
        keyButton = Button(this).apply { text = if (apiKey.isBlank()) "Set Gemini API key" else "Gemini API key saved · Change"; isAllCaps = false; setOnClickListener { showApiKeyDialog() } }
        content.addView(keyButton, matchWrap())
        content.addView(TextView(this).apply { text = "Your key stays on this device. Never share it or commit it to GitHub."; textSize = 12f; setTextColor(0xFF687386.toInt()); setPadding(0, dp(4), 0, dp(18)) })
        openRouterKeyButton = Button(this).apply { text = if (openRouterApiKey.isBlank()) "Set OpenRouter API key (optional)" else "OpenRouter API key saved · Change"; isAllCaps = false; setOnClickListener { showOpenRouterKeyDialog() } }
        content.addView(openRouterKeyButton, matchWrap())
        content.addView(TextView(this).apply { text = "Translation provider"; textSize = 16f; setTypeface(null, Typeface.BOLD); setTextColor(0xFF172033.toInt()); setPadding(0, dp(14), 0, dp(4)) })
        translationProvider = Spinner(this)
        translationProvider.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOf("Google Gemini", "OpenRouter AI"))
        content.addView(translationProvider, matchWrap())
        content.addView(TextView(this).apply { text = "OpenRouter model"; textSize = 16f; setTypeface(null, Typeface.BOLD); setTextColor(0xFF172033.toInt()); setPadding(0, dp(8), 0, dp(4)) })
        openRouterModel = Spinner(this)
        openRouterModel.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOf("Primary · Qwen 2.5 Coder 32B Free", "Uncensor option · Llama 3.1 8B Free"))
        content.addView(openRouterModel, matchWrap())
        content.addView(TextView(this).apply { text = "Create subtitles"; textSize = 21f; setTextColor(0xFF172033.toInt()); setTypeface(null, Typeface.BOLD) })
        content.addView(TextView(this).apply { text = "Choose an audio file, then select the language you want."; textSize = 14f; setTextColor(0xFF687386.toInt()); setPadding(0, dp(6), 0, dp(18)) })
        content.addView(Button(this).apply { text = "Choose audio file"; isAllCaps = false; setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply { addCategory(Intent.CATEGORY_OPENABLE); type = "audio/*" }
            startActivityForResult(intent, pickAudio)
        } }, matchWrap())
        fileLabel = TextView(this).apply { text = "No file selected"; textSize = 14f; setTextColor(0xFF687386.toInt()); setPadding(0, dp(12), 0, dp(24)) }
        content.addView(fileLabel)
        content.addView(TextView(this).apply { text = "Translate into"; textSize = 16f; setTypeface(null, Typeface.BOLD); setTextColor(0xFF172033.toInt()); setPadding(0, 0, 0, dp(8)) })
        language = Spinner(this)
        language.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOf("Thai", "English", "Chinese (Simplified)", "Japanese", "Korean", "Spanish", "French", "German"))
        content.addView(language, matchWrap())
        content.addView(Button(this).apply { text = "Test Gemini API"; isAllCaps = false; setOnClickListener { testApi() } }, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(12) })
        content.addView(Button(this).apply { text = "Start transcription & translation"; isAllCaps = false; setOnClickListener { startTranscription() } }, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(8) })
        status = TextView(this).apply { text = "Ready. Add your Gemini API key to begin."; textSize = 14f; setTextColor(0xFF687386.toInt()); setPadding(0, dp(16), 0, dp(12)) }
        content.addView(status)
        resultText = TextView(this).apply { textSize = 14f; setTextColor(0xFF172033.toInt()); setPadding(dp(12), dp(12), dp(12), dp(12)); setBackgroundColor(0xFFECEFF6.toInt()) }
        content.addView(resultText)
        content.addView(TextView(this).apply { text = "Cloud processing sends your audio to Google and uses your account quota and terms."; textSize = 12f; setTextColor(0xFF687386.toInt()); setPadding(0, dp(14), 0, dp(10)) })
        scroll.addView(content); root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f)); setContentView(root)
    }

    private fun showApiKeyDialog() {
        val input = EditText(this).apply { hint = "Paste Gemini API key"; inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD; setText(apiKey) }
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(20), dp(8), dp(20), 0); addView(input) }
        AlertDialog.Builder(this).setTitle("Gemini API key").setMessage("Create a key in Google AI Studio. Do not share it. This prototype stores it in app preferences on this device.")
            .setView(box).setPositiveButton("Save") { _, _ ->
                val value = input.text.toString().trim()
                if (value.isNotBlank()) { prefs.edit().putString("gemini_api_key", value).apply(); keyButton.text = "Gemini API key saved · Change"; status.text = "Key saved. Tap Test Gemini API." }
                else { prefs.edit().remove("gemini_api_key").apply(); keyButton.text = "Set Gemini API key"; status.text = "API key cleared." }
            }.setNegativeButton("Cancel", null).show()
    }

    private fun showOpenRouterKeyDialog() {
        val input = EditText(this).apply { hint = "Paste OpenRouter API key"; inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD; setText(openRouterApiKey) }
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(20), dp(8), dp(20), 0); addView(input) }
        AlertDialog.Builder(this).setTitle("OpenRouter API key").setMessage("Create a key in OpenRouter. Keep it private. This prototype stores it in app preferences on this device.")
            .setView(box).setPositiveButton("Save") { _, _ ->
                val value = input.text.toString().trim()
                if (value.isNotBlank()) { prefs.edit().putString("openrouter_api_key", value).apply(); openRouterKeyButton.text = "OpenRouter API key saved · Change"; status.text = "OpenRouter key saved." }
                else { prefs.edit().remove("openrouter_api_key").apply(); openRouterKeyButton.text = "Set OpenRouter API key (optional)"; status.text = "OpenRouter API key cleared." }
            }.setNegativeButton("Cancel", null).show()
    }

    private fun testOpenRouterApi() {
        if (openRouterApiKey.isBlank()) { status.text = "Set your OpenRouter API key first."; return }
        status.text = "Testing OpenRouter API…"
        executor.execute {
            val message = try {
                val conn = (URL("https://openrouter.ai/api/v1/models").openConnection() as HttpURLConnection)
                conn.requestMethod = "GET"; conn.connectTimeout = 15000; conn.readTimeout = 15000
                conn.setRequestProperty("Authorization", "Bearer " + openRouterApiKey)
                val code = conn.responseCode
                val body = (if (code in 200..299) conn.inputStream else conn.errorStream)?.bufferedReader()?.use { it.readText() } ?: ""
                conn.disconnect()
                if (code in 200..299) "OpenRouter API connection successful. Key is accepted." else "OpenRouter API error ($code): " + extractError(body)
            } catch (e: Exception) { "OpenRouter connection failed: " + (e.message ?: e.javaClass.simpleName) }
            runOnUiThread { status.text = message }
        }
    }
    private fun testApi() {
        if (apiKey.isBlank()) { status.text = "First tap Set Gemini API key."; return }
        status.text = "Testing connection to Gemini…"
        executor.execute {
            val message = try {
                val conn = (URL("https://generativelanguage.googleapis.com/v1beta/models?key=" + apiKey).openConnection() as HttpURLConnection)
                conn.requestMethod = "GET"; conn.connectTimeout = 15000; conn.readTimeout = 15000
                val code = conn.responseCode
                val body = (if (code in 200..299) conn.inputStream else conn.errorStream)?.bufferedReader()?.use { it.readText() } ?: ""
                conn.disconnect()
                if (code in 200..299) "Gemini API connection successful. Key is accepted." else "Gemini API error ($code): " + extractError(body)
            } catch (e: Exception) { "Connection failed: " + (e.message ?: e.javaClass.simpleName) }
            runOnUiThread { status.text = message }
        }
    }

    private fun startTranscription() {
        if (apiKey.isBlank()) { status.text = "Set your Gemini API key first. Gemini is used for audio transcription."; return }
        if (translationProvider.selectedItemPosition == 1 && openRouterApiKey.isBlank()) { status.text = "Set your OpenRouter API key first to use OpenRouter for translation."; return }
        val uri = selectedAudio
        if (uri == null) { status.text = "Choose an audio file first."; return }
        status.text = "Reading audio file…"; resultText.text = ""
        executor.execute {
            try {
                val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: error("Cannot read selected file")
                if (bytes.size > 18 * 1024 * 1024) error("File is larger than 18 MB. Choose a smaller audio file for this prototype.")
                val mime = contentResolver.getType(uri) ?: "audio/mpeg"
                runOnUiThread { status.text = "Uploading audio to Gemini for transcription… (${bytes.size / 1024} KB)" }
                val transcribed = callGemini("gemini-3.5-transcribe", JSONArray()
                    .put(JSONObject().put("text", "Transcribe the speech in this audio. Detect the spoken language automatically. Return only the spoken words as a transcript, without commentary."))
                    .put(JSONObject().put("inline_data", JSONObject().put("mime_type", mime).put("data", Base64.encodeToString(bytes, Base64.NO_WRAP)))))
                runOnUiThread { status.text = "Translating transcript to ${language.selectedItem}…" }
                val translated = callGemini("gemini-3.8-flash", JSONArray().put(JSONObject().put("text", "Translate the following transcript into ${language.selectedItem}. Return only the translation and preserve paragraph breaks.\n\n" + transcribed)))
                runOnUiThread { resultText.text = "TRANSCRIPT\n\n$transcribed\n\nTRANSLATION — ${language.selectedItem}\n\n$translated"; status.text = "Done. Review below. Timed SRT export is not implemented yet." }
            } catch (e: Exception) { runOnUiThread { status.text = "Processing failed: " + (e.message ?: e.javaClass.simpleName) } }
        }
    }

    private fun callGemini(model: String, parts: JSONArray): String {
        var lastError: Exception? = null
        val delays = longArrayOf(2000L, 5000L, 10000L)
        for (attempt in 0..3) {
            try {
                return callGeminiOnce(model, parts)
            } catch (e: Exception) {
                lastError = e
                val message = e.message.orEmpty()
                val temporary = message.contains("Gemini HTTP 503") ||
                    message.contains("Gemini HTTP 429") ||
                    message.contains("Gemini HTTP 500") ||
                    message.contains("Gemini HTTP 502")
                if (!temporary || attempt == 3) throw e
                try { Thread.sleep(delays[attempt]) } catch (_: InterruptedException) {
                    Thread.currentThread().interrupt()
                    throw Exception("Request cancelled.")
                }
            }
        }
        throw lastError ?: Exception("Gemini request failed.")
    }

    private fun callGeminiOnce(model: String, parts: JSONArray): String {
        val conn = (URL("https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=" + apiKey).openConnection() as HttpURLConnection)
        conn.requestMethod = "POST"; conn.connectTimeout = 20000; conn.readTimeout = 120000; conn.doOutput = true
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
        val request = JSONObject().put("contents", JSONArray().put(JSONObject().put("parts", parts)))
        if (model == "gemini-3.5-transcribe") {
            request.put("generationConfig", JSONObject().put("audioTranscriptionConfig", JSONObject().put("mode", "VERBATIM")))
        }
        val body = request.toString()
        conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
        val code = conn.responseCode
        val response = (if (code in 200..299) conn.inputStream else conn.errorStream)?.bufferedReader()?.use { it.readText() } ?: ""
        conn.disconnect()
        if (code !in 200..299) throw Exception("Gemini HTTP $code: " + extractError(response))
        val json = JSONObject(response)
        val candidates = json.optJSONArray("candidates")
        if (candidates == null || candidates.length() == 0) {
            throw Exception("Gemini returned no candidate. Response: " + response.take(500))
        }
        val candidate = candidates.optJSONObject(0) ?: throw Exception("Gemini returned an empty candidate.")
        val responseParts = candidate.optJSONObject("content")?.optJSONArray("parts")
        val output = StringBuilder()
        if (responseParts != null) {
            for (i in 0 until responseParts.length()) {
                val part = responseParts.optJSONObject(i) ?: continue
                val text = part.optString("text", "")
                if (text.isNotBlank()) output.append(text)
                val audioTx = part.optJSONObject("audioTranscription")
                if (audioTx != null) {
                    val txText = audioTx.optString("text", "")
                    if (txText.isNotBlank()) output.append(txText)
                    val words = audioTx.optJSONArray("words")
                    if (txText.isBlank() && words != null) {
                        for (w in 0 until words.length()) {
                            val word = words.optJSONObject(w)?.optString("word", "") ?: ""
                            if (word.isNotBlank()) output.append(word).append(' ')
                        }
                    }
                }
            }
        }
        return output.toString().trim().takeIf { it.isNotBlank() }
            ?: throw Exception("Gemini response had no transcript text. Finish reason: " + candidate.optString("finishReason", "unknown") + ". Raw response: " + response.take(700))
    }
    private fun extractError(body: String): String = try { JSONObject(body).optJSONObject("error")?.optString("message") ?: body.take(300) } catch (_: Exception) { body.take(300) }

    @Deprecated("Deprecated in Android")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == pickAudio && resultCode == RESULT_OK) {
            selectedAudio = data?.data
            selectedAudio?.let { uri ->
                try { contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) {}
                fileLabel.text = queryDisplayName(uri) ?: uri.lastPathSegment ?: "Audio file selected"
                status.text = "File selected. Ready to process."
            }
        }
    }
    private fun queryDisplayName(uri: Uri): String? = try {
        contentResolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)?.use { if (it.moveToFirst()) it.getString(0) else null }
    } catch (_: Exception) { null }
    override fun onDestroy() { executor.shutdownNow(); super.onDestroy() }
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
    private fun matchWrap() = LinearLayout.LayoutParams(-1, -2)
}
