package ru.teomikki.dykoprod.sender

import jakarta.annotation.PostConstruct
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands
import org.telegram.telegrambots.meta.api.methods.send.*
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.bots.AbsSender
import org.telegram.telegrambots.meta.exceptions.TelegramApiException

@Component
class TelegramMessageSender(
    @Lazy
    private val absSender: AbsSender,
) : MessageSender {

    companion object {
        private val logger = LoggerFactory.getLogger(TelegramMessageSender::class.java)
    }

    override fun sendText(
        chatId: Long,
        text: String,
        parseMode: String?,
        replyKeyboard: ReplyKeyboardMarkup?,
        inlineKeyboard: InlineKeyboardMarkup?
    ): Int? {
        return try {
            val message = absSender.execute(
                SendMessage(chatId.toString(), text).apply {
                    this.parseMode = parseMode
                    replyMarkup = inlineKeyboard ?: replyKeyboard
                }
            ) as Message
            message.messageId
        } catch (e: TelegramApiException) {
            null
        }
    }

    override fun sendPhoto(
        chatId: Long,
        photoUrl: String,
        caption: String?,
        parseMode: String?,
        replyKeyboard: ReplyKeyboardMarkup?,
        inlineKeyboard: InlineKeyboardMarkup?
    ): Int? {
        return try {
            val message = absSender.execute(
                SendPhoto(chatId.toString(), InputFile(photoUrl)).apply {
                    this.caption = caption
                    this.parseMode = parseMode
                    replyMarkup = inlineKeyboard ?: replyKeyboard
                }
            ) as Message
            message.messageId
        } catch (e: TelegramApiException) {
            null
        }
    }

    override fun sendVideo(
        chatId: Long,
        videoUrl: String,
        caption: String?,
        parseMode: String?,
        replyKeyboard: ReplyKeyboardMarkup?,
        inlineKeyboard: InlineKeyboardMarkup?
    ): Int? {
        return try {
            val message = absSender.execute(
                SendVideo(chatId.toString(), InputFile(videoUrl)).apply {
                    this.caption = caption
                    this.parseMode = parseMode
                    replyMarkup = inlineKeyboard ?: replyKeyboard
                }
            ) as Message
            message.messageId
        } catch (e: TelegramApiException) {
            null
        }
    }

    override fun sendSticker(chatId: Long, stickerId: String): Int? {
        return try {
            val message = absSender.execute(
                SendSticker(chatId.toString(), InputFile(stickerId))
            ) as Message
            message.messageId
        } catch (e: TelegramApiException) {
            null
        }
    }

    override fun sendWindowAlert(callbackQuery: CallbackQuery, alertText: String) {
        try {
            val answerCallbackQuery = absSender.execute(
                AnswerCallbackQuery().apply {
                    callbackQueryId = callbackQuery.id
                    text = alertText
                    showAlert = true
                }
            )
        } catch (e: TelegramApiException) {
            throw e
        }
    }

    override fun editMessage(
        chatId: Long,
        messageId: Int,
        newText: String,
        parseMode: String?,
        inlineKeyboard: InlineKeyboardMarkup?,
        disableWebPagePreview: Boolean
    ): Int? {
        return try {
            val message = absSender.execute(
                EditMessageText().apply {
                    this.chatId = chatId.toString()
                    this.messageId = messageId
                    this.text = newText
                    this.parseMode = parseMode
                    this.replyMarkup = inlineKeyboard
                    this.disableWebPagePreview = disableWebPagePreview
                }
            ) as Message
            message.messageId
        } catch (e: TelegramApiException) {
            null
        }
    }

    override fun deleteMessage(chatId: Long, messageId: Int): Boolean {
        return try {
            absSender.execute(DeleteMessage(chatId.toString(), messageId))
            true
        } catch (e: TelegramApiException) {
            false
        }
    }

    override fun deleteSystemMessage(chatId: Long, messageId: Int): Boolean {
        return try {
            deleteMessage(chatId, messageId)
            logger.debug("System message deleted: $messageId in chat $chatId")
            true
        } catch (e: Exception) {
            logger.error("Failed to delete system message in chat $chatId", e)
            false
        }
    }
}