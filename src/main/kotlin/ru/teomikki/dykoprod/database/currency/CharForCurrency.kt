package ru.teomikki.dykoprod.database.currency

object CharForCurrency {
    const val TABLE_NAME = "currency"

    const val COLUMN_CURRENCY_CODE = "currency_code"
    const val COLUMN_OFFICIAL_COURSE = "official_course"
    const val COLUMN_FIX_COEFFICIENT = "fix_coefficient"
    const val COLUMN_FIXED_COURSE = "fixed_course"
    const val COLUMN_FIXED_BY_ADMIN = "fixed_by_admin"
    const val COLUMN_DATA_TIME = "data_time"

    const val DATABASE_NAME = "/data/CurrencyDataBase.db"

    const val CREATE_TABLE = """
        CREATE TABLE IF NOT EXISTS $TABLE_NAME (
            $COLUMN_CURRENCY_CODE TEXT PRIMARY KEY,
            $COLUMN_OFFICIAL_COURSE DOUBLE NOT NULL,
            $COLUMN_FIX_COEFFICIENT DOUBLE DEFAULT 1.0,
            $COLUMN_FIXED_COURSE DOUBLE,
            $COLUMN_FIXED_BY_ADMIN TEXT,
            $COLUMN_DATA_TIME TEXT NOT NULL
        )
    """

    const val GET_FIX_COEFFICIENT = """
        SELECT $COLUMN_FIX_COEFFICIENT 
        FROM $TABLE_NAME 
        WHERE $COLUMN_CURRENCY_CODE = ?
    """

    const val CHECK_COURSE = """
        SELECT COUNT(*) 
        FROM $TABLE_NAME 
        WHERE $COLUMN_CURRENCY_CODE = ?
    """

    const val UPDATE_COURSE = """
        UPDATE $TABLE_NAME 
        SET $COLUMN_OFFICIAL_COURSE = ?,
            $COLUMN_FIXED_COURSE = ?,
            $COLUMN_DATA_TIME = ?
        WHERE $COLUMN_CURRENCY_CODE = ?
    """

    const val INSERT_COURSE = """
        INSERT INTO $TABLE_NAME (
            $COLUMN_CURRENCY_CODE,
            $COLUMN_OFFICIAL_COURSE,
            $COLUMN_FIX_COEFFICIENT,
            $COLUMN_FIXED_COURSE,
            $COLUMN_DATA_TIME
        ) VALUES (?, ?, ?, ?, ?)
    """

    const val GET_FIXED_COURSE = """
        SELECT $COLUMN_FIXED_COURSE 
        FROM $TABLE_NAME 
        WHERE $COLUMN_CURRENCY_CODE = ? 
        ORDER BY $COLUMN_DATA_TIME DESC 
        LIMIT 1
    """

    const val UPDATE_FIX_COEFFICIENT = """
    UPDATE $TABLE_NAME 
    SET $COLUMN_FIX_COEFFICIENT = ?,
        $COLUMN_FIXED_COURSE = $COLUMN_OFFICIAL_COURSE + ?,
        $COLUMN_DATA_TIME = ?
    WHERE $COLUMN_CURRENCY_CODE = ?
"""
}