package com.gestionfuncionarios.vista;

import com.gestionfuncionarios.seguridad.ControlAcceso;
import com.gestionfuncionarios.seguridad.PasswordHasher;
import com.gestionfuncionarios.seguridad.ServicioAutenticacion;
import com.gestionfuncionarios.seguridad.ServicioAutenticacion.ResultadoAutenticacion;
import com.gestionfuncionarios.seguridad.SesionUsuario;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Arrays;

/**
 * Ventana grafica de inicio de sesion (Login) para el Sistema de Gestion de Funcionarios.
 * 
 * Principios de seguridad aplicados:
 * - Uso de JPasswordField y manejo de contraseñas mediante char[] con borrado de memoria.
 * - Validacion de campos vacios antes del envio.
 * - Mensajes neutrales ante fallos de credenciales (evita enumeracion de usuarios).
 * - Manejo de cuentas inactivas.
 * - Inicio seguro de la ventana principal condicionada al rol autenticado.
 */
public class VentanaLogin extends JFrame {

    private final ServicioAutenticacion servicioAuth;

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnIniciarSesion;
    private JButton btnSalir;
    private JLabel lblMensajeEstado;

    public VentanaLogin() {
        this(new ServicioAutenticacion());
    }

    public VentanaLogin(ServicioAutenticacion servicioAuth) {
        this.servicioAuth = servicioAuth;
        configurarLook();
        inicializarComponentes();
    }

