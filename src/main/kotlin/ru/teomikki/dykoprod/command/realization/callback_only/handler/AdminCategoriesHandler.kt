package ru.teomikki.dykoprod.command.realization.callback_only.handler

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import ru.teomikki.dykoprod.database.categories.CategoriesHelper
import ru.teomikki.dykoprod.database.categories.Category
import ru.teomikki.dykoprod.database.currency.CurrencyHelper
import ru.teomikki.dykoprod.keyboards.InlineKeyboardTemplates
import ru.teomikki.dykoprod.resources.MessageTemplates
import ru.teomikki.dykoprod.sender.MessageSender
import ru.teomikki.dykoprod.utils.MessageHandlerUtils

@Component
class AdminCategoriesHandler (
    private val messageSender: MessageSender,
    private val messageTemplates: MessageTemplates,
    private val messageHandlerUtils: MessageHandlerUtils,
    private val inlineKeyboardTemplates: InlineKeyboardTemplates,
    private val categoriesHelper: CategoriesHelper,
    private val currencyHelper: CurrencyHelper
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun handleEditCategoriesCallback(update: Update) {
        val chatId = update.callbackQuery.message.chatId
        val callbackData = update.callbackQuery.data

        try {
            when {
                callbackData.startsWith("edit_categories_select:") -> showCategoryDetails(chatId, callbackData)
                callbackData.startsWith("edit_categories_") -> handleCoeffAdjustment(chatId, callbackData)
                else -> logger.warn("Unknown edit categories callback: $callbackData")
            }
        } catch (e: Exception) {
            logger.error("Error processing edit categories callback", e)
            messageSender.sendText(chatId, "⚠️ Ошибка при загрузке информации. Попробуйте позже.")
        }
    }

    private fun showCategoryDetails(chatId: Long, callbackData: String) {
        val parts = callbackData.split(":")
        val categoryName = parts[1]
        val category = categoriesHelper.getCategory(categoryName) ?: run {
            messageSender.sendText(chatId, "❌ Категория не найдена")
            return
        }
        showCategoryEditMenu(chatId, category)
    }

    private fun handleCoeffAdjustment(chatId: Long, callbackData: String) {
        val parts = callbackData.split(":")
        val (action, value, categoryName) = parts

        val category = categoriesHelper.getCategory(categoryName) ?: run {
            messageSender.sendText(chatId, "❌ Категория не найдена")
            return
        }

        when (action) {
            "edit_categories_coeff_inc" -> updateCoefficient(category, value.toInt())
            "edit_categories_coeff_dec" -> updateCoefficient(category, -value.toInt())

            "edit_categories_promo_toggle" -> {
                togglePromo(chatId, category, categoryName)
                return  // После togglePromo не продолжаем, так как он сам управляет переходом
            }
            "edit_categories_promo_inc" -> updatePromo(category, value.toInt())
            "edit_categories_promo_dec" -> updatePromo(category, -value.toInt())
            "edit_categories_promo_reset" -> {
                resetPromo(category)
                showCategoryEditMenu(chatId, categoriesHelper.getCategory(categoryName)!!)
                return
            }
            else -> {
                logger.warn("Unknown action: $action")
                return
            }
        }

        val updatedCategory = categoriesHelper.getCategory(categoryName) ?: run {
            messageSender.sendText(chatId, "❌ Категория не найдена после обновления")
            return
        }

        // Для остальных действий определяем панель на основе типа действия
        when {
            action.startsWith("edit_categories_promo") -> showPromoEditMenu(chatId, updatedCategory)
            else -> showCategoryEditMenu(chatId, updatedCategory)
        }
    }


    private fun updateCoefficient(category: Category, delta: Int) {
        categoriesHelper.updateCoefficient(category.name, (category.coefficient + delta).coerceIn(0, Int.MAX_VALUE))
    }

    private fun showCategoryEditMenu(chatId: Long, category: Category) {
        showEditMenu(chatId, category, inlineKeyboardTemplates.getCategoryEditMenuIK(category))
    }

    private fun showPromoEditMenu(chatId: Long, category: Category) {
        showEditMenu(chatId, category, inlineKeyboardTemplates.getPromoEditMenuIK(category))
    }

    companion object {
        private const val DEFAULT_PROMO_PERCENT = 10
        private const val MIN_PROMO = 0
        private const val MAX_PROMO = 100
    }

    private fun togglePromo(chatId: Long, category: Category, categoryName: String) {
        if (category.promo == null || category.promo == 0) {
            categoriesHelper.updatePromo(category.name, DEFAULT_PROMO_PERCENT)
            categoriesHelper.getCategory(categoryName)?.let { showPromoEditMenu(chatId, it) }
        } else {
            resetPromo(category)
            categoriesHelper.getCategory(categoryName)?.let { showCategoryEditMenu(chatId, it) }
        }
    }

    private fun updatePromo(category: Category, delta: Int) {
        val newPromo = (category.promo ?: 0) + delta
        categoriesHelper.updatePromo(category.name, newPromo.coerceIn(MIN_PROMO, MAX_PROMO)) // Ограничение скидки от 0% до 100%
    }

    private fun resetPromo(category: Category) {
        categoriesHelper.updatePromo(category.name, 0)
    }

    private fun showEditMenu(
        chatId: Long,
        category: Category,
        keyboard: InlineKeyboardMarkup
    ) {
        val currencyRate = currencyHelper.getFixedCourse("CNY") ?: run {
            messageSender.sendText(chatId, "⚠️ Курс валюты не найден")
            return
        }

        messageHandlerUtils.handleMessageEdit(
            chatId = chatId,
            newText = messageTemplates.getCategoryEditMessage(category, currencyRate),
            inlineKeyboard = keyboard,
            parseMode = "HTML",
            logger = logger
        )
    }
}