package com.costostock.msproveedor.service

import com.costostock.msproveedor.dto.FotoValidada
import com.costostock.msproveedor.exception.ApiException
import java.util.Base64

// RF-05: revisa que la foto nativa enviada por la app sea una imagen real y de tamaño razonable
object FotoValidator {

    private val JPEG = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte())
    private val PNG = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47)

    fun validar(base64: String, maxBytes: Int): FotoValidada {
        // Acepta "data:image/jpeg;base64,AAAA..." o solo "AAAA..."
        val limpio = base64.substringAfter("base64,").trim()
        if (limpio.isEmpty()) throw ApiException(400, "RF-05: la foto está vacía")

        val bytes = try {
            Base64.getMimeDecoder().decode(limpio)
        } catch (e: IllegalArgumentException) {
            throw ApiException(400, "RF-05: la foto no tiene un formato Base64 válido")
        }

        if (bytes.size > maxBytes) {
            throw ApiException(400, "RF-05: la foto supera el máximo de ${maxBytes / 1024} KB")
        }

        // Se revisan los primeros bytes ("firma") del archivo para saber si es JPEG o PNG
        val tipo = when {
            empiezaCon(bytes, JPEG) -> "image/jpeg"
            empiezaCon(bytes, PNG) -> "image/png"
            else -> throw ApiException(400, "RF-05: la foto debe ser una imagen JPEG o PNG")
        }
        return FotoValidada(bytes, tipo)
    }

    private fun empiezaCon(datos: ByteArray, firma: ByteArray): Boolean =
        datos.size >= firma.size && firma.indices.all { datos[it] == firma[it] }
}
