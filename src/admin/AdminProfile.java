/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package admin;

import config.SessionManager;
import config.config;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Administrator
 */
public class AdminProfile extends javax.swing.JFrame {

   private static final String DEFAULT_IMAGE_RESOURCE = "/image/Profile.png";
    private final String imagePackagePath;
 
    public AdminProfile() {
        initComponents();
        imagePackagePath = System.getProperty("user.dir") + File.separator
                + "src" + File.separator + "image" + File.separator;
        loadProfile();
        loadProfileImage();
    }
 
     private void loadProfile() {
        SessionManager session = SessionManager.getInstance();
        DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Name", "Email", "Type", "Status"}, 0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        String sql = "SELECT acc_id, name, email, type, status FROM ACCOUNTS WHERE acc_id = ?";
        try (Connection conn = config.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, session.getUserId());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("acc_id"), rs.getString("name"),
                    rs.getString("email"), rs.getString("type"), rs.getString("status")
                });
            }
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this,
                "Error loading profile: " + e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
        table.setModel(model);
    }
 
    private void loadProfileImage() {
        ImageIcon icon = null;
        java.net.URL url = getClass().getResource(DEFAULT_IMAGE_RESOURCE);
        if (url != null) icon = new ImageIcon(url);
        if (icon == null || icon.getIconWidth() <= 0) {
            File f = new File(imagePackagePath + "Profile.png");
            if (f.exists()) icon = new ImageIcon(f.getAbsolutePath());
        }
        if (icon != null && icon.getIconWidth() > 0) {
            Image scaled = icon.getImage().getScaledInstance(130, 100, Image.SCALE_SMOOTH);
            profile.setIcon(new ImageIcon(scaled));
            profile.setText("");
        } else {
            profile.setText("No Image");
            profile.setForeground(java.awt.Color.WHITE);
        }
    }
 
    private void editProfileImage() {
        int confirm = javax.swing.JOptionPane.showConfirmDialog(this,
            "Do you want to replace the current profile image?",
            "Edit Profile Image", javax.swing.JOptionPane.YES_NO_OPTION,
            javax.swing.JOptionPane.QUESTION_MESSAGE);
        if (confirm != javax.swing.JOptionPane.YES_OPTION) return;
 
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select New Profile Image");
        chooser.setFileFilter(new FileNameExtensionFilter(
            "Image Files (*.png, *.jpg, *.jpeg)", "png", "jpg", "jpeg"));
        chooser.setAcceptAllFileFilterUsed(false);
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
 
        File selected = chooser.getSelectedFile();
        String nameLower = selected.getName().toLowerCase();
        if (!nameLower.endsWith(".png") && !nameLower.endsWith(".jpg") && !nameLower.endsWith(".jpeg")) {
            javax.swing.JOptionPane.showMessageDialog(this,
                "Invalid file! Please select a PNG or JPG image.",
                "Invalid File", javax.swing.JOptionPane.ERROR_MESSAGE); return;
        }
        if (selected.length() == 0) {
            javax.swing.JOptionPane.showMessageDialog(this,
                "The selected file is empty. Please choose a valid image.",
                "Empty File", javax.swing.JOptionPane.ERROR_MESSAGE); return;
        }
        ImageIcon testIcon = new ImageIcon(selected.getAbsolutePath());
        if (testIcon.getIconWidth() <= 0) {
            javax.swing.JOptionPane.showMessageDialog(this,
                "File cannot be read as an image. Please try another.",
                "Invalid Image", javax.swing.JOptionPane.ERROR_MESSAGE); return;
        }
 
        File destination = new File(imagePackagePath + "Profile.png");
        try {
            destination.getParentFile().mkdirs();
            Files.copy(selected.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
            Image scaled = testIcon.getImage().getScaledInstance(130, 100, Image.SCALE_SMOOTH);
            profile.setIcon(new ImageIcon(scaled));
            profile.setText("");
            javax.swing.JOptionPane.showMessageDialog(this,
                "Profile image updated and saved successfully!",
                "Success", javax.swing.JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            javax.swing.JOptionPane.showMessageDialog(this,
                "Failed to save image: " + e.getMessage(),
                "Save Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }
 
    private void deleteProfileImage() {
        File current = new File(imagePackagePath + "Profile.png");
        if (!current.exists()) {
            javax.swing.JOptionPane.showMessageDialog(this,
                "No profile image file found to delete.",
                "Nothing to Delete", javax.swing.JOptionPane.WARNING_MESSAGE); return;
        }
        int confirm = javax.swing.JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete the current profile image?\nThe default picture will be restored.",
            "Delete Profile Image", javax.swing.JOptionPane.YES_NO_OPTION,
            javax.swing.JOptionPane.WARNING_MESSAGE);
        if (confirm != javax.swing.JOptionPane.YES_OPTION) return;
 
        if (!current.delete()) {
            javax.swing.JOptionPane.showMessageDialog(this,
                "Could not delete the image. It may be in use.",
                "Delete Failed", javax.swing.JOptionPane.ERROR_MESSAGE); return;
        }
 
        java.net.URL defaultUrl = getClass().getResource(DEFAULT_IMAGE_RESOURCE);
        if (defaultUrl != null) {
            try (java.io.InputStream in = defaultUrl.openStream()) {
                current.getParentFile().mkdirs();
                Files.copy(in, current.toPath(), StandardCopyOption.REPLACE_EXISTING);
                loadProfileImage();
                javax.swing.JOptionPane.showMessageDialog(this,
                    "Profile image deleted. Default image restored.",
                    "Deleted", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                profile.setIcon(null); profile.setText("No Image");
                profile.setForeground(java.awt.Color.WHITE);
            }
        } else {
            profile.setIcon(null); profile.setText("No Image");
            profile.setForeground(java.awt.Color.WHITE);
            javax.swing.JOptionPane.showMessageDialog(this, "Profile image deleted.",
                "Deleted", javax.swing.JOptionPane.INFORMATION_MESSAGE);
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

        jButton2 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        jButton3 = new javax.swing.JButton();
        Back = new javax.swing.JButton();
        profile = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();

        jButton2.setText("EDIT");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(0, 102, 255));
        jPanel1.setLayout(null);

        jPanel2.setBackground(new java.awt.Color(0, 0, 255));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Century Gothic", 1, 18)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("PROFILE ");
        jPanel2.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 10, 290, 60));

        jPanel1.add(jPanel2);
        jPanel2.setBounds(0, 0, 600, 80);

        table.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(table);

        jPanel1.add(jScrollPane1);
        jScrollPane1.setBounds(10, 130, 400, 200);

        jButton3.setBackground(new java.awt.Color(0, 102, 204));
        jButton3.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        jButton3.setText("UPDATE");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton3);
        jButton3.setBounds(330, 100, 83, 27);

        Back.setBackground(new java.awt.Color(0, 102, 204));
        Back.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        Back.setText("BACK");
        Back.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BackActionPerformed(evt);
            }
        });
        jPanel1.add(Back);
        Back.setBounds(510, 90, 71, 27);
        jPanel1.add(profile);
        profile.setBounds(450, 130, 130, 100);

        jButton1.setText("DELETE");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton1);
        jButton1.setBounds(510, 240, 80, 23);

        jButton4.setText("EDIT");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton4);
        jButton4.setBounds(450, 240, 60, 23);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 600, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 385, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        UpdateAdProf up = new UpdateAdProf();
        up.setVisible(true);
        this.dispose();

        // TODO add your handling code here:
    }//GEN-LAST:event_jButton3ActionPerformed

    private void BackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BackActionPerformed
        admin ad = new admin();
        ad.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_BackActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        deleteProfileImage();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
         editProfileImage();
    }//GEN-LAST:event_jButton4ActionPerformed

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
            java.util.logging.Logger.getLogger(AdminProfile.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AdminProfile.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AdminProfile.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AdminProfile.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

          try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName()); break;
                }
            }
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(AdminProfile.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        java.awt.EventQueue.invokeLater(() -> new AdminProfile().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton Back;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel profile;
    private javax.swing.JTable table;
    // End of variables declaration//GEN-END:variables
}
