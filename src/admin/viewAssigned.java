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
public class viewAssigned extends javax.swing.JFrame {

    /**
     * Creates new form viewAssigned
     */
    public viewAssigned() {
        initComponents();
        if (!SessionManager.getInstance().isLoggedIn()) {
            javax.swing.JOptionPane.showMessageDialog(null,
                "Access Denied! Please log in first.",
                "Unauthorized", javax.swing.JOptionPane.ERROR_MESSAGE);
            LOGin log = new LOGin();
            log.setVisible(true);
            this.dispose();
            return;
        }

        displayAssigned();
    }
    private void updateAssigned() {
        int selectedRow = tableassined.getSelectedRow();

        if (selectedRow == -1) {
            javax.swing.JOptionPane.showMessageDialog(this,
                "Please select a record from the table first.",
                "No Record Selected", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        String asId       = tableassined.getValueAt(selectedRow, 0).toString();
        String taskId     = tableassined.getValueAt(selectedRow, 2).toString();
        String assignedTo = tableassined.getValueAt(selectedRow, 3).toString();
        String currentRole= tableassined.getValueAt(selectedRow, 4) != null
                            ? tableassined.getValueAt(selectedRow, 4).toString() : "";

        // Ask for the new role using an input dialog pre-filled with current value
        String newRole = (String) javax.swing.JOptionPane.showInputDialog(
            this,
            "AS_ID: " + asId + "  |  Task ID: " + taskId
            + "  |  Assigned To ID: " + assignedTo + "\n\nEnter new ROLE:",
            "Update Role",
            javax.swing.JOptionPane.PLAIN_MESSAGE,
            null, null, currentRole);

        // Cancelled or empty
        if (newRole == null) return;
        newRole = newRole.trim();
        if (newRole.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this,
                "Role cannot be empty.",
                "Validation Error", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Confirm before saving
        int confirm = javax.swing.JOptionPane.showConfirmDialog(this,
            "Update AS_ID " + asId + " role to: \"" + newRole + "\"?\n\nDo you want to continue?",
            "Confirm Update", javax.swing.JOptionPane.YES_NO_OPTION,
            javax.swing.JOptionPane.QUESTION_MESSAGE);

        if (confirm != javax.swing.JOptionPane.YES_OPTION) return;

        String sql = "UPDATE \"ASSIGNED TASK\" SET ROLE = ? WHERE AS_ID = ?";

        try (Connection conn = config.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newRole);
            pstmt.setString(2, asId);
            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                javax.swing.JOptionPane.showMessageDialog(this,
                    "Record updated successfully!",
                    "Updated", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                displayAssigned();
            }

        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this,
                "Database Error: " + e.getMessage(),
                "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void displayAssigned() {
        String sql = "SELECT AS_ID, ASSIGNEDBY, TASKASSIGNED, ASSINGEDTO, ROLE "
                   + "FROM \"ASSIGNED TASK\"";
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
    
private void searchUser(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            displayAssigned();
            return;
        }

        String sql     = "SELECT AS_ID, ASSIGNEDBY, TASKASSIGNED, ASSINGEDTO, ROLE "
                       + "FROM \"ASSIGNED TASK\" "
                       + "WHERE CAST(AS_ID AS TEXT) LIKE ? OR "
                       + "CAST(ASSIGNEDBY AS TEXT) LIKE ? OR "
                       + "CAST(TASKASSIGNED AS TEXT) LIKE ? OR "
                       + "CAST(ASSINGEDTO AS TEXT) LIKE ? OR "
                       + "ROLE LIKE ?";
        String pattern = "%" + keyword.trim() + "%";

        try (Connection conn = config.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, pattern);
            pstmt.setString(2, pattern);
            pstmt.setString(3, pattern);
            pstmt.setString(4, pattern);
            pstmt.setString(5, pattern);

            try (ResultSet rs = pstmt.executeQuery()) {
                populateTable(rs);
            }

        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this,
                "Search Error: " + e.getMessage(),
                "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    
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
        tableassined.setModel(model);

        if (rows.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this,
                "No assigned tasks found.",
                "No Results", javax.swing.JOptionPane.INFORMATION_MESSAGE);
        }
    }
      
         private void deleteAssigned() {
        int selectedRow = tableassined.getSelectedRow();

        if (selectedRow == -1) {
            javax.swing.JOptionPane.showMessageDialog(this,
                "Please select a record from the table first.",
                "No Record Selected", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        String asId      = tableassined.getValueAt(selectedRow, 0).toString();
        String taskId    = tableassined.getValueAt(selectedRow, 2).toString();
        String assignedTo= tableassined.getValueAt(selectedRow, 3).toString();

        int confirm = javax.swing.JOptionPane.showConfirmDialog(this,
            "Delete assignment record?\n"
            + "AS_ID: " + asId + "\n"
            + "Task ID: " + taskId + "  |  Assigned To ID: " + assignedTo + "\n\n"
            + "Are you sure?",
            "Confirm Delete", javax.swing.JOptionPane.YES_NO_OPTION,
            javax.swing.JOptionPane.WARNING_MESSAGE);

        if (confirm != javax.swing.JOptionPane.YES_OPTION) return;

        String sql = "DELETE FROM \"ASSIGNED TASK\" WHERE AS_ID = ?";

        try (Connection conn = config.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, asId);
            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                javax.swing.JOptionPane.showMessageDialog(this,
                    "Record deleted successfully!",
                    "Deleted", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                displayAssigned();
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
        user = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        user1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        HOME = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tableassined = new javax.swing.JTable();
        UP = new javax.swing.JButton();
        ADD = new javax.swing.JButton();
        DEL = new javax.swing.JButton();
        SEARCH = new javax.swing.JTextField();
        Search = new javax.swing.JButton();

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

        user.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        user.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        user.setText("VIEW USER");
        user.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                userMouseClicked(evt);
            }
        });
        jPanel3.add(user, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 60, 70, 30));

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
        jPanel3.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 130, 150, 30));

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

