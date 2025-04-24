package ru.teomikki.dykoprod.sender

import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup

interface MessageSender {
    fun sendText(
        chatId: Long,
        text: String,
        parseMode: String? = null,
        replyKeyboard: ReplyKeyboardMarkup? = null,
        inlineKeyboard: InlineKeyboardMarkup? = null
    ): Int?

    fun sendPhoto(
        chatId: Long,
        photoUrl: String,
        caption: String? = null,
        parseMode: String? = null,
        replyKeyboard: ReplyKeyboardMarkup? = null,
        inlineKeyboard: InlineKeyboardMarkup? = null
    ): Int?

    fun sendVideo(
        chatId: Long,
        videoUrl: String,
        caption: String? = null,
        parseMode: String? = null,
        replyKeyboard: ReplyKeyboardMarkup? = null,
        inlineKeyboard: InlineKeyboardMarkup? = null
    ): Int?

    fun sendSticker(
        chatId: Long,
        stickerId: String
    ): Int?

    fun sendWindowAlert(
        callbackQuery: CallbackQuery,
        alertText: String,
    )

    fun editMessage(
        chatId: Long,
        messageId: Int,
        newText: String,
        parseMode: String? = null,
        inlineKeyboard: InlineKeyboardMarkup? = null,
        disableWebPagePreview: Boolean = false
    ): Int?

    fun deleteMessage(
        chatId: Long,
        messageId: Int,
    ): Boolean

    fun deleteSystemMessage(
        chatId: Long,
        messageId: Int
    ): Boolean
}