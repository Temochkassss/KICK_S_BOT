package ru.teomikki.dykoprod.command.realization.callback_only.handler

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import ru.teomikki.dykoprod.database.admin.AdminHelper
import ru.teomikki.dykoprod.database.categories.CategoriesHelper
import ru.teomikki.dykoprod.database.clients.ClientsHelper
import ru.teomikki.dykoprod.database.response.ResponseHelper
import ru.teomikki.dykoprod.keyboards.InlineKeyboardTemplates
import ru.teomikki.dykoprod.resources.MessageTemplates
import ru.teomikki.dykoprod.sender.MessageSender
import ru.teomikki.dykoprod.state.UserStateService
import ru.teomikki.dykoprod.utils.MessageHandlerUtils
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class RequestManagerHandler(
    private val messageSender: MessageSender,
    private val messageTemplates: MessageTemplates,
    private val userStateService: UserStateService,
    private val inlineKeyboardTemplates: InlineKeyboardTemplates,
    private val messageHandlerUtils: MessageHandlerUtils,
    private val databaseClientHelper: ClientsHelper,
    private val categoryHandler: CalculateCategoryHandler,
    private val adminHelper: AdminHelper,
    private val categoriesHelper: CategoriesHelper,
    private val responseHelper: ResponseHelper,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun handleRequestManagerCallback(update: Update) {
        val chatId = update.callbackQuery.message.chatId
        val callbackData = update.callbackQuery.data

        try {
            when (callbackData) {
                "cancel_manager_request" -> deleteRequestManagerMessage(chatId)
                "confirm_manager_request" -> sendRequestForManager(chatId)
                else -> logger.warn("Unknown Request Manager callback: $callbackData")
            }
        } catch (e: Exception) {
            logger.error("Error processing Request Manager callback", e)
            messageSender.sendText(chatId, "⚠️ Ошибка при загрузке информации. Попробуйте позже.")
        }
    }

    private fun deleteRequestManagerMessage(chatId: Long) {
        val userState = userStateService.getState(chatId)

        //  Обнуление состояний и возможного процесса запроса контакта
        userState.currentInteraction = null
        userState.contactMessageId?.let { messageSender.deleteMessage(chatId, it) }

        messageHandlerUtils.handleMessageEdit(
            chatId = chatId,
            newText = messageTemplates.getMainMenuMessage(),
            inlineKeyboard = inlineKeyboardTemplates.getStartIK(chatId),
            parseMode = "HTML",
            logger = logger
        )
    }

    private fun sendRequestForManager(chatId: Long) {
        val userState = userStateService.getState(chatId)
        val client = databaseClientHelper.getUser(chatId)
        val dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yy HH:mm"))

        // Удаление процесса получения контакта
        userState.contactMessageId?.let { messageSender.deleteMessage(chatId, it) }

        // Формирование сообщения для менеджера
        val managerMessage = messageTemplates.getManagerRequestMessage(
            username = client?.username,
            dateTime = dateTime,
            question = userState.currentManagerQuestion,
            selectedCategory = userState.currentCategoryLabel,
            mostViewedCategory = client?.category?.let { categoriesHelper.getCustomName(it) },
            phoneNumber = client?.phoneNumber
        )

        // Генерация и сохранение отклика
        val responseId = responseHelper.generateUniqueResponseId()

        responseHelper.insertResponse(
            responseId = responseId,
            chatId = chatId,
            question = userState.currentManagerQuestion,
            username = client?.username ?: "аноним",
            phoneNumber = client?.phoneNumber ?: "не указан",
            category = userState.currentCategoryLabel,
            mostViewedCategory = client?.category?.let { categoriesHelper.getCustomName(it) },
            dateTime = dateTime
        )

        // Отправка менеджеру
        val adminChatId = getAdminChatId() ?: 1038512328
        messageSender.sendText(
            chatId = adminChatId,
            text = managerMessage,
            inlineKeyboard = inlineKeyboardTemplates.getProcessingConfirmationIK(responseId),
            parseMode = "HTML"
        )?.let { messageId ->
            userStateService.addResponseMessageMapping(adminChatId, responseId, messageId)
            userStateService.addAlertTextMessageMapping(adminChatId, responseId, managerMessage)
        }

        // Уведомление пользователя
        val adminUsername = adminHelper.getAdminUsername(adminChatId)
        messageHandlerUtils.handleMessageEdit(
            chatId = chatId,
            newText = messageTemplates.getConfirmationMessage(adminUsername),
            inlineKeyboard = inlineKeyboardTemplates.getStartIK(chatId),
            parseMode = "HTML",
            logger = logger
        )

        userState.currentInteraction = null
    }

    private fun getAdminChatId(): Long? = adminHelper.getAdminChatId()
}