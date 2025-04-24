package ru.teomikki.dykoprod.database.categories

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import ru.teomikki.dykoprod.command.realization.callback_only.handler.CalculateCategoryHandler
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

@Repository
class CategoriesHelper(
    private val categoryHandler: CalculateCategoryHandler
) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private lateinit var connection: Connection

    private fun connect(){
        try {
            val url = "jdbc:sqlite:${CharForCategories.DATABASE_NAME}"
            connection = DriverManager.getConnection(url)
            logger.debug("Database connection established to {}", CharForCategories.DATABASE_NAME)
        } catch (e: SQLException) {
            logger.error("Connection failed to {}: {}", CharForCategories.DATABASE_NAME, e.message, e)
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
        val createTableQuery = CharForCategories.CREATE_TABLE
        try {
            connect()
            connection.createStatement().execute(createTableQuery)
            logger.info("Table {} created successfully", CharForCategories.TABLE_NAME)
        } catch (e: SQLException) {
            logger.error("Table creation failed: {}", e.message, e)
            throw e
        } finally {
            disconnect()
        }
    }

    fun getCustomName(name: String): String {
        try {
            connect()
            val query = CharForCategories.GET_CUSTOM_NAME
            connection.prepareStatement(query).use { stmt ->
                stmt.setString(1, name)
                val resultSet = stmt.executeQuery()
                if (resultSet.next()) {
                    return resultSet.getString(CharForCategories.COLUMN_CUSTOM_NAME)
                } else {
                    return "Категория не найдена ⚠️"
                }
            }
        } catch (e: SQLException) {
            logger.error("Error get custom name for {}: {}", name, e.message, e)
            return "Категория не найдена ⚠️"
        } finally {
            disconnect()
        }
    }

    fun getAllCategories(): List<Category> {
        try {
            connect()
            val categories = mutableListOf<Category>()
            val query = CharForCategories.GET_ALL_CATEGORIES
            connection.prepareStatement(query).use { stmt ->
                val result = stmt.executeQuery()
                while (result.next()) {
                    categories.add(
                        Category(
                            name = result.getString(CharForCategories.COLUMN_CATEGORY),
                            customName = result.getString(CharForCategories.COLUMN_CUSTOM_NAME),
                            coefficient = result.getInt(CharForCategories.COLUMN_COEFFICIENT),
                            promo = result.getInt(CharForCategories.COLUMN_PROMO),
                        )
                    )
                }
            }
            return categories
        } catch (e: SQLException) {
            logger.error("Error fetching categories: {}", e.message, e)
            return emptyList()
        } finally {
            disconnect()
        }
    }

    fun getCategory(name: String): Category? {
        try {
            connect()
            val query = CharForCategories.GET_CATEGORY
            connection.prepareStatement(query).use { stmt ->
                stmt.setString(1, name)
                val resultSet = stmt.executeQuery()
                if (resultSet.next()) {
                    return Category(
                        name = resultSet.getString(CharForCategories.COLUMN_CATEGORY),
                        customName = resultSet.getString(CharForCategories.COLUMN_CUSTOM_NAME),
                        coefficient = resultSet.getInt(CharForCategories.COLUMN_COEFFICIENT),
                        promo = resultSet.getInt(CharForCategories.COLUMN_PROMO),
                    )
                } else {
                    return null
                }
            }
        } catch (e: SQLException) {
            logger.error("Error get category {}: {}", name, e.message, e)
            return null
        } finally {
            disconnect()
        }
    }

    fun updateCoefficient(category: String, newCoefficient: Int): Boolean {
        try {
            connect()
            val query = CharForCategories.UPDATE_COEFFICIENT
            connection.prepareStatement(query).use { stmt ->
                stmt.setInt(1, newCoefficient)
                stmt.setString(2, category)
                return stmt.executeUpdate() > 0
            }
        } catch (e: SQLException) {
            logger.error("Error update coefficient for category {}: {}", category, e.message, e)
            return false
        } finally {
            disconnect()
        }
    }

    fun updatePromo(category: String, promo: Int): Boolean {
        try {
            connect()
            val query = CharForCategories.UPDATE_PROMO
            connection.prepareStatement(query).use { stmt ->
                stmt.setInt(1, promo)
                stmt.setString(2, category)
                return stmt.executeUpdate() > 0
            }
        } catch (e: SQLException) {
            logger.error("Error update promo for category {}: {}", category, e.message, e)
            return false
        } finally {
            disconnect()
        }
    }
}