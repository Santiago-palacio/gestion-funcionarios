package com.gestionfuncionarios.vista;

import com.gestionfuncionarios.dao.EstudioDAO;
import com.gestionfuncionarios.dao.EstudioDAOImpl;
import com.gestionfuncionarios.dao.FuncionarioDAO;
import com.gestionfuncionarios.dao.FuncionarioDAOImpl;
import com.gestionfuncionarios.dao.GrupoFamiliarDAO;
import com.gestionfuncionarios.dao.GrupoFamiliarDAOImpl;
import com.gestionfuncionarios.modelo.Estudio;
import com.gestionfuncionarios.modelo.Funcionario;
import com.gestionfuncionarios.modelo.GrupoFamiliar;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Ventana principal de escritorio para la gestion integral de funcionarios.
 * Desarrollada en Swing con separacion de capas mediante el patron DAO.
 */
public class VentanaFuncionarios extends JFrame {

    // DAOs
    private final FuncionarioDAO funcionarioDAO;
    private final GrupoFamiliarDAO familiarDAO;
    private final EstudioDAO estudioDAO;

    // Componentes del Formulario de Funcionarios
    private JTextField txtId;
    private JComboBox<String> cbTipoDoc;
    private JTextField txtNumDoc;
    private JTextField txtNombres;
    private JTextField txtApellidos;
    private JComboBox<String> cbEstadoCivil;
    private JComboBox<String> cbSexo;
    private JTextField txtDireccion;
    private JTextField txtTelefono;
    private JTextField txtFechaNac;

    // Botones del Formulario
    private JButton btnGuardar;
    private JButton btnActualizar;
    private JButton btnEliminar;
    private JButton btnLimpiar;
    private JButton btnBuscar;

    // Tabla de Funcionarios
    private JTable tblFuncionarios;
    private DefaultTableModel modeloTablaFuncionarios;

    // Tablas de Relaciones
    private JTable tblFamiliares;
    private DefaultTableModel modeloTablaFamiliares;
    private JButton btnAgregarFamiliar;
    private JButton btnEliminarFamiliar;

    private JTable tblEstudios;
    private DefaultTableModel modeloTablaEstudios;
    private JButton btnAgregarEstudio;
    private JButton btnEliminarEstudio;

    // Barra de Estado
    private JLabel lblEstado;

