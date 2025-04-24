package ru.teomikki.dykoprod

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling


@SpringBootApplication
@EnableScheduling
class TelegramBotApplication

fun main(args: Array<String>) {
	runApplication<TelegramBotApplication>(*args)
}