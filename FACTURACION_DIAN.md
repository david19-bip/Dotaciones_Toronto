# Módulo de Facturación Electrónica Automática — DIAN

Módulo completo de facturación electrónica integrado al sistema **Dotaciones Toronto**
(Spring Boot 3.2 · Java 17 · MySQL · Thymeleaf).

Emite la **factura electrónica de venta** automáticamente cuando el vendedor marca un
pedido como **Entregado**, genera el **XML UBL 2.1 + CUFE**, lo **firma con XAdES-BES**,
produce la **representación gráfica en PDF con código QR** y lo **transmite a la DIAN**
(o lo deja en modo simulación, sin trámites).

---

## 1. Qué se agregó

```
src/main/java/.../facturacion/
├── config/
│   ├── FacturacionProperties.java          Todas las propiedades (facturacion.*)
│   ├── FacturacionSchemaInitializer.java   Crea tablas y siembra la resolución
│   └── FacturacionConfig.java              Habilita tareas programadas
├── controller/
│   └── FacturacionController.java          Panel /admin/facturacion
├── dian/
│   ├── DianGateway.java                    Contrato del canal de transmisión
│   ├── DianSimuladoGateway.java            Modo SIMULADO (sin trámites)
│   ├── DianDirectoGateway.java             Modo DIAN (SOAP + mTLS)
│   ├── ProveedorTecnologicoGateway.java    Modo PROVEEDOR (API REST)
│   ├── ResultadoEnvio.java / ResultadoConsulta.java
├── entity/                                 FacturaElectronica, DetalleFacturaElectronica,
│                                           EventoFactura, ResolucionFacturacion
├── repository/                             Repositorios JPA
└── service/
    ├── CufeService.java                    SHA-384, CUFE, dígito de verificación, QR
    ├── NumeracionService.java              Consecutivo con bloqueo (SELECT FOR UPDATE)
    ├── XmlFacturaService.java              XML UBL 2.1 + extensiones DIAN
    ├── FirmaDigitalService.java            Firma XMLDSig + XAdES-BES (Santuario)
    ├── QrService.java                      Código QR (ZXing)
    ├── PdfFacturaService.java              Representación gráfica (OpenPDF)
    ├── FacturaEmisionService.java          Persistencia transaccional
    ├── FacturacionService.java             Orquestador del proceso
    └── FacturaEstadoScheduler.java         Consulta periódica de estados

src/main/resources/
├── db/facturacion_dian.sql                 DDL de las 4 tablas
└── templates/admin/facturacion_admin.html
                      factura_detalle_admin.html
                      facturacion_config_admin.html
```

**Archivos del proyecto que se modificaron:**

| Archivo | Cambio |
|---|---|
| `pom.xml` | Dependencias `xmlsec` (firma) y `zxing` (QR), H2 para pruebas |
| `application.properties` | Bloque `facturacion.*` completo y documentado |
| `VendedorController.java` | Al marcar **Entregado** → `facturacionService.emitirDesdeVenta(...)` |
| `templates/admin/*.html` | Enlace **Facturación DIAN** en el menú lateral |
| `DotacionestorontoApplicationTests.java` | Corre sobre H2 (ya no exige MySQL encendido) |

---

## 2. Cómo funciona el flujo automático

```
Vendedor marca el pedido como "Entregado"
        │
        ├─ 1. Se crea la Venta (código original del proyecto)
        ├─ 2. NumeraciónService: toma el siguiente consecutivo de la resolución autorizada
        ├─ 3. Se calculan ítems, base gravable e IVA
        ├─ 4. CufeService: CUFE con SHA-384 (cadena oficial del Anexo 1.9)
        ├─ 5. XmlFacturaService: XML UBL 2.1 + DianExtensions + QR
        ├─ 6. FirmaDigitalService: firma XAdES-BES (si hay certificado)
        ├─ 7. DianGateway: envía por el canal configurado
        └─ 8. Se guarda estado, trackId, mensajes y bitácora de eventos
```

Si algo falla, **la venta y el pedido nunca se deshacen**: la factura queda en estado
`ERROR` con el detalle guardado y el administrador puede **reenviar** desde el panel.

### Estados de una factura

| Estado | Significado |
|---|---|
| `GENERADA` | XML y CUFE listos, pendiente de envío |
| `ENVIADA` | Recibida por la DIAN (ZipKey); el resultado llega después |
| `ACEPTADA` | Validada y aceptada por la DIAN ✅ |
| `RECHAZADA` | Rechazada por reglas de validación (ver mensaje DIAN) |
| `ERROR` | Fallo técnico (sin certificado, sin red, configuración incompleta) |

