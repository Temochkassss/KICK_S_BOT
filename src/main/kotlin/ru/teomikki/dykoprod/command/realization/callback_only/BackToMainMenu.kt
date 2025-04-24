package ru.teomikki.dykoprod.command.realization.callback_only

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import ru.teomikki.dykoprod.command.Command
import ru.teomikki.dykoprod.keyboards.InlineKeyboardTemplates
import ru.teomikki.dykoprod.resources.MessageTemplates
import ru.teomikki.dykoprod.state.UserStateService
import ru.teomikki.dykoprod.utils.MessageHandlerUtils

@Component
class BackToMainMenu(
    private val messageTemplates: MessageTemplates,
    private val messageHandlerUtils: MessageHandlerUtils,
    private val inlineKeyboardTemplates: InlineKeyboardTemplates,
    private val userStateService: UserStateService,
): Command {

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    override fun name(): String = "back_to_main_menu"

    override fun execute(update: Update) = Unit // Not implemented for callback-only command

    override fun handleCallback(update: Update) {
        // Получение chatId независимо от природы его возникновения
        val chatId = messageHandlerUtils.getChatId(update)

        messageHandlerUtils.handleMessageEdit(
            chatId = chatId,
            newText = messageTemplates.getMainMenuMessage(),
            inlineKeyboard = inlineKeyboardTemplates.getStartIK(chatId),
            parseMode = "HTML",
            logger = logger
        )
    }
}