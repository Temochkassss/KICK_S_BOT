package ru.teomikki.dykoprod.database.categories

data class Category(
    val name: String,
    val customName: String = "Custom Name",
    val coefficient: Int,
    var promo: Int?,
)