---

## 3. Modos de operación

Se cambia con **una sola propiedad**: `facturacion.modo`.

| Modo | Qué hace | Requisitos | Costo |
|---|---|---|---|
| `SIMULADO` | Genera XML, CUFE, firma (si hay certificado), PDF y QR. Marca la factura como ACEPTADA indicando “simulación”. | Ninguno | Gratis |
| `DIAN` | Transmite a los servicios web SOAP de la DIAN con autenticación mutua (mTLS) usando `SendTestSetAsync` / `SendBillAsync` / `GetStatusZip`. | Certificado digital, Software ID + PIN, habilitación, resolución de numeración | La DIAN **no cobra** por transmitir; solo el certificado |
| `PROVEEDOR` | Envía el XML a un proveedor tecnológico por API REST (se configura la URL y el token). | Contrato con el proveedor | Mensualidad del proveedor |

> **Ruta más rápida y gratuita:** empezar en `SIMULADO` (funciona hoy mismo) y pasar a
> `DIAN` cuando se tengan certificado y resolución. No cuesta nada transmitir a la DIAN.

---

## 4. Configuración (`application.properties`)

```properties
# Interruptor y modo
facturacion.habilitada=true
facturacion.modo=SIMULADO            # SIMULADO | DIAN | PROVEEDOR
facturacion.emision-automatica=true  # factura al entregar el pedido
facturacion.ambiente=2               # 1 = Producción, 2 = Pruebas/habilitación
facturacion.auto-crear-tablas=true

# Datos del emisor (CAMBIAR por los reales del RUT)
facturacion.emisor.nit=900123456
facturacion.emisor.razon-social=DOTACIONES TORONTO S.A.S.
facturacion.emisor.regimen=48        # 48 responsable de IVA, 49 no responsable
facturacion.emisor.responsabilidades=O-13,O-15,O-23
...

# Numeración autorizada por la DIAN
facturacion.numeracion.prefijo=DT
facturacion.numeracion.desde=1
facturacion.numeracion.hasta=5000
facturacion.numeracion.clave-tecnica=...
facturacion.numeracion.resolucion-numero=18760000001
facturacion.numeracion.vigencia-desde=2025-01-01
facturacion.numeracion.vigencia-hasta=2027-12-31

# Impuestos (los precios del sistema ya incluyen IVA)
facturacion.impuestos.iva-porcentaje=19
facturacion.impuestos.precios-incluyen-iva=true

# DIAN directa
facturacion.dian.software-id=...
facturacion.dian.software-pin=...
facturacion.dian.certificado-ruta=file:C:/firma/firma.p12
facturacion.dian.certificado-password=****
```

La **resolución** también queda en la tabla `resolucion_facturacion`; si se cambia de
rango se actualiza allí (el consecutivo vive en base de datos, no en memoria).

---

## 5. Base de datos

Las tablas se crean solas al arrancar (`facturacion.auto-crear-tablas=true`).
Si se prefiere hacerlo a mano (phpMyAdmin), ejecutar:

```
src/main/resources/db/facturacion_dian.sql
```

Tablas: `factura_electronica`, `factura_electronica_detalle`,
`factura_electronica_evento`, `resolucion_facturacion`.
Son tablas nuevas: **no modifican ni borran nada de las existentes**.

---

## 6. Panel de administración

| Ruta | Descripción |
|---|---|
| `GET /admin/facturacion` | Listado con indicadores y filtro por estado |
| `GET /admin/facturacion/{id}` | Detalle: ítems, totales, CUFE, QR, bitácora |
| `GET /admin/facturacion/{id}/pdf` | Representación gráfica (PDF con QR) |
| `GET /admin/facturacion/{id}/xml` | XML UBL 2.1 firmado |
| `GET /admin/facturacion/{id}/qr.png` | Código QR |
| `POST /admin/facturacion/{id}/reenviar` | Reintentar el envío |
| `POST /admin/facturacion/{id}/consultar` | Consultar el estado en la DIAN |
| `POST /admin/facturacion/emitir-venta` | Emitir la factura de una venta que no la tenga |
| `GET /admin/facturacion/configuracion` | Estado del módulo y de la resolución |

Todo el módulo está protegido con `ROLE_ADMIN` (regla ya existente `/admin/**`).

---

## 7. Qué se genera exactamente

