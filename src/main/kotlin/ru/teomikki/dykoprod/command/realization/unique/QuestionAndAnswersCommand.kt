package ru.teomikki.dykoprod.command.realization.unique

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import ru.teomikki.dykoprod.command.Command
import ru.teomikki.dykoprod.keyboards.InlineKeyboardTemplates
import ru.teomikki.dykoprod.resources.MessageTemplates
import ru.teomikki.dykoprod.utils.MessageHandlerUtils

@Component
class QuestionAndAnswersCommand(
    private val messageTemplates: MessageTemplates,
    private val inlineKeyboardTemplates: InlineKeyboardTemplates,
    private val messageHandlerUtils: MessageHandlerUtils
) : Command {

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    override fun name() = "/questions_answers"

    override fun execute(update: Update) {
        val chatId = messageHandlerUtils.getChatId(update)
        handleQuestionAndAnswersRequest(chatId)
    }

    override fun handleCallback(update: Update) {
        val chatId = messageHandlerUtils.getChatId(update)
        handleQuestionAndAnswersRequest(chatId)
    }

    private fun handleQuestionAndAnswersRequest(chatId: Long) {
        messageHandlerUtils.handleMessageEdit(
            chatId = chatId,
            newText = messageTemplates.getQuestionsAnswersMessage(),
            inlineKeyboard = inlineKeyboardTemplates.getFaqIK(),
            parseMode = "HTML",
            logger = logger
        )
    }
}