    public VentanaFuncionarios() {
        this.funcionarioDAO = new FuncionarioDAOImpl();
        this.familiarDAO = new GrupoFamiliarDAOImpl();
        this.estudioDAO = new EstudioDAOImpl();

        configurarLook();
        inicializarComponentes();
        cargarTablaFuncionarios();
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
            // Si Nimbus no esta disponible, usa el look nativo del sistema
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored2) {
            }
        }
    }

    private void inicializarComponentes() {
        setTitle("Sistema de Gestion de Funcionarios - Organizacion");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1150, 780);
        setMinimumSize(new Dimension(950, 650));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(5, 5));

        // 1. Encabezado institucional
        add(crearPanelEncabezado(), BorderLayout.NORTH);

        // 2. Panel Central dividido: Formulario a la izquierda, Tablas a la derecha
        JSplitPane splitPrincipal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                crearPanelFormulario(),
                crearPanelVisualizacion());
        splitPrincipal.setDividerLocation(390);
        splitPrincipal.setContinuousLayout(true);
        add(splitPrincipal, BorderLayout.CENTER);

        // 3. Barra de Estado
        add(crearBarraEstado(), BorderLayout.SOUTH);
    }

    private JPanel crearPanelEncabezado() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(24, 43, 73));
        panel.setBorder(new EmptyBorder(12, 20, 12, 20));

        JLabel lblTitulo = new JLabel("GESTION INSTITUCIONAL DE FUNCIONARIOS");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(Color.WHITE);

        JLabel lblSubtitulo = new JLabel("Modulo de Administracion y Control - Desarrollo de Software Seguro");
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitulo.setForeground(new Color(190, 210, 240));

        panel.add(lblTitulo, BorderLayout.NORTH);
        panel.add(lblSubtitulo, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelFormulario() {
        JPanel panelContenedor = new JPanel(new BorderLayout(5, 5));
        panelContenedor.setBorder(new CompoundBorder(
                new EmptyBorder(8, 8, 8, 4),
                new TitledBorder(BorderFactory.createLineBorder(new Color(180, 190, 205)), "Datos del Funcionario")
        ));

        JPanel panelCampos = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Fila 0: ID Funcionario (Auto-generado / Solo lectura)
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        panelCampos.add(new JLabel("ID Funcionario:"), gbc);
        txtId = new JTextField();
        txtId.setEditable(false);
        txtId.setBackground(new Color(240, 240, 240));
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.7;
        panelCampos.add(txtId, gbc);

        // Fila 1: Tipo Documento
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        panelCampos.add(new JLabel("Tipo Documento *:"), gbc);
        cbTipoDoc = new JComboBox<>(new String[]{"CC", "TI", "CE", "PAS"});
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.7;
        panelCampos.add(cbTipoDoc, gbc);

        // Fila 2: Numero Documento + Boton Buscar
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        panelCampos.add(new JLabel("No. Documento *:"), gbc);
        
        JPanel panelDoc = new JPanel(new BorderLayout(4, 0));
        txtNumDoc = new JTextField();
        btnBuscar = new JButton("Buscar");
        btnBuscar.setToolTipText("Buscar funcionario por este numero de documento");
        btnBuscar.addActionListener(e -> buscarPorDocumento());
        panelDoc.add(txtNumDoc, BorderLayout.CENTER);
        panelDoc.add(btnBuscar, BorderLayout.EAST);

        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 0.7;
        panelCampos.add(panelDoc, gbc);

        // Fila 3: Nombres
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.3;
        panelCampos.add(new JLabel("Nombres *:"), gbc);
        txtNombres = new JTextField();
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 0.7;
        panelCampos.add(txtNombres, gbc);

        // Fila 4: Apellidos
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.3;
        panelCampos.add(new JLabel("Apellidos *:"), gbc);
        txtApellidos = new JTextField();
        gbc.gridx = 1; gbc.gridy = 4; gbc.weightx = 0.7;
        panelCampos.add(txtApellidos, gbc);

        // Fila 5: Estado Civil
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0.3;
        panelCampos.add(new JLabel("Estado Civil:"), gbc);
        cbEstadoCivil = new JComboBox<>(new String[]{"Soltero(a)", "Casado(a)", "Union Libre", "Divorciado(a)", "Viudo(a)"});
        gbc.gridx = 1; gbc.gridy = 5; gbc.weightx = 0.7;
        panelCampos.add(cbEstadoCivil, gbc);

        // Fila 6: Sexo
        gbc.gridx = 0; gbc.gridy = 6; gbc.weightx = 0.3;
        panelCampos.add(new JLabel("Sexo:"), gbc);
        cbSexo = new JComboBox<>(new String[]{"Masculino", "Femenino", "Otro"});
        gbc.gridx = 1; gbc.gridy = 6; gbc.weightx = 0.7;
        panelCampos.add(cbSexo, gbc);

        // Fila 7: Direccion
        gbc.gridx = 0; gbc.gridy = 7; gbc.weightx = 0.3;
        panelCampos.add(new JLabel("Direccion:"), gbc);
        txtDireccion = new JTextField();
        gbc.gridx = 1; gbc.gridy = 7; gbc.weightx = 0.7;
        panelCampos.add(txtDireccion, gbc);

        // Fila 8: Telefono
        gbc.gridx = 0; gbc.gridy = 8; gbc.weightx = 0.3;
        panelCampos.add(new JLabel("Telefono:"), gbc);
        txtTelefono = new JTextField();
        gbc.gridx = 1; gbc.gridy = 8; gbc.weightx = 0.7;
        panelCampos.add(txtTelefono, gbc);

        // Fila 9: Fecha Nacimiento
        gbc.gridx = 0; gbc.gridy = 9; gbc.weightx = 0.3;
        panelCampos.add(new JLabel("Nacimiento:"), gbc);
        txtFechaNac = new JTextField();
        txtFechaNac.setToolTipText("Formato AAAA-MM-DD (Ejemplo: 1995-04-12)");
        gbc.gridx = 1; gbc.gridy = 9; gbc.weightx = 0.7;
        panelCampos.add(txtFechaNac, gbc);

        // Mensaje de formato debajo del campo
        gbc.gridx = 1; gbc.gridy = 10;
        JLabel lblFormato = new JLabel("Formato: AAAA-MM-DD (Ej: 1995-04-12)");
        lblFormato.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        lblFormato.setForeground(Color.GRAY);
        panelCampos.add(lblFormato, gbc);

        // Panel de Botones
        JPanel panelBotones = new JPanel(new GridLayout(2, 2, 6, 6));
        panelBotones.setBorder(new EmptyBorder(10, 5, 5, 5));

        btnGuardar = new JButton("Guardar Nuevo");
        btnGuardar.setBackground(new Color(33, 115, 70));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnGuardar.addActionListener(e -> accionGuardar());

        btnActualizar = new JButton("Actualizar");
        btnActualizar.setBackground(new Color(30, 85, 150));
        btnActualizar.setForeground(Color.WHITE);
        btnActualizar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnActualizar.setEnabled(false);
        btnActualizar.addActionListener(e -> accionActualizar());

        btnEliminar = new JButton("Eliminar");
        btnEliminar.setBackground(new Color(175, 45, 45));
        btnEliminar.setForeground(Color.WHITE);
        btnEliminar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnEliminar.setEnabled(false);
        btnEliminar.addActionListener(e -> accionEliminar());

        btnLimpiar = new JButton("Limpiar / Nuevo");
        btnLimpiar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnLimpiar.addActionListener(e -> limpiarFormulario());

        panelBotones.add(btnGuardar);
        panelBotones.add(btnActualizar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnLimpiar);

        panelContenedor.add(new JScrollPane(panelCampos), BorderLayout.CENTER);
        panelContenedor.add(panelBotones, BorderLayout.SOUTH);

        return panelContenedor;
    }

    private JPanel crearPanelVisualizacion() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(new EmptyBorder(8, 4, 8, 8));

        // 1. Tabla Superior: Funcionarios
        JPanel panelTablaSup = new JPanel(new BorderLayout(5, 5));
        panelTablaSup.setBorder(new TitledBorder(BorderFactory.createLineBorder(new Color(180, 190, 205)), "Listado General de Funcionarios"));

        JPanel panelBarraTabla = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnRecargar = new JButton("Recargar Lista");
        btnRecargar.addActionListener(e -> {
            cargarTablaFuncionarios();
            limpiarFormulario();
        });
        panelBarraTabla.add(btnRecargar);
        panelTablaSup.add(panelBarraTabla, BorderLayout.NORTH);

        String[] colsFuncionarios = {
            "ID", "Tipo", "Documento", "Nombres", "Apellidos",
            "Estado Civil", "Sexo", "Direccion", "Telefono", "F. Nacimiento"
        };
        modeloTablaFuncionarios = new DefaultTableModel(colsFuncionarios, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Solo seleccion, edicion via formulario
            }
        };

        tblFuncionarios = new JTable(modeloTablaFuncionarios);
        tblFuncionarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblFuncionarios.setAutoCreateRowSorter(true);
        tblFuncionarios.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionarFilaFuncionario();
            }
        });

        JScrollPane scrollFuncionarios = new JScrollPane(tblFuncionarios);
        panelTablaSup.add(scrollFuncionarios, BorderLayout.CENTER);

        // 2. Pestañas Inferiores: Familiares y Estudios
        JTabbedPane tabRelaciones = new JTabbedPane();
        tabRelaciones.setBorder(new TitledBorder(BorderFactory.createLineBorder(new Color(180, 190, 205)), "Relaciones del Funcionario Seleccionado"));

        tabRelaciones.addTab("Grupo Familiar", crearPanelFamiliares());
        tabRelaciones.addTab("Estudios Academicos", crearPanelEstudios());

        // Split vertical entre tabla superior y pestañas inferiores
        JSplitPane splitVertical = new JSplitPane(JSplitPane.VERTICAL_SPLIT, panelTablaSup, tabRelaciones);
        splitVertical.setDividerLocation(340);
        splitVertical.setContinuousLayout(true);

        panel.add(splitVertical, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPanelFamiliares() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));

        String[] cols = {"ID Familiar", "ID Funcionario", "Nombres", "Apellidos", "Parentesco", "F. Nacimiento"};
        modeloTablaFamiliares = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblFamiliares = new JTable(modeloTablaFamiliares);
        tblFamiliares.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel barraAcciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnAgregarFamiliar = new JButton("+ Agregar Familiar");
        btnAgregarFamiliar.setEnabled(false);
        btnAgregarFamiliar.addActionListener(e -> agregarFamiliarModal());

        btnEliminarFamiliar = new JButton("- Eliminar Familiar Seleccionado");
        btnEliminarFamiliar.setEnabled(false);
        btnEliminarFamiliar.addActionListener(e -> eliminarFamiliarSeleccionado());

        barraAcciones.add(btnAgregarFamiliar);
        barraAcciones.add(btnEliminarFamiliar);

        tblFamiliares.getSelectionModel().addListSelectionListener(e -> {
            boolean haySeleccion = tblFamiliares.getSelectedRow() != -1;
            btnEliminarFamiliar.setEnabled(haySeleccion);
        });

        panel.add(barraAcciones, BorderLayout.NORTH);
        panel.add(new JScrollPane(tblFamiliares), BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPanelEstudios() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));

        String[] cols = {"ID Estudio", "ID Funcionario", "Universidad", "Nivel Academico", "Titulo Obtenido"};
        modeloTablaEstudios = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblEstudios = new JTable(modeloTablaEstudios);
        tblEstudios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel barraAcciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnAgregarEstudio = new JButton("+ Agregar Estudio");
        btnAgregarEstudio.setEnabled(false);
        btnAgregarEstudio.addActionListener(e -> agregarEstudioModal());

        btnEliminarEstudio = new JButton("- Eliminar Estudio Seleccionado");
        btnEliminarEstudio.setEnabled(false);
        btnEliminarEstudio.addActionListener(e -> eliminarEstudioSeleccionado());

        barraAcciones.add(btnAgregarEstudio);
        barraAcciones.add(btnEliminarEstudio);

        tblEstudios.getSelectionModel().addListSelectionListener(e -> {
            boolean haySeleccion = tblEstudios.getSelectedRow() != -1;
            btnEliminarEstudio.setEnabled(haySeleccion);
        });

        panel.add(barraAcciones, BorderLayout.NORTH);
        panel.add(new JScrollPane(tblEstudios), BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearBarraEstado() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(4, 10, 4, 10));
        panel.setBackground(new Color(235, 238, 245));

        lblEstado = new JLabel("Listo.");
        lblEstado.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(lblEstado, BorderLayout.WEST);

        JLabel lblInfo = new JLabel("Arquitectura DAO | MySQL JDBC | Java 17");
        lblInfo.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblInfo.setForeground(Color.DARK_GRAY);
        panel.add(lblInfo, BorderLayout.EAST);

        return panel;
    }

    // ==============================================================
    // OPERACIONES CRUD DE FUNCIONARIOS
    // ==============================================================

    public void cargarTablaFuncionarios() {
        modeloTablaFuncionarios.setRowCount(0);
        try {
            List<Funcionario> lista = funcionarioDAO.listarTodos();
            for (Funcionario f : lista) {
                modeloTablaFuncionarios.addRow(new Object[]{
                    f.getIdFuncionario(),
                    f.getTipoDocumento(),
                    f.getNumeroDocumento(),
                    f.getNombres(),
                    f.getApellidos(),
                    f.getEstadoCivil(),
                    f.getSexo(),
                    f.getDireccion(),
                    f.getTelefono(),
                    (f.getFechaNacimiento() != null ? f.getFechaNacimiento().toString() : "")
                });
            }
            lblEstado.setText("Se cargaron " + lista.size() + " funcionario(s) desde la base de datos.");
            lblEstado.setForeground(Color.BLACK);
        } catch (SQLException e) {
            mostrarError("Error al consultar funcionarios", e.getMessage());
            lblEstado.setText("Error al conectar con la base de datos.");
            lblEstado.setForeground(Color.RED);
        }
    }

    private void seleccionarFilaFuncionario() {
        int fila = tblFuncionarios.getSelectedRow();
        if (fila == -1) {
            btnActualizar.setEnabled(false);
            btnEliminar.setEnabled(false);
            btnAgregarFamiliar.setEnabled(false);
            btnAgregarEstudio.setEnabled(false);
            limpiarRelaciones();
            return;
        }

        int filaModelo = tblFuncionarios.convertRowIndexToModel(fila);
        Integer id = (Integer) modeloTablaFuncionarios.getValueAt(filaModelo, 0);
        String tipoDoc = String.valueOf(modeloTablaFuncionarios.getValueAt(filaModelo, 1));
        String numDoc = String.valueOf(modeloTablaFuncionarios.getValueAt(filaModelo, 2));
        String nombres = String.valueOf(modeloTablaFuncionarios.getValueAt(filaModelo, 3));
        String apellidos = String.valueOf(modeloTablaFuncionarios.getValueAt(filaModelo, 4));
        String estadoCivil = String.valueOf(modeloTablaFuncionarios.getValueAt(filaModelo, 5));
        String sexo = String.valueOf(modeloTablaFuncionarios.getValueAt(filaModelo, 6));
        String direccion = String.valueOf(modeloTablaFuncionarios.getValueAt(filaModelo, 7));
        String telefono = String.valueOf(modeloTablaFuncionarios.getValueAt(filaModelo, 8));
        String fechaNac = String.valueOf(modeloTablaFuncionarios.getValueAt(filaModelo, 9));

        txtId.setText(id != null ? id.toString() : "");
        cbTipoDoc.setSelectedItem(tipoDoc);
        txtNumDoc.setText(numDoc);
        txtNombres.setText(nombres);
        txtApellidos.setText(apellidos);
        cbEstadoCivil.setSelectedItem(estadoCivil);
        cbSexo.setSelectedItem(sexo);
        txtDireccion.setText("null".equals(direccion) ? "" : direccion);
        txtTelefono.setText("null".equals(telefono) ? "" : telefono);
        txtFechaNac.setText("null".equals(fechaNac) ? "" : fechaNac);

        btnGuardar.setEnabled(false);
        btnActualizar.setEnabled(true);
        btnEliminar.setEnabled(true);
        btnAgregarFamiliar.setEnabled(true);
        btnAgregarEstudio.setEnabled(true);

        if (id != null) {
            cargarRelaciones(id);
            lblEstado.setText("Funcionario seleccionado: " + nombres + " " + apellidos + " (ID: " + id + ")");
            lblEstado.setForeground(Color.BLACK);
        }
    }

    private void cargarRelaciones(int idFuncionario) {
        // Cargar Familiares
        modeloTablaFamiliares.setRowCount(0);
        try {
            List<GrupoFamiliar> familiares = familiarDAO.listarPorFuncionario(idFuncionario);
            for (GrupoFamiliar gf : familiares) {
                modeloTablaFamiliares.addRow(new Object[]{
                    gf.getIdFamiliar(),
                    gf.getIdFuncionario(),
                    gf.getNombres(),
                    gf.getApellidos(),
                    gf.getParentesco(),
                    (gf.getFechaNacimiento() != null ? gf.getFechaNacimiento().toString() : "")
                });
            }
        } catch (SQLException e) {
            mostrarError("Error al cargar grupo familiar", e.getMessage());
        }

        // Cargar Estudios
        modeloTablaEstudios.setRowCount(0);
        try {
            List<Estudio> estudios = estudioDAO.listarPorFuncionario(idFuncionario);
            for (Estudio est : estudios) {
                modeloTablaEstudios.addRow(new Object[]{
                    est.getIdEstudio(),
                    est.getIdFuncionario(),
                    est.getUniversidad(),
                    est.getNivelAcademico(),
                    est.getTituloObtenido()
                });
            }
        } catch (SQLException e) {
            mostrarError("Error al cargar estudios", e.getMessage());
        }
    }

    private void limpiarRelaciones() {
        modeloTablaFamiliares.setRowCount(0);
        modeloTablaEstudios.setRowCount(0);
        btnEliminarFamiliar.setEnabled(false);
        btnEliminarEstudio.setEnabled(false);
    }

    private void accionGuardar() {
        String tipoDoc = (String) cbTipoDoc.getSelectedItem();
        String numDoc = txtNumDoc.getText();
        String nombres = txtNombres.getText();
        String apellidos = txtApellidos.getText();
        String estadoCivil = (String) cbEstadoCivil.getSelectedItem();
        String sexo = (String) cbSexo.getSelectedItem();
        String direccion = txtDireccion.getText();
        String telefono = txtTelefono.getText();
        String fechaNacTexto = txtFechaNac.getText();

        ValidadorFuncionario.ResultadoValidacion res = ValidadorFuncionario.validar(
                tipoDoc, numDoc, nombres, apellidos, fechaNacTexto, telefono, direccion
        );

        if (!res.isValido()) {
            mostrarAdvertencia("Datos Invalidos", res.getMensajeError());
            return;
        }

        try {
            if (funcionarioDAO.existeDocumento(numDoc.trim(), null)) {
                mostrarAdvertencia("Documento Duplicado", "Ya existe un funcionario registrado con el documento: " + numDoc.trim());
                return;
            }

            LocalDate fechaNac = ValidadorFuncionario.parsearFecha(fechaNacTexto);
            Funcionario f = new Funcionario(
                    tipoDoc, numDoc.trim(), nombres.trim(), apellidos.trim(),
                    estadoCivil, sexo, direccion.trim(), telefono.trim(), fechaNac
            );

            boolean exito = funcionarioDAO.registrar(f);
            if (exito) {
                mostrarExito("Registro Exitoso", "El funcionario ha sido registrado con el ID: " + f.getIdFuncionario());
                limpiarFormulario();
                cargarTablaFuncionarios();
            } else {
                mostrarError("Error al registrar", "No se pudo insertar el registro en la base de datos.");
            }

        } catch (SQLException e) {
            mostrarError("Error en la Base de Datos", e.getMessage());
        }
    }

    private void accionActualizar() {
        String idTexto = txtId.getText();
        if (idTexto == null || idTexto.trim().isEmpty()) {
            mostrarAdvertencia("Seleccion Requerida", "Debe seleccionar un funcionario de la tabla para actualizar.");
            return;
        }

        int idFuncionario;
        try {
            idFuncionario = Integer.parseInt(idTexto.trim());
        } catch (NumberFormatException e) {
            mostrarError("Identificador Invalido", "El ID del funcionario no es un numero valido.");
            return;
        }

        String tipoDoc = (String) cbTipoDoc.getSelectedItem();
        String numDoc = txtNumDoc.getText();
        String nombres = txtNombres.getText();
        String apellidos = txtApellidos.getText();
        String estadoCivil = (String) cbEstadoCivil.getSelectedItem();
        String sexo = (String) cbSexo.getSelectedItem();
        String direccion = txtDireccion.getText();
        String telefono = txtTelefono.getText();
        String fechaNacTexto = txtFechaNac.getText();

        ValidadorFuncionario.ResultadoValidacion res = ValidadorFuncionario.validar(
                tipoDoc, numDoc, nombres, apellidos, fechaNacTexto, telefono, direccion
        );

        if (!res.isValido()) {
            mostrarAdvertencia("Datos Invalidos", res.getMensajeError());
            return;
        }

        try {
            if (funcionarioDAO.existeDocumento(numDoc.trim(), idFuncionario)) {
                mostrarAdvertencia("Documento Duplicado", "El documento '" + numDoc.trim() + "' ya pertenece a otro funcionario.");
                return;
            }

            LocalDate fechaNac = ValidadorFuncionario.parsearFecha(fechaNacTexto);
            Funcionario f = new Funcionario(
                    idFuncionario, tipoDoc, numDoc.trim(), nombres.trim(), apellidos.trim(),
                    estadoCivil, sexo, direccion.trim(), telefono.trim(), fechaNac
            );

            boolean exito = funcionarioDAO.actualizar(f);
            if (exito) {
                mostrarExito("Actualizacion Exitosa", "Los datos del funcionario han sido actualizados correctamente.");
                cargarTablaFuncionarios();
            } else {
                mostrarError("Error al actualizar", "No se encontro el registro o no hubo cambios aplicados.");
            }

        } catch (SQLException e) {
            mostrarError("Error en la Base de Datos", e.getMessage());
        }
    }

    private void accionEliminar() {
        String idTexto = txtId.getText();
        if (idTexto == null || idTexto.trim().isEmpty()) {
            mostrarAdvertencia("Seleccion Requerida", "Debe seleccionar un funcionario de la tabla para eliminar.");
            return;
        }

        int idFuncionario;
        try {
            idFuncionario = Integer.parseInt(idTexto.trim());
        } catch (NumberFormatException e) {
            mostrarError("Identificador Invalido", "El ID del funcionario no es un numero valido.");
            return;
        }

        String nombreCompleto = txtNombres.getText() + " " + txtApellidos.getText();
        String doc = txtNumDoc.getText();

        // Confirmacion de seguridad obligatoria
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "¿Esta seguro de que desea eliminar al funcionario?\n\n"
                + "ID: " + idFuncionario + "\n"
                + "Nombre: " + nombreCompleto + "\n"
                + "Documento: " + doc + "\n\n"
                + "Esta accion es permanente e irreversible.",
                "Confirmacion de Eliminacion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean exito = funcionarioDAO.eliminar(idFuncionario);
                if (exito) {
                    mostrarExito("Funcionario Eliminado", "El registro del funcionario ha sido eliminado exitosamente.");
                    limpiarFormulario();
                    cargarTablaFuncionarios();
                } else {
                    mostrarError("Error al eliminar", "No se encontro el funcionario en la base de datos.");
                }
            } catch (SQLException e) {
                mostrarError("Restriccion de Integridad", e.getMessage());
            }
        }
    }

    private void buscarPorDocumento() {
        String doc = txtNumDoc.getText();
        if (doc == null || doc.trim().isEmpty()) {
            mostrarAdvertencia("Campo Requerido", "Ingrese el numero de documento que desea buscar.");
            return;
        }

        try {
            Funcionario f = funcionarioDAO.buscarPorDocumento(doc.trim());
            if (f != null) {
                txtId.setText(f.getIdFuncionario().toString());
                cbTipoDoc.setSelectedItem(f.getTipoDocumento());
                txtNumDoc.setText(f.getNumeroDocumento());
                txtNombres.setText(f.getNombres());
                txtApellidos.setText(f.getApellidos());
                cbEstadoCivil.setSelectedItem(f.getEstadoCivil());
                cbSexo.setSelectedItem(f.getSexo());
                txtDireccion.setText(f.getDireccion() != null ? f.getDireccion() : "");
                txtTelefono.setText(f.getTelefono() != null ? f.getTelefono() : "");
                txtFechaNac.setText(f.getFechaNacimiento() != null ? f.getFechaNacimiento().toString() : "");

                btnGuardar.setEnabled(false);
                btnActualizar.setEnabled(true);
                btnEliminar.setEnabled(true);
                btnAgregarFamiliar.setEnabled(true);
                btnAgregarEstudio.setEnabled(true);

                cargarRelaciones(f.getIdFuncionario());
                lblEstado.setText("Funcionario encontrado: " + f.getNombreCompleto());
                lblEstado.setForeground(Color.BLACK);

                // Seleccionar en la tabla si esta visible
                for (int i = 0; i < tblFuncionarios.getRowCount(); i++) {
                    Object val = tblFuncionarios.getValueAt(i, 2);
                    if (doc.trim().equals(String.valueOf(val))) {
                        tblFuncionarios.setRowSelectionInterval(i, i);
                        tblFuncionarios.scrollRectToVisible(tblFuncionarios.getCellRect(i, 0, true));
                        break;
                    }
                }

            } else {
                mostrarInformacion("No Encontrado", "No se encontro ningun funcionario registrado con el documento: " + doc.trim());
            }
        } catch (SQLException e) {
            mostrarError("Error en la consulta", e.getMessage());
        }
    }

    private void limpiarFormulario() {
        txtId.setText("");
        cbTipoDoc.setSelectedIndex(0);
        txtNumDoc.setText("");
        txtNombres.setText("");
        txtApellidos.setText("");
        cbEstadoCivil.setSelectedIndex(0);
        cbSexo.setSelectedIndex(0);
        txtDireccion.setText("");
        txtTelefono.setText("");
        txtFechaNac.setText("");

        tblFuncionarios.clearSelection();
        btnGuardar.setEnabled(true);
        btnActualizar.setEnabled(false);
        btnEliminar.setEnabled(false);
        btnAgregarFamiliar.setEnabled(false);
        btnAgregarEstudio.setEnabled(false);

        limpiarRelaciones();
        lblEstado.setText("Formulario listo para nuevo registro.");
        lblEstado.setForeground(Color.BLACK);
    }

    // ==============================================================
    // GESTION MODAL DE FAMILIARES Y ESTUDIOS
    // ==============================================================

    private void agregarFamiliarModal() {
        String idTexto = txtId.getText();
        if (idTexto == null || idTexto.trim().isEmpty()) return;
        int idFuncionario;
        try {
            idFuncionario = Integer.parseInt(idTexto.trim());
        } catch (NumberFormatException ex) {
            mostrarError("Error", "Identificador de funcionario no valido.");
            return;
        }

        JTextField tfNombres = new JTextField();
        JTextField tfApellidos = new JTextField();
        JComboBox<String> cbParentesco = new JComboBox<>(new String[]{"Conyuge", "Hijo(a)", "Padre", "Madre", "Hermano(a)", "Otro"});
        JTextField tfFechaNac = new JTextField();

        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        panel.add(new JLabel("Nombres *:"));
        panel.add(tfNombres);
        panel.add(new JLabel("Apellidos *:"));
        panel.add(tfApellidos);
        panel.add(new JLabel("Parentesco *:"));
        panel.add(cbParentesco);
        panel.add(new JLabel("F. Nacimiento (AAAA-MM-DD):"));
        panel.add(tfFechaNac);

        int result = JOptionPane.showConfirmDialog(this, panel, "Registrar Nuevo Familiar",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String nom = tfNombres.getText();
            String ape = tfApellidos.getText();
            String par = (String) cbParentesco.getSelectedItem();
            String fec = tfFechaNac.getText();

            ValidadorFuncionario.ResultadoValidacion valRes = 
                    ValidadorFuncionario.validarFamiliar(nom, ape, par, fec);
            if (!valRes.isValido()) {
                mostrarAdvertencia("Datos Invalidos", valRes.getMensajeError());
                return;
            }

            LocalDate fechaNac = ValidadorFuncionario.parsearFecha(fec);

            try {
                GrupoFamiliar gf = new GrupoFamiliar(idFuncionario, nom.trim(), ape.trim(), par, fechaNac);
                familiarDAO.registrar(gf);
                mostrarExito("Familiar Registrado", "Se registro el familiar exitosamente.");
                cargarRelaciones(idFuncionario);
            } catch (SQLException ex) {
                mostrarError("Error al registrar familiar", ex.getMessage());
            }
        }
    }

    private void eliminarFamiliarSeleccionado() {
        int fila = tblFamiliares.getSelectedRow();
        if (fila == -1) return;

        int filaModelo = tblFamiliares.convertRowIndexToModel(fila);
        int idFamiliar = (int) modeloTablaFamiliares.getValueAt(filaModelo, 0);
        String nombre = (String) modeloTablaFamiliares.getValueAt(filaModelo, 2);

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Desea eliminar al familiar: " + nombre + "?",
                "Confirmar Eliminacion", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                familiarDAO.eliminar(idFamiliar);
                mostrarExito("Familiar Eliminado", "El familiar ha sido eliminado correctamente.");
                String idActual = txtId.getText();
                if (idActual != null && !idActual.trim().isEmpty()) {
                    int idFunc = Integer.parseInt(idActual.trim());
                    cargarRelaciones(idFunc);
                }
            } catch (SQLException ex) {
                mostrarError("Error al eliminar familiar", ex.getMessage());
            } catch (NumberFormatException ex) {
                // Si el ID en el formulario no es valido, simplemente se omite la recarga
            }
        }
    }

    private void agregarEstudioModal() {
        String idTexto = txtId.getText();
        if (idTexto == null || idTexto.trim().isEmpty()) return;
        int idFuncionario;
        try {
            idFuncionario = Integer.parseInt(idTexto.trim());
        } catch (NumberFormatException ex) {
            mostrarError("Error", "Identificador de funcionario no valido.");
            return;
        }

        JTextField tfUniv = new JTextField();
        JComboBox<String> cbNivel = new JComboBox<>(new String[]{"Bachiller", "Tecnico", "Tecnologo", "Universitario", "Especializacion", "Maestria", "Doctorado"});
        JTextField tfTitulo = new JTextField();

        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        panel.add(new JLabel("Universidad / Institucion *:"));
        panel.add(tfUniv);
        panel.add(new JLabel("Nivel Academico *:"));
        panel.add(cbNivel);
        panel.add(new JLabel("Titulo Obtenido:"));
        panel.add(tfTitulo);

        int result = JOptionPane.showConfirmDialog(this, panel, "Registrar Nuevo Estudio",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String univ = tfUniv.getText();
            String niv = (String) cbNivel.getSelectedItem();
            String tit = tfTitulo.getText();

            ValidadorFuncionario.ResultadoValidacion valRes = 
                    ValidadorFuncionario.validarEstudio(univ, niv, tit);
            if (!valRes.isValido()) {
                mostrarAdvertencia("Datos Invalidos", valRes.getMensajeError());
                return;
            }

            try {
                Estudio est = new Estudio(idFuncionario, univ.trim(), niv, tit != null ? tit.trim() : "");
                estudioDAO.registrar(est);
                mostrarExito("Estudio Registrado", "Se registro el estudio academico exitosamente.");
                cargarRelaciones(idFuncionario);
            } catch (SQLException ex) {
                mostrarError("Error al registrar estudio", ex.getMessage());
            }
        }
    }

    private void eliminarEstudioSeleccionado() {
        int fila = tblEstudios.getSelectedRow();
        if (fila == -1) return;

        int filaModelo = tblEstudios.convertRowIndexToModel(fila);
        int idEstudio = (int) modeloTablaEstudios.getValueAt(filaModelo, 0);
        String univ = (String) modeloTablaEstudios.getValueAt(filaModelo, 2);

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Desea eliminar el estudio en: " + univ + "?",
                "Confirmar Eliminacion", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                estudioDAO.eliminar(idEstudio);
                mostrarExito("Estudio Eliminado", "El estudio academico ha sido eliminado correctamente.");
                String idActual = txtId.getText();
                if (idActual != null && !idActual.trim().isEmpty()) {
                    int idFunc = Integer.parseInt(idActual.trim());
                    cargarRelaciones(idFunc);
                }
            } catch (SQLException ex) {
                mostrarError("Error al eliminar estudio", ex.getMessage());
            } catch (NumberFormatException ex) {
                // Si el ID en el formulario no es valido, se omite recarga
            }
        }
    }

    // ==============================================================
    // DIALOGOS AUXILIARES
    // ==============================================================

    private void mostrarExito(String titulo, String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, titulo, JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarAdvertencia(String titulo, String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, titulo, JOptionPane.WARNING_MESSAGE);
    }

    private void mostrarInformacion(String titulo, String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, titulo, JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarError(String titulo, String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, titulo, JOptionPane.ERROR_MESSAGE);
    }
}
