/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package user;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
/**
 *
 * @author USER31
 */
public class TRY extends javax.swing.JFrame {

 private File uploadedImageFile = null;
 
    public TRY() {
        initComponents();
 
        // Style the uploadhere label as an image drop zone
        uploadhere.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        uploadhere.setVerticalAlignment(javax.swing.SwingConstants.CENTER);
        uploadhere.setText("No image selected");
        uploadhere.setForeground(new java.awt.Color(255, 255, 255));
        uploadhere.setFont(new java.awt.Font("Century Gothic", Font.PLAIN, 11));
        uploadhere.setBorder(javax.swing.BorderFactory.createLineBorder(
            new java.awt.Color(255, 255, 255)));
    }
 
    // ---------------------------------------------------------------
    // UPLOAD: opens chooser → shows image in uploadhere label
    // ---------------------------------------------------------------
    private void uploadImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select an Image");
        chooser.setFileFilter(new FileNameExtensionFilter(
            "Image Files (jpg, png, gif, bmp)", "jpg", "jpeg", "png", "gif", "bmp"));
        chooser.setAcceptAllFileFilterUsed(false);
 
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            uploadedImageFile = chooser.getSelectedFile();
 
            // Show preview inside uploadhere — use its actual size
            java.awt.Image scaled = new ImageIcon(uploadedImageFile.getAbsolutePath())
                .getImage()
                .getScaledInstance(
                    uploadhere.getWidth()  > 0 ? uploadhere.getWidth()  : 270,
                    uploadhere.getHeight() > 0 ? uploadhere.getHeight() : 100,
                    java.awt.Image.SCALE_SMOOTH);
            uploadhere.setIcon(new ImageIcon(scaled));
            uploadhere.setText("");
 
