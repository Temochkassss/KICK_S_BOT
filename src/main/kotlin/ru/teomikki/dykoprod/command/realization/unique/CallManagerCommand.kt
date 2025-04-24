package ru.teomikki.dykoprod.command.realization.unique

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import ru.teomikki.dykoprod.command.Command
import ru.teomikki.dykoprod.database.response.ResponseHelper
import ru.teomikki.dykoprod.keyboards.InlineKeyboardTemplates
import ru.teomikki.dykoprod.state.UserStateService
import ru.teomikki.dykoprod.utils.MessageHandlerUtils

@Component
class CallManagerCommand(
    private val inlineKeyboardTemplates: InlineKeyboardTemplates,
    private val userStateService: UserStateService,
    private val messageHandlerUtils: MessageHandlerUtils,
    private val responseHelper: ResponseHelper,

    ): Command {
    companion object {
        const val AWAITING_QUESTION = "AWAITING_MANAGER_QUESTION"
        const val AWAITING_CONTACT = "AWAITING_USER_CONTACT"
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    override fun name() = "/call_the_manager"

    override fun execute(update: Update) {
        sendAwaitingQuestionMessage(update)
    }

    override fun handleCallback(update: Update) {
        sendAwaitingQuestionMessage(update)
    }

    private fun sendAwaitingQuestionMessage(update: Update) {
        val chatId = messageHandlerUtils.getChatId(update)
        val userState = userStateService.getState(chatId)

        val keyboard = if(responseHelper.haveResponses(chatId)) {
            inlineKeyboardTemplates.getCancelAndResponseIK()
        } else {
            inlineKeyboardTemplates.getCancelRequestIK()
        }

        // Установление текущего состояния
        userState.apply {
            currentInteraction = AWAITING_QUESTION
        }

        messageHandlerUtils.handleMessageEdit(
            chatId = chatId,
            newText = "✍️ Напишите ваш вопрос менеджеру:",
            inlineKeyboard = keyboard,
            logger = logger
        )
    }
}