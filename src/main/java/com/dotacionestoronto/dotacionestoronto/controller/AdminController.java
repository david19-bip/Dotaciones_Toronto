package com.dotacionestoronto.dotacionestoronto.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.dotacionestoronto.dotacionestoronto.entity.Usuario;
import com.dotacionestoronto.dotacionestoronto.entity.UsuarioRol;
import com.dotacionestoronto.dotacionestoronto.repository.DevolucionRepository;
import com.dotacionestoronto.dotacionestoronto.repository.PedidoRepository;
import com.dotacionestoronto.dotacionestoronto.repository.ProductoRepository;
import com.dotacionestoronto.dotacionestoronto.repository.UsuarioRepository;
import com.dotacionestoronto.dotacionestoronto.repository.UsuarioRolRepository;
import com.dotacionestoronto.dotacionestoronto.service.ReporteUsuariosExcelService;
import com.dotacionestoronto.dotacionestoronto.service.ReporteUsuariosService;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioRolRepository usuarioRolRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private DevolucionRepository devolucionRepository;

    @Autowired
    private ReporteUsuariosService reporteUsuariosService;

    @Autowired
    private ReporteUsuariosExcelService reporteUsuariosExcelService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private HttpSession session;

    // =========================================================
    // PANEL ADMINISTRADOR
    // =========================================================

    @GetMapping("/panel")
    public String panel(Model model) {

        Usuario usuarioActual = getUsuarioActual();

        if (usuarioActual == null) {
            return "redirect:/login";
        }

        model.addAttribute(
                "nombre",
                usuarioActual.getNombre()
        );

        // Tarjetas de resumen del panel (antes llegaban vacías)
        model.addAttribute("totalUsuarios", usuarioRepository.count());
        model.addAttribute("totalProductos", productoRepository.count());
        model.addAttribute("totalPedidos", pedidoRepository.count());
        model.addAttribute("totalDevoluciones", devolucionRepository.count());

        return "admin/panel_admin";
    }

    // =========================================================
    // USUARIOS
    // =========================================================

    @GetMapping("/usuarios")
    public String usuarios(Model model) {

        cargarUsuariosParaVista(model);

        Usuario usuarioActual = getUsuarioActual();

        if (usuarioActual != null) {
            model.addAttribute(
                    "nombre",
                    usuarioActual.getNombre()
            );
        }

        return "admin/usuarios_admin";
    }

    // =========================================================
    // CREAR USUARIO
    // =========================================================

    @Transactional
    @PostMapping("/usuarios/crear")
    public String crearUsuario(
            @RequestParam String nombre,
            @RequestParam String documento,
            @RequestParam String correo,
            @RequestParam String telefono,
            @RequestParam String direccion,
            @RequestParam String password,
            @RequestParam Integer rol,
            Model model) {

        if (usuarioRepository.findByCorreo(correo) != null) {

            model.addAttribute(
                    "error",
                    "Ya existe un usuario con ese correo."
            );

            cargarUsuariosParaVista(model);

            Usuario usuarioActual = getUsuarioActual();

            if (usuarioActual != null) {
                model.addAttribute(
                        "nombre",
                        usuarioActual.getNombre()
                );
            }

            return "admin/usuarios_admin";
        }

        if (usuarioRepository.findByDocumento(documento) != null) {

            model.addAttribute(
                    "error",
                    "Ya existe un usuario con ese documento."
            );

            cargarUsuariosParaVista(model);

            Usuario usuarioActual = getUsuarioActual();

            if (usuarioActual != null) {
                model.addAttribute(
                        "nombre",
                        usuarioActual.getNombre()
                );
            }

            return "admin/usuarios_admin";
        }

        Usuario usuario = new Usuario();

        usuario.setNombre(nombre);
        usuario.setDocumento(documento);
        usuario.setCorreo(correo);
        usuario.setTelefono(telefono);
        usuario.setDireccion(direccion);
        usuario.setContrasena(
                passwordEncoder.encode(password)
        );
        usuario.setIdRol(rol);
        usuario.setEstado("Habilitado");

        usuarioRepository.save(usuario);

        // Guardar también el rol en la tabla de múltiples roles
        UsuarioRol usuarioRol =
                new UsuarioRol(
                        usuario.getId(),
                        rol
                );

        usuarioRolRepository.save(usuarioRol);

        return "redirect:/admin/usuarios";
    }

    // =========================================================
    // EDITAR USUARIO
    // =========================================================

    @GetMapping("/usuarios/editar")
    public String editarUsuarioForm(
            @RequestParam Long id,
            Model model) {

        Usuario usuario =
                usuarioRepository
                        .findById(id)
                        .orElse(null);

        if (usuario == null) {
            return "redirect:/admin/usuarios";
        }

        List<UsuarioRol> rolesUsuario =
                usuarioRolRepository
                        .findByIdUsuario(usuario.getId());

        List<Integer> rolesAsignados =
                rolesUsuario.stream()
                        .map(UsuarioRol::getIdRol)
                        .sorted()
                        .toList();

        // Si todavía no tiene registros en usuario_rol,
        // usamos el rol antiguo de usuario.
        if (rolesAsignados.isEmpty()
                && usuario.getIdRol() != null) {

            rolesAsignados =
                    List.of(usuario.getIdRol());
        }

        Map<Integer, String> rolesDisponibles =
                new LinkedHashMap<>();

        rolesDisponibles.put(
                1,
                "Administrador"
        );

        rolesDisponibles.put(
                2,
                "Cliente"
        );

        rolesDisponibles.put(
                3,
                "Vendedor"
        );

        rolesDisponibles.put(
                4,
                "Bodeguero"
        );

        model.addAttribute(
                "usuario",
                usuario
        );

        model.addAttribute(
                "rolesAsignados",
                rolesAsignados
        );

        model.addAttribute(
                "rolesDisponibles",
                rolesDisponibles
        );

        Usuario usuarioActual =
                getUsuarioActual();

        if (usuarioActual != null) {
            model.addAttribute(
                    "nombre",
                    usuarioActual.getNombre()
            );
        }

        return "admin/editar_usuario_admin";
    }

    // =========================================================
    // ACTUALIZAR USUARIO
    // =========================================================

    @Transactional
    @PostMapping("/usuarios/actualizar")
    public String actualizarUsuarioAdmin(
            @RequestParam Long idUsuario,
            @RequestParam String correo,
            @RequestParam String telefono,
            @RequestParam String direccion,
            @RequestParam(
                    name = "roles",
                    required = false
            )
            List<Integer> roles,
            Model model) {

        Usuario usuario =
                usuarioRepository
                        .findById(idUsuario)
                        .orElse(null);

        if (usuario == null) {
            return "redirect:/admin/usuarios";
        }

        // Debe tener al menos un rol
        if (roles == null || roles.isEmpty()) {

            return "redirect:/admin/usuarios/editar?id="
                    + idUsuario
                    + "&error=rol";
        }

        Usuario administradorActual =
                getUsuarioActual();

        // El administrador actual no puede quitarse
        // el rol de Administrador a sí mismo.
        if (administradorActual != null
                && administradorActual
                        .getId()
                        .equals(idUsuario)
                && !roles.contains(1)) {

            return "redirect:/admin/usuarios/editar?id="
                    + idUsuario
                    + "&error=admin";
        }

        usuario.setCorreo(correo);
        usuario.setTelefono(telefono);
        usuario.setDireccion(direccion);

        // Mantener el primer rol también en el campo
        // antiguo id_rol_fk para compatibilidad.
        usuario.setIdRol(roles.get(0));

        usuarioRepository.save(usuario);

        // Eliminar los roles anteriores
        usuarioRolRepository.deleteByIdUsuario(idUsuario);

        // Guardar todos los nuevos roles
        for (Integer idRol : roles) {

            UsuarioRol usuarioRol =
                    new UsuarioRol(
                            idUsuario,
                            idRol
                    );

            usuarioRolRepository.save(
                    usuarioRol
            );
        }

        return "redirect:/admin/usuarios";
    }

    // =========================================================
    // HABILITAR / INHABILITAR USUARIO
    // =========================================================

    @PostMapping("/usuarios/cambiar-estado")
    public String cambiarEstadoUsuario(
            @RequestParam Long id) {

        Usuario usuario =
                usuarioRepository
                        .findById(id)
                        .orElse(null);

        if (usuario != null) {

            if ("Habilitado".equals(
                    usuario.getEstado())) {

                usuario.setEstado(
                        "Inhabilitado"
                );

            } else {

                usuario.setEstado(
                        "Habilitado"
                );
            }

            usuarioRepository.save(usuario);
        }

        return "redirect:/admin/usuarios";
    }

    // =========================================================
    // CARGAR USUARIOS Y TODOS SUS ROLES
    // =========================================================

    private void cargarUsuariosParaVista(
            Model model) {

        List<Usuario> usuarios =
                usuarioRepository.findAll();

        Map<Long, List<String>>
                nombresRolesPorUsuario =
                new LinkedHashMap<>();

        Map<Long, String>
                rolesFiltroPorUsuario =
                new LinkedHashMap<>();

        for (Usuario usuario : usuarios) {

            List<Integer> idsRoles =
                    usuarioRolRepository
                            .findByIdUsuario(
                                    usuario.getId()
                            )
                            .stream()
                            .map(UsuarioRol::getIdRol)
                            .sorted()
                            .toList();

            // Compatibilidad con usuarios antiguos
            if (idsRoles.isEmpty()
                    && usuario.getIdRol() != null) {

                idsRoles =
                        List.of(
                                usuario.getIdRol()
                        );
            }

            List<String> nombresRoles =
                    idsRoles.stream()
                            .map(
                                    this::obtenerNombreRol
                            )
                            .toList();

            nombresRolesPorUsuario.put(
                    usuario.getId(),
                    nombresRoles
            );

            rolesFiltroPorUsuario.put(
                    usuario.getId(),
                    String.join(
                            ",",
                            nombresRoles
                    )
            );
        }

        model.addAttribute(
                "usuarios",
                usuarios
        );

        model.addAttribute(
                "nombresRolesPorUsuario",
                nombresRolesPorUsuario
        );

        model.addAttribute(
                "rolesFiltroPorUsuario",
                rolesFiltroPorUsuario
        );
    }

    // =========================================================
    // TOGGLE ESTADO (ruta usada por usuarios_admin.html)
    // =========================================================

    @PostMapping("/usuarios/toggleEstado")
    public String toggleEstadoUsuario(
            @RequestParam Long idUsuario,
            RedirectAttributes redirect) {

        Usuario usuario =
                usuarioRepository
                        .findById(idUsuario)
                        .orElse(null);

        if (usuario != null) {

            if ("Habilitado".equals(
                    usuario.getEstado())) {

                usuario.setEstado(
                        "Inhabilitado"
                );

            } else {

                usuario.setEstado(
                        "Habilitado"
                );
            }

            usuarioRepository.save(usuario);

            redirect.addFlashAttribute(
                    "exito",
                    "El usuario "
                            + usuario.getNombre()
                            + " quedó en estado "
                            + usuario.getEstado()
                            + "."
            );
        }

        return "redirect:/admin/usuarios";
    }

    // =========================================================
    // REPORTES DE USUARIOS (PDF y Excel)
    // =========================================================

    @GetMapping("/usuarios/reporte")
    public ResponseEntity<byte[]> reporteUsuarios() {

        byte[] pdf =
                reporteUsuariosService.generarReporte(
                        usuarioRepository.findAll()
                );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename("reporte_usuarios.pdf")
                        .build()
        );

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }

    @GetMapping("/usuarios/reporte/excel")
    public ResponseEntity<byte[]> reporteUsuariosExcel() {

        byte[] excel =
                reporteUsuariosExcelService.generarReporte(
                        usuarioRepository.findAll()
                );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(
                MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                )
        );
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename("reporte_usuarios.xlsx")
                        .build()
        );

        return ResponseEntity.ok()
                .headers(headers)
                .body(excel);
    }

    // =========================================================
    // NOMBRE DEL ROL
    // =========================================================

    private String obtenerNombreRol(
            Integer idRol) {

        if (idRol == null) {
            return "Desconocido";
        }

        return switch (idRol) {

            case 1 ->
                    "Admin";

            case 2 ->
                    "Cliente";

            case 3 ->
                    "Vendedor";

            case 4 ->
                    "Bodeguero";

            default ->
                    "Desconocido";
        };
    }

    // =========================================================
    // OBTENER USUARIO ACTUAL
    // =========================================================

    private Usuario getUsuarioActual() {

        return (Usuario)
                session.getAttribute(
                        "usuarioLogueado"
                );
    }
}