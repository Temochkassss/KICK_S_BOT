package ru.teomikki.dykoprod.service

import org.springframework.stereotype.Service
import org.telegram.telegrambots.bots.DefaultBotOptions
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import ru.teomikki.dykoprod.command.ContactDispatcher
import ru.teomikki.dykoprod.command.TextDispatcher
import ru.teomikki.dykoprod.property.BotProperty

@Service
class BotService(
    private val botProperty: BotProperty,
    private val textDispatcher: TextDispatcher,
    private val contactDispatcher: ContactDispatcher,
    options: DefaultBotOptions
) : TelegramLongPollingBot(options, botProperty.token) {

    override fun getBotUsername(): String = botProperty.username

    override fun onUpdateReceived(update: Update) {
        try {
            when {
                update.hasMessage() -> {
                    if (update.message.hasText()) {
                        textDispatcher.handleCommand(update)
                    }
                    if (update.message.hasContact()) {
                        contactDispatcher.handleContact(update)
                    }
                }
                update.hasCallbackQuery() -> {
                    println("update.callbackQuery.data = ${update.callbackQuery.data}")
                    textDispatcher.handleCallback(update)
                }
            }
        } catch (e: Exception) {
            println("Ошибка в BotService: ${e.message}")
            e.printStackTrace()
        }
    }
}
