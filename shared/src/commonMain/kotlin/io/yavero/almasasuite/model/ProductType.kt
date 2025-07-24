package io.yavero.almasasuite.model

import kotlinx.serialization.Serializable


@Serializable
enum class ProductType {
    RING, BRACELET, NECKLACE, EARRING, OTHER;

    companion object {
        fun fromString(value: String): ProductType {
            return when (value.lowercase()) {
                "ring" -> RING
                "bracelet" -> BRACELET
                "necklace" -> NECKLACE
                "earring" -> EARRING
                else -> OTHER
            }
        }

        fun toDbValue(type: ProductType): String {
            return when (type) {
                RING -> "ring"
                BRACELET -> "bracelet"
                NECKLACE -> "necklace"
                EARRING -> "earring"
                OTHER -> "other"
            }
        }
    }
}