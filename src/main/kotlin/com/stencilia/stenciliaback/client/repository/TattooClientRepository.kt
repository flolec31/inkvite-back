package com.stencilia.stenciliaback.client.repository

import com.stencilia.stenciliaback.client.entity.TattooClient
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface TattooClientRepository : JpaRepository<TattooClient, UUID> {
    fun findByEmail(email: String): TattooClient?
}
