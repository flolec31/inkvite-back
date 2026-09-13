package com.inkvite.inkviteback.auth.client.repository

import com.inkvite.inkviteback.auth.client.entity.ClientAccessCode
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ClientAccessCodeRepository : JpaRepository<ClientAccessCode, UUID>