**CUFE** (Anexo Técnico 1.9, numeral 11.2):

```
CUFE = SHA-384(NumFac + FecFac + HorFac + ValFac +
               CodImp1 + ValImp1 + CodImp2 + ValImp2 + CodImp3 + ValImp3 +
               ValTot + NitOFE + NumAdq + ClTec + TipoAmbiente)
```

**SoftwareSecurityCode** = `SHA-384(SoftwareID + PIN + NumFac)`.

**XML**: `Invoice` UBL 2.1 (`ProfileExecutionID` = ambiente), con `DianExtensions`
(InvoiceControl, SoftwareProvider, SoftwareSecurityCode, QRCode), `AccountingSupplierParty`,
`AccountingCustomerParty`, `TaxTotal`, `LegalMonetaryTotal` e `InvoiceLine`.

**Firma**: XMLDSig enveloped + XAdES-BES (`SignedProperties` con `SigningTime`,
`SigningCertificate`, `SignaturePolicyIdentifier` implícita y `DataObjectFormat`),
RSA-SHA256, con Apache Santuario — la misma librería con la que la DIAN valida.

**PDF**: representación gráfica con los requisitos del artículo 35 de la Resolución
000165 de 2023: datos del emisor y adquirente, numeración autorizada, fecha y hora,
ítems, impuestos, totales, CUFE, código QR y leyenda de validación.

---

## 8. Paso a paso para producción con la DIAN

1. **RUT actualizado** con la responsabilidad de facturar y actividad económica.
2. **Certificado digital** de firma electrónica (entidad de certificación autorizada,
   típicamente ~$100.000–$200.000 COP/año). Guardarlo en `facturacion.dian.certificado-ruta`.
3. **Habilitación** como facturador electrónico en el portal DIAN
   (Servicios informáticos electrónicos → Facturación electrónica). Allí se descarga el
   **set de pruebas** (`facturacion.numeracion.test-set-id`).
4. **Registrar el software** en el portal DIAN: entrega **Software ID** y **PIN**.
5. **Resolución de numeración**: prefijo, rango y **clave técnica**
   (`facturacion.numeracion.*`). Suele tardar pocos días hábiles.
6. Probar en `modo=DIAN` con `ambiente=2` y el set de pruebas hasta que la DIAN apruebe.
7. Pasar a `ambiente=1` (producción). Listo.

Mientras no se tenga lo anterior, el sistema funciona en `SIMULADO` sin perder trabajo:
los documentos, el CUFE, el QR y el PDF ya se generan igual.

---

## 9. Pruebas

```bash
./mvnw test
```

16 pruebas automatizadas:

- `FacturacionCoreTest` — SHA-384, cadena y CUFE, SoftwareSecurityCode, dígito de
  verificación, XML UBL bien formado y escapado, **firma verificada con Santuario**,
  PDF y QR.
- `FacturacionIntegracionTest` — flujo completo sobre H2: emisión desde una venta,
  consecutivos únicos, persistencia de ítems y bitácora, y todas las pantallas del panel
  (listado, detalle, PDF, XML, QR, configuración, reenvío) más control de acceso por rol.
- `DocumentosEjemploTest` — deja en `target/demo/` un XML firmado, el PDF y el QR de
  ejemplo para revisar el resultado sin base de datos.

---

## 10. Alcance y siguientes pasos sugeridos

Incluido: factura electrónica de venta, emisión automática, firma, PDF, QR, panel,
reintentos, bitácora y los tres canales de transmisión.

Pendiente (si se requiere):
- **Nota crédito electrónica** para las devoluciones (reutiliza el mismo motor:
  `CreditNote` UBL 2.1 + CUDE).
- Envío de la factura al correo del cliente (SMTP).
- Firma del PDF (la DIAN no lo exige: el XML es el documento con validez fiscal).
- Documento equivalente POS, si se factura en mostrador.
- Envío de eventos RADIAN, si el negocio lo requiere.

---

## 11. Seguridad

- El XML y el CUFE quedan almacenados para auditoría y para reproducir el PDF.
- La contraseña del certificado y el PIN **no** deben quedar en el repositorio:
  usar variables de entorno (`FACTURACION_DIAN_CERTIFICADO_PASSWORD`,
  `FACTURACION_DIAN_SOFTWARE_PIN`) o un gestor de secretos en producción.
- El consecutivo se toma con `SELECT ... FOR UPDATE`: dos ventas simultáneas nunca
  reciben el mismo número.
