package com.school045;

import com.school045.config.DatabaseConfig;
import com.school045.db.Database;
import com.school045.service.StaffService;
import com.school045.ui.StaffMainPanel;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class StaffManageApplication {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // keep default look & feel
        }

        DatabaseConfig config = new DatabaseConfig();
        Database database = new Database(config);
        database.initializeSchemaIfNeeded();
        StaffService service = new StaffService(database);
        boolean screenshotOnly = hasArg(args, "--screenshot");

        SwingUtilities.invokeLater(() -> {
            StaffMainPanel panel = new StaffMainPanel(service);
            if (screenshotOnly) {
                saveScreenshot(panel);
                System.exit(0);
                return;
            }
            JFrame frame = new JFrame("教职工管理系统（045）");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    private static boolean hasArg(String[] args, String option) {
        if (args == null) {
            return false;
        }
        for (String arg : args) {
            if (option.equalsIgnoreCase(arg)) {
                return true;
            }
        }
        return false;
    }

    private static void saveScreenshot(StaffMainPanel panel) {
        Dimension size = new Dimension(1200, 800);
        panel.setPreferredSize(size);
        panel.setSize(size);
        panel.doLayout();
        BufferedImage image = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, size.width, size.height);
        panel.printAll(g2);
        g2.dispose();
        try {
            ImageIO.write(image, "png", new File("ui-preview.png"));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(panel, "无法保存界面截图: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}
