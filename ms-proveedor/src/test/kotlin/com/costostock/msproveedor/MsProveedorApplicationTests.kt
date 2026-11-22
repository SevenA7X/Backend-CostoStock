package com.costostock.msproveedor

import com.costostock.msproveedor.exception.ApiException
import com.costostock.msproveedor.service.FotoValidator
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.context.SpringBootTest
import java.util.Base64
import kotlin.test.assertEquals

@SpringBootTest
class MsProveedorApplicationTests {

    companion object {
        // Foto JPEG real de 16x16 px (la que entregaría la cámara, convertida a Base64)
        const val JPEG_BASE64 = "/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAA0JCgsKCA0LCgsODg0PEyAVExISEyccHhcgLikxMC4pLSwzOko+MzZGNywtQFdBRkxOUlNSMj5aYVpQYEpRUk//2wBDAQ4ODhMREyYVFSZPNS01T09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT0//wAARCAAQABADASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAAAgEDAwIEAwUFBAQAAAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkKFhcYGRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWGh4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4+Tl5ufo6erx8vP09fb3+Pn6/8QAHwEAAwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREAAgECBAQDBAcFBAQAAQJ3AAECAxEEBSExBhJBUQdhcRMiMoEIFEKRobHBCSMzUvAVYnLRChYkNOEl8RcYGRomJygpKjU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOEhYaHiImKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk5ebn6Onq8vP09fb3+Pn6/9oADAMBAAIRAxEAPwB/h7Qv7b+0f6T5Hk7f+We7Oc+49KPEOhf2J9n/ANJ8/wA7d/yz24xj3PrR4e13+xPtH+jef523/lptxjPsfWjxDrv9t/Z/9G8jyd3/AC03Zzj2HpXL7vL5nD7nJ5n/2Q=="
        const val PNG_BASE64 = "iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAIAAACQkWg2AAAALklEQVR4XmPUqzViIAUwkaIYBGivgQXZSRebzmJ1oX6dMVyc9k4akTYwDoO0BAAoAgTbpUDPngAAAABJRU5ErkJggg=="
        const val MAX = 2_097_152
    }

    @Test
    fun contextLoads() {
    }

    @Test
    fun aceptaUnaFotoJpeg() {
        val foto = FotoValidator.validar(JPEG_BASE64, MAX)
        assertEquals("image/jpeg", foto.contentType)
    }

    @Test
    fun aceptaUnaFotoPngConPrefijoDataUrl() {
        val foto = FotoValidator.validar("data:image/png;base64,$PNG_BASE64", MAX)
        assertEquals("image/png", foto.contentType)
    }

    @Test
    fun rechazaArchivosQueNoSonImagenes() {
        val texto = Base64.getEncoder().encodeToString("no soy una foto".toByteArray())
        val error = assertThrows<ApiException> { FotoValidator.validar(texto, MAX) }
        assertEquals(400, error.status)
    }

    @Test
    fun rechazaFotosDemasiadoGrandes() {
        val error = assertThrows<ApiException> { FotoValidator.validar(JPEG_BASE64, 100) }
        assertEquals(400, error.status)
    }

    @Test
    fun rechazaBase64Invalido() {
        val error = assertThrows<ApiException> { FotoValidator.validar("@@@###", MAX) }
        assertEquals(400, error.status)
    }
}
