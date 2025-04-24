package ru.teomikki.dykoprod.command.realization.callback_only.handler

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import ru.teomikki.dykoprod.database.categories.CategoriesHelper
import ru.teomikki.dykoprod.database.currency.CurrencyHelper
import ru.teomikki.dykoprod.keyboards.InlineKeyboardTemplates
import ru.teomikki.dykoprod.resources.MessageTemplates
import ru.teomikki.dykoprod.sender.MessageSender
import ru.teomikki.dykoprod.state.UserStateService
import ru.teomikki.dykoprod.utils.MessageHandlerUtils
import java.lang.Exception

@Component
class CalculateCategoryHandler(
    private val messageSender: MessageSender,
    private val messageTemplates: MessageTemplates,
    private val inlineKeyboardTemplates: InlineKeyboardTemplates,
    private val messageHandlerUtils: MessageHandlerUtils,
    private val userStateService: UserStateService,
    @Lazy
    private val categoriesHelper: CategoriesHelper,
    private val currencyHelper: CurrencyHelper
) {

    companion object {
        const val AWAITING_CALCULATION_INPUT = "awaiting_calculation_input"
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    fun handleCalculateCategoryCallback(update: Update) {
        val chatId = update.callbackQuery.message.chatId
        val callbackData = update.callbackQuery.data

        try {
            when {
                callbackData == "close_calculate_category" -> closeCalculateMessage(chatId)
                callbackData == "back_to_calculate_category" -> showMainCategoryMenu(chatId)
                callbackData.startsWith("calculate_") -> processCategoryItem(chatId, callbackData)
                else -> logger.warn("Unknown calculate callback: $callbackData")
            }
        } catch (e: Exception) {
            logger.error("Error processing Category callback", e)
            messageSender.sendText(chatId, "⚠️ Ошибка при загрузке информации. Попробуйте позже.")
        }
    }

    private fun closeCalculateMessage(chatId: Long) {
        messageHandlerUtils.handleMessageEdit(
            chatId = chatId,
            newText = messageTemplates.getMainMenuMessage(),
            inlineKeyboard = inlineKeyboardTemplates.getStartIK(chatId),
            parseMode = "HTML",
            logger = logger
        )
    }

    private fun showMainCategoryMenu(chatId: Long) {
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

        // Обнуление состояний
        userStateService.getState(chatId).currentInteraction = null
    }

    private fun processCategoryItem(chatId: Long, callbackData: String?) {
        callbackData?.let {
            val categoryName = it.removePrefix("calculate_")
            val category = categoriesHelper.getCategory(categoryName)

            if (category == null) {
                messageSender.sendText(chatId, "❌ Категория не найдена")
                return
            }

            // Установка состояни ожидания суммы
            userStateService.getState(chatId).apply {
                currentInteraction = AWAITING_CALCULATION_INPUT
            }

            // Отправляем сообщение с просьбой ввести сумму
            messageHandlerUtils.handleMessageEdit(
                chatId = chatId,
                newText = "Введите стоимость товара в ¥ (CNY):",
                inlineKeyboard = inlineKeyboardTemplates.getBackToCategoryIK(),
                parseMode = "HTML",
                logger = logger
            )

            // Обновляем статистику просмотров
            userStateService.updateChosenCategory(
                chatId = chatId,
                category = categoryName,
                label = category.customName
            )
        }
    }
}
