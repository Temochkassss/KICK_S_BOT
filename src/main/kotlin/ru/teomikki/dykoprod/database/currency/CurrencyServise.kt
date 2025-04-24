package ru.teomikki.dykoprod.database.currency

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class CurrencyService(
    private val currencyHelper: CurrencyHelper
) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val CURRENCY_API_URL = "https://api.exchangerate-api.com/v4/latest/CNY"

    @Scheduled(fixedRate = 3600000) // Каждый час
    fun updateCurrencyRate() {
        try {
            val restTemplate = RestTemplate()
            val response = restTemplate.getForObject(CURRENCY_API_URL, Map::class.java) // GET-запрос
            val rate = response?.get("rates") as Map<*, *> // MAP с курсами валют
            val rubRate = rate["RUB"] as Double

            currencyHelper.saveCurrencyCourse("CNY", rubRate)
        } catch (e: Exception) {
            logger.error("Error update currency: ${e.message}")
        }
    }
}