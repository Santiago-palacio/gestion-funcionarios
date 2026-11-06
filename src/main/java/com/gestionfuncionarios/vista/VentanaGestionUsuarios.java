package com.gestionfuncionarios.vista;

import com.gestionfuncionarios.modelo.Rol;
import com.gestionfuncionarios.modelo.Usuario;
import com.gestionfuncionarios.seguridad.AccesoDenegadoException;
import com.gestionfuncionarios.seguridad.ControlAcceso;
import com.gestionfuncionarios.seguridad.ServicioUsuario;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Ventana de Gestion de Usuarios del Sistema (Java Swing).
 * Exclusiva para operadores con rol ADMINISTRADOR.
 * 
 * Permite:
 * - Crear nuevos usuarios con contraseñas cifradas en BCrypt.
 * - Modificar nombre de usuario, rol y estado.
 * - Activar y desactivar usuarios con proteccion del ultimo administrador.
 * - Restablecer contraseñas de forma segura.
 */
public class VentanaGestionUsuarios extends JDialog {

    private final ServicioUsuario servicioUsuario;

    // Componentes del Formulario
    private JTextField txtId;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JComboBox<Rol> cbRol;
    private JComboBox<String> cbEstado;

    // Botones de Accion
    private JButton btnCrear;
    private JButton btnActualizar;
    private JButton btnCambiarEstado;
    private JButton btnRestablecerPassword;
    private JButton btnLimpiar;
    private JButton btnCerrar;

    // Tabla de Usuarios
    private JTable tblUsuarios;
    private DefaultTableModel modeloTabla;
    private JLabel lblTotalUsuarios;

    public VentanaGestionUsuarios(Frame parent) {
        super(parent, "Gestión de Usuarios y Control de Acceso (RBAC)", true);
        this.servicioUsuario = new ServicioUsuario();

        // Verificacion estricta de autorizacion en la logica antes de abrir
        ControlAcceso.validarPermisoEscritura("Acceder a Gestion de Usuarios");

        inicializarComponentes();
        cargarTablaUsuarios();
    }

    private void inicializarComponentes() {
        setSize(850, 620);
        setMinimumSize(new Dimension(750, 520));
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout(5, 5));

        // 1. Encabezado institucional
        add(crearPanelEncabezado(), BorderLayout.NORTH);

