package ru.teomikki.dykoprod.command.realization.unique

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import ru.teomikki.dykoprod.command.Command

@Component
class ReviewsCommand : Command {
    override fun name() = "/see_reviews"

    override fun execute(update: Update) {
        println("see_reviews")
    }

    override fun handleCallback(update: Update) {
        println("see_reviews")
    }
}