package cl.duoc.costostock.transacciones.repository;

import cl.duoc.costostock.transacciones.model.Lote;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;


@Component
public class LoteRepository {

    private final Map<Long, Lote> lotes = new LinkedHashMap<>();

    public LoteRepository() {
        LocalDateTime ahora = LocalDateTime.now();
        // id, nombre, descripción, categoría, normal, oferta, stock, lat, lon, fin oferta (h), caducidad (días), kg CO2, alcohol, tienda, dirección, horario
        agregar(1, "Pack Bebidas Energéticas", "Pack de 6 bebidas energéticas", "ABARROTES_ENVASADOS", 8000, 4400, 10, -33.0245, -71.5518, ahora.plusHours(6), ahora.plusDays(1), 0.5, false, "Bodega Viña Centro", "Av. Libertad 1020, Viña del Mar");
        agregar(2, "Cervezas Artesanales", "Caja de 12 cervezas artesanales", "BEBIDAS_ALCOHOLICAS", 15000, 11000, 24, -33.0441, -71.6243, ahora.plusHours(12), ahora.plusDays(30), 1.2, true, "Licorería El Puerto", "Cochrane 450, Valparaíso");
        agregar(3, "Arvejas Congeladas 1kg", "Arvejas congeladas en bolsa de 1 kg", "CONGELADOS", 2500, 1200, 15, -33.0153, -71.5500, ahora.plusHours(24), ahora.plusDays(2), 0.3, false, "Frigorífico del Norte", "1 Norte 800, Viña del Mar");
        agregar(4, "Helado Vainilla 2L", "Helado de vainilla, tarro de 2 litros", "CONGELADOS", 7000, 3500, 8, -33.0475, -71.4425, ahora.plusHours(24), ahora.plusDays(2), 0.8, false, "Heladería Central", "Av. Los Carrera 600, Quilpué");
        agregar(5, "Ibuprofeno 400mg", "Caja de 20 comprimidos", "FARMACIA", 3000, 1500, 20, -33.0230, -71.5520, ahora.plusHours(12), ahora.plusDays(1), 0.4, false, "Farmacia San Juan", "Valparaíso 200, Viña del Mar");
        agregar(6, "Vitamina C Efervescente", "Tubo de 20 tabletas efervescentes", "FARMACIA", 8500, 4990, 5, -33.0230, -71.5520, ahora.plusHours(24), ahora.plusDays(15), 1.0, false, "Farmacia San Juan", "Valparaíso 200, Viña del Mar");
        agregar(7, "Ampolletas LED", "Pack de 4 ampolletas LED 9W", "FERRETERIA", 2000, 800, 30, -33.0480, -71.4440, ahora.plusHours(12), ahora.plusDays(1), 0.2, false, "Ferretería El Maestro", "Freire 350, Quilpué");
        agregar(8, "Pintura Blanca 1 Galón", "Látex blanco interior, 1 galón", "FERRETERIA", 15000, 7500, 2, -33.0480, -71.4440, ahora.plusHours(12), ahora.plusDays(2), 1.5, false, "Ferretería El Maestro", "Freire 350, Quilpué");
        agregar(9, "Hamburguesas Congeladas", "Caja de 12 hamburguesas de vacuno", "CONGELADOS", 6000, 3200, 12, -33.0460, -71.6180, ahora.plusHours(8), ahora.plusDays(1), 2.0, false, "Distribuidora Frío", "Errázuriz 900, Valparaíso");
        agregar(10, "Vienesas Envasadas 1kg", "Vienesas de pollo envasadas al vacío", "ABARROTES_ENVASADOS", 5500, 2800, 18, -33.0500, -71.6150, ahora.plusHours(24), ahora.plusDays(7), 1.1, false, "Minimarket Los Pinos", "Pedro Montt 120, Valparaíso");
        agregar(11, "Fideos Espagueti 400g", "Fideos espagueti, paquete de 400 g", "ABARROTES_ENVASADOS", 900, 500, 50, -33.0420, -71.6200, ahora.plusHours(48), ahora.plusDays(365), 0.1, false, "Supermercado Ahorro", "Brasil 500, Valparaíso");
        agregar(12, "Salsa de Tomate 200g", "Salsa de tomate envasada, 200 g", "ABARROTES_ENVASADOS", 650, 300, 40, -33.0470, -71.4430, ahora.plusHours(24), ahora.plusDays(3), 0.1, false, "Supermercado Ahorro", "Av. Los Carrera 410, Quilpué");
    }

    private void agregar(long id, String nombre, String descripcion, String categoria,
                         double normal, double oferta, int stock, double lat, double lon,
                         LocalDateTime finOferta, LocalDateTime caducidad, double kgCo2,
                         boolean alcohol, String tienda, String direccion) {
        lotes.put(id, new Lote(id, nombre, descripcion, categoria, normal, oferta, stock, lat, lon,
                finOferta, caducidad, kgCo2, alcohol, tienda, direccion, "09:00 - 20:00"));
    }

    public List<Lote> todos() {
        return new ArrayList<>(lotes.values());
    }

    public Optional<Lote> buscar(long id) {
        return Optional.ofNullable(lotes.get(id));
    }
}
