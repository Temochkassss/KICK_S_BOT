package ru.teomikki.dykoprod.animation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import ru.teomikki.dykoprod.command.realization.callback_only.BackToMainMenu
import ru.teomikki.dykoprod.command.realization.callback_only.BackToMainMenu.Companion
import ru.teomikki.dykoprod.keyboards.InlineKeyboardTemplates
import ru.teomikki.dykoprod.resources.MessageTemplates
import ru.teomikki.dykoprod.sender.MessageSender
import ru.teomikki.dykoprod.state.UserStateService
import ru.teomikki.dykoprod.utils.MessageHandlerUtils

@Component
class Animation(
    private val messageSender: MessageSender,
    private val messageHandlerUtils: MessageHandlerUtils,
    private val messageTemplates: MessageTemplates,
    private val inlineKeyboardTemplates: InlineKeyboardTemplates,
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun showProcessingAnimation(chatId: Long, responseId: String) {
        CoroutineScope(Dispatchers.Default).launch {
            try {

                // Первый этап - редактирование сообщения
                messageHandlerUtils.handleMessageEdit(
                    chatId= chatId,
                    newText = "⏳ Идет обработка...",
                    parseMode = "HTML",
                    logger = logger
                )

                delay(2000) // Задержка перед отправкой стикера

                // Отправка стикера
                val stickerMessageId = messageSender.sendSticker(
                    chatId = chatId,
                    stickerId = "CAACAgIAAxkBAAENckRneWCwWbtOewgx-fc1SpxeTltsNgACSQIAAladvQoqlwydCFMhDjYE"
                )

                delay(2500) // Время показа стикера

                // Удаление стикера
                stickerMessageId?.let {
                    messageSender.deleteMessage(chatId, it)
                }

                // Возврат до стартового сообщения
                messageHandlerUtils.handleMessageEdit(
                    chatId = chatId,
                    newText = messageTemplates.getMainMenuMessage(),
                    inlineKeyboard = inlineKeyboardTemplates.getStartIK(chatId),
                    parseMode = "HTML",
                    logger = logger
                )

            } catch (e: Exception) {
                logger.error("Error in processing animation: ${e.message}")
                messageSender.sendText(chatId, "⚠️ Произошла ошибка при обработке")
            }
        }
    }
}