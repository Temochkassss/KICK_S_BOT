package ru.teomikki.dykoprod.command.realization.callback_only

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import ru.teomikki.dykoprod.command.Command
import ru.teomikki.dykoprod.database.response.ResponseHelper
import ru.teomikki.dykoprod.keyboards.InlineKeyboardTemplates
import ru.teomikki.dykoprod.utils.MessageHandlerUtils
import kotlin.math.ceil

@Component
class ShowUserResponses(
    private val messageHandlerUtils: MessageHandlerUtils,
    private val inlineKeyboardTemplates: InlineKeyboardTemplates,
    private val responseHelper: ResponseHelper,
): Command {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    override fun name(): String = "my_responses"

    override fun execute(update: Update) = Unit // Not implemented for callback-only command

    override fun handleCallback(update: Update) {
        val chatId = messageHandlerUtils.getChatId(update)
        showUserResponses(chatId)
    }

    fun showUserResponses(chatId: Long, page: Int = 1) {
        val pageSize = 5
        val totalResponses = responseHelper.countResponsesByChatId(chatId)
        val totalPages = ceil(totalResponses.toDouble() / pageSize).toInt() // Округление вверх до ближайшего целого
        val responses = responseHelper.getUserResponses(chatId, page, pageSize)

        messageHandlerUtils.handleMessageEdit(
            chatId = chatId,
            newText = "🗂 Для просмотра отклика — выберите:",
            inlineKeyboard = inlineKeyboardTemplates.getUserResponsesIK(responses, page, totalPages),
            logger = logger
        )
    }
}