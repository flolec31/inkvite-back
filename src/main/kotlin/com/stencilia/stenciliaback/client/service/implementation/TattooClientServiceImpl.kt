package com.stencilia.stenciliaback.client.service.implementation

import com.stencilia.stenciliaback.client.entity.TattooClient
import com.stencilia.stenciliaback.client.repository.TattooClientRepository
import com.stencilia.stenciliaback.client.service.TattooClientService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class TattooClientServiceImpl(
    private val repository: TattooClientRepository
) : TattooClientService {

    @Transactional
    override fun findOrCreate(
        email: String,
        firstName: String,
        lastName: String
    ): TattooClient =
        repository.findByEmail(email)
            ?: repository.save(TattooClient(email = email, firstName = firstName, lastName = lastName))
}
