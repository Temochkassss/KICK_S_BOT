package ru.teomikki.dykoprod.command.realization.reply_only

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import ru.teomikki.dykoprod.command.Command
import ru.teomikki.dykoprod.database.admin.AdminHelper
import ru.teomikki.dykoprod.database.clients.ClientsHelper
import ru.teomikki.dykoprod.keyboards.InlineKeyboardTemplates
import ru.teomikki.dykoprod.resources.MessageTemplates
import ru.teomikki.dykoprod.sender.MessageSender
import ru.teomikki.dykoprod.state.UserStateService
import ru.teomikki.dykoprod.utils.MessageHandlerUtils

@Component
class AdminCommand(
    private val messageSender: MessageSender,
    private val messageTemplates: MessageTemplates,
    private val inlineKeyboardTemplates: InlineKeyboardTemplates,
    private val userStateService: UserStateService,
    private val messageHandlerUtils: MessageHandlerUtils,
    private val adminHelper: AdminHelper,
) : Command {

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    override fun name() = "/admin"

    override fun execute(update: Update) {
        val chatId = messageHandlerUtils.getChatId(update)

        // Устанавливаем для данного chatId права администрирования
        val isAdmin = adminHelper.isUserAdmin(chatId)
        userStateService.getState(chatId).isAdmin = isAdmin

        if (isAdmin) {
            messageHandlerUtils.handleMessageEdit(
                chatId = chatId,
                newText = messageTemplates.getMainMenuMessage(),
                inlineKeyboard = inlineKeyboardTemplates.getStartIK(chatId),
                parseMode = "HTML",
                logger = logger
            )
        }
    }

    override fun handleCallback(update: Update) = Unit // Not implemented for reply-only command
}