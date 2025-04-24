package ru.teomikki.dykoprod.state

import org.springframework.stereotype.Service
import ru.teomikki.dykoprod.database.clients.ClientsHelper
import java.util.concurrent.ConcurrentHashMap

@Service
class UserStateService(
    private val databaseClientHelper: ClientsHelper
) {
    private val userStates = ConcurrentHashMap<Long, UserState>()

    fun getState(chatId: Long): UserState {
        return userStates.getOrPut(chatId){ UserState() }
    }

    fun updateMessageId(chatId: Long, newMessageId: Int) {
        val state = getState(chatId)
        state.currentMessageId?.let {
            state.previousMessages.add(it)
        }
        state.currentMessageId = newMessageId
    }

    fun clearState(chatId: Long) {
        userStates.remove(chatId)
    }

    fun getCurrentMessageId(chatId: Long): Int? {
        return userStates[chatId]?.currentMessageId
    }

    fun updateChosenCategory(chatId: Long, category: String, label: String) {
        val state = getState(chatId)

        // Обновляем счетчик просмотров
        state.categoryViews[category] = state.categoryViews.getOrDefault(category, 0) + 1

        // Находим самую популярную категорию
        val mostViewed = state.categoryViews.maxByOrNull { it.value }?.key

        // Сохраняем в базу данных
        mostViewed?.let {
            databaseClientHelper.updateMostViewedCategory(chatId, it)
        }

        state.currentCategory = category
        state.currentCategoryLabel = label
    }

    fun isAdmin(chatId: Long): Boolean {
        return userStates[chatId]?.isAdmin ?: false
    }

    fun haveResponses(chatId: Long): Boolean {
        return userStates[chatId]?.haveResponses ?: false
    }

    // Сохранить связь ResponseId → MessageId
    fun addResponseMessageMapping(chatId: Long, responseId: String, messageId: Int) {
        val state = getState(chatId)
        state.mapMessageIdByResponseId[responseId] = messageId
    }

    // Получить MessageId по ResponseId
    fun getMessageIdByResponseId(chatId: Long, responseId: String): Int? {
        return userStates[chatId]?.mapMessageIdByResponseId?.get(responseId)
    }

    // Удалить связь по ResponseId
    fun removeResponseMessageMapping(chatId: Long, responseId: String) {
        userStates[chatId]?.mapMessageIdByResponseId?.remove(responseId)
    }

    // Сохранить связь ResponseId → Alert
    fun addAlertTextMessageMapping(chatId: Long, responseId: String, alertText: String) {
        val state = getState(chatId)
        state.mapAlertTextByResponseId[responseId] = alertText
    }

    // Получить Alert по ResponseId
    fun getAlertTextIdByResponseId(chatId: Long, responseId: String): String? {
        return userStates[chatId]?.mapAlertTextByResponseId?.get(responseId)
    }

    // Удалить связь по ResponseId
    fun removeAlertTextMessageMapping(chatId: Long, responseId: String) {
        userStates[chatId]?.mapAlertTextByResponseId?.remove(responseId)
    }
}