        // 2. Panel Central dividido: Formulario a la izquierda, Tabla a la derecha
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                crearPanelFormulario(),
                crearPanelTabla());
        split.setDividerLocation(340);
        split.setContinuousLayout(true);
        add(split, BorderLayout.CENTER);

        // 3. Barra inferior de estado
        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.setBorder(new EmptyBorder(6, 15, 6, 15));
        panelInferior.setBackground(new Color(238, 241, 246));

        lblTotalUsuarios = new JLabel("Cargando usuarios...");
        lblTotalUsuarios.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panelInferior.add(lblTotalUsuarios, BorderLayout.WEST);

        btnCerrar = new JButton("Cerrar");
        btnCerrar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnCerrar.addActionListener(e -> dispose());
        panelInferior.add(btnCerrar, BorderLayout.EAST);

        add(panelInferior, BorderLayout.SOUTH);
    }

    private JPanel crearPanelEncabezado() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(24, 43, 73));
        panel.setBorder(new EmptyBorder(12, 20, 12, 20));

        JLabel lblTitulo = new JLabel("GESTIÓN Y ADMINISTRACIÓN DE USUARIOS");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("Control de Cuentas, Asignación de Roles y Restablecimiento de Credenciales");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSub.setForeground(new Color(190, 210, 240));

        panel.add(lblTitulo, BorderLayout.NORTH);
        panel.add(lblSub, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelFormulario() {
        JPanel panelContenedor = new JPanel(new BorderLayout(5, 5));
        panelContenedor.setBorder(new CompoundBorder(
                new EmptyBorder(8, 8, 8, 4),
                new TitledBorder(BorderFactory.createLineBorder(new Color(180, 190, 205)), "Datos de la Cuenta")
        ));

        JPanel panelCampos = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 6, 5, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Fila 0: ID Usuario
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.35;
        panelCampos.add(new JLabel("ID Usuario:"), gbc);
        txtId = new JTextField();
        txtId.setEditable(false);
        txtId.setBackground(new Color(240, 240, 240));
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.65;
        panelCampos.add(txtId, gbc);

        // Fila 1: Username
        gbc.gridx = 0; gbc.gridy = 1;
        panelCampos.add(new JLabel("Usuario *:"), gbc);
        txtUsername = new JTextField();
        gbc.gridx = 1; gbc.gridy = 1;
        panelCampos.add(txtUsername, gbc);

        // Fila 2: Contraseña (inicial)
        gbc.gridx = 0; gbc.gridy = 2;
        panelCampos.add(new JLabel("Contraseña *:"), gbc);
        txtPassword = new JPasswordField();
        gbc.gridx = 1; gbc.gridy = 2;
        panelCampos.add(txtPassword, gbc);

        // Fila 3: Rol
        gbc.gridx = 0; gbc.gridy = 3;
        panelCampos.add(new JLabel("Rol Asignado *:"), gbc);
        cbRol = new JComboBox<>(new Rol[]{Rol.ADMINISTRADOR, Rol.CONSULTA});
        gbc.gridx = 1; gbc.gridy = 3;
        panelCampos.add(cbRol, gbc);

        // Fila 4: Estado
        gbc.gridx = 0; gbc.gridy = 4;
        panelCampos.add(new JLabel("Estado:"), gbc);
        cbEstado = new JComboBox<>(new String[]{"Activo", "Inactivo"});
        gbc.gridx = 1; gbc.gridy = 4;
        panelCampos.add(cbEstado, gbc);

        // Panel de Botones
        JPanel panelBotones = new JPanel(new GridLayout(5, 1, 6, 6));
        panelBotones.setBorder(new EmptyBorder(10, 8, 8, 8));

        btnCrear = new JButton("Crear Usuario");
        btnCrear.setBackground(new Color(33, 115, 70));
        btnCrear.setForeground(Color.WHITE);
        btnCrear.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCrear.addActionListener(e -> accionCrearUsuario());

        btnActualizar = new JButton("Guardar Cambios");
        btnActualizar.setBackground(new Color(30, 85, 150));
        btnActualizar.setForeground(Color.WHITE);
        btnActualizar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnActualizar.setEnabled(false);
        btnActualizar.addActionListener(e -> accionActualizarUsuario());

        btnCambiarEstado = new JButton("Activar / Desactivar");
        btnCambiarEstado.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnCambiarEstado.setEnabled(false);
        btnCambiarEstado.addActionListener(e -> accionAlternarEstado());

        btnRestablecerPassword = new JButton("Restablecer Contraseña");
        btnRestablecerPassword.setBackground(new Color(190, 110, 20));
        btnRestablecerPassword.setForeground(Color.WHITE);
        btnRestablecerPassword.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnRestablecerPassword.setEnabled(false);
        btnRestablecerPassword.addActionListener(e -> accionRestablecerPassword());

        btnLimpiar = new JButton("Limpiar / Nuevo");
        btnLimpiar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnLimpiar.addActionListener(e -> limpiarFormulario());

        panelBotones.add(btnCrear);
        panelBotones.add(btnActualizar);
        panelBotones.add(btnCambiarEstado);
        panelBotones.add(btnRestablecerPassword);
        panelBotones.add(btnLimpiar);

        panelContenedor.add(new JScrollPane(panelCampos), BorderLayout.CENTER);
        panelContenedor.add(panelBotones, BorderLayout.SOUTH);

        return panelContenedor;
    }

    private JPanel crearPanelTabla() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(new CompoundBorder(
                new EmptyBorder(8, 4, 8, 8),
                new TitledBorder(BorderFactory.createLineBorder(new Color(180, 190, 205)), "Usuarios Registrados")
        ));

        String[] cols = {"ID", "Usuario", "Rol", "Estado", "Fecha Creación"};
        modeloTabla = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblUsuarios = new JTable(modeloTabla);
        tblUsuarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblUsuarios.setAutoCreateRowSorter(true);

        // Renderer personalizado para columna de Estado (verde si activo, rojo si inactivo)
        tblUsuarios.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if ("Activo".equals(value)) {
                    c.setForeground(new Color(25, 115, 50));
                    setFont(getFont().deriveFont(Font.BOLD));
                } else {
                    c.setForeground(new Color(180, 40, 40));
                    setFont(getFont().deriveFont(Font.BOLD));
                }
                return c;
            }
        });

        tblUsuarios.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionarFilaUsuario();
            }
        });

        panel.add(new JScrollPane(tblUsuarios), BorderLayout.CENTER);
        return panel;
    }

    private void cargarTablaUsuarios() {
        modeloTabla.setRowCount(0);
        try {
            List<Usuario> lista = servicioUsuario.listarUsuarios();
            for (Usuario u : lista) {
                modeloTabla.addRow(new Object[]{
                        u.getIdUsuario(),
                        u.getUsername(),
                        (u.getRol() != null ? u.getRol().getNombre() : "SIN ROL"),
                        (u.isActivo() ? "Activo" : "Inactivo"),
                        (u.getFechaCreacion() != null ? u.getFechaCreacion().toString() : "N/D")
                });
            }
            lblTotalUsuarios.setText("Total usuarios registrados: " + lista.size());
        } catch (AccesoDenegadoException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Acceso Denegado", JOptionPane.WARNING_MESSAGE);
            dispose();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al consultar usuarios: " + e.getMessage(),
                    "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void seleccionarFilaUsuario() {
        int fila = tblUsuarios.getSelectedRow();
        if (fila == -1) {
            btnActualizar.setEnabled(false);
            btnCambiarEstado.setEnabled(false);
            btnRestablecerPassword.setEnabled(false);
            btnCrear.setEnabled(true);
            txtPassword.setEnabled(true);
            return;
        }

        int filaModelo = tblUsuarios.convertRowIndexToModel(fila);
        Integer id = (Integer) modeloTabla.getValueAt(filaModelo, 0);
        String username = String.valueOf(modeloTabla.getValueAt(filaModelo, 1));
        String rolStr = String.valueOf(modeloTabla.getValueAt(filaModelo, 2));
        String estadoStr = String.valueOf(modeloTabla.getValueAt(filaModelo, 3));

        txtId.setText(id != null ? id.toString() : "");
        txtUsername.setText(username);
        txtPassword.setText("");
        txtPassword.setEnabled(false); // La contraseña se cambia con el boton dedicado de restablecer

        if ("ADMINISTRADOR".equalsIgnoreCase(rolStr)) {
            cbRol.setSelectedItem(Rol.ADMINISTRADOR);
        } else {
            cbRol.setSelectedItem(Rol.CONSULTA);
        }

        cbEstado.setSelectedItem(estadoStr);

        btnCrear.setEnabled(false);
        btnActualizar.setEnabled(true);
        btnCambiarEstado.setEnabled(true);
        btnRestablecerPassword.setEnabled(true);

        if ("Activo".equals(estadoStr)) {
            btnCambiarEstado.setText("Desactivar Cuenta");
            btnCambiarEstado.setBackground(new Color(240, 220, 220));
        } else {
            btnCambiarEstado.setText("Activar Cuenta");
            btnCambiarEstado.setBackground(new Color(220, 240, 220));
        }
    }

    private void accionCrearUsuario() {
        String username = txtUsername.getText();
        char[] passChars = txtPassword.getPassword();
        Rol rol = (Rol) cbRol.getSelectedItem();

        if (passChars == null || passChars.length == 0) {
            JOptionPane.showMessageDialog(this, "Debe ingresar una contraseña para el nuevo usuario.",
                    "Campo Requerido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String password = new String(passChars);

        try {
            Usuario nuevo = servicioUsuario.crearUsuario(username, password, rol);
            JOptionPane.showMessageDialog(this,
                    "Usuario '" + nuevo.getUsername() + "' registrado exitosamente con rol: " + nuevo.getRol(),
                    "Registro Exitoso", JOptionPane.INFORMATION_MESSAGE);
            limpiarFormulario();
            cargarTablaUsuarios();

        } catch (AccesoDenegadoException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Acceso Denegado", JOptionPane.WARNING_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Validación", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error de base de datos: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void accionActualizarUsuario() {
        String idStr = txtId.getText();
        if (idStr == null || idStr.isBlank()) return;

        int idUsuario = Integer.parseInt(idStr);
        String username = txtUsername.getText();
        Rol rol = (Rol) cbRol.getSelectedItem();
        boolean activo = "Activo".equals(cbEstado.getSelectedItem());

        try {
            servicioUsuario.actualizarUsuario(idUsuario, username, rol, activo);
            JOptionPane.showMessageDialog(this, "Información del usuario actualizada exitosamente.",
                    "Actualización Exitosa", JOptionPane.INFORMATION_MESSAGE);
            cargarTablaUsuarios();

        } catch (AccesoDenegadoException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Acceso Denegado / Seguridad", JOptionPane.WARNING_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Validación", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error de base de datos: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void accionAlternarEstado() {
        String idStr = txtId.getText();
        if (idStr == null || idStr.isBlank()) return;

        int idUsuario = Integer.parseInt(idStr);
        String estadoActual = (String) cbEstado.getSelectedItem();
        boolean nuevoEstado = !"Activo".equals(estadoActual);
        String accionTexto = nuevoEstado ? "activar" : "desactivar";

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de que desea " + accionTexto + " la cuenta de '" + txtUsername.getText() + "'?",
                "Confirmar Cambio de Estado", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                servicioUsuario.cambiarEstado(idUsuario, nuevoEstado);
                JOptionPane.showMessageDialog(this,
                        "La cuenta ha sido " + (nuevoEstado ? "activada" : "desactivada") + " correctamente.",
                        "Estado Actualizado", JOptionPane.INFORMATION_MESSAGE);
                cargarTablaUsuarios();
                limpiarFormulario();

            } catch (AccesoDenegadoException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Operación Bloqueada por Seguridad",
                        JOptionPane.WARNING_MESSAGE);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error al actualizar estado: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void accionRestablecerPassword() {
        String idStr = txtId.getText();
        if (idStr == null || idStr.isBlank()) return;

        int idUsuario = Integer.parseInt(idStr);
        String username = txtUsername.getText();

        JPasswordField pfNueva = new JPasswordField();
        JPasswordField pfConfirmar = new JPasswordField();
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.add(new JLabel("Nueva Contraseña:"));
        panel.add(pfNueva);
        panel.add(new JLabel("Confirmar Contraseña:"));
        panel.add(pfConfirmar);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Restablecer Contraseña para: " + username,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String p1 = new String(pfNueva.getPassword());
            String p2 = new String(pfConfirmar.getPassword());

            if (!p1.equals(p2)) {
                JOptionPane.showMessageDialog(this, "Las contraseñas no coinciden.",
                        "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                servicioUsuario.restablecerPassword(idUsuario, p1);
                JOptionPane.showMessageDialog(this,
                        "Contraseña restablecida exitosamente para el usuario '" + username + "'.",
                        "Contraseña Actualizada", JOptionPane.INFORMATION_MESSAGE);

            } catch (AccesoDenegadoException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Acceso Denegado", JOptionPane.WARNING_MESSAGE);
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Validación", JOptionPane.WARNING_MESSAGE);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error al actualizar contraseña: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void limpiarFormulario() {
        txtId.setText("");
        txtUsername.setText("");
        txtPassword.setText("");
        txtPassword.setEnabled(true);
        cbRol.setSelectedIndex(0);
        cbEstado.setSelectedIndex(0);

        tblUsuarios.clearSelection();
        btnCrear.setEnabled(true);
        btnActualizar.setEnabled(false);
        btnCambiarEstado.setEnabled(false);
        btnRestablecerPassword.setEnabled(false);
    }
}

