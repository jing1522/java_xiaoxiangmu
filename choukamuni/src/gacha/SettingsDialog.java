package gacha;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * 设置对话框 —— 修改抽卡概率参数，保存后生效并重置游戏状态。
 */
public class SettingsDialog extends JDialog {
    private final ProbabilityConfig config;
    private boolean confirmed = false;

    private final JTextField fiveStarRateField;
    private final JTextField fourStarRateField;
    private final JTextField softPityStartField;
    private final JTextField softPityIncrementField;
    private final JTextField hardPityField;
    private final JTextField initialXingShengField;
    private final JTextField costPerPullField;

    public SettingsDialog(JFrame owner, ProbabilityConfig config) {
        super(owner, "抽卡设置", true);
        this.config = config;
        setSize(380, 380);
        setLocationRelativeTo(owner);
        setResizable(false);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(42, 47, 58));
        panel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JPanel form = new JPanel(new GridLayout(7, 2, 10, 8));
        form.setOpaque(false);

        Font labelFont = new Font("Microsoft YaHei", Font.PLAIN, 13);
        Color labelColor = new Color(200, 200, 200);

        fiveStarRateField = addRow(form, "五星基础概率", config.baseFiveStarRate, labelFont, labelColor);
        fourStarRateField = addRow(form, "四星基础概率", config.baseFourStarRate, labelFont, labelColor);
        softPityStartField = addRow(form, "软保底起始抽数", config.softPityStart, labelFont, labelColor);
        softPityIncrementField = addRow(form, "软保底每抽递增", config.softPityIncrement, labelFont, labelColor);
        hardPityField = addRow(form, "硬保底抽数", config.hardPityPulls, labelFont, labelColor);
        initialXingShengField = addRow(form, "初始星声", config.initialXingSheng, labelFont, labelColor);
        costPerPullField = addRow(form, "每抽消耗", config.costPerPull, labelFont, labelColor);

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonRow.setOpaque(false);

        JButton saveBtn = new JButton("保存");
        saveBtn.setFont(new Font("Microsoft YaHei", Font.BOLD, 13));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setBackground(new Color(100, 180, 120));
        saveBtn.setFocusPainted(false);
        saveBtn.addActionListener(e -> { saveConfig(); });

        JButton cancelBtn = new JButton("取消");
        cancelBtn.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setBackground(new Color(120, 120, 130));
        cancelBtn.setFocusPainted(false);
        cancelBtn.addActionListener(e -> dispose());

        buttonRow.add(saveBtn);
        buttonRow.add(cancelBtn);

        panel.add(form, BorderLayout.CENTER);
        panel.add(buttonRow, BorderLayout.SOUTH);
        add(panel);
    }

    private JTextField addRow(JPanel form, String label, Object value, Font font, Color color) {
        JLabel lbl = new JLabel(label + "：");
        lbl.setFont(font);
        lbl.setForeground(color);
        form.add(lbl);

        JTextField field = new JTextField(String.valueOf(value));
        field.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        field.setBackground(new Color(60, 65, 75));
        field.setForeground(new Color(220, 220, 220));
        field.setCaretColor(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 100, 110), 1),
            new EmptyBorder(4, 6, 4, 6)
        ));
        form.add(field);
        return field;
    }

    private void saveConfig() {
        try {
            config.baseFiveStarRate = Double.parseDouble(fiveStarRateField.getText());
            config.baseFourStarRate = Double.parseDouble(fourStarRateField.getText());
            config.softPityStart = Integer.parseInt(softPityStartField.getText());
            config.softPityIncrement = Double.parseDouble(softPityIncrementField.getText());
            config.hardPityPulls = Integer.parseInt(hardPityField.getText());
            config.initialXingSheng = Integer.parseInt(initialXingShengField.getText());
            config.costPerPull = Integer.parseInt(costPerPullField.getText());
            confirmed = true;
            dispose();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "请输入有效的数字！", "格式错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isConfirmed() { return confirmed; }
}
