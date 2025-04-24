package ru.teomikki.dykoprod.resources

import org.springframework.stereotype.Service
import ru.teomikki.dykoprod.database.admin.AdminHelper
import ru.teomikki.dykoprod.database.categories.Category
import ru.teomikki.dykoprod.database.response.ResponseHelper

@Service
class MessageTemplates(
    private val adminHelper: AdminHelper,
) {
    fun getWelcomeMessage(userName: String): String {
        val notNullUserName = if (userName != "") {
            ", $userName"
        } else {
            ""
        }
        return """
            <b>Привет$notNullUserName! 👋</b>
            
            Я твой персональный помощник для работы с POIZON.
            Помогу рассчитать стоимость заказов и отвечу на вопросы.
            
            ✨ <i>Используй команды:</i>
            /calculate_the_cost - Расчёт стоимости
            /help - Помощь по боту
            /see_reviews - Отзывы клиентов
            
            #POIZON #Помощник #Калькулятор
        """.trimIndent()
    }

    fun getMainMenuMessage(): String {
        return """
            Итак, чем именно я могу Вам помочь?
        """.trimIndent()
    }

    fun getHelpMessage(): String {
        return """
            Вот что я могу сделать для тебя:
            
            /calculate - Рассчитать стоимость заказа
            /help - Получить справку по боту
            /reviews - Посмотреть отзывы клиентов
            /call_the_manager - Связаться с менеджером
            /questions_answers - Просмотреть ответы на частые вопросы
            
            Если у тебя есть другие вопросы, обращайся!
        """.trimIndent()
    }

    fun getCalculateMessage(): String {
        return """
            Выберите категорию товара:
        """.trimIndent()
    }

    fun getUserConfirmationMessage(messageText: String, phoneNumber: String?) = """
        📬 <b>Ваш запрос на связь с менеджером</b>
        ━━━━━━━━━━━━━━━━━━━━
        
        ✉️ <b>Вопрос:</b>
        <i>$messageText</i>
                    
        📱 <b>Контактные данные:</b>
        • Номер телефона: ${phoneNumber ?: "не указан"}
        
        ━━━━━━━━━━━━━━━━━━━━
        <i>Проверьте данные перед отправкой</i>
    """.trimIndent()

    fun getResponseDetailsMessage(response: ResponseHelper.ResponseDetails): String {
        val adminChatId = adminHelper.getAdminChatId()
        val adminUserName = adminChatId?.let { adminHelper.getAdminUsername(it) }
        return """
            📄 <b>Детали обращения #${response.responseId.take(6)}</b>
            ━━━━━━━━━━━━━━━━━━━━
            
            📅 <b>Дата:</b> ${response.dateTime}
            👤 <b>Автор:</b> @${response.username ?: "аноним"}
            👨🏻‍💻 <b>Менеджер:</b> @$adminUserName
            
            📝 <b>Текст обращения:</b>
            <i>${response.question ?: "не указан"}</i>
            
            📱 <b>Контакты:</b>
            • Телефон: ${response.phoneNumber ?: "не указан"}
            
            ━━━━━━━━━━━━━━━━━━━━
            <i>Категория: ${response.category ?: "не указана"}</i>
        """.trimIndent()
    }

    fun getConfirmationMessage(username: String?) = """
        ✅ <b>Запрос успешно отправлен!</b>
        
        Наш <b><a href="https://t.me/${username ?: ""}">менеджер</a></b> свяжется с Вами в <b>ближайшее время</b>.
        
        ⏳ Время обработки: Пн-Пт 10:00-19:00
        📍 Часовой пояс: МСК
    """.trimIndent()

    fun getManagerRequestMessage(
        username: String?,
        dateTime: String,
        question: String?,
        selectedCategory: String?,
        mostViewedCategory: String?,
        phoneNumber: String?
    ) = """
        🔔 <b>НОВЫЙ ЗАПРОС ОТ КЛИЕНТА</b> 🔔
        
        ┏━━━━━━━━━━━━━━━━━━┓
        👤 <b><a href="https://t.me/${username ?: ""}">Клиент:</a></b> @${username ?: "Аноним"}
        📆 <b>Время:</b> $dateTime
        ┗━━━━━━━━━━━━━━━━━━┛
        
        📝 <b>Вопрос:</b>
        <i>${question ?: "Не указан"}</i>
        
        🗂️ <b>Категории:</b>
        • Выбранная: ${selectedCategory ?: "не выбрана"}
        • Популярная: ${mostViewedCategory ?: "новый пользователь"}
        
        📲 <b>Контакты:</b>
        • Номер телефона: ${phoneNumber ?: "не указан"}
      
    """.trimIndent()

    fun getAlertMessage() : String = """
        После нажатия кнопки "✅ Да", отклик будет отмечен как "завершенный".
        
        * Его можно будет найти в настройках администратора.
    """.trimIndent()

    fun getUnderDevelopmentMessage() : String = """
        Данная кнопка в разработке 👨🏻‍💻
        По уточнениям: @TemochkaMik с 9:00 до 18:00
    """.trimIndent()

    fun getQuestionsAnswersMessage(): String = "ℹ️ <b>Часто задаваемые вопросы</b>"

    fun getCurrencyRateMessage(currencyCode: String, officialCourse: Double, fixCoeff: Double): String {
        val fixedCourse = officialCourse + fixCoeff

        return """
            💰 <b>Управление курсом $currencyCode</b>
            
            📊 Текущий курс: ${"%.2f".format(officialCourse).replace(".", ",")} ¥
            ⚡️ Надбавка: ${if (fixCoeff >= 0) "+" else ""}${"%.2f".format(fixCoeff).replace(".", ",")}
            🔢 Итоговый курс: <b>${"%.2f".format(fixedCourse).replace(".", ",")} ₽/¥</b>
            
            📈 Примеры пересчета:
              • 20 ¥ = ${"%.2f".format(20 * fixedCourse).replace(".", ",")} ₽
              • 50 ¥ = ${"%.2f".format(50 * fixedCourse).replace(".", ",")} ₽
              • 70 ¥ = ${"%.2f".format(70 * fixedCourse).replace(".", ",")} ₽
              • 100 ¥ = ${"%.2f".format(100 * fixedCourse).replace(".", ",")} ₽
              • 200 ¥ = ${"%.2f".format(200 * fixedCourse).replace(".", ",")} ₽
            
            <i>Используйте кнопки для регулировки надбавки</i>
        """.trimIndent()
    }

    fun getCategoryEditMessage(
        category: Category,
        currencyRate: Double
    ): String {
        val hasPromo = category.promo?.let { it > 0 } ?: false

        val formula = if (hasPromo) {
            "(CNY × курс) + коэффициент - ${category.promo!!}%"
        } else {
            "(CNY × курс) + коэффициент"
        }

        val examples = listOf(100, 200, 250, 300, 400).joinToString("\n") { cny ->
            val base = (cny * currencyRate) + category.coefficient
            val total = if (hasPromo) {
                base * (1 - category.promo!! / 100.0)
            } else {
                base
            }

            val baseFormatted = "%.2f".format(base).replace(".", ",")
            val totalFormatted = "%.2f".format(total).replace(".", ",")

            if (hasPromo) {
                "• $cny ¥ = $baseFormatted ₽ -${category.promo}% = $totalFormatted ₽"
            } else {
                "• $cny ¥ = $totalFormatted ₽"
            }
        }

        return """
            |🎯 Категория: <b>${category.customName}</b>
            |${"─".repeat(20)}
            |
            |📌 <b>Основные параметры:</b>
            |├ Коэффициент: <code>+${category.coefficient} ₽</code>
            |├ Курс: ${"%.2f".format(currencyRate)} ₽/¥
            |${if (hasPromo) "└ Скидка: 🏷️ ${category.promo}%\n" else ""}
            |📈 <b>Примеры расчета:</b>
            |$examples
            |
            |🔍 <i>Формула: 
            |     $formula</i>
        """.trimMargin()
    }

    fun getCalculatedCostMessage(cnyAmount: Double, currencyRate: Double, category: Category): String {
        val hasPromo = category.promo?.let { it > 0 } ?: false

        val base = (cnyAmount * currencyRate) + category.coefficient
        val total = if (hasPromo) base * (1 - category.promo!! / 100.0) else base

        return """
            |🎯 <b>Детали расчёта</b>
            |${"▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"}
            |
            |🛒 <b>Категория:</b> ${category.customName}
            |🌐 <b>Курс:</b> ${"%.2f".format(currencyRate)} ₽/¥
            |📦 <b>Сумма в CNY:</b> ${"%.2f".format(cnyAmount)} ¥
            |🔧 <b>Коэффициент:</b> +${category.coefficient} ₽
            |${if (hasPromo) "🎁 <b>Скидка:</b> ${category.promo}%\n" else ""}
            |
            |🧮 <b>Пошаговый расчёт:</b>
            |├ ${"%.2f".format(cnyAmount)} ¥ × ${"%.2f".format(currencyRate)} ₽ = ${"%.2f".format(cnyAmount * currencyRate).replace(".", ",")} ₽
            |├ ${"%.2f".format(cnyAmount * currencyRate).replace(".", ",")} ₽ + ${category.coefficient} ₽  = ${"%.2f".format(base).replace(".", ",")} ₽
            |${if (hasPromo) "└ ${"%.2f".format(base).replace(".", ",")} ₽ - ${category.promo}% = ${"%.2f".format(total).replace(".", ",")} ₽\n" else ""}
            |${"▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"}
            |
            |💸 <b>Итого:</b> ${"%.2f".format(total).replace(".", ",")} ₽ ${if (hasPromo) "🎉" else "✨"}
            |
            |📩 <i>Нужна помощь? Нажмите кнопку ниже</i> 👇
        """.trimMargin()
    }
} 