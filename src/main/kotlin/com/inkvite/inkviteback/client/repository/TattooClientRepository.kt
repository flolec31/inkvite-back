package com.inkvite.inkviteback.client.repository

import com.inkvite.inkviteback.client.entity.TattooClient
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface TattooClientRepository : JpaRepository<TattooClient, UUID> {
    fun findByEmail(email: String): TattooClient?
}
