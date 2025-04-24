package ru.teomikki.dykoprod.command.realization.unique

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import ru.teomikki.dykoprod.command.Command
import ru.teomikki.dykoprod.database.categories.CategoriesHelper
import ru.teomikki.dykoprod.keyboards.InlineKeyboardTemplates
import ru.teomikki.dykoprod.resources.MessageTemplates
import ru.teomikki.dykoprod.sender.MessageSender
import ru.teomikki.dykoprod.utils.MessageHandlerUtils

@Component
class CalculateCommand(
    private val messageTemplates: MessageTemplates,
    private val inlineKeyboardTemplates: InlineKeyboardTemplates,
    private val messageHandlerUtils: MessageHandlerUtils,
    private val messageSender: MessageSender,
    private val categoriesHelper: CategoriesHelper
) : Command {

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    override fun name() = "/calculate_the_cost"

    override fun execute(update: Update) {
        val chatId = messageHandlerUtils.getChatId(update)
        handleCalculationRequest(chatId)
    }

    override fun handleCallback(update: Update) {
        val chatId = messageHandlerUtils.getChatId(update)
        handleCalculationRequest(chatId)
    }

    private fun handleCalculationRequest(chatId: Long) {
        val categories = categoriesHelper.getAllCategories()
        if (categories.isEmpty()) {
            messageSender.sendText(chatId, "⚠️ Нет доступных категорий.")
            return
        }
        messageHandlerUtils.handleMessageEdit(
            chatId = chatId,
            newText = messageTemplates.getCalculateMessage(),
            inlineKeyboard = inlineKeyboardTemplates.getCalculateIK(categories),
            parseMode = "HTML",
            logger = logger
        )
    }
}