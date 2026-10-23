package com.dotacionestoronto.dotacionestoronto.facturacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.dotacionestoronto.dotacionestoronto.entity.DetalleVenta;
import com.dotacionestoronto.dotacionestoronto.entity.Pedido;
import com.dotacionestoronto.dotacionestoronto.entity.Producto;
import com.dotacionestoronto.dotacionestoronto.entity.Usuario;
import com.dotacionestoronto.dotacionestoronto.entity.Venta;
import com.dotacionestoronto.dotacionestoronto.facturacion.entity.FacturaElectronica;
import com.dotacionestoronto.dotacionestoronto.facturacion.repository.DetalleFacturaElectronicaRepository;
import com.dotacionestoronto.dotacionestoronto.facturacion.repository.FacturaElectronicaRepository;
import com.dotacionestoronto.dotacionestoronto.facturacion.service.FacturacionService;
import com.dotacionestoronto.dotacionestoronto.repository.DetalleVentaRepository;
import com.dotacionestoronto.dotacionestoronto.repository.PedidoRepository;
import com.dotacionestoronto.dotacionestoronto.repository.ProductoRepository;
import com.dotacionestoronto.dotacionestoronto.repository.UsuarioRepository;
import com.dotacionestoronto.dotacionestoronto.repository.VentaRepository;

