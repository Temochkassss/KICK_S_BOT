package ru.teomikki.dykoprod.command.realization.callback_only

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import ru.teomikki.dykoprod.command.Command
import ru.teomikki.dykoprod.command.realization.callback_only.BackToMainMenu.Companion
import ru.teomikki.dykoprod.keyboards.InlineKeyboardTemplates
import ru.teomikki.dykoprod.utils.MessageHandlerUtils

@Component
class OpenAdminPanel(
    private val messageHandlerUtils: MessageHandlerUtils,
    private val inlineKeyboardTemplates: InlineKeyboardTemplates
): Command {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    override fun name(): String = "admin_panel"

    override fun execute(update: Update) = Unit // Not implemented for callback-only command

    override fun handleCallback(update: Update) {
        val chatId = messageHandlerUtils.getChatId(update)

        messageHandlerUtils.handleMessageEdit(
            chatId = chatId,
            newText = "⚠️ Режим администратора активирован ⚠️",
            inlineKeyboard = inlineKeyboardTemplates.getAdminPanelIK(),
            parseMode = "HTML",
            logger = logger
        )
    }

}