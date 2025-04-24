package ru.teomikki.dykoprod.command.realization.callback_only.handler

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.api.objects.Update
import ru.teomikki.dykoprod.animation.Animation
import ru.teomikki.dykoprod.command.realization.callback_only.ShowAdminResponses
import ru.teomikki.dykoprod.database.response.ResponseHelper
import ru.teomikki.dykoprod.keyboards.InlineKeyboardTemplates
import ru.teomikki.dykoprod.resources.MessageTemplates
import ru.teomikki.dykoprod.sender.MessageSender
import ru.teomikki.dykoprod.state.UserStateService
import ru.teomikki.dykoprod.utils.MessageHandlerUtils

@Component
class ProcessingHandler(
    private val userStateService: UserStateService,
    private val messageSender: MessageSender,
    private val responseHelper: ResponseHelper,
    private val messageTemplates: MessageTemplates,
    private val inlineKeyboardTemplates: InlineKeyboardTemplates,
    private val animation: Animation,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun handleProcessingCallback(update: Update) {
        val chatId = update.callbackQuery.message.chatId
        val callbackData = update.callbackQuery.data

        try {
            when {
                callbackData.startsWith("proc_confirm:") -> showConfirmationProcessingMenu(chatId, callbackData)
                callbackData.contains("yes:") -> confirmCompetitionProcessing(chatId, callbackData)
                callbackData.contains("no:") -> cancelCompetitionProcessing(chatId, callbackData)
                callbackData.contains("what:") -> aboutCompetitionProcessing(update.callbackQuery)
                else -> logger.warn("Unknown user responses callback: $callbackData")
            }
        } catch (e: Exception) {
            logger.error("Error processing Processing callback", e)
            messageSender.sendText(chatId, "⚠️ Ошибка при загрузке информации. Попробуйте позже.")
        }
    }

    private fun showConfirmationProcessingMenu(chatId: Long, callbackData: String) {
        val responseId = callbackData.split(":")[1]
        val messageId = userStateService.getMessageIdByResponseId(chatId, responseId)
        val alertText = userStateService.getAlertTextIdByResponseId(chatId, responseId)

        messageId?.let {
            messageSender.editMessage(
                chatId = chatId,
                messageId = it,
                newText = alertText ?: "Подтверждаете заверешние обрабоки отклика?",
                inlineKeyboard = inlineKeyboardTemplates.getProcessingConfirmationMenu(responseId),
                parseMode = "HTML"
            )
        }
    }

    private fun confirmCompetitionProcessing(chatId: Long, callbackData: String) {
        val responseId = callbackData.split(":")[1]
        val messageId = userStateService.getMessageIdByResponseId(chatId, responseId)
        val userChatId = responseHelper.getChatIdByResponseId(responseId)

        // Удаление сообщения у администратора
        messageId?.let { messageSender.deleteMessage(chatId, it) }

        // Анимация обработкки отклика у пользователя
        userChatId?.let { animation.showProcessingAnimation(it, responseId) }

        // Обнуление связей
        userStateService.removeResponseMessageMapping(chatId, responseId)
        userStateService.removeAlertTextMessageMapping(chatId, responseId)
    }

    private fun cancelCompetitionProcessing(chatId: Long, callbackData: String) {
        val responseId = callbackData.split(":")[1]
        val messageId = userStateService.getMessageIdByResponseId(chatId, responseId)
        val alertText = userStateService.getAlertTextIdByResponseId(chatId, responseId)

        messageId?.let {
            messageSender.editMessage(
                chatId = chatId,
                messageId = it,
                newText = alertText ?: "⚠️ Произошла ошибка. Отклик сохранен в настройках администратора.",
                inlineKeyboard = inlineKeyboardTemplates.getProcessingConfirmationIK(responseId),
                parseMode = "HTML"
            )
        }
    }

    private fun aboutCompetitionProcessing(callbackQuery: CallbackQuery) {
        messageSender.sendWindowAlert(
            callbackQuery = callbackQuery,
            alertText = messageTemplates.getAlertMessage()
        )
    }

}