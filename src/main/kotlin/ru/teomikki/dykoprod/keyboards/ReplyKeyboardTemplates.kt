package ru.teomikki.dykoprod.keyboards

import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow

@Service
class ReplyKeyboardTemplates {
    fun getUserContactRK() = ReplyKeyboardMarkup().apply {
        keyboard = listOf(
            KeyboardRow().apply {
                add(KeyboardButton("📱 Поделиться номером").apply {
                    requestContact = true
                })
            },
        )
        resizeKeyboard = true
        oneTimeKeyboard = true
    }
}