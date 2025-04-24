package ru.teomikki.dykoprod.recieve

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import ru.teomikki.dykoprod.command.realization.unique.CallManagerCommand.Companion.AWAITING_QUESTION
import ru.teomikki.dykoprod.command.realization.unique.CallManagerCommand.Companion.AWAITING_CONTACT
import ru.teomikki.dykoprod.command.realization.callback_only.handler.CalculateCategoryHandler.Companion.AWAITING_CALCULATION_INPUT
import ru.teomikki.dykoprod.database.categories.CategoriesHelper
import ru.teomikki.dykoprod.database.clients.ClientsHelper
import ru.teomikki.dykoprod.database.currency.CurrencyHelper
import ru.teomikki.dykoprod.keyboards.InlineKeyboardTemplates
import ru.teomikki.dykoprod.keyboards.ReplyKeyboardTemplates
import ru.teomikki.dykoprod.resources.MessageTemplates
import ru.teomikki.dykoprod.sender.MessageSender
import ru.teomikki.dykoprod.state.UserStateService
import ru.teomikki.dykoprod.utils.MessageHandlerUtils

@Component
class MessageReceived(
    private val userStateService: UserStateService,
    private val inlineKeyboardTemplates: InlineKeyboardTemplates,
    private val replyKeyboardTemplates: ReplyKeyboardTemplates,
    private val databaseClientHelper: ClientsHelper,
    private val messageHandlerUtils: MessageHandlerUtils,
    private val messageSender: MessageSender,
    private val messageTemplates: MessageTemplates,
    private val currencyHelper: CurrencyHelper,
    private val categoriesHelper: CategoriesHelper,
) {

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    fun handleManagerRequestInput(update: Update) {
        val chatId = update.message.chatId
        val userState = userStateService.getState(chatId)

        when (userState.currentInteraction) {
            AWAITING_QUESTION -> {
                parseConfirmationRequest(update)
            }
            AWAITING_CONTACT -> {
                getUserContact(update)
            }
            AWAITING_CALCULATION_INPUT -> {
                handleCalculationInput(update)
            }
        }
    }

    private fun handleCalculationInput(update: Update) {
        val chatId = update.message.chatId
        val userState = userStateService.getState(chatId)
        val inputText = update.message.text

        // Парсим число
        val cnyAmount = try {
            inputText.replace(",", ".").toDouble()
        } catch (e: NumberFormatException) {
            messageHandlerUtils.handleMessageEdit(
                chatId = chatId,
                newText = "⚠️ Введите корректное число (например: 100.23)",
                inlineKeyboard = inlineKeyboardTemplates.getBackToCategoryIK(),
                parseMode = "HTML",
                logger= logger
            )
            return
        }

        // Получаем данные для расчета
        val category = categoriesHelper.getCategory(userState.currentCategory!!)
        val currencyRate = currencyHelper.getFixedCourse("CNY")

        if (category == null || currencyRate == null) {
            messageHandlerUtils.handleMessageEdit(
                chatId = chatId,
                newText = "⚠️ Ошибка расчета. Попробуйте позже.",
                inlineKeyboard = inlineKeyboardTemplates.getBackToCategoryIK(),
                parseMode = "HTML",
                logger= logger
            )
            return
        }

        messageHandlerUtils.handleMessageEdit(
            chatId = chatId,
            newText = messageTemplates.getCalculatedCostMessage(cnyAmount, currencyRate, category),
            inlineKeyboard = inlineKeyboardTemplates.getStartIK(chatId),
            parseMode = "HTML",
            logger = logger
        )

        // Сбрасываем состояние
        userState.currentInteraction = null
    }

    private fun parseConfirmationRequest(update: Update) {
        val chatId = update.message.chatId
        val messageText = update.message.text
        val userState = userStateService.getState(chatId)
        val client = databaseClientHelper.getUser(chatId)

        // Сохранение сообщения в состояниях
        userState.currentManagerQuestion = messageText

        client?.phoneNumber?.let {
            sendConfirmationRequest(chatId, it, messageText)
        }
    }

    private fun getUserContact(update: Update) {
        val chatId = update.message.chatId
        val contact = update.message.contact
        val phoneNumber = formatPhoneNumber(contact.phoneNumber)
        val userState = userStateService.getState(chatId)

        // Удаление сообщений процесса получения контакта
        messageSender.deleteMessage(chatId, update.message.messageId)
        userState.contactMessageId?.let { messageSender.deleteMessage(chatId, it) }

        // Сохраняем в базу
        databaseClientHelper.updatePhoneNumber(chatId, phoneNumber)

        // Обнуляем состояния
        userState.currentInteraction = null
        userState.contactMessageId = null

        userState.currentManagerQuestion?.let {
            sendConfirmationRequest(chatId, phoneNumber, it)
        }
    }

    private fun sendConfirmationRequest(chatId: Long, phoneNumber: String, messageText: String) {
        val userState = userStateService.getState(chatId)


        if (phoneNumber == "не указан") {
            messageSender.sendText(
                chatId = chatId,
                text = "При желании Вы можете оставить свои контакты в заявке",
                replyKeyboard = replyKeyboardTemplates.getUserContactRK()
            )?.let { messageId ->
                userState.contactMessageId = messageId
            }

            userState.currentInteraction = AWAITING_CONTACT
        }

        messageHandlerUtils.handleMessageEdit(
            chatId = chatId,
            newText = messageTemplates.getUserConfirmationMessage(messageText, phoneNumber),
            inlineKeyboard = inlineKeyboardTemplates.getManagerConfirmationIK(),
            parseMode = "HTML",
            logger = logger
        )
    }

    private fun formatPhoneNumber(phone: String): String {
        return if (phone.startsWith("+7")) {
            String.format(
                "%s (%s) %s-%s-%s",
                phone.substring(0, 2),
                phone.substring(2, 5),
                phone.substring(5, 8),
                phone.substring(8, 10),
                phone.substring(10)
            )
        } else if (phone.startsWith("7") || phone.startsWith("8")) {
            String.format(
                "+7 (%s) %s-%s-%s",
                phone.substring(1, 4),
                phone.substring(4, 7),
                phone.substring(7, 9),
                phone.substring(9)
            )
        } else {
            phone
        }
    }
}