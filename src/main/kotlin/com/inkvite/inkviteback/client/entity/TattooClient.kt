package com.inkvite.inkviteback.client.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "tattoo_client")
class TattooClient(
    @Id var id: UUID = UUID.randomUUID(),
    var email: String,
    var firstName: String,
    var lastName: String,
) {
    fun getFullName() = "${firstName.replaceFirstChar(Char::titlecase)} ${lastName.replaceFirstChar(Char::titlecase)}"
}
