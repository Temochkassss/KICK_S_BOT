package ru.teomikki.dykoprod.configuration

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import ru.teomikki.dykoprod.database.admin.AdminHelper
import ru.teomikki.dykoprod.database.categories.CategoriesHelper
import ru.teomikki.dykoprod.database.clients.ClientsHelper
import ru.teomikki.dykoprod.database.currency.CurrencyHelper
import ru.teomikki.dykoprod.database.response.ResponseHelper

@Configuration
class DatabaseConfiguration(
    private val clientsHelper: ClientsHelper,
    private val adminHelper: AdminHelper,
    private val currencyHelper: CurrencyHelper,
    private val categoriesHelper: CategoriesHelper,
    private val responseHelper: ResponseHelper
) {

    @PostConstruct
    fun init() {
        try {
            clientsHelper.createClientsTable()
            adminHelper.createAdminTable()
            currencyHelper.createCurrencyTable()
            categoriesHelper.createCurrencyTable()
            responseHelper.createClientsTable()

            logger.info("Database table initialized successfully")
        } catch (e: Exception) {
            logger.error("Database initialization failed", e)
            throw IllegalStateException("Failed to initialize database", e)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DatabaseConfiguration::class.java)
    }
}