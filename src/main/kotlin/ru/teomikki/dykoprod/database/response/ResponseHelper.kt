package ru.teomikki.dykoprod.database.response

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.*

@Repository
class ResponseHelper {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private lateinit var connection: Connection

    private fun connect(){
        try {
            val url = "jdbc:sqlite:${CharForResponse.DATABASE_NAME}"
            connection = DriverManager.getConnection(url)
            logger.debug("Database connection established to {}", CharForResponse.DATABASE_NAME)
        } catch (e: SQLException) {
            logger.error("Connection failed to {}: {}", CharForResponse.DATABASE_NAME, e.message, e)
            throw e
        }
    }

    private fun disconnect() {
        try {
            if (::connection.isInitialized && !connection.isClosed) {
                connection.close()
                logger.trace("Database connection closed")
            }
        } catch (e: SQLException) {
            logger.warn("Error closing connection: {}", e.message, e)
        }
    }

    fun createClientsTable() {
        val createTableQuery = CharForResponse.CREATE_TABLE
        try {
            connect()
            connection.createStatement().execute(createTableQuery)
            logger.info("Table {} created successfully", CharForResponse.TABLE_NAME)
        } catch (e: SQLException) {
            logger.error("Table creation failed: {}", e.message, e)
            throw e
        } finally {
            disconnect()
        }
    }

    fun generateUniqueResponseId(): String {
        var responseId: String
        do {
            responseId = UUID.randomUUID().toString().replace("-", "")
        } while (isResponseIdExists(responseId))
        return responseId
    }

    private fun isResponseIdExists(responseId: String): Boolean {
        val checkQuery = CharForResponse.CHECK_RESPONSE_ID_EXISTENCE
        try {
            connect()
            connection.prepareStatement(checkQuery).use { stmt ->
                stmt.setString(1, responseId)
                val resultSet = stmt.executeQuery()
                resultSet.next()
                return  resultSet.getInt(1) > 0
            }
        } catch (e: SQLException) {
            logger.error("Response ID check failed", e)
            return false
        } finally {
            disconnect()
        }
    }

    fun insertResponse(
        responseId: String,
        chatId: Long,
        question: String?,
        username: String?,
        phoneNumber: String?,
        category: String?,
        mostViewedCategory: String?,
        dateTime: String
    ) {
        val insertQuery = CharForResponse.INSERT_RESPONSE
        try {
            connect()
            connection.prepareStatement(insertQuery).use { stmt ->
                stmt.setString(1, responseId)
                stmt.setLong(2, chatId)
                stmt.setString(3, question)
                stmt.setString(4, username)
                stmt.setString(5, phoneNumber)
                stmt.setString(6, category)
                stmt.setString(7, mostViewedCategory)
                stmt.setString(8, dateTime)
                stmt.executeUpdate()
            }
            logger.info("Response inserted successfully [ID: {}]", responseId)
        } catch (e: SQLException) {
            logger.error("Response insertion failed", e)
            throw e
        } finally {
            disconnect()
        }
    }

    fun haveResponses(chatId: Long): Boolean {
        val checkQuery = CharForResponse.COUNT_RESPONSES_BY_CHAT_ID
        try {
            connect()
            connection.prepareStatement(checkQuery).use { stmt ->
                stmt.setLong(1, chatId)
                val resultSet = stmt.executeQuery()
                resultSet.next()
                return resultSet.getInt(1) > 0
            }
        } catch (e: SQLException) {
            logger.error("Responses check failed", e)
            return false
        } finally {
            disconnect()
        }
    }

    fun countResponsesByChatId(chatId: Long): Int {
        val countQuery = CharForResponse.COUNT_RESPONSES_BY_CHAT_ID
        try {
            connect()
            connection.prepareStatement(countQuery).use { stmt ->
                stmt.setLong(1, chatId)
                val resultSet = stmt.executeQuery()
                resultSet.next()
                return resultSet.getInt(1)
            }
        } catch (e: SQLException) {
            logger.error("Responses for user count failed", e)
            return 0
        } finally {
            disconnect()
        }
    }

    fun countAllResponses(): Int {
        val countQuery = CharForResponse.COUNT_ALL_RESPONSES
        try {
            connect()
            connection.prepareStatement(countQuery).use { stmt ->
                val resultSet = stmt.executeQuery()
                resultSet.next()
                return resultSet.getInt(1)
            }
        } catch (e: SQLException) {
            logger.error("Responses for admin count failed", e)
            return 0
        } finally {
            disconnect()
        }
    }

