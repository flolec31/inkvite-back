package com.stencilia.stenciliaback.client.service

import com.stencilia.stenciliaback.client.entity.TattooClient

fun interface TattooClientService {
    fun findOrCreate(email: String, firstName: String, lastName: String): TattooClient
}
