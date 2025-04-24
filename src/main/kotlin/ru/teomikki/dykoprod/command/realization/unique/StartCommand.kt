package ru.teomikki.dykoprod.command.realization.unique

import org.slf4j.LoggerFactory
import ru.teomikki.dykoprod.database.clients.ClientsHelper
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import ru.teomikki.dykoprod.command.Command
import ru.teomikki.dykoprod.command.realization.reply_only.AdminCommand
import ru.teomikki.dykoprod.command.realization.reply_only.AdminCommand.Companion
import ru.teomikki.dykoprod.database.admin.AdminHelper
import ru.teomikki.dykoprod.keyboards.InlineKeyboardTemplates
import ru.teomikki.dykoprod.resources.MessageTemplates
import ru.teomikki.dykoprod.sender.MessageSender
import ru.teomikki.dykoprod.state.UserStateService
import ru.teomikki.dykoprod.utils.MessageHandlerUtils

@Component
class StartCommand(
    private val messageSender: MessageSender,
    private val messageTemplates: MessageTemplates,
    private val inlineKeyboardTemplates: InlineKeyboardTemplates,
    private val userStateService: UserStateService,
    private val databaseClientHelper: ClientsHelper,
    private val adminHelper: AdminHelper,
    private val messageHandlerUtils: MessageHandlerUtils,
) : Command {

    companion object {
        private const val START_PHOTO_URL = "https://disk.yandex.ru/i/-XVdl-sK-AgBWQ"
        private val logger = LoggerFactory.getLogger(StartCommand::class.java) // Внедрение логгера - проф. замена println()
    }

    override fun name() = "/start"

    override fun execute(update: Update) {
        val message = update.message
        val chatId = message.chatId
        val user = message.from
        val userName = user.userName

        registerUserToDB(chatId, userName)
        sendWelcomeMessage(chatId, userName)
    }

    private fun registerUserToDB(
        chatId: Long,
        userName: String?,
    ) {
        try {
            databaseClientHelper.registerOrUpdateClient(
                chatId = chatId,
                username = userName,
            )
            logger.info("User saved to DB | chatId: {}, username: {}", chatId, userName)
        } catch (e: Exception) {
            logger.error("Failed to save user to DB | chatId: {}, error: {}", chatId, e.message, e)
        }
    }

    override fun handleCallback(update: Update) {
        val callbackQuery = update.callbackQuery
        val chatId = callbackQuery.message.chatId
        val userName = callbackQuery.from.userName

        cleanChatHistory(chatId)
        sendWelcomeMessage(chatId, userName)
    }

    private fun sendWelcomeMessage(chatId: Long, userName: String) {
        try {
            messageSender.sendPhoto(
                chatId = chatId,
                photoUrl = START_PHOTO_URL,
                caption = messageTemplates.getWelcomeMessage(userName),
                parseMode = "HTML"
            )?.let { messageId ->
                userStateService.updateMessageId(chatId, messageId)
                logger.info("Welcome message sent to $chatId (ID: $messageId)")
            }
            // Отправка главной панели управления
            sendMainMenu(chatId)
        } catch (e: Exception) {
            logger.error("Failed to send welcome message to $chatId", e)
            sendFallbackMessage(chatId, userName)
        }
    }

    private fun sendMainMenu(chatId: Long) {
        try {
            // Проверяем пользователя на права администрирования
            val isAdmin = adminHelper.isUserAdmin(chatId)
            userStateService.getState(chatId).isAdmin = isAdmin

            messageSender.sendText(
                chatId = chatId,
                text = messageTemplates.getMainMenuMessage(),
                inlineKeyboard = inlineKeyboardTemplates.getStartIK(chatId),
                parseMode = "HTML"
            )?.let { messageId ->
                userStateService.updateMessageId(chatId, messageId)
                logger.info("Main menu sent to $chatId (ID: $messageId)")
            }
        } catch (e: Exception) {
            logger.error("Failed to send main menu to $chatId", e)
        }
    }

    private fun cleanChatHistory(chatId: Long) {
        userStateService.getState(chatId).let { state ->
            val allMessages = state.run {
                previousMessages.toMutableList().apply {
                    currentMessageId?.let(::add)
                }
            }

            deleteMessagesSafely(chatId, allMessages)
            userStateService.clearState(chatId)
            logger.info("Chat history cleaned for $chatId")
        }
    }

    private fun deleteMessagesSafely(chatId: Long, messageIds: List<Int>) {
        messageIds.forEach { messageId ->
            try {
                if (messageSender.deleteMessage(chatId, messageId)) {
                    logger.debug("Message deleted: $messageId in chat $chatId")
                }
            } catch (e: Exception) {
                logger.error("Failed to delete message $messageId in chat $chatId", e)
            }
        }
    }

    private fun sendFallbackMessage(chatId: Long, userName: String) {
        try {
            messageSender.sendText(
                chatId = chatId,
                text = messageTemplates.getWelcomeMessage(userName),
                inlineKeyboard = inlineKeyboardTemplates.getStartIK(chatId),
                parseMode = "HTML"
            )
        } catch (e: Exception) {
            logger.error("Critical failure in chat $chatId", e)
        }
    }
}