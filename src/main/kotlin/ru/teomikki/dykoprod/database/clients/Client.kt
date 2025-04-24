package ru.teomikki.dykoprod.database.clients

data class Client(
    val chatId: Long,
    val username: String?,
    val phoneNumber: String?,
    val category: String?
)