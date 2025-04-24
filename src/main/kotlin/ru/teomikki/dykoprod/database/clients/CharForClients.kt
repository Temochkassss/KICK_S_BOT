package ru.teomikki.dykoprod.database.clients

object CharForClients {
    const val TABLE_NAME = "clients"

    const val COLUMN_ID = "id"
    const val COLUMN_CHAT_ID = "chat_id"
    const val COLUMN_USERNAME = "username"
    const val COLUMN_PHONE_NUMBER = "phone_number"
    const val COLUMN_MOST_VIEWED_CATEGORY = "most_viewed_category"

    const val DATABASE_NAME = "/data/ClientsDataBase.db"

    const val CREATE_TABLE = """
        CREATE TABLE IF NOT EXISTS $TABLE_NAME (
            $COLUMN_ID INTEGER PRIMARY KEY,
            $COLUMN_CHAT_ID LONG UNIQUE NOT NULL,
            $COLUMN_USERNAME TEXT,
            $COLUMN_PHONE_NUMBER TEXT,
            $COLUMN_MOST_VIEWED_CATEGORY TEXT
        )
    """

    const val CHECK_EXISTENCE_QUERY = """
        SELECT COUNT(*)
        FROM $TABLE_NAME
        WHERE $COLUMN_CHAT_ID = ?
    """

    const val INSERT_USER = """
        INSERT INTO $TABLE_NAME 
        ($COLUMN_CHAT_ID, 
        $COLUMN_USERNAME, 
        $COLUMN_PHONE_NUMBER, 
        $COLUMN_MOST_VIEWED_CATEGORY)
        VALUES (?, ?, ?, ?)
    """

    const val UPDATE_USER = """
        UPDATE $TABLE_NAME SET
        $COLUMN_USERNAME = ?,
        $COLUMN_PHONE_NUMBER = ?,
        $COLUMN_MOST_VIEWED_CATEGORY = ?
        WHERE $COLUMN_CHAT_ID = ?
    """

    const val GET_USER = """
        SELECT *
        FROM $TABLE_NAME
        WHERE $COLUMN_CHAT_ID = ?
    """

    const val UPDATE_PHONE_NUMBER = """
        UPDATE $TABLE_NAME SET
        $COLUMN_PHONE_NUMBER = ?
        WHERE $COLUMN_CHAT_ID = ?
    """

    const val UPDATE_MOST_VIEWED_CATEGORY = """
        UPDATE $TABLE_NAME 
        SET $COLUMN_MOST_VIEWED_CATEGORY = ?
        WHERE $COLUMN_CHAT_ID = ?
    """
}