package com.example.myapplication.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SaavnSearchResponse(
    val total: Int = 0,
    val start: Int = 0,
    val results: List<SaavnTrack> = emptyList(),
)

@Serializable
data class SaavnTrack(
    val id: String = "",
    val title: String = "",
    val subtitle: String = "",
    val image: String = "",
    @SerialName("more_info") val moreInfo: SaavnMoreInfo = SaavnMoreInfo(),
) {
    val highResImage: String
        get() = image.replace("150x150", "500x500")

    val artistName: String
        get() {
            val artists = moreInfo.artistMap?.primaryArtists
            return if (!artists.isNullOrEmpty()) {
                artists.joinToString(", ") { it.name }
            } else {
                subtitle.substringBefore(" - ")
            }
        }
}

@Serializable
data class SaavnMoreInfo(
    val album: String = "",
    @SerialName("album_id") val albumId: String = "",
    val duration: String = "",
    @SerialName("encrypted_media_url") val encryptedMediaUrl: String = "",
    @SerialName("320kbps") val is320kbps: String = "",
    @SerialName("has_lyrics") val hasLyrics: String = "",
    val language: String = "",
    @SerialName("artistMap") val artistMap: SaavnArtistMap? = null,
)

@Serializable
data class SaavnArtistMap(
    @SerialName("primary_artists") val primaryArtists: List<SaavnArtist> = emptyList(),
    val artists: List<SaavnArtist> = emptyList(),
)

@Serializable
data class SaavnArtist(
    val id: String = "",
    val name: String = "",
    val image: String = "",
)

@Serializable
data class SaavnAuthResponse(
    @SerialName("auth_url") val authUrl: String = "",
    val type: String = "",
    val status: String = "",
)

@Serializable
data class SaavnSongDetailResponse(
    val songs: List<SaavnSongDetail> = emptyList(),
)

@Serializable
data class SaavnSongDetail(
    val id: String = "",
    val song: String = "",
    val album: String = "",
    @SerialName("primary_artists") val primaryArtists: String = "",
    val image: String = "",
    val duration: String = "",
    @SerialName("media_preview_url") val mediaPreviewUrl: String = "",
    @SerialName("encrypted_media_url") val encryptedMediaUrl: String = "",
)
