fun main(args: Array<String>) {
    if (args.isEmpty()) {
        return
    }

    val botToken = args[0]
    val botService = TelegramBotService(botToken)
    var updateId: Long = 0

    while (true) {
        try {
            Thread.sleep(2000)
            val updatesJson = botService.getUpdates(updateId)
            println("Получен JSON: $updatesJson")
            val blocks = TelegramBotService.UPDATE_OBJECT_REGEX.findAll(updatesJson).map { it.value }.toList()

            if (blocks.isEmpty()) {
                continue
            }
            val ids = blocks.mapNotNull { TelegramBotService.extractUpdateId(it) }
            updateId = (ids.maxOrNull() ?: updateId) + 1
            for (block in blocks) {
                val chatId = TelegramBotService.extractChatId(block)
                val text = TelegramBotService.extractMessageText(block)

                if (chatId != null && text != null) {
                    println("Получено сообщение '$text' от chat_id $chatId")
                    if (text == "Hello") {
                        val resp = botService.sendMessage(chatId, text)
                        println("Отправлен ответ: $resp")
                    }
                }
            }
        } catch (e: Exception) {
            println("Ошибка в основном цикле: ${e.message}")
            Thread.sleep(3000)
        }
    }
}