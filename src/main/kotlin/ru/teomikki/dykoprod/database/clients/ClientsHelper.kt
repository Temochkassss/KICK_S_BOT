package ru.teomikki.dykoprod.database.clients


import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

@Repository
class ClientsHelper() {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private lateinit var connection: Connection

    private fun connect(){
        try {
            val url = "jdbc:sqlite:${CharForClients.DATABASE_NAME}"
            connection = DriverManager.getConnection(url)
            logger.debug("Database connection established to {}", CharForClients.DATABASE_NAME)
        } catch (e: SQLException) {
            logger.error("Connection failed to {}: {}", CharForClients.DATABASE_NAME, e.message, e)
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
        val createTableQuery = CharForClients.CREATE_TABLE
        try {
            connect()
            connection.createStatement().execute(createTableQuery)
            logger.info("Table {} created successfully", CharForClients.TABLE_NAME)
        } catch (e: SQLException) {
            logger.error("Table creation failed: {}", e.message, e)
            throw e
        } finally {
            disconnect()
        }
    }

    fun registerOrUpdateClient(
        chatId: Long,
        username: String?,
        phoneNumber: String? = null,
        category: String? = null
    ) {
        logger.info("Attempting to save/update client [chatId: {}]", chatId)
        connect()
        try {
            // Получаем текущие данные пользователя (если есть)
            val existingUser = getUser(chatId)

            connect()

            // Определяем новые значения для полей
            val newPhone = phoneNumber ?: existingUser?.phoneNumber ?: "не указан"
            val newCategory = category ?: existingUser?.category ?: "новый пользователь"

            // Проверяем существование пользователя
            val checkExistenceQuery = CharForClients.CHECK_EXISTENCE_QUERY
            val checkExistenceStatement = connection.prepareStatement(checkExistenceQuery)
            checkExistenceStatement.setLong(1, chatId)

            val checkExistenceResultSet = checkExistenceStatement.executeQuery()
            checkExistenceResultSet.next()
            val exists = checkExistenceResultSet.getInt(1) > 0
            logger.info("User exist: {}", exists)

            // Вставляем или обновляем
            val updateOrInsertQuery = if (exists) CharForClients.UPDATE_USER else CharForClients.INSERT_USER
            val updateOrInsertStatement = connection.prepareStatement(updateOrInsertQuery)
            logger.info("Executing {} query", if (exists) "UPDATE" else "INSERT")

            if (exists) {
                updateOrInsertStatement.setString(1, username)
                updateOrInsertStatement.setString(2, newPhone)
                updateOrInsertStatement.setString(3, newCategory)
                updateOrInsertStatement.setLong(4, chatId)
            } else {
                updateOrInsertStatement.setLong(1, chatId)
                updateOrInsertStatement.setString(2, username)
                updateOrInsertStatement.setString(3, phoneNumber)
                updateOrInsertStatement.setString(4, category)
            }
            val affectedRows = updateOrInsertStatement.executeUpdate()
            logger.info("Operation completed successfully. Affected rows: {}", affectedRows)

        } catch (e: SQLException) {
            logger.error("Database operation failed for chatId {}: {}", chatId, e.message, e)
            throw e
        } finally {
            disconnect()
        }
    }

    fun getUser(chatId: Long): Client? {
        logger.debug("Fetching client data for chatId: {}", chatId)
        connect()
        return try {
            val getQuery = CharForClients.GET_USER
            val getStatement = connection.prepareStatement(getQuery)
            getStatement.setLong(1, chatId)

            val getUserResultSet = getStatement.executeQuery()
            if (getUserResultSet.next()) {
                logger.trace("Client record found for chatId: {}", chatId)
                Client(
                    chatId = getUserResultSet.getLong(CharForClients.COLUMN_CHAT_ID),
                    username = getUserResultSet.getString(CharForClients.COLUMN_USERNAME),
                    phoneNumber = getUserResultSet.getString(CharForClients.COLUMN_PHONE_NUMBER),
                    category = getUserResultSet.getString(CharForClients.COLUMN_MOST_VIEWED_CATEGORY)
                )
            } else {
                logger.debug("No client found for chatId: {}", chatId)
                null
            }
        } catch (e: SQLException) {
            logger.error("Query failed for chatId {}: {}", chatId, e.message, e)
            throw e
        } finally {
            disconnect()
        }
    }

    fun updatePhoneNumber(chatId: Long, phoneNumber: String?) {
        logger.info("Attempting to update phone number [chatId: {}]", chatId)
        connect()
        try {
            val updateQuery = CharForClients.UPDATE_PHONE_NUMBER
            connection.prepareStatement(updateQuery). use { stmt ->
                stmt.setString(1, phoneNumber)
                stmt.setLong(2, chatId)
                stmt.executeUpdate()
            }
        } catch (e: SQLException) {
            logger.error("Database operation failed for chatId {}: {}", chatId, e.message, e)
            throw e
        } finally {
            disconnect()
        }
    }

    fun updateMostViewedCategory(chatId: Long, category: String) {
        logger.info("Updating most viewed category [chatId: {}, category: {}]", chatId, category)
        connect()
        try {
            val updateQuery = CharForClients.UPDATE_MOST_VIEWED_CATEGORY
            connection.prepareStatement(updateQuery).use { stmt ->
                stmt.setString(1, category)
                stmt.setLong(2, chatId)
                stmt.executeUpdate()
            }
        } catch (e: SQLException) {
            logger.error("Error updating most viewed category", e)
            throw e
        } finally {
            disconnect()
        }
    }
}

