package ru.teomikki.dykoprod.database.response

object CharForResponse {
    const val TABLE_NAME = "response"

    const val COLUMN_ID = "id"
    const val COLUMN_RESPONSE_ID = "response_id"
    const val COLUMN_CHAT_ID = "chat_id"
    const val COLUMN_QUESTION = "question"
    const val COLUMN_USERNAME = "username"
    const val COLUMN_PHONE_NUMBER = "phone_number"
    const val COLUMN_CATEGORY = "category"
    const val COLUMN_MOST_VIEWED_CATEGORY = "most_viewed_category"
    const val COLUMN_DATA_TIME = "data_time"

    const val DATABASE_NAME = "/data/ClientsDataBase.db"

    const val CREATE_TABLE = """
        CREATE TABLE IF NOT EXISTS $TABLE_NAME (
            $COLUMN_ID INTEGER PRIMARY KEY,
            $COLUMN_RESPONSE_ID TEXT UNIQUE NOT NULL,
            $COLUMN_CHAT_ID LONG NOT NULL,
            $COLUMN_QUESTION TEXT,
            $COLUMN_USERNAME TEXT DEFAULT "аноним",
            $COLUMN_PHONE_NUMBER TEXT DEFAULT "не указан",
            $COLUMN_CATEGORY TEXT DEFAULT "не выбрана",
            $COLUMN_MOST_VIEWED_CATEGORY TEXT DEFAULT "новый пользователь",
            $COLUMN_DATA_TIME TEXT NOT NULL
        )
    """

    const val INSERT_RESPONSE = """
        INSERT INTO $TABLE_NAME 
        (
        $COLUMN_RESPONSE_ID, 
        $COLUMN_CHAT_ID, 
        $COLUMN_QUESTION, 
        $COLUMN_USERNAME, 
        $COLUMN_PHONE_NUMBER, 
        $COLUMN_CATEGORY, 
        $COLUMN_MOST_VIEWED_CATEGORY, 
        $COLUMN_DATA_TIME
        )
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
    """

    const val CHECK_RESPONSE_ID_EXISTENCE = """
        SELECT COUNT(*) 
        FROM $TABLE_NAME 
        WHERE $COLUMN_RESPONSE_ID = ?
    """

    // Запросы для пользователя
    const val SELECT_RESPONSES_BY_CHAT_ID = """
        SELECT $COLUMN_RESPONSE_ID, $COLUMN_DATA_TIME 
        FROM $TABLE_NAME 
        WHERE $COLUMN_CHAT_ID = ? 
        ORDER BY $COLUMN_DATA_TIME DESC
    """

    const val COUNT_RESPONSES_BY_CHAT_ID = """
        SELECT COUNT(*) 
        FROM $TABLE_NAME 
        WHERE $COLUMN_CHAT_ID = ?
    """

    const val GET_CHAT_ID_BY_RESPONSE_ID = """
        SELECT $COLUMN_CHAT_ID
        FROM $TABLE_NAME
        WHERE $COLUMN_RESPONSE_ID = ?
        LIMIT 1
    """

    // Запросы для администратора
    const val SELECT_ALL_RESPONSES = """
        SELECT $COLUMN_RESPONSE_ID, 
               $COLUMN_DATA_TIME 
        FROM $TABLE_NAME 
        ORDER BY $COLUMN_DATA_TIME DESC
    """

    const val COUNT_ALL_RESPONSES = """
        SELECT COUNT(*) 
        FROM $TABLE_NAME
    """

    // Запрос для деталей отклика
    const val SELECT_RESPONSE_BY_ID = """
        SELECT * 
        FROM $TABLE_NAME 
        WHERE $COLUMN_RESPONSE_ID = ?
    """
}