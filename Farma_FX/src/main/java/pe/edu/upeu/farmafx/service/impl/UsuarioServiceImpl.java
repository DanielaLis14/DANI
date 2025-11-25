package pe.edu.upeu.farmafx.service.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.farmafx.exception.ModelNotFoundException;
import pe.edu.upeu.farmafx.model.Perfil;
import pe.edu.upeu.farmafx.model.Usuario;
import pe.edu.upeu.farmafx.repository.ICrudGenericRepository;
import pe.edu.upeu.farmafx.repository.UsuarioRepository;
import pe.edu.upeu.farmafx.service.IPerfilService;
import pe.edu.upeu.farmafx.service.IUsuarioService;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl extends CrudGenericServiceImpl<Usuario, Long> implements IUsuarioService {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioServiceImpl.class);

    private final UsuarioRepository usuarioRepository;
    private final IPerfilService perfilService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    protected ICrudGenericRepository<Usuario, Long> getRepository() {
        return usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario loginUsuario(String username, String password) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ModelNotFoundException("Credenciales inválidas"));

        if (!passwordEncoder.matches(password, usuario.getPasswordHash())) {
            throw new ModelNotFoundException("Credenciales inválidas");
        }

        return usuario;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeUsername(String username) {
        return usuarioRepository.existsByUsername(username);
    }

    @Override
    @Transactional
    public Usuario registrarCliente(String username, String password,
                                    String nombreCompleto, String email, String telefono) {
        logger.debug("Iniciando registro de cliente con username: {}", username);

        if (existeUsername(username)) {
            logger.warn("Intento de registro con username duplicado: {}", username);
            throw new IllegalArgumentException("El nombre de usuario ya existe");
        }

        Perfil perfilCliente = perfilService.findAll().stream()
                .filter(p -> "Cliente".equalsIgnoreCase(p.getNombrePerfil()))
                .findFirst()
                .orElseThrow(() -> {
                    logger.error("Perfil Cliente no encontrado en la base de datos");
                    return new RuntimeException("Perfil Cliente no encontrado en el sistema");
                });

        String hash = passwordEncoder.encode(password);
        logger.debug("Contraseña encriptada para username: {}", username);

        Usuario usuario = Usuario.builder()
                .username(username)
                .passwordHash(hash)
                .nombreCompleto(nombreCompleto)
                .emailUsuario(email != null && !email.isBlank() ? email : null)
                .estadoUsuario(true)
                .perfil(perfilCliente)
                .build();

        Usuario guardado = usuarioRepository.save(usuario);
        logger.info("Cliente registrado exitosamente - Username: {}, ID: {}, Perfil: {}",
                username, guardado.getIdUsuario(), perfilCliente.getNombrePerfil());

        return guardado;
    }
}
