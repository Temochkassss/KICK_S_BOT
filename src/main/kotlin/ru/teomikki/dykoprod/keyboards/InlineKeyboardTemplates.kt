package ru.teomikki.dykoprod.keyboards

import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import ru.teomikki.dykoprod.database.categories.CategoriesHelper
import ru.teomikki.dykoprod.database.categories.Category
import ru.teomikki.dykoprod.database.response.ResponseHelper
import ru.teomikki.dykoprod.state.UserStateService
import kotlin.math.min

@Service
class InlineKeyboardTemplates(
    private val userStateService: UserStateService,
    @Lazy
    private val categoriesHelper: CategoriesHelper,
) {
    fun getStartIK(chatId: Long) = InlineKeyboardMarkup().apply {
        keyboard = listOf(
            listOf(
                InlineKeyboardButton("\uD83D\uDCB0 Рассчитать стоимость").apply {
                    callbackData = "/calculate_the_cost"
                }
            ),
            listOf(
                InlineKeyboardButton("\uD83D\uDC68\u200D\uD83D\uDCBC Связь с менеджером").apply {
                    callbackData = "/call_the_manager"
                }
            ),
            listOf(
                InlineKeyboardButton("❓ Вопросы и ответы").apply {
                    callbackData = "/questions_answers"
                },
            ),
            listOf(
                InlineKeyboardButton("\uD83D\uDD04 Перезапустить").apply {
                    callbackData = "/start"
                },
                InlineKeyboardButton("  ОТЗЫВЫ ☞").apply {
                    url = "https://t.me/kicks_k/409"
                }
            )
        ).toMutableList()

        val isAdmin = userStateService.getState(chatId).isAdmin

        if (isAdmin) {
            keyboard.add(
                listOf(
                    InlineKeyboardButton("⚙️ Настройки").apply {
                        this.callbackData = "admin_panel"
                    }
                )
            )
        }
    }

    fun getCalculateIK(categories: List<Category>): InlineKeyboardMarkup {
        return InlineKeyboardMarkup().apply {
            keyboard = categories.map { category ->
                listOf(
                    InlineKeyboardButton(category.customName).apply {
                        callbackData = "calculate_${category.name}"
                    }
                )
            }.toMutableList()
            keyboard.add(
                listOf(
                    InlineKeyboardButton("⌵    Свернуть").apply {
                        callbackData = "back_to_main_menu"
                    },
                ),
            )
        }
    }

    fun getBackOrCloseCategoryIK() = InlineKeyboardMarkup().apply {
        keyboard = listOf(
            listOf(
                InlineKeyboardButton("↩️ Назад к категориям").apply {
                    callbackData = "back_to_calculate_category"
                }
            ),
            listOf(
                InlineKeyboardButton("✖️ Закрыть").apply {
                    this.callbackData = "close_calculate_category"
                }
            )
        )
    }

    fun getBackToCategoryIK() = InlineKeyboardMarkup().apply {
        keyboard = listOf(
            listOf(
                InlineKeyboardButton("⌵    Вернуться").apply {
                    callbackData = "back_to_calculate_category"
                }
            )
        )
    }

    fun getFaqIK() = InlineKeyboardMarkup().apply {
        keyboard = listOf(
            listOf(
                InlineKeyboardButton("📦 Как сделать заказ").apply {
                    callbackData = "faq_order_guide"
                }
            ),
            listOf(
                InlineKeyboardButton("🧮 Порядок расчета").apply {
                    callbackData = "faq_calculation"
                }
            ),
            listOf(
                InlineKeyboardButton("🔍 Поиск артикула").apply {
                    callbackData = "faq_article_search"
                }
            ),
            listOf(
                InlineKeyboardButton("🚚 Доставка и сроки").apply {
                    callbackData = "faq_delivery"
                }
            ),
            listOf(
                InlineKeyboardButton("⚫🔵 Кнопки POIZON").apply {
                    callbackData = "faq_poizon_buttons"
                }
            ),
            listOf(
                InlineKeyboardButton("\uD83D\uDC68\u200D\uD83D\uDCBC Консультация").apply {
                    callbackData = "/call_the_manager"
                }
            ),
            listOf(
                InlineKeyboardButton("⌵ Свернуть").apply {
                    callbackData = "back_to_main_menu"
                }
            )
        )
    }

    fun getBackToFAQIK() = InlineKeyboardMarkup().apply {
        keyboard = listOf(
            listOf(
                InlineKeyboardButton("↩️ Назад к вопросам").apply {
                    this.callbackData = "back_to_faq"
                },
            ),
            listOf(
                InlineKeyboardButton("✖️ Закрыть").apply {
                    this.callbackData = "close_faq"
                }
            )
        )
    }

    fun getProcessingConfirmationIK(responseId: String) = InlineKeyboardMarkup().apply {
        keyboard = listOf(
            listOf(
                InlineKeyboardButton("✅ Подтвердить обработку").apply {
                    callbackData = "proc_confirm:$responseId"
                }
            )
        )
    }

    fun getProcessingConfirmationMenu(responseId: String) = InlineKeyboardMarkup().apply {

        val buttons = listOf(
            InlineKeyboardButton("✅ Да").apply {
                callbackData = "proc_yes:$responseId"
            },
            InlineKeyboardButton("❌ Нет").apply {
                callbackData = "proc_no:$responseId"
            },
            InlineKeyboardButton("❓Чего").apply {
                callbackData = "proc_what:"
            }
        )

        // Перемешиваем кнопки для случайного порядка
        keyboard = listOf(buttons.shuffled())
    }

    fun getManagerConfirmationIK() = InlineKeyboardMarkup().apply {
        keyboard = listOf(
            listOf(
                InlineKeyboardButton("❌ Отменить").apply { callbackData = "cancel_manager_request" },
                InlineKeyboardButton("✅ Подтвердить").apply { callbackData = "confirm_manager_request" },
            ),
        )
    }

    fun getCancelRequestIK() = InlineKeyboardMarkup().apply {
        keyboard = listOf(
            listOf(
                InlineKeyboardButton("❌ Отменить запрос").apply { callbackData = "cancel_manager_request" },
            ),

        )
    }

    fun getCancelAndResponseIK() = InlineKeyboardMarkup().apply {
        keyboard = listOf(
            listOf(
                InlineKeyboardButton("❌ Отменить").apply { callbackData = "cancel_manager_request" },
                InlineKeyboardButton("📨 Мои отклики").apply { callbackData = "my_responses" }
            )
        )
    }

    fun getAdminPanelIK() = InlineKeyboardMarkup(
        listOf(
            listOf(
                InlineKeyboardButton("📨 Отклики").apply {
                    callbackData = "admin_view_responses"
                }
            ),
            listOf(
                InlineKeyboardButton("📈 Коэффициенты").apply {
                    callbackData = "admin_menu_categories_coeff"
                },
                InlineKeyboardButton("💱 Курс валюты").apply {
                    callbackData = "admin_menu_currency_rate"
                }
            ),
            listOf(
                InlineKeyboardButton("📢 Рассылка").apply {
                    callbackData = "admin_menu_broadcast"
                }
            ),
            listOf(
                InlineKeyboardButton("🔐 Сменить пароль").apply {
                    callbackData = "admin_menu_change_password"
                },
                InlineKeyboardButton("👨💻 Разработчик").apply {
                    url = "https://t.me/TemochkaMik"
                }
            ),
            listOf(
                InlineKeyboardButton("⌵    Свернуть").apply {
                    callbackData = "back_to_main_menu"
                },
            ),
        )
    )

    fun getUserResponsesIK(
        responses: List<ResponseHelper.ResponsePreview>,
        currentPage: Int,
        totalPages: Int
    ): InlineKeyboardMarkup {
        val keyboard = mutableListOf<List<InlineKeyboardButton>>()

        // Вертикальные кнопки (каждая в отдельном ряду)
        responses.take(5).forEach { response ->
            keyboard.add(
                listOf(
                    InlineKeyboardButton("📅 ${response.dateTime}").apply {
                        callbackData = "user_response:${response.responseId}"
                    }
                )
            )
        }

        // Пагинация
        if (totalPages > 1) {
            val paginationRow = mutableListOf<InlineKeyboardButton>()

            if (currentPage > 1) {
                paginationRow.add(
                    InlineKeyboardButton("◀️").apply {
                        callbackData = "user_responses_page:${currentPage - 1}"
                    }
                )
            }

            paginationRow.add(
                InlineKeyboardButton("${currentPage}/${totalPages}").apply {
                    callbackData = "user_responses_show_pages:$currentPage"
                }
            )

            if (currentPage < totalPages) {
                paginationRow.add(
                    InlineKeyboardButton("▶️").apply {
                        callbackData = "user_responses_page:${currentPage + 1}"
                    }
                )
            }

            if (paginationRow.isNotEmpty()) {
                keyboard.add(paginationRow)
            }
        }

        // Кнопка возврата
        keyboard.add(
            listOf(
                InlineKeyboardButton("↩️ На главную").apply {
                    callbackData = "back_to_main_menu"
                }
            )
        )

        return InlineKeyboardMarkup(keyboard)
    }

    fun getBackToUserResponseListIK(): InlineKeyboardMarkup {
        return InlineKeyboardMarkup(
            listOf(
                listOf(
                    InlineKeyboardButton("↩️ К списку").apply {
                        callbackData = "back_to_user_responses_list"
                    }
                )
            )
        )
    }

    fun getUserResponsesPageSelectionIK(currentPage: Int, totalPages: Int): InlineKeyboardMarkup {
        val buttonsPerRow = 5
        val pagesPerBlock = 25 // 5 рядов по 5 кнопок
        val keyboard = mutableListOf<List<InlineKeyboardButton>>()

        // Определяем текущий блок
        val currentBlock = (currentPage - 1) / pagesPerBlock
        val startPage = currentBlock * pagesPerBlock + 1
        val endPage = min(startPage + pagesPerBlock - 1, totalPages)

        // Создаём кнопки только для текущего блока
        val blockPages = (startPage..endPage).map { page ->
            InlineKeyboardButton(
                if (page == currentPage) "• $page •" else page.toString()
            ).apply {
                callbackData = "user_responses_page:$page"
            }
        }

        // Разбиваем на ряды по 5 кнопок
        blockPages.chunked(buttonsPerRow).forEach { rowButtons ->
            keyboard.add(rowButtons)
        }

        // Добавляем навигацию по блокам только если страниц больше чем pagesPerBlock
        if (totalPages > pagesPerBlock) {
            val navigationRow = mutableListOf<InlineKeyboardButton>()

            // Кнопка "Предыдущие" (если не в первом блоке)
            if (currentBlock > 0) {
                navigationRow.add(
                    InlineKeyboardButton("◀️ Предыдущие").apply {
                        callbackData = "user_responses_block:${currentBlock - 1}"
                    }
                )
            }

            // Кнопка "Следующие" (если не в последнем блоке)
            if ((currentBlock + 1) * pagesPerBlock < totalPages) {
                navigationRow.add(
                    InlineKeyboardButton("Следующие ▶️").apply {
                        callbackData = "user_responses_block:${currentBlock + 1}"
                    }
                )
            }

            if (navigationRow.isNotEmpty()) {
                keyboard.add(navigationRow)
            }
        }

        // Добавляем кнопку возврата
        keyboard.add(
            listOf(
                InlineKeyboardButton("↩️ Назад к списку").apply {
                    callbackData = "back_to_user_responses_list"
                }
            )
        )

        return InlineKeyboardMarkup(keyboard)
    }


    fun getAdminResponsesIK(
        responses: List<ResponseHelper.ResponsePreview>,
        currentPage: Int,
        totalPages: Int
    ): InlineKeyboardMarkup {
        val keyboard = mutableListOf<List<InlineKeyboardButton>>()

        // Вертикальные кнопки (каждая в отдельном ряду)
        responses.take(5).forEach { response ->
            keyboard.add(
                listOf(
                    InlineKeyboardButton("📅 ${response.dateTime} (ID: ${response.responseId.take(6)})").apply {
                        callbackData = "admin_response:${response.responseId}"
                    }
                )
            )
        }

        // Пагинация
        if (totalPages > 1) {
            val paginationRow = mutableListOf<InlineKeyboardButton>()

            if (currentPage > 1) {
                paginationRow.add(
                    InlineKeyboardButton("◀️").apply {
                        callbackData = "admin_responses_page:${currentPage - 1}"
                    }
                )
            }

            paginationRow.add(
                InlineKeyboardButton("${currentPage}/${totalPages}").apply {
                    callbackData = "admin_responses_show_pages:$currentPage"
                }
            )

            if (currentPage < totalPages) {
                paginationRow.add(
                    InlineKeyboardButton("▶️").apply {
                        callbackData = "admin_responses_page:${currentPage + 1}"
                    }
                )
            }

            if (paginationRow.isNotEmpty()) {
                keyboard.add(paginationRow)
            }
        }

        // Кнопка возврата в админ-панель
        keyboard.add(
            listOf(
                InlineKeyboardButton("↩️ В админку").apply {
                    callbackData = "admin_panel"
                }
            )
        )

        return InlineKeyboardMarkup(keyboard)
    }

    fun getAdminResponseDetailsIK(): InlineKeyboardMarkup {
        return InlineKeyboardMarkup(
            listOf(
                listOf(
                    InlineKeyboardButton("↩️ К списку").apply {
                        callbackData = "back_to_admin_responses_list"
                    }
                )
            )
        )
    }

    fun getAdminResponsesPageSelectionIK(currentPage: Int, totalPages: Int): InlineKeyboardMarkup {
        val buttonsPerRow = 5
        val pagesPerBlock = 25 // 5 рядов по 5 кнопок
        val keyboard = mutableListOf<List<InlineKeyboardButton>>()

        // Определяем текущий блок
        val currentBlock = (currentPage - 1) / pagesPerBlock
        val startPage = currentBlock * pagesPerBlock + 1
        val endPage = min(startPage + pagesPerBlock - 1, totalPages)

        // Создаём кнопки только для текущего блока
        val blockPages = (startPage..endPage).map { page ->
            InlineKeyboardButton(
                if (page == currentPage) "• $page •" else page.toString()
            ).apply {
                callbackData = "admin_responses_page:$page"
            }
        }

        // Разбиваем на ряды по 5 кнопок
        blockPages.chunked(buttonsPerRow).forEach { rowButtons ->
            keyboard.add(rowButtons)
        }

        // Добавляем навигацию по блокам только если страниц больше чем pagesPerBlock
        if (totalPages > pagesPerBlock) {
            val navigationRow = mutableListOf<InlineKeyboardButton>()

            // Кнопка "Предыдущие" (если не в первом блоке)
            if (currentBlock > 0) {
                navigationRow.add(
                    InlineKeyboardButton("◀️ Предыдущие").apply {
                        callbackData = "admin_responses_block:${currentBlock - 1}"
                    }
                )
            }

            // Кнопка "Следующие" (если не в последнем блоке)
            if ((currentBlock + 1) * pagesPerBlock < totalPages) {
                navigationRow.add(
                    InlineKeyboardButton("Следующие ▶️").apply {
                        callbackData = "admin_responses_block:${currentBlock + 1}"
                    }
                )
            }

            if (navigationRow.isNotEmpty()) {
                keyboard.add(navigationRow)
            }
        }

        // Добавляем кнопку возврата
        keyboard.add(
            listOf(
                InlineKeyboardButton("↩️ Назад к списку").apply {
                    callbackData = "back_to_admin_responses_list"
                }
            )
        )

        return InlineKeyboardMarkup(keyboard)
    }



    fun getCurrencyRateIK(currentCoeff: Double, currencyCode: String = "CNY"): InlineKeyboardMarkup {
        return InlineKeyboardMarkup().apply {
            keyboard = mutableListOf(
                // Заголовок с текущим коэффициентом
                listOf(
                    InlineKeyboardButton( "⚡️Текущая надбавка: ${"%.2f".format(currentCoeff)} ¥").apply {
                        callbackData = "none"
                    }
                ),

                // Основные кнопки регулировки
                listOf(
                    InlineKeyboardButton("➖ 0.05").apply {
                        callbackData = "currency_rate_dec:0.05:$currencyCode"
                    },
                    InlineKeyboardButton("➕ 0.05").apply {
                        callbackData = "currency_rate_inc:0.05:$currencyCode"
                    }
                ),
                listOf(
                    InlineKeyboardButton("➖ 0.10").apply {
                        callbackData = "currency_rate_dec:0.10:$currencyCode"
                    },
                    InlineKeyboardButton("➕ 0.10").apply {
                        callbackData = "currency_rate_inc:0.10:$currencyCode"
                    }
                ),
                listOf(
                    InlineKeyboardButton("➖ 0.50").apply {
                        callbackData = "currency_rate_dec:0.50:$currencyCode"
                    },
                    InlineKeyboardButton("➕ 0.50").apply {
                        callbackData = "currency_rate_inc:0.50:$currencyCode"
                    }
                ),

                // Управление и дополнительные действия
                listOf(
                    InlineKeyboardButton("◀️ Назад").apply {
                        callbackData = "admin_panel"
                    },
                    InlineKeyboardButton("🔄 Сбросить").apply {
                        callbackData = "currency_rate_reset:0.00:$currencyCode"
                    },
                ),
            )
        }
    }

    fun getCategoriesForEditIK(categories: List<Category>): InlineKeyboardMarkup {
        val keyboard = mutableListOf<List<InlineKeyboardButton>>()

        categories.forEach { category ->
            val customName = category.customName
            val coefficient = category.coefficient

            keyboard.add(
                listOf(
                    InlineKeyboardButton("$customName ~ $coefficient ₽").apply {
                        callbackData = "edit_categories_select:${category.name}"
                    },
                )
            )
        }

        keyboard.add(
            listOf(
                InlineKeyboardButton("◀️ Назад").apply {
                    callbackData = "admin_panel"
                }
            )
        )

        return InlineKeyboardMarkup(keyboard)
    }

    fun getCategoryEditMenuIK(category: Category): InlineKeyboardMarkup {
        return InlineKeyboardMarkup().apply {
            keyboard = mutableListOf(
                listOf(
                    InlineKeyboardButton( "⚡️Текущая надбавка: ${category.coefficient} ₽").apply {
                        callbackData = "none"
                    }
                ),
                listOf(
                    InlineKeyboardButton("➖ 50 ₽").apply {
                        callbackData = "edit_categories_coeff_dec:50:${category.name}"
                    },
                    InlineKeyboardButton("➕ 50 ₽").apply {
                        callbackData = "edit_categories_coeff_inc:50:${category.name}"
                    }
                ),
                listOf(
                    InlineKeyboardButton("➖ 100 ₽").apply {
                        callbackData = "edit_categories_coeff_dec:100:${category.name}"
                    },
                    InlineKeyboardButton("➕ 100 ₽").apply {
                        callbackData = "edit_categories_coeff_inc:100:${category.name}"
                    }
                ),
                listOf(
                    InlineKeyboardButton("➖ 250 ₽").apply {
                        callbackData = "edit_categories_coeff_dec:250:${category.name}"
                    },
                    InlineKeyboardButton("➕ 250 ₽").apply {
                        callbackData = "edit_categories_coeff_inc:250:${category.name}"
                    }
                ),
                listOf(
                    InlineKeyboardButton(if (category.promo == null || category.promo == 0) "🎁 Добавить скидку" else "❌ Убрать скидку").apply {
                        callbackData = "edit_categories_promo_toggle:0:${category.name}"
                    }
                ),
                listOf(
                    InlineKeyboardButton("◀️ Назад").apply {
                        callbackData = "admin_menu_categories_coeff"
                    }
                )
            )
        }
    }


    fun getPromoEditMenuIK(category: Category): InlineKeyboardMarkup {
        return InlineKeyboardMarkup().apply {
            keyboard = mutableListOf(
                listOf(
                    InlineKeyboardButton( "⚡️Текущая скидка: ${category.promo} %").apply {
                        callbackData = "none"
                    }
                ),
                listOf(
                    InlineKeyboardButton("➖ 1 %").apply {
                        callbackData = "edit_categories_promo_dec:1:${category.name}"
                    },
                    InlineKeyboardButton("➕ 1 %").apply {
                        callbackData = "edit_categories_promo_inc:1:${category.name}"
                    }
                ),
                listOf(
                    InlineKeyboardButton("➖ 5 %").apply {
                        callbackData = "edit_categories_promo_dec:5:${category.name}"
                    },
                    InlineKeyboardButton("➕ 5 %").apply {
                        callbackData = "edit_categories_promo_inc:5:${category.name}"
                    }
                ),
                listOf(
                    InlineKeyboardButton("➖ 10 %").apply {
                        callbackData = "edit_categories_promo_dec:10:${category.name}"
                    },
                    InlineKeyboardButton("➕ 10 %").apply {
                        callbackData = "edit_categories_promo_inc:10:${category.name}"
                    }
                ),
                listOf(
                    InlineKeyboardButton("➖ 25 %").apply {
                        callbackData = "edit_categories_promo_dec:25:${category.name}"
                    },
                    InlineKeyboardButton("➕ 25 %").apply {
                        callbackData = "edit_categories_promo_inc:25:${category.name}"
                    }
                ),
                listOf(
                    InlineKeyboardButton("🔄 Сбросить").apply {
                        callbackData = "edit_categories_promo_reset:0:${category.name}" }
                ),
                listOf(
                    InlineKeyboardButton("◀️ Назад").apply {
                        callbackData = "edit_categories_select:${category.name}"
                    }
                )
            )
        }
    }

}