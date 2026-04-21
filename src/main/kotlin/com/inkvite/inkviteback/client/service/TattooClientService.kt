package com.inkvite.inkviteback.client.service

import com.inkvite.inkviteback.client.entity.TattooClient

fun interface TattooClientService {
    fun findOrCreate(email: String, firstName: String, lastName: String): TattooClient
}
