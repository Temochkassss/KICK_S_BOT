package ru.teomikki.dykoprod.command

import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import ru.teomikki.dykoprod.recieve.MessageReceived

@Component
class ContactDispatcher(
    @Lazy
    private val messageReceived: MessageReceived
) {
    fun handleContact(update: Update) {
        messageReceived.handleManagerRequestInput(update)
    }
}