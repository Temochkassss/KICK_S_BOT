package ru.teomikki.dykoprod.database.admin

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import ru.teomikki.dykoprod.database.clients.CharForClients
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

@Repository
class AdminHelper {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private lateinit var connection: Connection

    private fun connect(){
        try {
            val url = "jdbc:sqlite:${CharForAdmin.DATABASE_NAME}"
            connection = DriverManager.getConnection(url)
            logger.debug("Database connection established to {}", CharForAdmin.DATABASE_NAME)
        } catch (e: SQLException) {
            logger.error("Connection failed to {}: {}", CharForAdmin.DATABASE_NAME, e.message, e)
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

    fun createAdminTable() {
        val createTableQuery = CharForAdmin.CREATE_TABLE
        try {
            connect()
            connection.createStatement().execute(createTableQuery)
            logger.info("Table {} created successfully", CharForAdmin.TABLE_NAME)
        } catch (e: SQLException) {
            logger.error("Table creation failed: {}", e.message, e)
            throw e
        } finally {
            disconnect()
        }
    }

    fun isUserAdmin(chatId: Long): Boolean {
        logger.info("Attempting to open admin page [chatId: {}]", chatId)
        connect()
        try {
            // Проверяем права пользователя
            val checkExistenceQuery = CharForAdmin.CHECK_EXISTENCE_QUERY
            val checkExistenceStatement = connection.prepareStatement(checkExistenceQuery)
            checkExistenceStatement.setLong(1, chatId)

            val checkExistenceResultSet = checkExistenceStatement.executeQuery()
            checkExistenceResultSet.next()
            val exists = checkExistenceResultSet.getInt(1) > 0

            logger.info("User is admin: {}", exists)

            return exists
        } catch (e: SQLException) {
            throw e
        } finally {
            disconnect()
        }
    }

    fun getAdminChatId(): Long? {
        connect()
        return try {
            val query = CharForAdmin.SELECT_ADMIN_CHAT_ID_QUERY
            val statement = connection.prepareStatement(query)
            val resultSet = statement.executeQuery()

            if (resultSet.next()) {
                val chatId = resultSet.getLong(CharForAdmin.COLUMN_CHAT_ID)
                logger.info("Admin chatId found: {}", chatId)
                chatId
            } else {
                logger.warn("No admin found in the database")
                null
            }
        } catch (e: SQLException) {
            logger.error("Failed to fetch admin chatId: {}", e.message, e)
            null
        } finally {
            disconnect()
        }
    }

    fun getAdminUsername(chatId: Long): String? {
        connect()
        return try {
            val query = CharForAdmin.SELECT_ADMIN_USERNAME_QUERY
            connection.prepareStatement(query).use { stmt ->
                stmt.setLong(1, chatId)
                val resultSet = stmt.executeQuery()
                resultSet.next()
                resultSet.getString(1)
            }
        } catch (e: SQLException) {
            logger.error("No admin found in the database by chatId {}", chatId)
            null
        } finally {
            disconnect()
        }
    }

//    fun verifyPassword(chatId: Long, password: String): Boolean {
//        connect()
//        return try {
//            val query = CharForAdmin.GET_PASSWORD
//            connection.prepareStatement(query).use { stmt ->
//                stmt.setLong(1, chatId)
//                val rs = stmt.executeQuery()
//                if (rs.next()) {
//                    val storedHash = rs.getString(1)
//                    println("Stored hash: $storedHash") // Логируем хэш из базы
//                    println("Input password: $password") // Логируем введённый пароль
//                    BCrypt.verify(password, storedHash).also {
//                        println("Verification result: $it")
//                    }
//                } else {
//                    false
//                }
//            }
//        } catch (e: SQLException) {
//            logger.error("Password verification failed", e)
//            false
//        } finally {
//            disconnect()
//        }
//    }
//
//    fun updateAdmin(
//        oldChatId: Long,
//        newChatId: Long,
//        newUsername: String?,
//        newPassword: String,
//        currentPassword: String
//    ): Boolean {
//        if (!verifyPassword(oldChatId, currentPassword)) return false
//
//        val hashedPassword = BCrypt.hash(newPassword) // Хэшируем новый пароль
//        logger.info("New password: {} Hashed new password: {}", newPassword, hashedPassword)
//
//        connect()
//        return try {
//            connection.prepareStatement(CharForAdmin.UPDATE_ADMIN).use { stmt ->
//                stmt.setLong(1, newChatId)
//                stmt.setString(2, newUsername)
//                stmt.setString(3, hashedPassword)
//                stmt.setLong(4, oldChatId)
//                stmt.executeUpdate() > 0
//            }
//        } catch (e: SQLException) {
//            logger.error("Admin update failed: ${e.message}")
//            false
//        } finally {
//            disconnect()
//        }
//    }
}