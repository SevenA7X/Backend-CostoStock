package com.costostock.mstransacciones.controller

import com.costostock.mstransacciones.model.Voucher
import com.costostock.mstransacciones.service.VoucherService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/vouchers")
@CrossOrigin(origins = ["*"])
class VoucherController(
    private val voucherService: VoucherService
) {

    // RF-14: Endpoint para generar el voucher digital (Lo consume el cliente al comprar)
    @PostMapping("/generar")
    fun generarVoucher(@RequestBody voucher: Voucher): ResponseEntity<Voucher> {
        val nuevoVoucher = voucherService.generarVoucher(voucher)
        return ResponseEntity.ok(nuevoVoucher)
    }

    // RF-16: Endpoint para validar el QR (Lo consume el comercio desde la app)
    @PostMapping("/validar")
    fun validarVoucher(
        @RequestParam codigoQr: String,
        @RequestParam idProveedor: Long
    ): ResponseEntity<Any> {
        return try {
            val voucherValidado = voucherService.validarYEntregar(codigoQr, idProveedor)
            ResponseEntity.ok(voucherValidado)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    // RF-17: Endpoint para descargar el historial de transacciones y métricas de impacto
    @GetMapping
    fun obtenerHistorialVouchers(): ResponseEntity<List<Voucher>> {
        val historial = voucherService.obtenerHistorial()
        return ResponseEntity.ok(historial)
    }
}