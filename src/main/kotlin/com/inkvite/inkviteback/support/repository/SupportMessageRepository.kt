package com.inkvite.inkviteback.support.repository

import com.inkvite.inkviteback.support.entity.SupportMessage
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface SupportMessageRepository : JpaRepository<SupportMessage, UUID>
