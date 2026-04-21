package com.inkvite.inkviteback.appointment.repository

import com.inkvite.inkviteback.appointment.entity.Reference
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ReferenceRepository : JpaRepository<Reference, UUID>
