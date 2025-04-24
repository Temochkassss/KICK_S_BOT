package ru.teomikki.dykoprod.utils

import org.slf4j.Logger
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import ru.teomikki.dykoprod.sender.MessageSender
import ru.teomikki.dykoprod.state.UserStateService

@Component
class MessageHandlerUtils(
    private val messageSender: MessageSender,
    private val userStateService: UserStateService
) {
    companion object {
        const val DEFAULT_ERROR_MESSAGE = "⚠️ Произошла ошибка. Попробуйте позже."
    }

    fun handleMessageEdit(
        chatId: Long,
        newText: String,
        inlineKeyboard: InlineKeyboardMarkup? = null,
        parseMode: String? = null,
        logger: Logger
    ) {
        try {
            val currentMessageId = userStateService.getCurrentMessageId(chatId)
            currentMessageId?.let { messageId ->
                messageSender.editMessage(
                    chatId = chatId,
                    messageId = messageId,
                    newText = newText,
                    inlineKeyboard = inlineKeyboard,
                    parseMode = parseMode
                )
            } ?: sendNewMessageWithStateUpdate(chatId, newText, inlineKeyboard, parseMode, logger, currentMessageId)
        } catch (e: Exception) {
            logger.error("Error handling message edit for chat: $chatId", e)
            sendErrorMessage(chatId, logger)
        }
    }

    private fun sendNewMessageWithStateUpdate(
        chatId: Long,
        text: String,
        inlineKeyboard: InlineKeyboardMarkup? = null,
        parseMode: String? = null,
        logger: Logger,
        lastMessageId: Int?
    ) {
        try {
            messageSender.sendText(
                chatId = chatId,
                text = text,
                inlineKeyboard = inlineKeyboard,
                parseMode = parseMode
            )?.let { messageId ->
                userStateService.updateMessageId(chatId, messageId)
                logger.info("Rewriten current message ID to $messageId")
            }
            // Удаление дубликата сообщения
            lastMessageId?.let {
                messageSender.deleteMessage(chatId, lastMessageId)
            }
        } catch (e: Exception) {
            logger.error("Error sending new message to chat: $chatId", e)
            sendErrorMessage(chatId, logger)
        }
    }

    fun sendErrorMessage(chatId: Long, logger: Logger, customMessage: String = DEFAULT_ERROR_MESSAGE) {
        try {
            messageSender.sendText(chatId, customMessage)
        } catch (e: Exception) {
            logger.error("Failed to send error message to chat: $chatId", e)
        }
    }

    fun getChatId(update: Update): Long {
        return update.message?.chatId ?: update.callbackQuery.message.chatId
    }
}