package com.stencilia.stenciliaback.support.repository

import com.stencilia.stenciliaback.support.entity.SupportMessage
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface SupportMessageRepository : JpaRepository<SupportMessage, UUID>
