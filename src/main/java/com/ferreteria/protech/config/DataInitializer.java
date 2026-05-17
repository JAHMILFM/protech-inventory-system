package com.ferreteria.protech.config;

import com.ferreteria.protech.model.*;
import com.ferreteria.protech.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Inicializador de datos de ejemplo para la aplicación.
 * Crea roles, usuario admin, categorías y productos de ejemplo.
 */
@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(RoleRepository roleRepo,
                               UserRepository userRepo,
                               CategoryRepository catRepo,
                               ProductRepository prodRepo,
                               PedidoRepository pedRepo,
                               KardexRepository kardexRepo,
                               PasswordEncoder encoder) {
        return args -> {
            // ── Crear Roles si no existen ──────────────────
            if (!roleRepo.existsByNombre("ADMIN")) {
                roleRepo.save(new Role("ADMIN", "Administrador del sistema"));
            }
            if (!roleRepo.existsByNombre("OPERARIO")) {
                roleRepo.save(new Role("OPERARIO", "Operario de almacén"));
            }
            if (!roleRepo.existsByNombre("PROVEEDOR")) {
                roleRepo.save(new Role("PROVEEDOR", "Proveedor de mercancía"));
            }
            if (!roleRepo.existsByNombre("CLIENTE")) {
                roleRepo.save(new Role("CLIENTE", "Cliente de la ferretería"));
            }

            // ── Crear Usuarios si no existen ───────────
            if (!userRepo.existsByUsername("admin")) {
                Role adminRole = roleRepo.findByNombre("ADMIN").orElseThrow();
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(encoder.encode("admin123"));
                admin.setNombreCompleto("Administrador Pro-Tech");
                admin.setEmail("admin@protech.com");
                admin.setTelefono("999-888-777");
                admin.setRoles(Set.of(adminRole));
                userRepo.save(admin);
            }
            if (!userRepo.existsByUsername("operario1")) {
                Role opRole = roleRepo.findByNombre("OPERARIO").orElseThrow();
                User op1 = new User();
                op1.setUsername("operario1");
                op1.setPassword(encoder.encode("op123"));
                op1.setNombreCompleto("Juan Pérez Quispe");
                op1.setEmail("jperez@protech.com");
                op1.setRoles(Set.of(opRole));
                userRepo.save(op1);
            }
            if (!userRepo.existsByUsername("operario2")) {
                Role opRole = roleRepo.findByNombre("OPERARIO").orElseThrow();
                User op2 = new User();
                op2.setUsername("operario2");
                op2.setPassword(encoder.encode("op123"));
                op2.setNombreCompleto("María García López");
                op2.setEmail("mgarcia@protech.com");
                op2.setRoles(Set.of(opRole));
                userRepo.save(op2);
            }

            // ── Crear Categorías si no existen ─────────────
            if (catRepo.count() == 0) {
                // Departamentos (Nivel 1)
                Category herramientas = new Category();
                herramientas.setNombre("Herramientas");
                herramientas.setDescripcion("Herramientas manuales y eléctricas");
                herramientas.setIcono("🔧");
                herramientas.setNivel(1);
                herramientas = catRepo.save(herramientas);

                Category electricidad = new Category();
                electricidad.setNombre("Electricidad");
                electricidad.setDescripcion("Material eléctrico y luminarias");
                electricidad.setIcono("⚡");
                electricidad.setNivel(1);
                electricidad = catRepo.save(electricidad);

                Category plomeria = new Category();
                plomeria.setNombre("Plomería");
                plomeria.setDescripcion("Tuberías, accesorios y grifería");
                plomeria.setIcono("🚿");
                plomeria.setNivel(1);
                plomeria = catRepo.save(plomeria);

                Category pinturas = new Category();
                pinturas.setNombre("Pinturas");
                pinturas.setDescripcion("Pinturas, brochas y accesorios");
                pinturas.setIcono("🎨");
                pinturas.setNivel(1);
                pinturas = catRepo.save(pinturas);

                Category construccion = new Category();
                construccion.setNombre("Construcción");
                construccion.setDescripcion("Materiales de construcción y obra gruesa");
                construccion.setIcono("🏗️");
                construccion.setNivel(1);
                construccion = catRepo.save(construccion);

                // Categorías (Nivel 2)
                Category manuales = new Category();
                manuales.setNombre("Herramientas Manuales");
                manuales.setDescripcion("Martillos, destornilladores, llaves");
                manuales.setNivel(2);
                manuales.setPadre(herramientas);
                manuales = catRepo.save(manuales);

                Category electricas = new Category();
                electricas.setNombre("Herramientas Eléctricas");
                electricas.setDescripcion("Taladros, sierras, amoladoras");
                electricas.setNivel(2);
                electricas.setPadre(herramientas);
                electricas = catRepo.save(electricas);

                Category cables = new Category();
                cables.setNombre("Cables y Conductores");
                cables.setDescripcion("Cables THW, NM, extensiones");
                cables.setNivel(2);
                cables.setPadre(electricidad);
                cables = catRepo.save(cables);

                Category tuberias = new Category();
                tuberias.setNombre("Tuberías PVC");
                tuberias.setDescripcion("Tubos y conexiones PVC");
                tuberias.setNivel(2);
                tuberias.setPadre(plomeria);
                tuberias = catRepo.save(tuberias);

                // ── Crear Productos de Ejemplo ─────────────
                if (prodRepo.count() == 0) {
                    crearProducto(prodRepo, "FPT-001", "7501234567890", "Martillo de Uña 16oz Stanley",
                            new BigDecimal("25.00"), new BigDecimal("45.90"), 150, 20,
                            "Stanley", "51-621", "1", "A", "3", manuales);

                    crearProducto(prodRepo, "FPT-002", "7501234567891", "Destornillador Phillips #2 Stanley",
                            new BigDecimal("8.50"), new BigDecimal("15.90"), 200, 30,
                            "Stanley", "69-262", "1", "A", "2", manuales);

                    crearProducto(prodRepo, "FPT-003", "7501234567892", "Taladro Percutor 1/2\" DeWalt 750W",
                            new BigDecimal("185.00"), new BigDecimal("349.90"), 35, 5,
                            "DeWalt", "DWD024", "2", "B", "1", electricas);

                    crearProducto(prodRepo, "FPT-004", "7501234567893", "Amoladora 4-1/2\" Bosch 850W",
                            new BigDecimal("120.00"), new BigDecimal("229.90"), 28, 5,
                            "Bosch", "GWS850", "2", "B", "2", electricas);

                    crearProducto(prodRepo, "FPT-005", "7501234567894", "Cable THW 14 AWG Negro (100m)",
                            new BigDecimal("45.00"), new BigDecimal("89.90"), 80, 15,
                            "Indeco", "THW-14N", "3", "C", "1", cables);

                    crearProducto(prodRepo, "FPT-006", "7501234567895", "Tubo PVC 1/2\" x 5m Presión",
                            new BigDecimal("4.50"), new BigDecimal("8.90"), 300, 50,
                            "Pavco", "PVC-12P", "4", "A", "1", tuberias);

                    crearProducto(prodRepo, "FPT-007", "7501234567896", "Llave Francesa 10\" Truper",
                            new BigDecimal("18.00"), new BigDecimal("34.90"), 60, 10,
                            "Truper", "LF-10", "1", "A", "4", manuales);

                    crearProducto(prodRepo, "FPT-008", "7501234567897", "Sierra Circular 7-1/4\" Makita",
                            new BigDecimal("230.00"), new BigDecimal("419.90"), 15, 3,
                            "Makita", "5007N", "2", "B", "3", electricas);

                    crearProducto(prodRepo, "FPT-009", "7501234567898", "Cinta Aislante Negra 3M",
                            new BigDecimal("2.80"), new BigDecimal("5.90"), 500, 100,
                            "3M", "1711", "3", "C", "2", cables);

                    crearProducto(prodRepo, "FPT-010", "7501234567899", "Llave Mixta Set 12 pzas Truper",
                            new BigDecimal("45.00"), new BigDecimal("89.90"), 40, 8,
                            "Truper", "LM-12S", "1", "B", "2", manuales);

                    // Productos con stock crítico para demostración
                    crearProducto(prodRepo, "FPT-011", "7501234567900", "Disco Diamante 4\" Bosch",
                            new BigDecimal("22.00"), new BigDecimal("42.90"), 3, 10,
                            "Bosch", "DD-4B", "2", "C", "1", electricas);

                    crearProducto(prodRepo, "FPT-012", "7501234567901", "Nivel de Burbuja 24\" Stanley",
                            new BigDecimal("15.00"), new BigDecimal("29.90"), 2, 5,
                            "Stanley", "42-074", "1", "A", "5", manuales);
                }
            }

            // ── Crear Pedidos si no existen ─────────────
            if (pedRepo.count() == 0) {
                crearPedido(pedRepo, "PED-001", "Constructora Lima SAC", 3, new BigDecimal("1250.00"), "Pendiente");
                crearPedido(pedRepo, "PED-002", "Ferretería El Sol", 5, new BigDecimal("890.50"), "Pendiente");
                crearPedido(pedRepo, "PED-003", "Taller Mecánico Norte", 2, new BigDecimal("445.00"), "Pendiente");
                crearPedido(pedRepo, "PED-004", "Distribuciones García", 8, new BigDecimal("3200.00"), "Completado");
            }

            // ── Crear Kardex (Movimientos) si no existen ─────────────
            if (kardexRepo.count() == 0) {
                User admin = userRepo.findByUsername("admin").orElse(null);
                
                prodRepo.findAll().forEach(p -> {
                    // Simular entrada inicial
                    crearMovimientoKardex(kardexRepo, p, Kardex.TipoMovimiento.ENTRADA, 
                            p.getStockActual() + 10, 0, p.getStockActual() + 10, 
                            p.getPrecioCosto(), "Inventario Inicial", "Proveedor S.A.", admin);
                    
                    // Simular una salida (venta)
                    crearMovimientoKardex(kardexRepo, p, Kardex.TipoMovimiento.SALIDA, 
                            10, p.getStockActual() + 10, p.getStockActual(), 
                            p.getPrecioVenta(), "Venta Mostrador", null, admin);
                });
            }
        };
    }

    private void crearProducto(ProductRepository repo, String sku, String ean, String nombre,
                                BigDecimal costo, BigDecimal venta, int stock, int stockMin,
                                String marca, String modelo, String pasillo, String lado,
                                String nivel, Category categoria) {
        Product p = new Product();
        p.setSku(sku);
        p.setEan13(ean);
        p.setNombre(nombre);
        p.setPrecioCosto(costo);
        p.setPrecioVenta(venta);
        p.setStockActual(stock);
        p.setStockMinimo(stockMin);
        p.setStockReserva(0);
        p.setMarca(marca);
        p.setModelo(modelo);
        p.setUbicacionPasillo(pasillo);
        p.setUbicacionLado(lado);
        p.setUbicacionNivel(nivel);
        p.setCategoria(categoria);
        p.setUnidadMedida("UND");
        repo.save(p);
    }

    private void crearPedido(PedidoRepository repo, String num, String cliente, int cant, BigDecimal total, String estado) {
        Pedido p = new Pedido();
        p.setNumeroPedido(num);
        p.setCliente(cliente);
        p.setCantidadProductos(cant);
        p.setTotal(total);
        p.setEstado(estado);
        repo.save(p);
    }

    private void crearMovimientoKardex(KardexRepository kardexRepo, Product producto, Kardex.TipoMovimiento tipo, 
            int cantidad, int stockAnterior, int stockNuevo, BigDecimal precioUnitario, 
            String motivo, String proveedor, User usuario) {
        Kardex k = new Kardex();
        k.setProducto(producto);
        k.setTipoMovimiento(tipo);
        k.setCantidad(cantidad);
        k.setStockAnterior(stockAnterior);
        k.setStockNuevo(stockNuevo);
        k.setPrecioUnitario(precioUnitario);
        k.setCostoTotal(precioUnitario.multiply(new BigDecimal(cantidad)));
        k.setMotivo(motivo);
        k.setProveedor(proveedor);
        k.setUsuario(usuario);
        kardexRepo.save(k);
    }
}
