package com.inkvite.inkviteback

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class InkviteBackApplication

fun main(args: Array<String>) {
    runApplication<InkviteBackApplication>(*args)
}
