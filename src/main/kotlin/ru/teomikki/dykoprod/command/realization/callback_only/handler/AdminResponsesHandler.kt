package ru.teomikki.dykoprod.command.realization.callback_only.handler

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import ru.teomikki.dykoprod.command.realization.callback_only.ShowAdminResponses
import ru.teomikki.dykoprod.database.response.ResponseHelper
import ru.teomikki.dykoprod.keyboards.InlineKeyboardTemplates
import ru.teomikki.dykoprod.resources.MessageTemplates
import ru.teomikki.dykoprod.sender.MessageSender
import ru.teomikki.dykoprod.utils.MessageHandlerUtils
import kotlin.math.ceil

@Component
class AdminResponsesHandler(
    private val responseHelper: ResponseHelper,
    private val messageSender: MessageSender,
    private val messageHandlerUtils: MessageHandlerUtils,
    private val messageTemplates: MessageTemplates,
    private val inlineKeyboardTemplates: InlineKeyboardTemplates,
    private val showAdminResponses: ShowAdminResponses
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun handleAdminResponsesCallback(update: Update) {
        val chatId = update.callbackQuery.message.chatId
        val callbackData = update.callbackQuery.data

        try {
            when {
                callbackData.startsWith("admin_response:") -> showResponseDetails(chatId, callbackData)
                callbackData.startsWith("admin_responses_page:") -> handlePaginationForResponses(chatId, callbackData)
                callbackData.startsWith("admin_responses_show_pages:") -> showPageMenu(chatId, callbackData.split(":")[1].toInt())
                callbackData.startsWith("admin_responses_block:") -> handleBlockNavigation(chatId, callbackData)
                callbackData == "back_to_admin_responses_list" -> showAdminResponses.showAdminResponses(chatId)

                else -> logger.warn("Unknown user responses callback: $callbackData")
            }
        } catch (e: Exception) {
            logger.error("Error processing user responses callback", e)
            messageSender.sendText(chatId, "⚠️ Ошибка при загрузке информации. Попробуйте позже.")
        }
    }

    private fun showResponseDetails(chatId: Long, callbackData: String) {
        val responseId = callbackData.split(":")[1]
        val response = responseHelper.getResponseDetails(responseId)

        response?.let {
            messageHandlerUtils.handleMessageEdit(
                chatId = chatId,
                newText = messageTemplates.getResponseDetailsMessage(it),
                inlineKeyboard = inlineKeyboardTemplates.getAdminResponseDetailsIK(),
                parseMode = "HTML",
                logger = logger
            )
        }
    }

    private fun showPageMenu(chatId: Long, currentPage: Int = 1) {  // Убрали callbackData из параметров
        val pageSize = 5
        val totalResponses = responseHelper.countAllResponses()
        val totalPages = ceil(totalResponses.toDouble() / pageSize).toInt()

        messageHandlerUtils.handleMessageEdit(
            chatId = chatId,
            newText = "Выберите страницу для просмотра:",
            inlineKeyboard = inlineKeyboardTemplates.getAdminResponsesPageSelectionIK(currentPage, totalPages),
            parseMode = "HTML",
            logger = logger
        )
    }

    private fun handleBlockNavigation(chatId: Long, callbackData: String) {
        val blockNumber = callbackData.split(":")[1].toInt()
        val startPage = blockNumber * 25 + 1  // 25 - размер блока (5x5 кнопок)
        showPageMenu(chatId, startPage)  // Передаем только номер страницы
    }

    private fun handlePaginationForResponses(chatId: Long, callbackData: String) {
        val page = callbackData.split(":")[1].toInt()
        showAdminResponses.showAdminResponses(
            chatId,
            page.coerceAtLeast(1)
        ) // coerceAtLeast - устанавливает минимальное значение
    }
}