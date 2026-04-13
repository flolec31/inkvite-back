package com.inkvite.inkviteback

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
    fromApplication<InkviteBackApplication>().with(TestcontainersConfiguration::class).run(*args)
}