/**
 * Prueba integral del módulo sobre una base de datos H2 en memoria:
 * emisión automática desde una venta, numeración consecutiva, CUFE, XML,
 * PDF y todas las pantallas del panel de facturación.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:facturacion;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.show-sql=false",
        "facturacion.auto-crear-tablas=false",
        "facturacion.modo=SIMULADO",
        "facturacion.ambiente=2"
})
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FacturacionIntegracionTest {

    @Autowired private FacturacionService facturacionService;
    @Autowired private FacturaElectronicaRepository facturaRepository;
    @Autowired private DetalleFacturaElectronicaRepository detalleRepository;
    @Autowired private VentaRepository ventaRepository;
    @Autowired private DetalleVentaRepository detalleVentaRepository;
    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private ProductoRepository productoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private MockMvc mockMvc;

    private Long idVenta;

    @BeforeEach
    void prepararVenta() {
        if (idVenta != null) {
            return;
        }

        Producto producto = new Producto();
        producto.setNombre("Camisa de trabajo");
        producto.setPrecio(new BigDecimal("119000"));
        producto.setTalla("M");
        producto.setColor("Azul");
        producto.setEstado("Activo");
        producto.setInhabilitado(false);
        producto = productoRepository.save(producto);

        Usuario cliente = new Usuario();
        cliente.setNombre("Cliente Prueba");
        cliente.setDocumento("1012345678");
        cliente.setCorreo("cliente.prueba@correo.com");
        cliente.setTelefono("3001234567");
        cliente.setDireccion("Calle 100 # 20-30");
        cliente.setContrasena("x");
        cliente.setIdRol(4);
        cliente.setEstado("Habilitado");
        cliente = usuarioRepository.save(cliente);

        Pedido pedido = new Pedido();
        pedido.setIdUsuario(cliente.getId());
        pedido.setFechaPedido(LocalDateTime.now());
        pedido.setEstado("Entregado");
        pedido.setOculto(false);
        pedido = pedidoRepository.save(pedido);

        Venta venta = new Venta();
        venta.setIdUsuario(cliente.getId());
        venta.setIdPedido(pedido.getId());
        venta.setFechaVenta(LocalDateTime.now());
        venta.setTotalPagado(119000.0);
        venta = ventaRepository.save(venta);

        DetalleVenta detalle = new DetalleVenta();
        detalle.setIdVenta(venta.getId());
        detalle.setIdProducto(producto.getId());
        detalle.setCantidad(1);
        detalle.setPrecioUnitario(new BigDecimal("119000.00"));
        detalleVentaRepository.save(detalle);

        idVenta = venta.getId();
    }

    @Test
    @DisplayName("La venta genera una factura ACEPTADA con CUFE, XML y PDF")
    void emisionAutomatica() {

        FacturaElectronica factura = facturacionService.emitirDesdeVenta(idVenta);

        assertNotNull(factura, "Debe generarse la factura");
        assertEquals(FacturaElectronica.ESTADO_ACEPTADA, factura.getEstado());
        assertNotNull(factura.getCufe());
        assertEquals(96, factura.getCufe().length());
        assertNotNull(factura.getXml());
        assertTrue(factura.getNumeroFactura().startsWith("DT"));
        assertTrue(factura.getXml().contains("<cbc:ID>" + factura.getNumeroFactura() + "</cbc:ID>"),
                "El XML debe llevar el número asignado");
        assertTrue(factura.getXml().contains(factura.getCufe()));
        assertEquals(1, factura.getDetalles().size());
        assertTrue(factura.getQrUrl().contains("documentkey="));

        // Valores: el precio incluye IVA (119.000 -> 100.000 + 19.000)
        assertEquals(0, new BigDecimal("100000.00").compareTo(factura.getValorBruto()));
        assertEquals(0, new BigDecimal("19000.00").compareTo(factura.getTotalIva()));
        assertEquals(0, new BigDecimal("119000.00").compareTo(factura.getTotalFactura()));

        byte[] pdf = facturacionService.generarPdf(factura.getId());
        assertTrue(pdf.length > 3000);
    }

    @Test
    @DisplayName("La numeración es consecutiva y no se repite")
    void numeracionConsecutiva() {

        FacturaElectronica primera = facturacionService.emitirDesdeVenta(idVenta);
        FacturaElectronica segunda = facturacionService.buscarPorVenta(idVenta);

        assertEquals(primera.getId(), segunda.getId(), "Una venta solo puede tener una factura");

        List<FacturaElectronica> todas = facturacionService.listarTodas();
        long consecutivos = todas.stream().map(FacturaElectronica::getNumeroFactura).distinct().count();
        assertEquals(todas.size(), consecutivos, "Los números de factura no deben repetirse");
    }

    @Test
    @WithMockUser(username = "admin@dotacionestoronto.com", roles = "ADMIN")
    @DisplayName("Las pantallas del módulo responden correctamente")
    void pantallas() throws Exception {

        FacturaElectronica factura = facturacionService.emitirDesdeVenta(idVenta);

        mockMvc.perform(get("/admin/facturacion"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Facturación electrónica DIAN")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(factura.getNumeroFactura())));

        mockMvc.perform(get("/admin/facturacion/" + factura.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(factura.getCufe())));

        mockMvc.perform(get("/admin/facturacion/" + factura.getId() + "/pdf"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"));

        mockMvc.perform(get("/admin/facturacion/" + factura.getId() + "/xml"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<Invoice")));

        mockMvc.perform(get("/admin/facturacion/" + factura.getId() + "/qr.png"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"));

        mockMvc.perform(get("/admin/facturacion/configuracion"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Numeración autorizada")));

        // Reenvío de la factura desde el panel
        mockMvc.perform(post("/admin/facturacion/" + factura.getId() + "/reenviar").with(csrf()))
                .andExpect(redirectedUrlPattern("/admin/facturacion/*"));
    }

    @Test
    @DisplayName("Un usuario sin rol de administrador no accede al módulo")
    @WithMockUser(username = "cliente@correo.com", roles = "CLIENTE")
    void seguridad() throws Exception {
        mockMvc.perform(get("/admin/facturacion"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Los datos de la factura quedan persistidos con sus ítems y eventos")
    void persistencia() {

        FacturaElectronica factura = facturacionService.emitirDesdeVenta(idVenta);

        FacturaElectronica cargada = facturacionService.buscar(factura.getId());
        assertNotNull(cargada);
        assertEquals(factura.getNumeroFactura(), cargada.getNumeroFactura());
        assertTrue(detalleRepository.findByIdFacturaOrderByNumeroLineaAsc(factura.getId()).size() >= 1);
        assertTrue(!cargada.getEventos().isEmpty(), "Debe registrar la bitácora del proceso");
        assertTrue(facturaRepository.findByIdVenta(idVenta).isPresent());
    }
}
