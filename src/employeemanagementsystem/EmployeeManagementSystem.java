/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package employeemanagementsystem;
 // Keep your actual package name here if NetBeans requires it
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class EmployeeManagementSystem extends JFrame {

    // Database Connection Parameters MUST go directly inside the class
    private static final String URL = "jdbc:mysql://localhost:3307/akhil";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    
    // Form Input Fields
    private JTextField txtEmpId, txtName, txtEmail, txtPhone, txtGender, txtDeptId, txtJobId, txtDate;
    
    // GUI Table Elements
    private JTable table;
    private DefaultTableModel tableModel;

    public EmployeeManagementSystem() {
        // Window Setup
        setTitle("Employee Management System - Advanced Java Technology");
        setSize(950, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // 1. INPUT FORM PANEL (Top Section)
        JPanel inputPanel = new JPanel(new GridLayout(4, 4, 10, 10));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Employee Details"));

        inputPanel.add(new JLabel("Employee ID (Update/Delete):"));
        txtEmpId = new JTextField();
        inputPanel.add(txtEmpId);

        inputPanel.add(new JLabel("Employee Name:"));
        txtName = new JTextField();
        inputPanel.add(txtName);

        inputPanel.add(new JLabel("Email Address:"));
        txtEmail = new JTextField();
        inputPanel.add(txtEmail);

        inputPanel.add(new JLabel("Phone Number:"));
        txtPhone = new JTextField();
        inputPanel.add(txtPhone);

        inputPanel.add(new JLabel("Gender:"));
        txtGender = new JTextField();
        inputPanel.add(txtGender);

        inputPanel.add(new JLabel("Department ID (1-4):"));
        txtDeptId = new JTextField();
        inputPanel.add(txtDeptId);

        inputPanel.add(new JLabel("Job ID (1-4):"));
        txtJobId = new JTextField();
        inputPanel.add(txtJobId);

        inputPanel.add(new JLabel("Joining Date (YYYY-MM-DD):"));
        txtDate = new JTextField();
        inputPanel.add(txtDate);

        add(inputPanel, BorderLayout.NORTH);

        // 2. DATA TABLE PANEL (Center Section)
        tableModel = new DefaultTableModel(new String[]{
            "ID", "Name", "Email", "Phone", "Gender", "Department", "Job Title", "Salary", "Joining Date"
        }, 0);
        table = new JTable(tableModel);
        
        // Auto-fill form inputs when clicking a row in the table
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int row = table.getSelectedRow();
                txtEmpId.setText(tableModel.getValueAt(row, 0).toString());
                txtName.setText(tableModel.getValueAt(row, 1).toString());
                txtEmail.setText(tableModel.getValueAt(row, 2).toString());
                txtPhone.setText(tableModel.getValueAt(row, 3).toString());
                txtGender.setText(tableModel.getValueAt(row, 4).toString());
                txtDate.setText(tableModel.getValueAt(row, 8).toString());
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);

        // 3. ACTION BUTTONS PANEL (Bottom Section)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));

        JButton btnAdd = new JButton("Add Employee");
        JButton btnView = new JButton("Refresh / View All");
        JButton btnUpdate = new JButton("Update Employee");
        JButton btnDelete = new JButton("Delete Employee");
        JButton btnClear = new JButton("Clear Form");

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnView);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear);

        add(buttonPanel, BorderLayout.SOUTH);

        // Button Action Listeners
        btnAdd.addActionListener(e -> createEmployee());
        btnView.addActionListener(e -> readEmployees());
        btnUpdate.addActionListener(e -> updateEmployee());
        btnDelete.addActionListener(e -> deleteEmployee());
        btnClear.addActionListener(e -> clearFields());

        // Automatically fetch and show data when window opens
        readEmployees();
    }

    // JDBC Connection Helper Method
    public Connection getConnection() {
        Connection conn = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "MySQL JDBC Driver not found!", "Driver Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Connection Failed: " + e.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
        return conn;
    }

    // CREATE - Add Employee to MySQL
    private void createEmployee() {
        String sql = "INSERT INTO employee (employee_name, email, phone, gender, department_id, job_id, joining_date) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (conn == null) return;

            pstmt.setString(1, txtName.getText().trim());
            pstmt.setString(2, txtEmail.getText().trim());
            pstmt.setString(3, txtPhone.getText().trim());
            pstmt.setString(4, txtGender.getText().trim());
            pstmt.setInt(5, Integer.parseInt(txtDeptId.getText().trim()));
            pstmt.setInt(6, Integer.parseInt(txtJobId.getText().trim()));
            pstmt.setDate(7, Date.valueOf(txtDate.getText().trim()));

            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Employee Added Successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            readEmployees();
            clearFields();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for Dept ID and Job ID.", "Input Error", JOptionPane.WARNING_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Invalid Date Format! Please use YYYY-MM-DD.", "Date Error", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage(), "SQL Exception", JOptionPane.ERROR_MESSAGE);
        }
    }

    // READ - Populate JTable with Database Records
    private void readEmployees() {
        tableModel.setRowCount(0); // Clear current table rows
        String sql = "SELECT e.employee_id, e.employee_name, e.email, e.phone, e.gender, " +
                     "d.department_name, j.job_title, j.salary, e.joining_date " +
                     "FROM employee e " +
                     "JOIN department d ON e.department_id = d.department_id " +
                     "JOIN job j ON e.job_id = j.job_id";

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (conn == null) return;

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("employee_id"),
                    rs.getString("employee_name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("gender"),
                    rs.getString("department_name"),
                    rs.getString("job_title"),
                    rs.getDouble("salary"),
                    rs.getDate("joining_date")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error Fetching Data: " + e.getMessage(), "SQL Exception", JOptionPane.ERROR_MESSAGE);
        }
    }

    // UPDATE - Update Phone and Job ID by Employee ID
    private void updateEmployee() {
        String sql = "UPDATE employee SET phone = ?, job_id = ? WHERE employee_id = ?";

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (conn == null) return;

            pstmt.setString(1, txtPhone.getText().trim());
            pstmt.setInt(2, Integer.parseInt(txtJobId.getText().trim()));
            pstmt.setInt(3, Integer.parseInt(txtEmpId.getText().trim()));

            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                JOptionPane.showMessageDialog(this, "Employee Updated Successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                readEmployees();
                clearFields();
            } else {
                JOptionPane.showMessageDialog(this, "No Employee found with ID: " + txtEmpId.getText(), "Warning", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error Updating Employee: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // DELETE - Remove Employee by ID
    private void deleteEmployee() {
        if (txtEmpId.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select or enter an Employee ID to delete.", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "DELETE FROM employee WHERE employee_id = ?";

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (conn == null) return;

            pstmt.setInt(1, Integer.parseInt(txtEmpId.getText().trim()));

            int rowsDeleted = pstmt.executeUpdate();
            if (rowsDeleted > 0) {
                JOptionPane.showMessageDialog(this, "Employee Deleted Successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                readEmployees();
                clearFields();
            } else {
                JOptionPane.showMessageDialog(this, "No Employee found with ID: " + txtEmpId.getText(), "Warning", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error Deleting Employee: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Reset Form Input Fields
    private void clearFields() {
        txtEmpId.setText("");
        txtName.setText("");
        txtEmail.setText("");
        txtPhone.setText("");
        txtGender.setText("");
        txtDeptId.setText("");
        txtJobId.setText("");
        txtDate.setText("");
    }

    // Main Method to Launch Application Window
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new EmployeeManagementSystem().setVisible(true);
        });
    }
}

