package ru.teomikki.dykoprod.sender

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update

@Component
class MessageCleanupInterceptor(
    private val messageSender: MessageSender
) {
    fun preHandle(update: Update) {
        if (update.hasMessage()) {
            val message = update.message
            if (message.isCommand() || message.isUserMessage()) {
                messageSender.deleteSystemMessage(
                    chatId = message.chatId,
                    messageId = message.messageId
                )
            }
        }
    }
}