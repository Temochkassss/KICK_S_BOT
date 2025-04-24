package ru.teomikki.dykoprod.database.admin

object CharForAdmin {
    const val TABLE_NAME = "admin"

    const val COLUMN_ID = "id"
    const val COLUMN_CHAT_ID = "chat_id"
    const val COLUMN_USERNAME = "username"
    const val COLUMN_PASSWORD = "password"

    const val DATABASE_NAME = "/data/ClientsDataBase.db"

    const val CREATE_TABLE = """
        CREATE TABLE IF NOT EXISTS $TABLE_NAME (
            $COLUMN_ID INTEGER PRIMARY KEY,
            $COLUMN_CHAT_ID LONG UNIQUE NOT NULL,
            $COLUMN_USERNAME TEXT,
            $COLUMN_PASSWORD TEXT
        )
    """

    const val CHECK_EXISTENCE_QUERY = """
        SELECT COUNT(*)
        FROM $TABLE_NAME
        WHERE $COLUMN_CHAT_ID = ?
    """

    const val SELECT_ADMIN_CHAT_ID_QUERY = """
        SELECT $COLUMN_CHAT_ID 
        FROM $TABLE_NAME 
        LIMIT 1
    """

    const val SELECT_ADMIN_USERNAME_QUERY = """
        SELECT $COLUMN_USERNAME
        FROM $TABLE_NAME
        WHERE $COLUMN_CHAT_ID = ?
    """

    const val UPDATE_ADMIN = """
        UPDATE $TABLE_NAME
        SET 
            $COLUMN_CHAT_ID = ?,
            $COLUMN_USERNAME = ?,
            $COLUMN_PASSWORD = ?
        WHERE $COLUMN_CHAT_ID = ?
    """

    const val GET_PASSWORD = """
        SELECT $COLUMN_PASSWORD
        FROM $TABLE_NAME
        WHERE $COLUMN_CHAT_ID = ?
    """
}