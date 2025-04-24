package ru.teomikki.dykoprod.state

data class UserState(
    var currentMessageId: Int? = null,
    var previousMessages: MutableList<Int> = mutableListOf(),
    var previousTextMessage: String? = null,

    var currentCategory: String? = null,       // Техническое название (напр. "category_sneakers")
    var currentCategoryLabel: String? = null,  // Русское название с эмодзи
    var categoryViews: MutableMap<String, Int> = mutableMapOf(),

    var currentInteraction: String? = null,

    var currentManagerQuestion: String? = null,
    var contactMessageId: Int? = null,

    var isAdmin: Boolean = false,
    var haveResponses: Boolean = false,

    var mapMessageIdByResponseId: MutableMap<String, Int> = mutableMapOf(),
    var mapAlertTextByResponseId: MutableMap<String, String> = mutableMapOf()

)