            JOptionPane.showMessageDialog(this,
                "Image attached: " + uploadedImageFile.getName() +
                "\nClick PRINT to print text and image.",
                "Image Attached", JOptionPane.INFORMATION_MESSAGE);
        }
    }
 
    // ---------------------------------------------------------------
    // PRINT: Page 1 = text, Page 2 = image (if uploaded)
    //        If no image → only Page 1 (text only)
    //        If no text  → only Page 1 (image only, fits full page)
    // ---------------------------------------------------------------
    private void printDocument() {
        final String textToPrint = jTextArea1.getText().trim();
        final boolean hasText  = !textToPrint.isEmpty();
        final boolean hasImage = uploadedImageFile != null;
 
        // Load image once outside Printable to avoid repeated IO
        final BufferedImage[] imgHolder = new BufferedImage[1];
        if (hasImage) {
            try {
                imgHolder[0] = ImageIO.read(uploadedImageFile);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                    "Could not read image: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
 
        // Determine total pages
        // Page 0 = text (always if text exists, or image-only if no text)
        // Page 1 = image (only if BOTH text AND image exist)
        final int totalPages = (hasText && hasImage) ? 2 : 1;
 
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("User Document");
 
        job.setPrintable(new Printable() {
            @Override
            public int print(Graphics g, PageFormat pf, int pageIndex) throws PrinterException {
                if (pageIndex >= totalPages) return Printable.NO_SUCH_PAGE;
 
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
 
                int x     = (int) pf.getImageableX() + 20;
                int y     = (int) pf.getImageableY() + 20;
                int pageW = (int) pf.getImageableWidth()  - 40;
                int pageH = (int) pf.getImageableHeight() - 40;
 
                g2.setColor(java.awt.Color.BLACK);
 
                // ── PAGE 0 ──
                if (pageIndex == 0) {
                    if (hasText && hasImage) {
                        // Text-only page
                        drawText(g2, textToPrint, x, y, 18);
 
                    } else if (hasText) {
                        // Text only (no image uploaded)
                        drawText(g2, textToPrint, x, y, 18);
 
                    } else {
                        // Image only (no text typed) — full page image
                        drawImage(g2, imgHolder[0], x, y, pageW, pageH);
                    }
                }
 
                // ── PAGE 1 (image page, only reached when both text+image exist) ──
                if (pageIndex == 1) {
                    drawImage(g2, imgHolder[0], x, y, pageW, pageH);
                }
 
                return Printable.PAGE_EXISTS;
            }
        });
 
        if (job.printDialog()) {
            try {
                job.print();
                if (hasImage) saveImageToProject(uploadedImageFile);
                String msg = hasText && hasImage
                    ? "Printed!\nPage 1: Text\nPage 2: Image"
                    : "Printed successfully!";
                JOptionPane.showMessageDialog(this, msg, "Done", JOptionPane.INFORMATION_MESSAGE);
            } catch (PrinterException e) {
                JOptionPane.showMessageDialog(this,
                    "Print failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
 
    /** Draws multi-line text onto the graphics context */
    private void drawText(Graphics2D g2, String text, int x, int y, int lineHeight) {
        g2.setFont(new Font("Century Gothic", Font.PLAIN, 12));
        int lineY = y + 14;
        for (String line : text.split("\n")) {
            g2.drawString(line, x, lineY);
            lineY += lineHeight;
        }
    }
 
    /** Draws image scaled to fit within the given area, keeping aspect ratio */
    private void drawImage(Graphics2D g2, BufferedImage img, int x, int y, int maxW, int maxH) {
        if (img == null) return;
        double ratio = (double) img.getWidth() / img.getHeight();
        int drawW = maxW;
        int drawH = (int) (drawW / ratio);
        if (drawH > maxH) {
            drawH = maxH;
            drawW = (int) (drawH * ratio);
        }
        g2.drawImage(img, x, y, drawW, drawH, null);
    }
 
    // ---------------------------------------------------------------
    // Save image to src/image/ folder
    // ---------------------------------------------------------------
    private void saveImageToProject(File src) {
        try {
            File imageDir = new File(new File("").getAbsolutePath()
                + File.separator + "src" + File.separator + "image");
            if (!imageDir.exists()) imageDir.mkdirs();
            File dest = new File(imageDir, src.getName());
            Files.copy(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("[Image saved to: " + dest.getAbsolutePath() + "]");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                "Save failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
 
 
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        PRINT = new javax.swing.JButton();
        upload = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jPanel3 = new javax.swing.JPanel();
        uploadhere = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(0, 102, 255));
        jPanel1.setLayout(null);

        jPanel2.setBackground(new java.awt.Color(0, 0, 255));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Century Gothic", 1, 18)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("USER DASHBOARD");
        jPanel2.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 10, 290, 60));

        jPanel1.add(jPanel2);
        jPanel2.setBounds(0, 0, 580, 80);

        jButton1.setBackground(new java.awt.Color(0, 153, 204));
        jButton1.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        jButton1.setText("BACK");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton1);
        jButton1.setBounds(470, 100, 71, 27);

        PRINT.setBackground(new java.awt.Color(0, 153, 204));
        PRINT.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        PRINT.setText("PRINT");
        PRINT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PRINTActionPerformed(evt);
            }
        });
        jPanel1.add(PRINT);
        PRINT.setBounds(470, 330, 73, 23);

        upload.setBackground(new java.awt.Color(0, 153, 204));
        upload.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        upload.setText("UPLOAD IMAGE");
        upload.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                uploadActionPerformed(evt);
            }
        });
        jPanel1.add(upload);
        upload.setBounds(300, 340, 140, 27);

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        jPanel1.add(jScrollPane1);
        jScrollPane1.setBounds(20, 90, 430, 160);

        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        jPanel3.add(uploadhere, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 270, 100));

        jPanel1.add(jPanel3);
        jPanel3.setBounds(20, 270, 270, 100);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 579, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 385, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
       user us = new user();
       us.setVisible(true);
       this.dispose();
        

// TODO add your handling code here:
    }//GEN-LAST:event_jButton1ActionPerformed

    private void PRINTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PRINTActionPerformed
       if (jTextArea1.getText().trim().isEmpty() && uploadedImageFile == null) {
            JOptionPane.showMessageDialog(this,
                "Nothing to print! Type something or upload an image first.",
                "Empty", JOptionPane.WARNING_MESSAGE);
            return;
        }
        printDocument();
    }//GEN-LAST:event_PRINTActionPerformed
 
    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        uploadImage();        // TODO add your handling code here:
    }//GEN-LAST:event_PRINTActionPerformed

    private void uploadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_uploadActionPerformed
         uploadImage();  // TODO add your handling code here:
    }//GEN-LAST:event_uploadActionPerformed

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
            java.util.logging.Logger.getLogger(TRY.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(TRY.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(TRY.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TRY.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
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
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(TRY.class.getName())
                .log(java.util.logging.Level.SEVERE, null, ex);
        }
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() { new TRY().setVisible(true); }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton PRINT;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JButton upload;
    private javax.swing.JLabel uploadhere;
    // End of variables declaration//GEN-END:variables
}
