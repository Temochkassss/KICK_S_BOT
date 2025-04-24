package ru.teomikki.dykoprod.configuration

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.telegram.telegrambots.bots.DefaultBotOptions
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession
import ru.teomikki.dykoprod.command.ContactDispatcher
import ru.teomikki.dykoprod.service.BotService
import ru.teomikki.dykoprod.property.BotProperty
import ru.teomikki.dykoprod.command.TextDispatcher
import ru.teomikki.dykoprod.database.admin.AdminHelper
import ru.teomikki.dykoprod.database.categories.CategoriesHelper
import ru.teomikki.dykoprod.database.clients.ClientsHelper
import ru.teomikki.dykoprod.database.currency.CurrencyHelper

@Configuration
class BotConfiguration {

    @Autowired
    private lateinit var botProperty: BotProperty

    @Autowired
    private lateinit var textDispatcher: TextDispatcher

    @Autowired
    private lateinit var contactDispatcher: ContactDispatcher


    @Bean
    fun botOptions(): DefaultBotOptions {
        return DefaultBotOptions().apply {
            maxThreads = 3
        }
    }

    @Bean
    fun botService(options: DefaultBotOptions): BotService {
        return BotService(botProperty, textDispatcher, contactDispatcher, options)
    }

    @EventListener(ContextRefreshedEvent::class)
    fun init() {
        try {
            val telegramBotsApi = TelegramBotsApi(DefaultBotSession::class.java)
            val bot = botService(botOptions())
            telegramBotsApi.registerBot(bot)
            println("Бот успешно зарегистрирован в Telegram API")
        } catch (e: TelegramApiException) {
            e.printStackTrace()
        }
    }
}
