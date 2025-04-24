package ru.teomikki.dykoprod.command.realization.unique

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import ru.teomikki.dykoprod.command.Command
import ru.teomikki.dykoprod.sender.MessageSender
import ru.teomikki.dykoprod.state.UserStateService

@Component
class HelpCommand(
    private val messageSender: MessageSender,
    private val userStateService: UserStateService
) : Command {
    override fun name() = "/help"

    override fun execute(update: Update) {

    }

    override fun handleCallback(update: Update) {
        println("help")
    }
}