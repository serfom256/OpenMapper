package io.github.serfom256.kotlininterop

import io.github.serfom256.annotations.entity.Field
import io.github.serfom256.annotations.entity.Model

/** Idiomatic Kotlin: immutable data class, val properties, no no-arg constructor. */
@Model(primaryKey = "id")
data class ProductData(
    @field:Field(name = "id") val id: Long,
    @field:Field(name = "name") val name: String,
    @field:Field(name = "price") val price: Double
)

/** Java-POJO shaped Kotlin: mutable vars with defaults, so a no-arg constructor exists. */
@Model(primaryKey = "id")
class ProductBean {
    @field:Field(name = "id")
    var id: Long = 0

    @field:Field(name = "name")
    var name: String? = null

    @field:Field(name = "price")
    var price: Double = 0.0
}
