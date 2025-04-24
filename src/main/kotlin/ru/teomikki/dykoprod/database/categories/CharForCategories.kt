package ru.teomikki.dykoprod.database.categories

object CharForCategories {
    const val TABLE_NAME = "categories"

    const val COLUMN_CATEGORY = "category"
    const val COLUMN_CUSTOM_NAME = "custom_name"
    const val COLUMN_COEFFICIENT= "coefficient"
    const val COLUMN_PROMO = "promo"

    const val DATABASE_NAME = "/data/CurrencyDataBase.db"

    const val CREATE_TABLE = """
        CREATE TABLE IF NOT EXISTS $TABLE_NAME (
            $COLUMN_CATEGORY TEXT,
            $COLUMN_COEFFICIENT INTEGER DEFAULT 666,
            $COLUMN_PROMO INTEGER,
            $COLUMN_CUSTOM_NAME TEXT DEFAULT $COLUMN_CATEGORY
        )
    """

    const val GET_CUSTOM_NAME = """
        SELECT $COLUMN_CUSTOM_NAME
        FROM $TABLE_NAME
        WHERE $COLUMN_CATEGORY = ?
        LIMIT 1
    """

    const val GET_ALL_CATEGORIES = """
        SELECT * 
        FROM $TABLE_NAME
    """

    const val GET_CATEGORY = """
        SELECT *
        FROM $TABLE_NAME
        WHERE $COLUMN_CATEGORY = ?
    """

    const val UPDATE_COEFFICIENT = """
        UPDATE $TABLE_NAME
        SET $COLUMN_COEFFICIENT = ?
        WHERE $COLUMN_CATEGORY = ?
    """

    const val UPDATE_PROMO = """
        UPDATE $TABLE_NAME
        SET $COLUMN_PROMO = ?
        WHERE $COLUMN_CATEGORY = ?
    """
}