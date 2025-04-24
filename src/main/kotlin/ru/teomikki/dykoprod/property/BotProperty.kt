package ru.teomikki.dykoprod.property

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "bot")
data class BotProperty(
    var username: String = "",
    var token: String = ""
)
