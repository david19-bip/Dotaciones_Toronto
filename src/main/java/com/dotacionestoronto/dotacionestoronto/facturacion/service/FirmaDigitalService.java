package com.dotacionestoronto.dotacionestoronto.facturacion.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Enumeration;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.xml.security.Init;
import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.transforms.Transforms;
import org.apache.xml.security.utils.Constants;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.dotacionestoronto.dotacionestoronto.facturacion.config.FacturacionProperties;

/**
 * Firma digital del XML de la factura electrónica.
 *
 * Produce una firma XMLDSig "enveloped" con propiedades XAdES-BES, que es lo
 * que exige el Anexo Técnico 1.9 de la DIAN:
 *
 *   - ds:Reference al documento completo (transformada enveloped + C14N)
 *   - ds:Reference a xades:SignedProperties (Type SignedProperties)
 *   - ds:KeyInfo con el certificado X.509
 *   - xades:SignedSignatureProperties con SigningTime, SigningCertificate y
 *     SignaturePolicyIdentifier (implícita)
 *
 * Se apoya en Apache Santuario (xmlsec), la misma librería con la que la DIAN
 * valida las firmas.
 */
@Service
public class FirmaDigitalService {

    private static final String NS_DS = "http://www.w3.org/2000/09/xmldsig#";
    private static final String NS_XADES = "http://uri.etsi.org/01903/v1.3.2#";
    private static final String NS_EXT = "urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2";
    private static final String TIPO_REFERENCIA_SIGNED_PROPS = "http://uri.etsi.org/01903#SignedProperties";
    private static final String DIGEST_SHA256 = "http://www.w3.org/2001/04/xmlenc#sha256";

    static {
        // Inicializa Santuario una sola vez por JVM
        if (!Init.isInitialized()) {
            Init.init();
        }
    }

    private final FacturacionProperties props;
    private final CufeService cufeService;

    private X509Certificate certificadoCache;
    private PrivateKey llaveCache;

    public FirmaDigitalService(FacturacionProperties props, CufeService cufeService) {
        this.props = props;
        this.cufeService = cufeService;
    }

    // ==========================================================
    // CARGA DEL CERTIFICADO
    // ==========================================================

    public boolean hayCertificado() {
        return props.getDian().getCertificadoRuta() != null
                && !props.getDian().getCertificadoRuta().isBlank();
    }

    public synchronized X509Certificate getCertificado() {
        cargarSiEsNecesario();
        return certificadoCache;
    }

    public String huellaCertificado() {
        if (!hayCertificado()) {
            return null;
        }
        try {
            cargarSiEsNecesario();
            return cufeService.sha256Hex(certificadoCache.getEncoded());
        } catch (Exception e) {
            return null;
        }
    }

    public String titularCertificado() {
        if (!hayCertificado()) {
            return null;
        }
        try {
            cargarSiEsNecesario();
            return certificadoCache.getSubjectX500Principal().getName();
        } catch (Exception e) {
            return null;
        }
    }

    private synchronized void cargarSiEsNecesario() {
        if (certificadoCache != null && llaveCache != null) {
            return;
        }
        if (!hayCertificado()) {
            return;
        }
        try {
            byte[] contenido = leerCertificado(props.getDian().getCertificadoRuta());

            KeyStore ks = KeyStore.getInstance("PKCS12");
            char[] pass = props.getDian().getCertificadoPassword() != null
                    ? props.getDian().getCertificadoPassword().toCharArray()
                    : new char[0];
            ks.load(new ByteArrayInputStream(contenido), pass);

            String alias = props.getDian().getCertificadoAlias();
            if (alias == null || alias.isBlank()) {
                Enumeration<String> aliases = ks.aliases();
                while (aliases.hasMoreElements()) {
                    String a = aliases.nextElement();
                    if (ks.isKeyEntry(a)) {
                        alias = a;
                        break;
                    }
                }
            }
            if (alias == null) {
                throw new IllegalStateException("El certificado no contiene una llave privada.");
            }

            llaveCache = (PrivateKey) ks.getKey(alias, pass);
            certificadoCache = (X509Certificate) ks.getCertificate(alias);

        } catch (Exception e) {
            throw new IllegalStateException("No fue posible cargar el certificado digital: " + e.getMessage(), e);
        }
    }

