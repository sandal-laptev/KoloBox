package ru.mokolomyagi.kolobox.data

data class MediaItemData(
    val url: String,
    val type: MediaType
)

enum class MediaType {
    IMAGE, VIDEO
}