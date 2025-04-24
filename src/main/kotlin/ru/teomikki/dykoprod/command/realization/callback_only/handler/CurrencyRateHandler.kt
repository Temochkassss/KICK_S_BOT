package ru.teomikki.dykoprod.command.realization.callback_only.handler

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import ru.teomikki.dykoprod.database.currency.CurrencyHelper
import ru.teomikki.dykoprod.keyboards.InlineKeyboardTemplates
import ru.teomikki.dykoprod.resources.MessageTemplates
import ru.teomikki.dykoprod.sender.MessageSender
import ru.teomikki.dykoprod.utils.MessageHandlerUtils

@Component
class CurrencyRateHandler(
    private val messageSender: MessageSender,
    private val messageTemplates: MessageTemplates,
    private val messageHandlerUtils: MessageHandlerUtils,
    private val inlineKeyboardTemplates: InlineKeyboardTemplates,
    private val currencyHelper: CurrencyHelper,
    private val adminMenuHandler: AdminMenuHandler
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun handleCurrencyRateCallback(update: Update) {
        val chatId = update.callbackQuery.message.chatId
        val callbackData = update.callbackQuery.data

        try {
            when {
                callbackData.startsWith("currency_rate_") -> handleCurrencyRateAdjustment(chatId, callbackData)
                else -> logger.warn("Unknown currency rate callback: $callbackData")
            }
        } catch (e: Exception) {
            logger.error("Error processing currency rate callback", e)
            messageSender.sendText(chatId, "⚠️ Ошибка при загрузке информации. Попробуйте позже.")
        }
    }

    private fun handleCurrencyRateAdjustment(chatId: Long, callbackData: String) {
        val parts = callbackData.split(":")
        val (action, value, currencyCode) = parts

        val (officialCourse, fixCoefficient) = adminMenuHandler.getCurrentCourseData(currencyCode)
        var newCoeff = fixCoefficient

        when (action) {
            "currency_rate_inc" -> newCoeff += value.toDouble()
            "currency_rate_dec" -> newCoeff -= value.toDouble()
            "currency_rate_reset" -> newCoeff = 0.0
        }
        // Сохранение нового коэффициента после изменения
        currencyHelper.updateFixCoefficient(currencyCode, newCoeff)

        val updatedCurrencyRateMessage = messageTemplates.getCurrencyRateMessage(currencyCode, officialCourse, newCoeff)

        messageHandlerUtils.handleMessageEdit(
            chatId = chatId,
            newText = updatedCurrencyRateMessage,
            inlineKeyboard = inlineKeyboardTemplates.getCurrencyRateIK(newCoeff, currencyCode),
            parseMode = "HTML",
            logger = logger
        )
    }
}