    private byte[] leerCertificado(String ruta) throws Exception {
        if (ruta.startsWith("classpath:")) {
            String cp = ruta.substring("classpath:".length());
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(cp)) {
                if (is == null) {
                    throw new IllegalStateException("No se encontró el certificado en classpath: " + cp);
                }
                return is.readAllBytes();
            }
        }
        String limpia = ruta.startsWith("file:") ? ruta.substring("file:".length()) : ruta;
        Path p = Paths.get(limpia);
        return Files.readAllBytes(p);
    }

    // ==========================================================
    // FIRMA
    // ==========================================================

    /**
     * Firma el XML de la factura. Si no hay certificado configurado devuelve
     * el XML sin modificar (modo simulación).
     */
    public String firmar(String xml) {
        if (!hayCertificado()) {
            return xml;
        }
        cargarSiEsNecesario();
        try {
            return firmar(xml, llaveCache, certificadoCache);
        } catch (Exception e) {
            throw new IllegalStateException("Error firmando el XML: " + e.getMessage(), e);
        }
    }

    /** Firma el XML con un certificado y llave dados (XAdES-BES enveloped). */
    public String firmar(String xml, PrivateKey llave, X509Certificate certificado) throws Exception {

        Document doc = parsear(xml);

        Element extensionContent = buscarSegundoExtensionContent(doc);
        if (extensionContent == null) {
            throw new IllegalStateException("El XML no tiene el contenedor de firma (UBLExtensions).");
        }

        String idFirma = "xmldsig-" + UUID.randomUUID();
        String idProps = idFirma + "-signedprops";
        String idRef0 = idFirma + "-ref0";

        // ---------- 1. Elemento ds:Signature ----------
        XMLSignature firma = new XMLSignature(doc, "",
                XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256,
                Canonicalizer.ALGO_ID_C14N_OMIT_COMMENTS);

        firma.setId(idFirma);
        extensionContent.appendChild(firma.getElement());

        // ---------- 2. Objeto XAdES (SignedProperties) ----------
        Element objeto = doc.createElementNS(Constants.SignatureSpecNS, "ds:Object");
        firma.getElement().appendChild(objeto);

        Element qualifying = doc.createElementNS(NS_XADES, "xades:QualifyingProperties");
        qualifying.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xades", NS_XADES);
        qualifying.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:ds", NS_DS);
        qualifying.setAttribute("Target", "#" + idFirma);
        objeto.appendChild(qualifying);

        Element signedProperties = crearSignedProperties(doc, certificado, idProps, idRef0);
        qualifying.appendChild(signedProperties);

        // Marca el atributo Id para que Santuario resuelva la referencia "#..."
        try {
            signedProperties.setIdAttributeNS(null, "Id", true);
        } catch (Exception ignored) {
            // algunos parsers no soportan setIdAttributeNS; Santuario aplica heurística
        }

        // ---------- 3. Referencia al documento completo ----------
        Transforms transformsDoc = new Transforms(doc);
        transformsDoc.addTransform(Transforms.TRANSFORM_ENVELOPED_SIGNATURE);
        transformsDoc.addTransform(Canonicalizer.ALGO_ID_C14N_OMIT_COMMENTS);
        firma.addDocument("", transformsDoc, DIGEST_SHA256, idRef0, null);

        // ---------- 4. Referencia a xades:SignedProperties ----------
        Transforms transformsProps = new Transforms(doc);
        transformsProps.addTransform(Canonicalizer.ALGO_ID_C14N_OMIT_COMMENTS);
        firma.addDocument("#" + idProps, transformsProps, DIGEST_SHA256,
                null, TIPO_REFERENCIA_SIGNED_PROPS);

        // ---------- 5. Certificado ----------
        firma.addKeyInfo(certificado);

        // ---------- 6. Firma ----------
        firma.sign(llave);

        return serializar(doc);
    }

    // ==========================================================
    // XAdES SignedProperties
    // ==========================================================
    private Element crearSignedProperties(Document doc, X509Certificate certificado,
                                          String idProps, String idRef0) throws Exception {

        Element signedProperties = doc.createElementNS(NS_XADES, "xades:SignedProperties");
        signedProperties.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xades", NS_XADES);
        signedProperties.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:ds", NS_DS);
        signedProperties.setAttribute("Id", idProps);

        // --- SignedSignatureProperties ---
        Element ssp = doc.createElementNS(NS_XADES, "xades:SignedSignatureProperties");

        Element signingTime = doc.createElementNS(NS_XADES, "xades:SigningTime");
        signingTime.setTextContent(OffsetDateTime.now(ZoneOffset.ofHours(-5)).toString());
        ssp.appendChild(signingTime);

        Element signingCertificate = doc.createElementNS(NS_XADES, "xades:SigningCertificate");
        Element cert = doc.createElementNS(NS_XADES, "xades:Cert");

        Element certDigest = doc.createElementNS(NS_XADES, "xades:CertDigest");
        Element digestMethod = doc.createElementNS(NS_DS, "ds:DigestMethod");
        digestMethod.setAttribute("Algorithm", DIGEST_SHA256);
        Element digestValue = doc.createElementNS(NS_DS, "ds:DigestValue");
        digestValue.setTextContent(cufeService.sha256Base64(certificado.getEncoded()));
        certDigest.appendChild(digestMethod);
        certDigest.appendChild(digestValue);
        cert.appendChild(certDigest);

        Element issuerSerial = doc.createElementNS(NS_XADES, "xades:IssuerSerial");
        Element issuerName = doc.createElementNS(NS_DS, "ds:X509IssuerName");
        issuerName.setTextContent(certificado.getIssuerX500Principal().getName());
        Element serialNumber = doc.createElementNS(NS_DS, "ds:X509SerialNumber");
        serialNumber.setTextContent(certificado.getSerialNumber().toString());
        issuerSerial.appendChild(issuerName);
        issuerSerial.appendChild(serialNumber);
        cert.appendChild(issuerSerial);

        signingCertificate.appendChild(cert);
        ssp.appendChild(signingCertificate);

        Element policy = doc.createElementNS(NS_XADES, "xades:SignaturePolicyIdentifier");
        policy.appendChild(doc.createElementNS(NS_XADES, "xades:SignaturePolicyImplied"));
        ssp.appendChild(policy);

        signedProperties.appendChild(ssp);

        // --- SignedDataObjectProperties ---
        Element sdo = doc.createElementNS(NS_XADES, "xades:SignedDataObjectProperties");
        Element dataObjectFormat = doc.createElementNS(NS_XADES, "xades:DataObjectFormat");
        dataObjectFormat.setAttribute("ObjectReference", "#" + idRef0);

        Element descripcion = doc.createElementNS(NS_XADES, "xades:Description");
        descripcion.setTextContent("Factura electrónica de venta");
        Element mimeType = doc.createElementNS(NS_XADES, "xades:MimeType");
        mimeType.setTextContent("text/xml");
        Element encoding = doc.createElementNS(NS_XADES, "xades:Encoding");
        encoding.setTextContent("UTF-8");

        dataObjectFormat.appendChild(descripcion);
        dataObjectFormat.appendChild(mimeType);
        dataObjectFormat.appendChild(encoding);
        sdo.appendChild(dataObjectFormat);
        signedProperties.appendChild(sdo);

        return signedProperties;
    }

    // ==========================================================
    // UTILIDADES XML
    // ==========================================================
    private Document parsear(String xml) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        DocumentBuilder builder = dbf.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(xml)));
    }

    private Element buscarSegundoExtensionContent(Document doc) {
        NodeList lista = doc.getElementsByTagNameNS(NS_EXT, "ExtensionContent");
        if (lista.getLength() >= 2) {
            return (Element) lista.item(1);
        }
        if (lista.getLength() == 1) {
            return (Element) lista.item(0);
        }
        return null;
    }

    private String serializar(Document doc) throws Exception {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.INDENT, "no");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        String salida = writer.toString();
        if (!salida.contains("<?xml")) {
            salida = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" + salida;
        }
        return salida.replace("\r\n", "\n").trim();
    }

    /** Indica si un XML ya viene firmado. */
    public boolean estaFirmado(String xml) {
        if (xml == null) {
            return false;
        }
        try {
            Document doc = parsear(xml);
            return doc.getElementsByTagNameNS(NS_DS, "Signature").getLength() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    /** Extrae el bloque ds:Signature (útil para mostrar la huella en la vista). */
    public String bloqueFirma(String xml) {
        if (xml == null) {
            return null;
        }
        int inicio = xml.indexOf("<ds:Signature");
        int fin = xml.lastIndexOf("</ds:Signature>");
        if (inicio >= 0 && fin > inicio) {
            return xml.substring(inicio, fin + "</ds:Signature>".length());
        }
        return null;
    }

    /** Nodo raíz del XML. */
    public Node raiz(String xml) throws Exception {
        return parsear(xml).getDocumentElement();
    }

    /** Bytes UTF-8 del XML. */
    public byte[] bytes(String xml) {
        return xml.getBytes(StandardCharsets.UTF_8);
    }
}
