package ru.teomikki.dykoprod.command

import org.telegram.telegrambots.meta.api.objects.Update

interface Command {
    fun name(): String
    fun execute(update: Update)
    fun handleCallback(update: Update)
}