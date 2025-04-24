package ru.teomikki.dykoprod.database.currency

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Repository
class CurrencyHelper {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    private lateinit var connection: Connection

    private fun connect(){
        try {
            val url = "jdbc:sqlite:${CharForCurrency.DATABASE_NAME}"
            connection = DriverManager.getConnection(url)
            logger.debug("Database connection established to {}", CharForCurrency.DATABASE_NAME)
        } catch (e: SQLException) {
            logger.error("Connection failed to {}: {}", CharForCurrency.DATABASE_NAME, e.message, e)
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

    fun createCurrencyTable() {
        val createTableQuery = CharForCurrency.CREATE_TABLE
        try {
            connect()
            connection.createStatement().execute(createTableQuery)
            logger.info("Table {} created successfully", CharForCurrency.TABLE_NAME)
        } catch (e: SQLException) {
            logger.error("Table creation failed: {}", e.message, e)
            throw e
        } finally {
            disconnect()
        }
    }

    fun saveCurrencyCourse(currencyCode: String, officialCourse: Double) {
        val currentTime = LocalDateTime.now().format(timeFormatter)

        connect()

        try {
            val fixCoefficient = getFixCoefficient(currencyCode)
            val fixedCourse = officialCourse + fixCoefficient

            connect()

            // Проверяем существование записи
            val checkQuery = CharForCurrency.CHECK_COURSE
            val checkStmt = connection.prepareStatement(checkQuery).apply {
                setString(1, currencyCode)
            }
            val resultSet = checkStmt.executeQuery()
            val exists = resultSet.next() && resultSet.getInt(1) > 0

            // SQL-запрос для обновления/вставки
            val query = if (exists) {
                CharForCurrency.UPDATE_COURSE
            } else {
                CharForCurrency.INSERT_COURSE
            }

            // Выполняем операцию
            connection.prepareStatement(query).use { stmt ->
                if (exists) {
                    stmt.setDouble(1, officialCourse)
                    stmt.setDouble(2, fixedCourse)
                    stmt.setString(3, currentTime)
                    stmt.setString(4, currencyCode)
                } else {
                    stmt.setString(1, currencyCode)
                    stmt.setDouble(2, officialCourse)
                    stmt.setDouble(3, fixCoefficient) // Сохраняем коэффициент
                    stmt.setDouble(4, fixedCourse)
                    stmt.setString(5, currentTime)
                }
                stmt.executeUpdate()
            }

            logger.info("Course {} : {} successful {}", currencyCode, officialCourse, if (exists) "UPDATED" else "INSERT")

        } catch (e: SQLException) {
            logger.error("Error save course: {}", e.message, e)
            throw e
        } finally {
            disconnect()
        }
    }

    fun getFixCoefficient(currencyCode: String): Double {
        connect()
        return try {
            val query = CharForCurrency.GET_FIX_COEFFICIENT
            connection.prepareStatement(query).use { stmt ->
                stmt.setString(1, currencyCode)
                val rs = stmt.executeQuery()
                rs.getDouble(1)
            }
        } catch (e: SQLException) {
            logger.error("Error getting coefficient for $currencyCode", e)
            0.0
        } finally {
            disconnect()
        }
    }


    // Дополнительный метод для получения последнего курса CNY
    fun getFixedCourse(currencyCode: String): Double? {
        connect()
        return try {
            val query = CharForCurrency.GET_FIXED_COURSE
            connection.prepareStatement(query).use { stmt ->
                stmt.setString(1, currencyCode)
                val resultSet = stmt.executeQuery()
                if (resultSet.next()) {
                    resultSet.getDouble(1)
                } else {
                    null
                }
            }
        } catch (e: SQLException) {
            logger.error("Error get course: {}", e.message, e)
            null
        } finally {
            disconnect()
        }
    }

    fun updateFixCoefficient(currencyCode: String, coeff: Double) {
        connect()
        try {
            val currentTime = LocalDateTime.now().format(timeFormatter)
            connection.prepareStatement(CharForCurrency.UPDATE_FIX_COEFFICIENT).use { stmt ->
                stmt.setDouble(1, coeff)  // COLUMN_FIX_COEFFICIENT
                stmt.setDouble(2, coeff)  // Для расчета COLUMN_FIXED_COURSE
                stmt.setString(3, currentTime)
                stmt.setString(4, currencyCode)
                stmt.executeUpdate()
            }
        } catch (e: SQLException) {
            logger.error("Error updating coefficient", e)
        } finally {
            disconnect()
        }
    }
}