    // Получение откликов пользователя с пагинацией
    fun getUserResponses(chatId: Long, page: Int, pageSize: Int): List<ResponsePreview> {
        val offset = (page - 1) * pageSize // Расчёт количества пропускаемых строк перед началом выборки
        return try {
            connect()
            connection.prepareStatement(CharForResponse.SELECT_RESPONSES_BY_CHAT_ID + " LIMIT ? OFFSET ?").use { stmt ->
                stmt.setLong(1, chatId)
                stmt.setInt(2, pageSize)
                stmt.setInt(3, offset)
                val rs = stmt.executeQuery()
                val responses = mutableListOf<ResponsePreview>()
                while (rs.next()) {
                    responses.add(
                        ResponsePreview(
                            responseId = rs.getString(CharForResponse.COLUMN_RESPONSE_ID),
                            dateTime = rs.getString(CharForResponse.COLUMN_DATA_TIME)
                        )
                    )
                }
                responses
            }
        } catch (e: SQLException) {
            logger.error("Error getting user responses", e)
            emptyList()
        } finally {
            disconnect()
        }
    }

    fun getChatIdByResponseId(responseId: String): Long? {
        connect()
        return try {
            connection.prepareStatement(CharForResponse.GET_CHAT_ID_BY_RESPONSE_ID).use { stmt ->
                stmt.setString(1, responseId)
                val resultSet = stmt.executeQuery()
                resultSet.next()
                resultSet.getLong(CharForResponse.COLUMN_CHAT_ID)
            }
        } catch (e: SQLException) {
            logger.error("Error getting chat Id by respone Id", e)
            null
        } finally {
            disconnect()
        }
    }

    // Получение всех откликов (для администратора)
    fun getAllResponses(page: Int, pageSize: Int): List<ResponsePreview> {
        val offset = (page - 1) * pageSize // Расчёт количества пропускаемых строк перед началом выборки
        return try {
            connect()
            connection.prepareStatement(CharForResponse.SELECT_ALL_RESPONSES + " LIMIT ? OFFSET ?").use { stmt ->
                stmt.setInt(1, pageSize)
                stmt.setInt(2, offset)
                val rs = stmt.executeQuery()
                val responses = mutableListOf<ResponsePreview>()
                while (rs.next()) {
                    responses.add(
                        ResponsePreview(
                            responseId = rs.getString(CharForResponse.COLUMN_RESPONSE_ID),
                            dateTime = rs.getString(CharForResponse.COLUMN_DATA_TIME)
                        )
                    )
                }
                responses
            }
        } catch (e: SQLException) {
            logger.error("Ошибка получения всех откликов", e)
            emptyList()
        } finally {
            disconnect()
        }
    }

    // Получение деталей отклика
    fun getResponseDetails(responseId: String): ResponseDetails? {
        return try {
            connect()
            connection.prepareStatement(CharForResponse.SELECT_RESPONSE_BY_ID).use { stmt ->
                stmt.setString(1, responseId)
                val rs = stmt.executeQuery()
                if (rs.next()) {
                    ResponseDetails(
                        responseId = rs.getString(CharForResponse.COLUMN_RESPONSE_ID),
                        chatId = rs.getLong(CharForResponse.COLUMN_CHAT_ID),
                        question = rs.getString(CharForResponse.COLUMN_QUESTION),
                        username = rs.getString(CharForResponse.COLUMN_USERNAME),
                        phoneNumber = rs.getString(CharForResponse.COLUMN_PHONE_NUMBER),
                        category = rs.getString(CharForResponse.COLUMN_CATEGORY),
                        mostViewedCategory = rs.getString(CharForResponse.COLUMN_MOST_VIEWED_CATEGORY),
                        dateTime = rs.getString(CharForResponse.COLUMN_DATA_TIME)
                    )
                } else null
            }
        } catch (e: SQLException) {
            logger.error("Ошибка получения деталей отклика", e)
            null
        } finally {
            disconnect()
        }
    }

    // Вспомогательные классы
    data class ResponsePreview(
        val responseId: String,
        val dateTime: String
    )

    data class ResponseDetails(
        val responseId: String,
        val chatId: Long,
        val question: String?,
        val username: String?,
        val phoneNumber: String?,
        val category: String?,
        val mostViewedCategory: String?,
        val dateTime: String
    )
}