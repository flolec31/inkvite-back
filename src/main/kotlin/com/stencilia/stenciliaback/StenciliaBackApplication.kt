package com.stencilia.stenciliaback

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class StenciliaBackApplication

fun main(args: Array<String>) {
    runApplication<StenciliaBackApplication>(*args)
}
