package ru.teomikki.dykoprod.command.realization.callback_only.handler

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import ru.teomikki.dykoprod.keyboards.InlineKeyboardTemplates
import ru.teomikki.dykoprod.resources.FAQTemplates
import ru.teomikki.dykoprod.resources.MessageTemplates
import ru.teomikki.dykoprod.sender.MessageSender
import ru.teomikki.dykoprod.utils.MessageHandlerUtils

@Component
class FaqHandler(
    private val messageSender: MessageSender,
    private val messageTemplates: MessageTemplates,
    private val faqTemplates: FAQTemplates,
    private val inlineKeyboardTemplates: InlineKeyboardTemplates,
    private val messageHandlerUtils: MessageHandlerUtils,
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun handleFaqCallback(update: Update) {
        val chatId = update.callbackQuery.message.chatId
        val callbackData = update.callbackQuery.data

        try {
            when {
                callbackData == "close_faq" -> deleteFaqMessage(chatId)
                callbackData == "back_to_faq" -> showMainFaqMenu(chatId)
                callbackData.startsWith("faq_") -> processFaqItem(chatId, callbackData)
                else -> logger.warn("Unknown FAQ callback: $callbackData")
            }
        } catch (e: Exception) {
            logger.error("Error processing FAQ callback", e)
            messageSender.sendText(chatId, "⚠️ Ошибка при загрузке информации. Попробуйте позже.")
        }
    }

    private fun deleteFaqMessage(chatId: Long) {
        messageHandlerUtils.handleMessageEdit(
            chatId = chatId,
            newText = messageTemplates.getMainMenuMessage(),
            inlineKeyboard = inlineKeyboardTemplates.getStartIK(chatId),
            parseMode = "HTML",
            logger = logger
        )
    }

    private fun showMainFaqMenu(chatId: Long) {
        messageHandlerUtils.handleMessageEdit(
            chatId = chatId,
            newText = messageTemplates.getQuestionsAnswersMessage(),
            inlineKeyboard = inlineKeyboardTemplates.getFaqIK(),
            parseMode = "HTML",
            logger = logger
        )
    }

    private fun processFaqItem(chatId: Long, callbackData: String) {
        val text = when (callbackData) {
            "faq_order_guide" -> faqTemplates.getOrderGuide()
            "faq_calculation" -> faqTemplates.getCalculationGuide()
            "faq_article_search" -> faqTemplates.getArticleSearchGuide()
            "faq_delivery" -> faqTemplates.getDeliveryGuide()
            "faq_poizon_buttons" -> faqTemplates.getPoizonButtonsGuide()
            else -> return
        }
        messageHandlerUtils.handleMessageEdit(
            chatId = chatId,
            newText = text,
            inlineKeyboard = inlineKeyboardTemplates.getBackToFAQIK(),
            parseMode = "HTML",
            logger = logger
        )
    }
}