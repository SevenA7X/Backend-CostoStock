package cl.duoc.costostock.core.dto;

public record MetricasVendedorDto(
        Long idProveedor,
        Integer totalLotesPublicados,
        Integer lotesActivos,
        Integer totalUnidadesVendidas,
        Integer ventasBrutasTotales,
        Integer montoLiquidoTotalAcumulado,
        Integer comisionTotalGenerada,
        Double totalKgCo2Evitado,
        Integer equivalenciaArbolesPlantados
) {
}