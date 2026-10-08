package com.pikacheat.mygandara.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ContactCategory(val label: String) {
    @SerialName("emergency") EMERGENCY("Emergency"),
    @SerialName("fire") FIRE("Fire"),
    @SerialName("police") POLICE("Police"),
    @SerialName("medical") MEDICAL("Hospital / medical"),
    @SerialName("disaster") DISASTER("Disaster response"),
    @SerialName("utility") UTILITY("Water / power"),
    @SerialName("other") OTHER("Other")
}

/** A row of the `emergency_contacts` table. */
@Serializable
data class EmergencyContact(
    val id: String,
    val name: String,
    val category: ContactCategory = ContactCategory.OTHER,
    val phone: String,
    val note: String? = null,
    @SerialName("sort_order") val sortOrder: Int = 100
)

/** Columns an admin may set (insert or update). */
@Serializable
data class EmergencyContactInput(
    val name: String,
    val category: ContactCategory,
    val phone: String,
    val note: String?,
    @SerialName("sort_order") val sortOrder: Int
)
