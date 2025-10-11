import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

const val TELEGRAM_API_BASE = "https://api.telegram.org/bot"

class TelegramBotService(private val botToken: String) {

    private val httpClient: HttpClient = HttpClient.newBuilder().build()
    private val baseUrl = "$TELEGRAM_API_BASE$botToken"

    fun getUpdates(offset: Long): String {
        val urlGetUpdates = "$baseUrl/getUpdates?offset=$offset&timeout=60"
        val request = HttpRequest.newBuilder()
            .uri(URI.create(urlGetUpdates))
            .GET()
            .build()
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendMessage(chatId: Long, text: String): String {
        if (text.isEmpty() || text.length > 4096) {
            return ""
        }

        val encoded = URLEncoder.encode(text, StandardCharsets.UTF_8.toString())
        val url = "$baseUrl/sendMessage?chat_id=$chatId&text=$encoded"
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build()
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    companion object {
        val UPDATE_OBJECT_REGEX = Regex("(?s)\\{.*?\\}(?=,\\s*\\{|\\s*\\])")

        fun extractUpdateId(updateBlock: String): Long? =
            Regex("\"update_id\"\\s*:\\s*(\\d+)").find(updateBlock)?.groupValues?.get(1)?.toLongOrNull()

        fun extractChatId(updateBlock: String): Long? {
            // Ищем "chat":{ ... "id": 12345 ... }
            val m = Regex("\"chat\"\\s*:\\s*\\{.*?\"id\"\\s*:\\s*(\\d+).*?\\}", RegexOption.DOT_MATCHES_ALL).find(
                updateBlock
            )
            return m?.groupValues?.get(1)?.toLongOrNull()
        }

        fun extractMessageText(updateBlock: String): String? {
            val m = Regex("\"text\"\\s*:\\s*\"(.*?)\"", RegexOption.DOT_MATCHES_ALL).find(updateBlock)
            val raw = m?.groupValues?.get(1) ?: return null
            return unescapeJsonString(raw)
        }

        private fun unescapeJsonString(s: String): String {
            var result = s
            val unicodeRegex = Regex("""\\u([0-9a-fA-F]{4})""")
            result = unicodeRegex.replace(result) { m ->
                val code = m.groupValues[1].toInt(16)
                code.toChar().toString()
            }
            result = result.replace("\\n", "\n")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\t", "\t")
                .replace("\\r", "\r")
            return result
        }
    }
}