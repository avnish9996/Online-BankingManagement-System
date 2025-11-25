import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.ArrayList;
import java.io.File;

public class BankSystemSQLite extends JFrame {
    // --- SQLite DB config (no external driver needed) ---
    static final String DB_URL = "jdbc:sqlite:bankdb.db";
    
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private User currentUser;
    private Account currentAccount;
    
    public BankSystemSQLite() {
        setTitle("Online Banking Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(null);
        setResizable(false);
        
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        // Initialize database
        try {
            initializeDatabase();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Database initialization failed: " + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        // Add panels
        mainPanel.add(createLoginPanel(), "LOGIN");
        mainPanel.add(createRegisterPanel(), "REGISTER");
        mainPanel.add(createUserDashboardPanel(), "USER_DASHBOARD");
        mainPanel.add(createAdminDashboardPanel(), "ADMIN_DASHBOARD");
        
        add(mainPanel);
        cardLayout.show(mainPanel, "LOGIN");
        
        // Load JDBC driver
        try { 
            Class.forName("org.sqlite.JDBC"); 
            System.out.println("SQLite JDBC Driver loaded successfully");
        } catch (Exception e) { 
            System.out.println("SQLite driver not found, will try to continue: " + e.getMessage());
        }
        
        setVisible(true);
    }
    
    private void initializeDatabase() throws Exception {
        try (Connection c = getConnection(); Statement stmt = c.createStatement()) {
            // Create tables if they don't exist
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "email TEXT UNIQUE NOT NULL, " +
                "phone TEXT, " +
                "role TEXT DEFAULT 'CUSTOMER', " +
                "password_hash TEXT NOT NULL, " +
                "salt TEXT NOT NULL, " +
                "status TEXT DEFAULT 'PENDING', " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            
            stmt.execute("CREATE TABLE IF NOT EXISTS accounts (" +
                "acc_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "acc_type TEXT DEFAULT 'SAVINGS', " +
                "balance REAL DEFAULT 0.00, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY(user_id) REFERENCES users(user_id))");
            
            stmt.execute("CREATE TABLE IF NOT EXISTS transactions (" +
                "tx_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "acc_id INTEGER NOT NULL, " +
                "tx_type TEXT NOT NULL, " +
                "amount REAL NOT NULL, " +
                "tx_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "description TEXT, " +
                "target_acc_id INTEGER, " +
                "FOREIGN KEY(acc_id) REFERENCES accounts(acc_id))");
            
            System.out.println("Database initialized successfully");
        }
    }
    
    // ========== LOGIN PANEL ==========
    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(240, 240, 240));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        JLabel titleLabel = new JLabel("Online Banking System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);
        
        JLabel emailLabel = new JLabel("Email:");
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        panel.add(emailLabel, gbc);
        
        JTextField emailField = new JTextField(20);
        gbc.gridx = 1;
        panel.add(emailField, gbc);
        
        JLabel passLabel = new JLabel("Password:");
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(passLabel, gbc);
        
        JPasswordField passField = new JPasswordField(20);
        gbc.gridx = 1;
        panel.add(passField, gbc);
        
        JButton loginBtn = new JButton("Login");
        loginBtn.setPreferredSize(new Dimension(100, 30));
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
        panel.add(loginBtn, gbc);
        
        JButton registerBtn = new JButton("Register");
        registerBtn.setPreferredSize(new Dimension(100, 30));
        gbc.gridx = 1;
        panel.add(registerBtn, gbc);
        
        JLabel msgLabel = new JLabel("");
        msgLabel.setForeground(Color.RED);
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        panel.add(msgLabel, gbc);
        
        loginBtn.addActionListener(e -> {
            String email = emailField.getText().trim();
            String password = new String(passField.getPassword()).trim();
            
            if (email.isEmpty() || password.isEmpty()) {
                msgLabel.setText("Please fill all fields");
                return;
            }
            
            try {
                User u = UserDAO.findByEmail(email);
                if (u == null) {
                    msgLabel.setText("No such user");
                    return;
                }
                
                if (!UserDAO.verifyPassword(email, password)) {
                    msgLabel.setText("Invalid credentials");
                    return;
                }
                
                if ("PENDING".equals(u.status)) {
                    msgLabel.setText("Account pending approval");
                    return;
                }
                
                if ("FROZEN".equals(u.status)) {
                    msgLabel.setText("Account is frozen");
                    return;
                }
                
                currentUser = u;
                currentAccount = AccountDAO.findByUserId(u.userId);
                
                if ("ADMIN".equals(u.role)) {
                    cardLayout.show(mainPanel, "ADMIN_DASHBOARD");
                } else {
                    cardLayout.show(mainPanel, "USER_DASHBOARD");
                }
                
                emailField.setText("");
                passField.setText("");
                msgLabel.setText("");
                
            } catch (Exception ex) {
                msgLabel.setForeground(Color.RED);
                ex.printStackTrace();
                msgLabel.setText("Login Error: " + ex.getClass().getSimpleName());
                JOptionPane.showMessageDialog(BankSystemSQLite.this, 
                    "Login Error:\n" + ex.getClass().getName() + "\n\nMessage: " + ex.getMessage(), 
                    "Error Details", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        registerBtn.addActionListener(e -> {
            emailField.setText("");
            passField.setText("");
            msgLabel.setText("");
            cardLayout.show(mainPanel, "REGISTER");
        });
        
        return panel;
    }
    
    // ========== REGISTER PANEL ==========
    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(240, 240, 240));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        JLabel titleLabel = new JLabel("Register New Account");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);
        
        JLabel nameLabel = new JLabel("Full Name:");
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        panel.add(nameLabel, gbc);
        JTextField nameField = new JTextField(20);
        gbc.gridx = 1;
        panel.add(nameField, gbc);
        
        JLabel emailLabel = new JLabel("Email:");
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(emailLabel, gbc);
        JTextField emailField = new JTextField(20);
        gbc.gridx = 1;
        panel.add(emailField, gbc);
        
        JLabel phoneLabel = new JLabel("Phone:");
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(phoneLabel, gbc);
        JTextField phoneField = new JTextField(20);
        gbc.gridx = 1;
        panel.add(phoneField, gbc);
        
        JLabel passLabel = new JLabel("Password:");
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(passLabel, gbc);
        JPasswordField passField = new JPasswordField(20);
        gbc.gridx = 1;
        panel.add(passField, gbc);
        
        JButton registerBtn = new JButton("Register");
        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(registerBtn, gbc);
        
        JButton backBtn = new JButton("Back");
        gbc.gridx = 1;
        panel.add(backBtn, gbc);
        
        JLabel msgLabel = new JLabel("");
        msgLabel.setForeground(Color.RED);
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        panel.add(msgLabel, gbc);
        
        registerBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String password = new String(passField.getPassword()).trim();
            
            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                msgLabel.setText("Please fill all fields");
                return;
            }
            
            try {
                int uid = UserDAO.createUser(name, email, phone, password, "CUSTOMER");
                if (uid > 0) {
                    AccountDAO.createAccountForUser(uid, 0.0);
                    msgLabel.setForeground(Color.GREEN);
                    msgLabel.setText("Registration successful! Please login.");
                    nameField.setText("");
                    emailField.setText("");
                    phoneField.setText("");
                    passField.setText("");
                    javax.swing.Timer timer = new javax.swing.Timer(2000, ev -> cardLayout.show(mainPanel, "LOGIN"));
                    timer.setRepeats(false);
                    timer.start();
                } else {
                    msgLabel.setForeground(Color.RED);
                    msgLabel.setText("Registration failed");
                }
            } catch (Exception ex) {
                msgLabel.setForeground(Color.RED);
                ex.printStackTrace();
                msgLabel.setText("Error: " + ex.getClass().getSimpleName());
                JOptionPane.showMessageDialog(BankSystemSQLite.this, 
                    "Registration Error:\n" + ex.getClass().getName() + "\n\nMessage: " + ex.getMessage(), 
                    "Error Details", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        backBtn.addActionListener(e -> {
            nameField.setText("");
            emailField.setText("");
            phoneField.setText("");
            passField.setText("");
            msgLabel.setText("");
            cardLayout.show(mainPanel, "LOGIN");
        });
        
        return panel;
    }
    
    // ========== USER DASHBOARD PANEL ==========
    private JPanel createUserDashboardPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(240, 240, 240));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        JLabel welcomeLabel = new JLabel("");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(welcomeLabel, gbc);
        
        JLabel balanceLabel = new JLabel("");
        balanceLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        gbc.gridy = 1;
        panel.add(balanceLabel, gbc);
        
        JButton viewBalanceBtn = new JButton("View Balance");
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        panel.add(viewBalanceBtn, gbc);
        
        JButton depositBtn = new JButton("Deposit");
        gbc.gridx = 1;
        panel.add(depositBtn, gbc);
        
        JButton withdrawBtn = new JButton("Withdraw");
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(withdrawBtn, gbc);
        
        JButton transferBtn = new JButton("Transfer");
        gbc.gridx = 1;
        panel.add(transferBtn, gbc);
        
        JButton statementBtn = new JButton("Statement");
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(statementBtn, gbc);
        
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setBackground(Color.RED);
        gbc.gridx = 1;
        panel.add(logoutBtn, gbc);
        
        viewBalanceBtn.addActionListener(e -> {
            try {
                Account acc = AccountDAO.findById(currentAccount.accId);
                balanceLabel.setText("Account Balance: $" + String.format("%.2f", acc.balance));
            } catch (Exception ex) {
                balanceLabel.setText("Error fetching balance");
            }
        });
        
        depositBtn.addActionListener(e -> showDepositDialog());
        withdrawBtn.addActionListener(e -> showWithdrawDialog());
        transferBtn.addActionListener(e -> showTransferDialog());
        statementBtn.addActionListener(e -> showStatementDialog());
        
        logoutBtn.addActionListener(e -> {
            currentUser = null;
            currentAccount = null;
            cardLayout.show(mainPanel, "LOGIN");
        });
        
        panel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                if (currentUser != null) {
                    welcomeLabel.setText("Welcome, " + currentUser.name);
                }
            }
        });
        
        return panel;
    }
    
    // ========== ADMIN DASHBOARD PANEL ==========
    private JPanel createAdminDashboardPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(240, 240, 240));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        JLabel welcomeLabel = new JLabel("");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(welcomeLabel, gbc);
        
        JButton viewPendingBtn = new JButton("View Pending Users");
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        panel.add(viewPendingBtn, gbc);
        
        JButton approveBtn = new JButton("Approve User");
        gbc.gridx = 1;
        panel.add(approveBtn, gbc);
        
        JButton freezeBtn = new JButton("Freeze User");
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(freezeBtn, gbc);
        
        JButton viewUserBtn = new JButton("View User Details");
        gbc.gridx = 1;
        panel.add(viewUserBtn, gbc);
        
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setBackground(Color.RED);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        panel.add(logoutBtn, gbc);
        
        viewPendingBtn.addActionListener(e -> showPendingUsersDialog());
        approveBtn.addActionListener(e -> showApproveDialog());
        freezeBtn.addActionListener(e -> showFreezeDialog());
        viewUserBtn.addActionListener(e -> showViewUserDialog());
        
        logoutBtn.addActionListener(e -> {
            currentUser = null;
            currentAccount = null;
            cardLayout.show(mainPanel, "LOGIN");
        });
        
        panel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                if (currentUser != null) {
                    welcomeLabel.setText("Admin Panel - Welcome, " + currentUser.name);
                }
            }
        });
        
        return panel;
    }
    
    // ========== DIALOGS ==========
    private void showDepositDialog() {
        JTextField amountField = new JTextField(10);
        JPanel panel = new JPanel();
        panel.add(new JLabel("Amount:"));
        panel.add(amountField);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Deposit", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                double amount = Double.parseDouble(amountField.getText());
                if (BankService.deposit(currentAccount.accId, amount)) {
                    JOptionPane.showMessageDialog(this, "Deposit successful!");
                } else {
                    JOptionPane.showMessageDialog(this, "Deposit failed");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid amount or database error");
            }
        }
    }
    
    private void showWithdrawDialog() {
        JTextField amountField = new JTextField(10);
        JPanel panel = new JPanel();
        panel.add(new JLabel("Amount:"));
        panel.add(amountField);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Withdraw", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                double amount = Double.parseDouble(amountField.getText());
                if (BankService.withdraw(currentAccount.accId, amount)) {
                    JOptionPane.showMessageDialog(this, "Withdrawal successful!");
                } else {
                    JOptionPane.showMessageDialog(this, "Withdrawal failed - insufficient funds?");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid amount or database error");
            }
        }
    }
    
    private void showTransferDialog() {
        JTextField toAccField = new JTextField(10);
        JTextField amountField = new JTextField(10);
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.add(new JLabel("To Account ID:"));
        panel.add(toAccField);
        panel.add(new JLabel("Amount:"));
        panel.add(amountField);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Transfer", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                int toAcc = Integer.parseInt(toAccField.getText());
                double amount = Double.parseDouble(amountField.getText());
                if (BankService.transfer(currentAccount.accId, toAcc, amount)) {
                    JOptionPane.showMessageDialog(this, "Transfer successful!");
                } else {
                    JOptionPane.showMessageDialog(this, "Transfer failed");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input or database error");
            }
        }
    }
    
    private void showStatementDialog() {
        try {
            List<String> txs = TransactionDAO.miniStatement(currentAccount.accId, 10);
            StringBuilder sb = new StringBuilder("Recent Transactions:\n\n");
            if (txs.isEmpty()) {
                sb.append("No transactions");
            } else {
                for (String tx : txs) {
                    sb.append(tx).append("\n");
                }
            }
            JTextArea textArea = new JTextArea(10, 40);
            textArea.setText(sb.toString());
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);
            JOptionPane.showMessageDialog(this, scrollPane, "Statement", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error fetching statement");
        }
    }
    
    private void showPendingUsersDialog() {
        try {
            List<User> pending = UserDAO.pendingUsers();
            StringBuilder sb = new StringBuilder();
            if (pending.isEmpty()) {
                sb.append("No pending users");
            } else {
                for (User u : pending) {
                    sb.append(u.userId).append(" | ").append(u.name).append(" | ").append(u.email).append("\n");
                }
            }
            JTextArea textArea = new JTextArea(10, 40);
            textArea.setText(sb.toString());
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);
            JOptionPane.showMessageDialog(this, scrollPane, "Pending Users", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error fetching pending users");
        }
    }
    
    private void showApproveDialog() {
        JTextField idField = new JTextField(10);
        JPanel panel = new JPanel();
        panel.add(new JLabel("User ID:"));
        panel.add(idField);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Approve User", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                int id = Integer.parseInt(idField.getText());
                User u = UserDAO.findById(id);
                if (u != null) {
                    UserDAO.setStatus(id, "ACTIVE");
                    JOptionPane.showMessageDialog(this, "User approved!");
                } else {
                    JOptionPane.showMessageDialog(this, "User not found");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error");
            }
        }
    }
    
    private void showFreezeDialog() {
        JTextField idField = new JTextField(10);
        JPanel panel = new JPanel();
        panel.add(new JLabel("User ID:"));
        panel.add(idField);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Freeze User", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                int id = Integer.parseInt(idField.getText());
                UserDAO.setStatus(id, "FROZEN");
                JOptionPane.showMessageDialog(this, "User frozen!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error");
            }
        }
    }
    
    private void showViewUserDialog() {
        JTextField idField = new JTextField(10);
        JPanel panel = new JPanel();
        panel.add(new JLabel("User ID:"));
        panel.add(idField);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "View User", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                int id = Integer.parseInt(idField.getText());
                User u = UserDAO.findById(id);
                if (u != null) {
                    String info = "ID: " + u.userId + "\nName: " + u.name + "\nEmail: " + u.email + 
                                  "\nRole: " + u.role + "\nStatus: " + u.status;
                    JOptionPane.showMessageDialog(this, info, "User Details", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "User not found");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error");
            }
        }
    }
    
    // ========== DATABASE CLASSES ==========
    static class User {
        int userId;
        String name, email, phone, role, status;
        User(int userId, String name, String email, String role, String status, String phone) {
            this.userId = userId; this.name = name; this.email = email; this.role = role; this.status = status; this.phone = phone;
        }
    }

    static class Account {
        int accId, userId;
        double balance;
        Account(int accId, int userId, double balance) { this.accId = accId; this.userId = userId; this.balance = balance; }
    }

    static Connection getConnection() throws Exception {
        return DriverManager.getConnection(DB_URL);
    }

    static String generateSalt() {
        SecureRandom sr = new SecureRandom();
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return bytesToHex(salt);
    }

    static String hash(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update((salt + password).getBytes(StandardCharsets.UTF_8));
            byte[] digest = md.digest();
            return bytesToHex(digest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static String bytesToHex(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (byte x : b) sb.append(String.format("%02x", x));
        return sb.toString();
    }

    static class UserDAO {
        static User findByEmail(String email) throws Exception {
            String sql = "SELECT user_id,name,email,phone,role,status FROM users WHERE email = ?";
            try (Connection c = getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
                p.setString(1, email);
                try (ResultSet rs = p.executeQuery()) {
                    if (rs.next()) {
                        return new User(rs.getInt("user_id"), rs.getString("name"), rs.getString("email"),
                                rs.getString("role"), rs.getString("status"), rs.getString("phone"));
                    }
                }
            }
            return null;
        }

        static int createUser(String name, String email, String phone, String plainPassword, String role) throws Exception {
            String salt = generateSalt();
            String hashVal = hash(plainPassword, salt);
            String sql = "INSERT INTO users (name,email,phone,role,password_hash,salt,status) VALUES (?,?,?,?,?,?,?)";
            try (Connection c = getConnection(); PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                p.setString(1, name);
                p.setString(2, email);
                p.setString(3, phone);
                p.setString(4, role == null ? "CUSTOMER" : role);
                p.setString(5, hashVal);
                p.setString(6, salt);
                p.setString(7, "ACTIVE");
                p.executeUpdate();
                try (ResultSet rs = p.getGeneratedKeys()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
            return -1;
        }

        static boolean verifyPassword(String email, String plainPassword) throws Exception {
            String sql = "SELECT password_hash, salt FROM users WHERE email = ?";
            try (Connection c = getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
                p.setString(1, email);
                try (ResultSet rs = p.executeQuery()) {
                    if (rs.next()) {
                        String storedHash = rs.getString("password_hash");
                        String salt = rs.getString("salt");
                        return storedHash.equals(hash(plainPassword, salt));
                    }
                }
            }
            return false;
        }

        static void setStatus(int userId, String status) throws Exception {
            String sql = "UPDATE users SET status = ? WHERE user_id = ?";
            try (Connection c = getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
                p.setString(1, status);
                p.setInt(2, userId);
                p.executeUpdate();
            }
        }

        static User findById(int id) throws Exception {
            String sql = "SELECT user_id,name,email,phone,role,status FROM users WHERE user_id = ?";
            try (Connection c = getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
                p.setInt(1, id);
                try (ResultSet rs = p.executeQuery()) {
                    if (rs.next()) return new User(rs.getInt("user_id"), rs.getString("name"), rs.getString("email"),
                                rs.getString("role"), rs.getString("status"), rs.getString("phone"));
                }
            }
            return null;
        }

        static List<User> pendingUsers() throws Exception {
            List<User> list = new ArrayList<>();
            String sql = "SELECT user_id,name,email,phone,role,status FROM users WHERE status = 'PENDING'";
            try (Connection c = getConnection(); PreparedStatement p = c.prepareStatement(sql);
                 ResultSet rs = p.executeQuery()) {
                while (rs.next()) list.add(new User(rs.getInt("user_id"), rs.getString("name"), rs.getString("email"),
                        rs.getString("role"), rs.getString("status"), rs.getString("phone")));
            }
            return list;
        }
    }

    static class AccountDAO {
        static int createAccountForUser(int userId, double initialDeposit) throws Exception {
            String sql = "INSERT INTO accounts (user_id, acc_type, balance) VALUES (?,?,?)";
            try (Connection c = getConnection(); PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                p.setInt(1, userId);
                p.setString(2, "SAVINGS");
                p.setDouble(3, initialDeposit);
                p.executeUpdate();
                try (ResultSet rs = p.getGeneratedKeys()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
            return -1;
        }

        static Account findById(int accId) throws Exception {
            String sql = "SELECT acc_id,user_id,balance FROM accounts WHERE acc_id = ?";
            try (Connection c = getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
                p.setInt(1, accId);
                try (ResultSet rs = p.executeQuery()) {
                    if (rs.next()) return new Account(rs.getInt("acc_id"), rs.getInt("user_id"), rs.getDouble("balance"));
                }
            }
            return null;
        }

        static Account findByUserId(int userId) throws Exception {
            String sql = "SELECT acc_id,user_id,balance FROM accounts WHERE user_id = ?";
            try (Connection c = getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
                p.setInt(1, userId);
                try (ResultSet rs = p.executeQuery()) {
                    if (rs.next()) return new Account(rs.getInt("acc_id"), rs.getInt("user_id"), rs.getDouble("balance"));
                }
            }
            return null;
        }

        static void updateBalance(Connection c, int accId, double newBalance) throws Exception {
            String sql = "UPDATE accounts SET balance = ? WHERE acc_id = ?";
            try (PreparedStatement p = c.prepareStatement(sql)) {
                p.setDouble(1, newBalance);
                p.setInt(2, accId);
                p.executeUpdate();
            }
        }
    }

    static class TransactionDAO {
        static void record(Connection c, int accId, String type, double amount, String desc, Integer targetAccId) throws Exception {
            String sql = "INSERT INTO transactions (acc_id, tx_type, amount, description, target_acc_id) VALUES (?,?,?,?,?)";
            try (PreparedStatement p = c.prepareStatement(sql)) {
                p.setInt(1, accId);
                p.setString(2, type);
                p.setDouble(3, amount);
                p.setString(4, desc);
                if (targetAccId == null) p.setNull(5, Types.INTEGER);
                else p.setInt(5, targetAccId);
                p.executeUpdate();
            }
        }

        static List<String> miniStatement(int accId, int limit) throws Exception {
            List<String> res = new ArrayList<>();
            String sql = "SELECT tx_time, tx_type, amount, description, target_acc_id FROM transactions WHERE acc_id = ? ORDER BY tx_time DESC LIMIT ?";
            try (Connection c = getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
                p.setInt(1, accId);
                p.setInt(2, limit);
                try (ResultSet rs = p.executeQuery()) {
                    while (rs.next()) {
                        res.add(String.format("%s | %s | %.2f | %s",
                                rs.getTimestamp("tx_time"), rs.getString("tx_type"), rs.getDouble("amount"),
                                rs.getString("description")));
                    }
                }
            }
            return res;
        }
    }

    static class BankService {
        static boolean deposit(int accId, double amount) throws Exception {
            if (amount <= 0) return false;
            try (Connection c = getConnection()) {
                c.setAutoCommit(false);
                Account acc = AccountDAO.findById(accId);
                if (acc == null) { c.rollback(); return false; }
                double newBal = acc.balance + amount;
                AccountDAO.updateBalance(c, accId, newBal);
                TransactionDAO.record(c, accId, "DEPOSIT", amount, "Deposit", null);
                c.commit();
                return true;
            }
        }

        static boolean withdraw(int accId, double amount) throws Exception {
            if (amount <= 0) return false;
            try (Connection c = getConnection()) {
                c.setAutoCommit(false);
                Account acc = AccountDAO.findById(accId);
                if (acc == null || acc.balance < amount) { c.rollback(); return false; }
                double newBal = acc.balance - amount;
                AccountDAO.updateBalance(c, accId, newBal);
                TransactionDAO.record(c, accId, "WITHDRAW", amount, "Withdraw", null);
                c.commit();
                return true;
            }
        }

        static boolean transfer(int fromAcc, int toAcc, double amount) throws Exception {
            if (amount <= 0 || fromAcc == toAcc) return false;
            try (Connection c = getConnection()) {
                c.setAutoCommit(false);
                Account aFrom = AccountDAO.findById(fromAcc);
                Account aTo = AccountDAO.findById(toAcc);
                if (aFrom == null || aTo == null || aFrom.balance < amount) { c.rollback(); return false; }
                AccountDAO.updateBalance(c, fromAcc, aFrom.balance - amount);
                AccountDAO.updateBalance(c, toAcc, aTo.balance + amount);
                TransactionDAO.record(c, fromAcc, "TRANSFER", amount, "Transfer to " + toAcc, toAcc);
                TransactionDAO.record(c, toAcc, "DEPOSIT", amount, "Transfer from " + fromAcc, fromAcc);
                c.commit();
                return true;
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BankSystemSQLite());
    }
}