        tableassined.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(tableassined);

        jPanel1.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 120, 400, 180));

        UP.setBackground(new java.awt.Color(0, 102, 204));
        UP.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        UP.setText("UPDATE");
        UP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UPActionPerformed(evt);
            }
        });
        jPanel1.add(UP, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 90, -1, 20));

        ADD.setBackground(new java.awt.Color(0, 102, 204));
        ADD.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        ADD.setText("ADD");
        ADD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ADDActionPerformed(evt);
            }
        });
        jPanel1.add(ADD, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 90, -1, 20));

        DEL.setBackground(new java.awt.Color(0, 102, 204));
        DEL.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        DEL.setText("DELETE");
        DEL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DELActionPerformed(evt);
            }
        });
        jPanel1.add(DEL, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 90, -1, 20));

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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 582, javax.swing.GroupLayout.PREFERRED_SIZE)
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

    private void userMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_userMouseClicked
        userView view = new userView();
        view.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_userMouseClicked

    private void jLabel4MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel4MouseClicked

        SessionManager.getInstance().clearSession();
        LOGin log = new LOGin();
        log.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_jLabel4MouseClicked

    private void jLabel4MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel4MouseEntered
        // TODO add your handling code here:
    }//GEN-LAST:event_jLabel4MouseEntered

    private void user1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_user1MouseClicked
        TASK ts = new TASK();
        ts.setVisible(true);
        this.dispose();

    }//GEN-LAST:event_user1MouseClicked

    private void jLabel3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel3MouseClicked
        viewAssigned ASS = new viewAssigned();
        ASS.setVisible(true);
        this.dispose();

        // TODO add your handling code here:
    }//GEN-LAST:event_jLabel3MouseClicked

    private void ADDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ADDActionPerformed
            viewTask v = new viewTask();
            v.setVisible(true);
            this.dispose();



        // TODO add your handling code here:
    }//GEN-LAST:event_ADDActionPerformed

    private void SEARCHActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SEARCHActionPerformed

        searchUser(SEARCH.getText());

        // TODO add your handling code here:
    }//GEN-LAST:event_SEARCHActionPerformed

    private void SearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SearchActionPerformed
        searchUser(SEARCH.getText());

        // TODO add your handling code here:
    }//GEN-LAST:event_SearchActionPerformed

    private void UPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UPActionPerformed
          updateAssigned();
    // TODO add your handling code here:
    }//GEN-LAST:event_UPActionPerformed

    private void DELActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DELActionPerformed
                  deleteAssigned();   
// TODO add your handling code here:
        // TODO add your handling code here:
    }//GEN-LAST:event_DELActionPerformed

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
            java.util.logging.Logger.getLogger(viewAssigned.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(viewAssigned.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(viewAssigned.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(viewAssigned.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(viewAssigned.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(viewAssigned.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(viewAssigned.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(viewAssigned.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new viewAssigned().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton ADD;
    private javax.swing.JButton DEL;
    private javax.swing.JButton HOME;
    private javax.swing.JTextField SEARCH;
    private javax.swing.JButton Search;
    private javax.swing.JButton UP;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tableassined;
    private javax.swing.JLabel user;
    private javax.swing.JLabel user1;
    // End of variables declaration//GEN-END:variables
}