    private void configurarLook() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored2) {
            }
        }
    }

    private void inicializarComponentes() {
        setTitle("Acceso al Sistema - Gestion de Funcionarios");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(460, 480);
        setResizable(false);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 1. Panel Superior: Encabezado Institucional
        JPanel panelEncabezado = new JPanel(new BorderLayout());
        panelEncabezado.setBackground(new Color(24, 43, 73));
        panelEncabezado.setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel lblTitulo = new JLabel("INICIO DE SESIÓN", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(Color.WHITE);

        JLabel lblSubtitulo = new JLabel("Control de Acceso y Autenticación Segura", SwingConstants.CENTER);
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitulo.setForeground(new Color(190, 210, 240));

        panelEncabezado.add(lblTitulo, BorderLayout.NORTH);
        panelEncabezado.add(Box.createVerticalStrut(4), BorderLayout.CENTER);
        panelEncabezado.add(lblSubtitulo, BorderLayout.SOUTH);
        add(panelEncabezado, BorderLayout.NORTH);

        // 2. Panel Central: Formulario de Credenciales
        JPanel panelFormulario = new JPanel();
        panelFormulario.setLayout(new BoxLayout(panelFormulario, BoxLayout.Y_AXIS));
        panelFormulario.setBorder(new EmptyBorder(25, 40, 15, 40));
        panelFormulario.setBackground(new Color(248, 249, 252));

        // Etiqueta y Campo Usuario
        JLabel lblUser = new JLabel("Nombre de Usuario:");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblUser.setForeground(new Color(40, 50, 70));
        lblUser.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtUsername = new JTextField();
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtUsername.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        txtUsername.setPreferredSize(new Dimension(360, 36));
        txtUsername.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Etiqueta y Campo Contraseña
        JLabel lblPass = new JLabel("Contraseña:");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPass.setForeground(new Color(40, 50, 70));
        lblPass.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtPassword = new JPasswordField();
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        txtPassword.setPreferredSize(new Dimension(360, 36));
        txtPassword.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Mensaje de estado en vivo
        lblMensajeEstado = new JLabel(" ");
        lblMensajeEstado.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblMensajeEstado.setForeground(new Color(180, 40, 40));
        lblMensajeEstado.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Accion al presionar ENTER en los campos de texto
        KeyAdapter enterListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    procesarLogin();
                }
            }
        };
        txtUsername.addKeyListener(enterListener);
        txtPassword.addKeyListener(enterListener);

        panelFormulario.add(lblUser);
        panelFormulario.add(Box.createVerticalStrut(6));
        panelFormulario.add(txtUsername);
        panelFormulario.add(Box.createVerticalStrut(14));
        panelFormulario.add(lblPass);
        panelFormulario.add(Box.createVerticalStrut(6));
        panelFormulario.add(txtPassword);
        panelFormulario.add(Box.createVerticalStrut(10));
        panelFormulario.add(lblMensajeEstado);

        add(panelFormulario, BorderLayout.CENTER);

        // 3. Panel Inferior: Botones de Accion y Pie
        JPanel panelInferior = new JPanel();
        panelInferior.setLayout(new BoxLayout(panelInferior, BoxLayout.Y_AXIS));
        panelInferior.setBorder(new EmptyBorder(0, 40, 20, 40));
        panelInferior.setBackground(new Color(248, 249, 252));

        btnIniciarSesion = new JButton("Iniciar Sesión");
        btnIniciarSesion.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnIniciarSesion.setBackground(new Color(24, 76, 140));
        btnIniciarSesion.setForeground(Color.WHITE);
        btnIniciarSesion.setFocusPainted(false);
        btnIniciarSesion.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnIniciarSesion.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnIniciarSesion.addActionListener(e -> procesarLogin());

        btnSalir = new JButton("Salir");
        btnSalir.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnSalir.setBackground(new Color(230, 235, 242));
        btnSalir.setForeground(new Color(50, 60, 80));
        btnSalir.setFocusPainted(false);
        btnSalir.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        btnSalir.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSalir.addActionListener(e -> System.exit(0));

        JLabel lblInfoSeguridad = new JLabel("Protección de contraseñas con BCrypt & RBAC", SwingConstants.CENTER);
        lblInfoSeguridad.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        lblInfoSeguridad.setForeground(Color.GRAY);
        lblInfoSeguridad.setAlignmentX(Component.CENTER_ALIGNMENT);

        panelInferior.add(btnIniciarSesion);
        panelInferior.add(Box.createVerticalStrut(8));
        panelInferior.add(btnSalir);
        panelInferior.add(Box.createVerticalStrut(12));
        panelInferior.add(lblInfoSeguridad);

        add(panelInferior, BorderLayout.SOUTH);

        // Establecer el boton de inicio de sesion por defecto
        getRootPane().setDefaultButton(btnIniciarSesion);
    }

    private void procesarLogin() {
        lblMensajeEstado.setText(" ");
        String username = txtUsername.getText();
        char[] passwordChars = txtPassword.getPassword();

        if (username == null || username.trim().isEmpty()) {
            mostrarErrorValidacion("Por favor ingrese su nombre de usuario.");
            txtUsername.requestFocus();
            Arrays.fill(passwordChars, '\0');
            return;
        }

        if (passwordChars == null || passwordChars.length == 0) {
            mostrarErrorValidacion("Por favor ingrese su contraseña.");
            txtPassword.requestFocus();
            return;
        }

        // Bloqueo temporal de controles mientras autentica
        btnIniciarSesion.setEnabled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        try {
            ResultadoAutenticacion resultado = servicioAuth.autenticar(username.trim(), passwordChars);

            if (resultado.isExitoso()) {
                // Autenticacion correcta: cerrar login y abrir ventana principal
                this.dispose();

                SwingUtilities.invokeLater(() -> {
                    try {
                        VentanaFuncionarios principal = new VentanaFuncionarios();
                        principal.setVisible(true);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null,
                                "Error critico al cargar la ventana principal: " + ex.getMessage(),
                                "Error de Aplicacion", JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();
                    }
                });

            } else {
                mostrarErrorValidacion(resultado.getMensaje());
                txtPassword.setText("");
                txtPassword.requestFocus();
            }

        } finally {
            btnIniciarSesion.setEnabled(true);
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private void mostrarErrorValidacion(String mensaje) {
        lblMensajeEstado.setText(mensaje);
        JOptionPane.showMessageDialog(this, mensaje, "Aviso de Autenticación", JOptionPane.WARNING_MESSAGE);
    }
}

