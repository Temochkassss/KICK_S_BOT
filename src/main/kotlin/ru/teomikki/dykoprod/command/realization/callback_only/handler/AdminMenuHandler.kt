package ru.teomikki.dykoprod.command.realization.callback_only.handler

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.api.objects.Update
import ru.teomikki.dykoprod.database.admin.AdminHelper
import ru.teomikki.dykoprod.database.categories.CategoriesHelper
import ru.teomikki.dykoprod.database.currency.CurrencyHelper
import ru.teomikki.dykoprod.keyboards.InlineKeyboardTemplates
import ru.teomikki.dykoprod.resources.MessageTemplates
import ru.teomikki.dykoprod.sender.MessageSender
import ru.teomikki.dykoprod.utils.MessageHandlerUtils

@Component
class AdminMenuHandler(
    private val messageSender: MessageSender,
    private val messageTemplates: MessageTemplates,
    private val messageHandlerUtils: MessageHandlerUtils,
    private val inlineKeyboardTemplates: InlineKeyboardTemplates,
    private val currencyHelper: CurrencyHelper,
    private val categoriesHelper: CategoriesHelper,
    private val adminHelper: AdminHelper,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun handleAdminMenuCallback(update: Update) {
        val chatId = update.callbackQuery.message.chatId
        val callbackQuery = update.callbackQuery
        val callbackData = callbackQuery.data

        try {
            when(callbackData) {
                "admin_menu_categories_coeff" -> handleCategoryCoeff(chatId)
                "admin_menu_currency_rate" -> handleCurrencyRate(chatId)
                "admin_menu_broadcast" -> createBroadcast(chatId, callbackQuery)
                "admin_menu_change_password" -> changePassword(chatId, callbackQuery)
                else -> logger.warn("Unknown user responses callback: $callbackData")
            }
        } catch (e: Exception) {
            logger.error("Error processing admin menu callback", e)
            messageSender.sendText(chatId, "⚠️ Ошибка при загрузке информации. Попробуйте позже.")
        }
    }

    private fun handleCategoryCoeff(chatId: Long) {
        try {
            val categories = categoriesHelper.getAllCategories()

            messageHandlerUtils.handleMessageEdit(
                chatId = chatId,
                newText = "Выберете категорию для редактирования:",
                inlineKeyboard = inlineKeyboardTemplates.getCategoriesForEditIK(categories),
                parseMode = "HTML",
                logger = logger
            )


        } catch (e: Exception){
            logger.error("Error handling category coefficient", e)
            messageSender.sendText(chatId, "⚠️ Ошибка загрузки категорий")
        }
    }


    private fun handleCurrencyRate(chatId: Long) {
        try {
            val currencyCode = "CNY"
            val (officialCourse, fixCoefficient) = getCurrentCourseData(currencyCode)

            messageHandlerUtils.handleMessageEdit(
                chatId = chatId,
                newText = messageTemplates.getCurrencyRateMessage(currencyCode, officialCourse, fixCoefficient),
                inlineKeyboard = inlineKeyboardTemplates.getCurrencyRateIK(fixCoefficient, currencyCode),
                parseMode = "HTML",
                logger = logger
            )
        } catch (e: Exception) {
            logger.error("Error handling currency rate", e)
            messageSender.sendText(chatId, "⚠️ Ошибка загрузки курса валюты")
        }
    }

    fun getCurrentCourseData(currencyCode: String): Pair<Double, Double> {
        val fixedCourse= currencyHelper.getFixedCourse(currencyCode) ?: 0.0
        val fixCoefficient = currencyHelper.getFixCoefficient(currencyCode)
        return Pair(fixedCourse - fixCoefficient, fixCoefficient)
    }


    private fun createBroadcast(chatId: Long, callbackQuery: CallbackQuery) {
        messageSender.sendWindowAlert(
            callbackQuery = callbackQuery,
            alertText = messageTemplates.getUnderDevelopmentMessage()
        )
    }

    private fun changePassword(chatId: Long, callbackQuery: CallbackQuery) {
        messageSender.sendWindowAlert(
            callbackQuery = callbackQuery,
            alertText = messageTemplates.getUnderDevelopmentMessage()
        )
    }


}