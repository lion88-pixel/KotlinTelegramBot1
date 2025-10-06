import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

fun main(args: Array<String>) {

    val botToken = args[0]
    var updateId: Long = 0
    while (true) {
        Thread.sleep(2000)
        val updates: String = getUpdates(botToken, updateId)
        println(updates)

        val updateIdRegex = "\"update_id\":(\\d+)".toRegex()
        val updateIdMatches = updateIdRegex.findAll(updates)
        var newLastUpdateId: Long = updateId

        if (updateIdMatches.any()) {
            updateIdMatches.forEach { match ->
                val currentParsedUpdateId = match.groupValues[1].toLong()
                if (currentParsedUpdateId >= newLastUpdateId) {
                    newLastUpdateId = currentParsedUpdateId
                }
            }
            updateId = newLastUpdateId + 1
        } else {
            println("Нет новых update_id в ответе.")
        }

        val messageTextRegex = "\"text\":\"(.*?)\"".toRegex()
        val messageTextMatches = messageTextRegex.findAll(updates)
        if (messageTextMatches.any()) {
            println("Сообщения:")
            messageTextMatches.forEach { match ->
                val text = match.groupValues[1]
                println(" - $text")
            }
        } else {
            println("Нет новых сообщений с текстом.")
        }
        println("Следующий updateId для запроса: $updateId")
        println("------------------------------------")
    }
}

fun getUpdates(botToken: String, updateId: Long): String {
    val urlGetUpdates = "https://api.telegram.org/bot$botToken/getUpdates?offset=$updateId"
    val client = HttpClient.newBuilder().build()
    val request = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
    val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
    return response.body()
}