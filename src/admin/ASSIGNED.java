/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package admin;

import config.SessionManager;
import config.config;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;
import main.LOGin;

/**
 *
 * @author Administrator
 */
public class ASSIGNED extends javax.swing.JFrame {

    private int taskId;

    /**
     * Default constructor — standalone use (no task pre-selected).
     */
    public ASSIGNED() {
        this(-1);
    }

    /**
     * Main constructor — called by viewTask with the selected task ID.
     */
     public ASSIGNED(int taskId) {
        this.taskId = taskId;
        initComponents();

        // ── Session guard — redirect to login if not logged in ─────────────
        if (!SessionManager.getInstance().isLoggedIn()) {
            javax.swing.JOptionPane.showMessageDialog(null,
                "Access Denied! Please log in first.",
                "Unauthorized", javax.swing.JOptionPane.ERROR_MESSAGE);
            LOGin log = new LOGin();
            log.setVisible(true);
            this.dispose();
            return;
        }

        displayUsers();
    }
    
    
     

    // ── Load all ACCOUNTS into the table ───────────────────────────────────
   private void displayUsers() {
        String sql = "SELECT acc_id, name, email, type, status FROM ACCOUNTS";
        try (Connection conn = config.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            populateTable(rs);
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this,
                "Load Error: " + e.getMessage(),
                "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Search users by name or email ──────────────────────────────────────
     private void searchUser(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            displayUsers();
            return;
        }

        String sql     = "SELECT acc_id, name, email, type, status FROM ACCOUNTS "
                       + "WHERE name LIKE ? OR email LIKE ?";
        String pattern = "%" + keyword.trim() + "%";

        try (Connection conn = config.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, pattern);
            pstmt.setString(2, pattern);

            try (ResultSet rs = pstmt.executeQuery()) {
                populateTable(rs);
            }

        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this,
                "Search Error: " + e.getMessage(),
                "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadUsersFromSQL(String sql, String[] unused) {
        try (Connection conn = config.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            populateTable(rs);

        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this,
                "Load Error: " + e.getMessage(),
                "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Fill usertable from a ResultSet ────────────────────────────────────
    private void populateTable(ResultSet rs) throws Exception {
        int colCount = rs.getMetaData().getColumnCount();
        Vector<String> columns = new Vector<>();
        for (int i = 1; i <= colCount; i++) {
            columns.add(rs.getMetaData().getColumnName(i));
        }

        Vector<Vector<Object>> rows = new Vector<>();
        while (rs.next()) {
            Vector<Object> row = new Vector<>();
            for (int i = 1; i <= colCount; i++) {
                row.add(rs.getObject(i));
            }
            rows.add(row);
        }

        DefaultTableModel model = new DefaultTableModel(rows, columns) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        usertable.setModel(model);

        if (rows.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this,
                "No users found.",
                "No Results", javax.swing.JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
      private void confirmAssignment() {

        // 1. Must have a valid task passed from viewTask
        if (taskId == -1) {
            javax.swing.JOptionPane.showMessageDialog(this,
                "No task selected. Please go back and select a task first.",
                "No Task", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 2. Must have selected a user row
        int selectedRow = usertable.getSelectedRow();
        if (selectedRow == -1) {
            javax.swing.JOptionPane.showMessageDialog(this,
                "Please select a user from the table first.",
                "No User Selected", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 3. Read selected user's ID and name
        int    userId   = Integer.parseInt(usertable.getValueAt(selectedRow, 0).toString());
        String userName = usertable.getValueAt(selectedRow, 1).toString();

        // 4. ROLE field must not be empty before confirming
        String role = ROLE.getText().trim();
        if (role.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this,
                "Please enter a ROLE for the assigned user before confirming.",
                "Role Required", javax.swing.JOptionPane.WARNING_MESSAGE);
            ROLE.requestFocus();
            return;
        }

        // 5. Confirmation dialog — shows User ID and Role, then YES / NO
        int choice = javax.swing.JOptionPane.showConfirmDialog(this,
            "Selected User ID: " + userId + "\n"
            + "Role: " + role + "\n\n"
            + "Do you want to continue?",
            "Confirm Assignment",
            javax.swing.JOptionPane.YES_NO_OPTION,
            javax.swing.JOptionPane.QUESTION_MESSAGE);

        if (choice != javax.swing.JOptionPane.YES_OPTION) {
            return; // NO — stay on screen
        }

        // 6. Save to "ASSIGNED TASK" table
        int assignedBy = SessionManager.getInstance().getUserId();

        String sql = "INSERT INTO \"ASSIGNED TASK\" (ASSIGNEDBY, TASKASSIGNED, ASSINGEDTO, ROLE) "
                   + "VALUES (?, ?, ?, ?)";

        try (Connection conn = config.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, assignedBy);
            pstmt.setInt(2, taskId);
            pstmt.setInt(3, userId);
            pstmt.setString(4, role);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                javax.swing.JOptionPane.showMessageDialog(this,
                    "Task ID " + taskId + " successfully assigned to "
                    + userName + " (User ID: " + userId + ")",
                    "Assignment Successful", javax.swing.JOptionPane.INFORMATION_MESSAGE);

                // Navigate back to viewTask
                viewTask vt = new viewTask();
                vt.setVisible(true);
                this.dispose();
            }

        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this,
                "Database Error: " + e.getMessage(),
                "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        user = new javax.swing.JLabel();
        user1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        HOME = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        usertable = new javax.swing.JTable();
        SEARCH = new javax.swing.JTextField();
        Search = new javax.swing.JButton();
        ASSIGNCONFIRM = new javax.swing.JButton();
        R = new javax.swing.JLabel();
        ROLE = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(0, 102, 255));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel2.setBackground(new java.awt.Color(0, 0, 255));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Century Gothic", 1, 18)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("ADMIN DASHBOARD");
        jPanel2.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 10, 290, 60));

        jPanel1.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 580, 80));

        jPanel3.setBackground(new java.awt.Color(51, 153, 255));
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel4.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("LOGOUT");
        jLabel4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel4MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel4MouseEntered(evt);
            }
        });
        jPanel3.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 254, 60, 30));

        user.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        user.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        user.setText("VIEW USER");
        user.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                userMouseClicked(evt);
            }
        });
        jPanel3.add(user, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 60, 70, 30));

        user1.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        user1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        user1.setText("CREATE TASK");
        user1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                user1MouseClicked(evt);
            }
        });
        jPanel3.add(user1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 100, 90, 30));

        jLabel3.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("VIEW ASSIGNED TASK");
        jLabel3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel3MouseClicked(evt);
            }
        });
        jPanel3.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 140, -1, -1));

        HOME.setBackground(new java.awt.Color(0, 102, 204));
        HOME.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        HOME.setText("HOME");
        HOME.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                HOMEActionPerformed(evt);
            }
        });
        jPanel3.add(HOME, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 20, -1, -1));

        jPanel1.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 80, 150, 310));

        usertable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(usertable);

        jPanel1.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 130, 400, 130));

        SEARCH.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        SEARCH.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SEARCHActionPerformed(evt);
            }
        });
        jPanel1.add(SEARCH, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 340, 220, 30));

        Search.setBackground(new java.awt.Color(0, 102, 204));
        Search.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        Search.setText("Search");
        Search.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SearchActionPerformed(evt);
            }
        });
        jPanel1.add(Search, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 340, -1, -1));

        ASSIGNCONFIRM.setBackground(new java.awt.Color(0, 102, 204));
        ASSIGNCONFIRM.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        ASSIGNCONFIRM.setText("CONFIRM");
        ASSIGNCONFIRM.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ASSIGNCONFIRMActionPerformed(evt);
            }
        });
        jPanel1.add(ASSIGNCONFIRM, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 90, -1, -1));

        R.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        R.setText("ROLE:");
        jPanel1.add(R, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 280, 70, -1));

        ROLE.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        ROLE.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ROLEActionPerformed(evt);
            }
        });
        jPanel1.add(ROLE, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 280, 100, -1));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 582, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 385, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jLabel4MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel4MouseClicked

        SessionManager.getInstance().clearSession();
        LOGin log = new LOGin();
        log.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_jLabel4MouseClicked

    private void jLabel4MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel4MouseEntered
        // TODO add your handling code here:
    }//GEN-LAST:event_jLabel4MouseEntered

    private void userMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_userMouseClicked
        userView view = new userView();
        view.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_userMouseClicked

    private void user1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_user1MouseClicked

        TASK ts = new TASK();
        ts.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_user1MouseClicked

    private void SEARCHActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SEARCHActionPerformed

        searchUser(SEARCH.getText());

        // TODO add your handling code here:
    }//GEN-LAST:event_SEARCHActionPerformed

    private void SearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SearchActionPerformed
        searchUser(SEARCH.getText());

        // TODO add your handling code here:
    }//GEN-LAST:event_SearchActionPerformed

    private void ASSIGNCONFIRMActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ASSIGNCONFIRMActionPerformed
            
        confirmAssignment();
        

        // TODO add your handling code here:
    }//GEN-LAST:event_ASSIGNCONFIRMActionPerformed

    private void ROLEActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ROLEActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ROLEActionPerformed

    private void jLabel3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel3MouseClicked
        viewAssigned ASS = new viewAssigned();
        ASS.setVisible(true);
        this.dispose();

        // TODO add your handling code here:
    }//GEN-LAST:event_jLabel3MouseClicked

    private void HOMEActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_HOMEActionPerformed
        admin ad = new admin();
        ad.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_HOMEActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ASSIGNED.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ASSIGNED.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ASSIGNED.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ASSIGNED.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

       try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(ASSIGNED.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ASSIGNED().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton ASSIGNCONFIRM;
    private javax.swing.JButton HOME;
    private javax.swing.JLabel R;
    private javax.swing.JTextField ROLE;
    private javax.swing.JTextField SEARCH;
    private javax.swing.JButton Search;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel user;
    private javax.swing.JLabel user1;
    private javax.swing.JTable usertable;
    // End of variables declaration//GEN-END:variables
}
