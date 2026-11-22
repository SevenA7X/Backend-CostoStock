package cl.duoc.costostock.util

import android.graphics.Bitmap
import android.util.Base64
import java.io.ByteArrayOutputStream

// Convierte la foto nativa (Bitmap de la cámara) en texto Base64 para enviarla en el JSON
fun Bitmap.aBase64(calidadJpeg: Int = 80): String {
    val salida = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.JPEG, calidadJpeg, salida)
    return Base64.encodeToString(salida.toByteArray(), Base64.NO_WRAP)
}
