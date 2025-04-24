package ru.teomikki.dykoprod.command

import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import ru.teomikki.dykoprod.command.realization.callback_only.handler.*
import ru.teomikki.dykoprod.recieve.MessageReceived
import ru.teomikki.dykoprod.sender.MessageCleanupInterceptor

@Component
class TextDispatcher(
    @Lazy
    private val commands: List<Command>,
    private val messageCleanup: MessageCleanupInterceptor,
    @Lazy
    private val faqHandler: FaqHandler,
    @Lazy
    private val calculateCategoryHandler: CalculateCategoryHandler,
    @Lazy
    private val requestManagerHandler: RequestManagerHandler,
    @Lazy
    private val userResponsesHandler: UserResponsesHandler,
    @Lazy
    private val adminResponsesHandler: AdminResponsesHandler,
    @Lazy
    private val processingHandler: ProcessingHandler,
    @Lazy
    private val adminMenuHandler: AdminMenuHandler,
    @Lazy
    private val currencyRateHandler: CurrencyRateHandler,
    @Lazy
    private val editCategoryHandler: AdminCategoriesHandler,
    @Lazy
    private val messageReceived: MessageReceived,
) {
    fun handleCommand(update: Update) {
        // Удаление системного сообщения
        messageCleanup.preHandle(update)

        val message = update.message
        if (message.isCommand) {
            val commandName = message.text.split(" ")[0]
            commands.find { it.name() == commandName }?.execute(update)
        }
        else {
            messageReceived.handleManagerRequestInput(update)
        }
    }

    fun handleCallback(update: Update) {
        val callbackQuery = update.callbackQuery
        val callbackName = callbackQuery.data

        when {
            callbackName.contains("calculate_category") -> calculateCategoryHandler.handleCalculateCategoryCallback(update)
            callbackName.contains("faq") -> faqHandler.handleFaqCallback(update)
            callbackName.contains("_manager_") -> requestManagerHandler.handleRequestManagerCallback(update)
            callbackName.contains("user_response") -> userResponsesHandler.handleUserResponsesCallback(update)
            callbackName.contains("admin_response") -> adminResponsesHandler.handleAdminResponsesCallback(update)
            callbackName.contains("admin_menu_") -> adminMenuHandler.handleAdminMenuCallback(update)
            callbackName.contains("proc_") -> processingHandler.handleProcessingCallback(update)
            callbackName.contains("currency_rate_") -> currencyRateHandler.handleCurrencyRateCallback(update)
            callbackName.contains("edit_categories") -> editCategoryHandler.handleEditCategoriesCallback(update)
            else -> {
                val command = commands.find { it.name() == callbackName}
                command?.handleCallback(update)
            }
        }
    }
}
