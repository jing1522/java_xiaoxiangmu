package gacha;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * 抽卡模拟器 —— 基于 Swing 的桌面应用。
 * 核心机制：稀有度概率 + 软/硬保底 + 大保底（歪了下次UP必出）+ 十连保底四星。
 */
public class GachaSimulator extends JFrame {

    // ==================== 配置与状态 ====================

    private final ProbabilityConfig config = new ProbabilityConfig();
    private final GameState state = new GameState(config.initialXingSheng);

    // ==================== UI组件 ====================

    private final DefaultListModel<String> resultListModel = new DefaultListModel<>();
    private final JList<String> resultList = new JList<>(resultListModel);
    private final JLabel statusLabel = new JLabel("请点击抽卡按钮");
    private final Random random = new Random();

    // ==================== 卡池定义 ====================

    private final List<String> fiveStarPool = Arrays.asList(state.upCharacter, "菲", "妮", "莫", "空", "维");
    private final List<String> fourStarPool = Arrays.asList("桃", "秧", "卜");
    private final List<String> threeStarPool = Arrays.asList("风车1", "风车2", "风车3");

    // ==================== UI标签（需要动态更新的） ====================

    private final JLabel pullCountLabel = new JLabel("抽卡次数：" + state.totalPulls);
    private final JLabel resourceLabel = new JLabel("星声：" + state.xingShengCount);
    private JLabel upLabel;
    private JLabel fivePityLabel;
    private JLabel fourPityLabel;

    // ==================== 入口与窗口初始化 ====================

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GachaSimulator::new);
    }

    public GachaSimulator() {
        super("抽卡模拟器");
        initUI();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(500, 650));
        setSize(560, 720);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        applyUIStyle();

        add(createTopPanel(), BorderLayout.NORTH);
        add(createResultPanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);

        setVisible(true);
    }

    // ==================== 主题样式（深色主题 + 金色点缀） ====================

    private void applyUIStyle() {
        Font defaultFont = new Font("Microsoft YaHei", Font.PLAIN, 13);
        Font titleFont = new Font("Microsoft YaHei", Font.BOLD, 14);
        Font buttonFont = new Font("Microsoft YaHei", Font.BOLD, 14);

        UIManager.put("Label.font", defaultFont);
        UIManager.put("Button.font", buttonFont);
        UIManager.put("List.font", defaultFont);

        Color bgColor = new Color(30, 34, 45);
        Color panelColor = new Color(42, 47, 58);
        Color goldColor = new Color(212, 175, 55);

        getContentPane().setBackground(bgColor);

        statusLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 13));
        statusLabel.setForeground(new Color(200, 200, 200));

        pullCountLabel.setFont(titleFont);
        pullCountLabel.setForeground(new Color(180, 200, 220));
        resourceLabel.setFont(titleFont);
        resourceLabel.setForeground(goldColor);

        resultList.setCellRenderer(new GachaResultRenderer());
        resultList.setFixedCellHeight(28);
        resultList.setBackground(panelColor);
        resultList.setForeground(new Color(220, 220, 220));
        resultList.setSelectionBackground(new Color(60, 70, 90));
        resultList.setSelectionForeground(Color.WHITE);
    }

    // ==================== 顶部面板：标题、UP角色、抽数、星声、保底 ====================

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 15, 5, 15));

        JLabel titleLabel = new JLabel("⬥ 抽 卡 模 拟 器 ⬥", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 20));
        titleLabel.setForeground(new Color(212, 175, 55));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JPanel infoRow = new JPanel(new BorderLayout());
        infoRow.setOpaque(false);

        upLabel = new JLabel("✦ UP: " + state.upCharacter + " ✦");
        upLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 16));
        upLabel.setForeground(new Color(255, 180, 80));

        infoRow.add(upLabel, BorderLayout.WEST);
        infoRow.add(pullCountLabel, BorderLayout.CENTER);
        infoRow.add(resourceLabel, BorderLayout.EAST);

        JPanel pityRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        pityRow.setOpaque(false);
        fivePityLabel = new JLabel("五星保底: 0/" + config.hardPityPulls);
        fivePityLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 11));
        fivePityLabel.setForeground(new Color(180, 160, 120));
        fourPityLabel = new JLabel("四星保底: 0/10");
        fourPityLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 11));
        fourPityLabel.setForeground(new Color(160, 140, 200));
        pityRow.add(fivePityLabel);
        pityRow.add(fourPityLabel);

        JPanel statusRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        statusRow.setOpaque(false);
        statusRow.add(statusLabel);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(infoRow, BorderLayout.CENTER);

        JPanel bottomWrap = new JPanel(new BorderLayout());
        bottomWrap.setOpaque(false);
        bottomWrap.add(pityRow, BorderLayout.NORTH);
        bottomWrap.add(statusRow, BorderLayout.SOUTH);
        panel.add(bottomWrap, BorderLayout.SOUTH);

        return panel;
    }

    // ==================== 中间面板：抽卡记录列表（点击条目查看详情） ====================

    private JScrollPane createResultPanel() {
        resultList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = resultList.locationToIndex(e.getPoint());
                if (index >= 0) {
                    String itemText = resultListModel.getElementAt(index);
                    int drawNumber = index + 1;
                    JOptionPane.showMessageDialog(
                        resultList,
                        "这是第 " + drawNumber + " 抽\n" + itemText,
                        "抽卡详情",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                }
            }
        });
        JScrollPane scrollPane = new JScrollPane(resultList);
        scrollPane.setBorder(
            BorderFactory.createTitledBorder(
                new LineBorder(new Color(212, 175, 55, 100), 1),
                "抽卡记录",
                TitledBorder.CENTER,
                TitledBorder.TOP,
                new Font("Microsoft YaHei", Font.BOLD, 13),
                new Color(212, 175, 55)
            )
        );
        scrollPane.getViewport().setBackground(new Color(42, 47, 58));
        scrollPane.setBackground(new Color(30, 34, 45));
        return scrollPane;
    }

    // ==================== 底部按钮面板：主按钮 + 功能按钮 + 无限模式 ====================

    private JPanel createButtonPanel() {
        JButton drawButton = createStyledButton("单 抽", new Color(212, 175, 55));
        drawButton.addActionListener(this::onDrawClick);

        JButton tenDrawButton = createStyledButton("十 连 抽", new Color(180, 130, 240));
        tenDrawButton.addActionListener(this::onTenDrawClick);

        JButton resetXingShengBtn = createSmallButton("重置星声", new Color(100, 160, 200));
        resetXingShengBtn.addActionListener(e -> onResetXingSheng());

        JButton resetPullsBtn = createSmallButton("重置抽数", new Color(100, 180, 160));
        resetPullsBtn.addActionListener(e -> onResetPulls());

        JButton changeUpBtn = createSmallButton("改UP名", new Color(200, 150, 90));
        changeUpBtn.addActionListener(e -> onChangeUpCharacter());

        JButton goldRecordBtn = createSmallButton("金记录", new Color(220, 180, 40));
        goldRecordBtn.addActionListener(e -> onShowGoldRecord());

        JButton settingsBtn = createSmallButton("设置", new Color(140, 150, 170));
        settingsBtn.addActionListener(e -> onOpenSettings());

        JCheckBox infiniteCheck = new JCheckBox("无限星声");
        infiniteCheck.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        infiniteCheck.setForeground(new Color(180, 200, 220));
        infiniteCheck.setOpaque(false);
        infiniteCheck.setFocusPainted(false);
        infiniteCheck.addActionListener(e -> {
            state.infiniteMode = infiniteCheck.isSelected();
            refreshResourceLabel();
        });

        JPanel mainRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        mainRow.setOpaque(false);
        mainRow.add(drawButton);
        mainRow.add(tenDrawButton);

        JPanel utilRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        utilRow.setOpaque(false);
        utilRow.add(resetXingShengBtn);
        utilRow.add(resetPullsBtn);
        utilRow.add(changeUpBtn);
        utilRow.add(goldRecordBtn);
        utilRow.add(settingsBtn);

        JPanel checkRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        checkRow.setOpaque(false);
        checkRow.add(infiniteCheck);

        JPanel buttonArea = new JPanel();
        buttonArea.setOpaque(false);
        buttonArea.setLayout(new BoxLayout(buttonArea, BoxLayout.Y_AXIS));
        buttonArea.add(mainRow);
        buttonArea.add(Box.createVerticalStrut(6));
        buttonArea.add(utilRow);
        buttonArea.add(Box.createVerticalStrut(4));
        buttonArea.add(checkRow);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 12, 10));
        panel.add(buttonArea, BorderLayout.CENTER);
        return panel;
    }

    // ==================== 按钮工厂方法 ====================

    private JButton createStyledButton(String text, Color baseColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Microsoft YaHei", Font.BOLD, 15));
        button.setForeground(Color.WHITE);
        button.setBackground(baseColor);
        button.setFocusPainted(false);
        button.setBorder(new CompoundBorder(
            new LineBorder(baseColor.brighter(), 1),
            new EmptyBorder(8, 24, 8, 24)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { button.setBackground(baseColor.brighter()); }
            @Override
            public void mouseExited(MouseEvent e) { button.setBackground(baseColor); }
        });
        return button;
    }

    private JButton createSmallButton(String text, Color baseColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(baseColor);
        button.setFocusPainted(false);
        button.setBorder(new CompoundBorder(
            new LineBorder(baseColor.brighter(), 1),
            new EmptyBorder(5, 14, 5, 14)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { button.setBackground(baseColor.brighter()); }
            @Override
            public void mouseExited(MouseEvent e) { button.setBackground(baseColor); }
        });
        return button;
    }

    // ==================== 单抽 / 十连抽事件处理（十连保底四星） ====================

    private void onDrawClick(ActionEvent e) {
        GachaItem item = drawGacha(false);
        if (item != null) {
            addResult(item);
        }
    }

    private void onTenDrawClick(ActionEvent e) {
        if (!state.infiniteMode && state.xingShengCount < config.costPerPull * 10) {
            JOptionPane.showMessageDialog(this,
                "星声不足，无法十连！\n需要 " + (config.costPerPull * 10) + " 星声，当前：" + state.xingShengCount,
                "资源不足",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean hasFourStarOrAbove = false;
        for (int i = 0; i < 10; i++) {
            boolean force = (i == 9 && !hasFourStarOrAbove);
            GachaItem item = drawGacha(force);
            if (item == null) {
                break;
            }
            if (!"三星".equals(item.getRarity())) {
                hasFourStarOrAbove = true;
            }
            addResult(item);
        }
    }

    // ==================== 功能按钮事件处理 ====================

    private void onResetXingSheng() {
        state.xingShengCount = config.initialXingSheng;
        refreshResourceLabel();
        statusLabel.setText("星声已重置为 " + config.initialXingSheng);
        statusLabel.setForeground(new Color(200, 200, 200));
    }

    private void onResetPulls() {
        state.totalPulls = 0;
        state.pullsSinceLastFiveStar = 0;
        state.pullsSinceLastFourStar = 0;
        state.guaranteeNextLyn = false;
        state.fiveStarRecords.clear();
        resultListModel.clear();
        pullCountLabel.setText("抽卡次数：" + state.totalPulls);
        statusLabel.setText("抽数已重置");
        statusLabel.setForeground(new Color(200, 200, 200));
        updatePityDisplay();
    }

    private void onChangeUpCharacter() {
        String newName = JOptionPane.showInputDialog(this,
            "请输入新的UP角色名称：", state.upCharacter);
        if (newName != null && !newName.trim().isEmpty()) {
            newName = newName.trim();
            state.upCharacter = newName;
            fiveStarPool.set(0, newName);
            upLabel.setText("✦ UP: " + newName + " ✦");
            statusLabel.setText("UP角色已改为：" + newName);
            statusLabel.setForeground(new Color(255, 180, 80));
        }
    }

    private void onShowGoldRecord() {
        if (state.fiveStarRecords.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "暂无五星记录，先去抽卡吧！",
                "金记录",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (GoldRecord r : state.fiveStarRecords) {
            sb.append(r.name).append("：").append(r.pulls).append(" 抽\n");
        }
        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        textArea.setBackground(new Color(42, 47, 58));
        textArea.setForeground(new Color(220, 220, 220));
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(350, 250));
        JOptionPane.showMessageDialog(this, scrollPane,
            "金记录", JOptionPane.INFORMATION_MESSAGE);
    }

    private void onOpenSettings() {
        SettingsDialog dialog = new SettingsDialog(this, config);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            state.xingShengCount = config.initialXingSheng;
            fivePityLabel.setText("五星保底: 0/" + config.hardPityPulls);
            onResetPulls();
            refreshResourceLabel();
            statusLabel.setText("设置已更新");
            statusLabel.setForeground(new Color(200, 200, 200));
        }
    }

    // ==================== 辅助：刷新资源标签 ====================

    private void refreshResourceLabel() {
        resourceLabel.setText(state.infiniteMode ? "星声：∞" : ("星声：" + state.xingShengCount));
        resourceLabel.setForeground(state.infiniteMode
            ? new Color(100, 220, 150)
            : new Color(212, 175, 55));
    }

    // ==================== 核心抽卡逻辑 ====================

    private GachaItem drawGacha(boolean forceFourStarOrAbove) {
        if (!state.infiniteMode && state.xingShengCount < config.costPerPull) {
            JOptionPane.showMessageDialog(this,
                "星声不足！当前剩余：" + state.xingShengCount,
                "无法抽卡",
                JOptionPane.WARNING_MESSAGE);
            return null;
        }
        state.pullsSinceLastFiveStar++;
        state.pullsSinceLastFourStar++;

        if (!state.infiniteMode) {
            state.xingShengCount -= config.costPerPull;
        }

        String rarity = determineRarity();

        if (forceFourStarOrAbove && "三星".equals(rarity)) {
            rarity = "四星";
        }

        GachaItem item;

        if ("五星".equals(rarity)) {
            int distFromLast = state.pullsSinceLastFiveStar;
            item = drawFiveStar(distFromLast);
            if (!state.infiniteMode) state.xingShengCount += 1000;
            state.pullsSinceLastFourStar = 0;
        } else if ("四星".equals(rarity)) {
            item = drawFromPool(fourStarPool, "四星");
            if (!state.infiniteMode) state.xingShengCount += 300;
            state.pullsSinceLastFourStar = 0;
        } else {
            item = drawFromPool(threeStarPool, "三星");
            if (!state.infiniteMode) state.xingShengCount += 50;
        }

        updateLynTracking(item);
        return item;
    }

    // ==================== 概率引擎：软保底递增 + 硬保底 + 四星保底 ====================

    private String determineRarity() {
        if (state.pullsSinceLastFiveStar >= config.hardPityPulls) {
            state.pullsSinceLastFourStar = 0;
            return "五星";
        }

        if (state.pullsSinceLastFourStar >= 10) {
            state.pullsSinceLastFourStar = 0;
            return "四星";
        }

        double fiveStarRate = config.baseFiveStarRate;
        if (state.pullsSinceLastFiveStar >= config.softPityStart) {
            int pityCount = state.pullsSinceLastFiveStar - config.softPityStart + 1;
            fiveStarRate = config.baseFiveStarRate + config.softPityIncrement * pityCount;
            if (fiveStarRate > 1.0) fiveStarRate = 1.0;
        }

        double fourStarRate = config.baseFourStarRate;
        double threeStarRate = 1.0 - fiveStarRate - fourStarRate;
        if (threeStarRate < 0) {
            fourStarRate = 1.0 - fiveStarRate;
        }

        double roll = random.nextDouble();

        if (roll < fiveStarRate) {
            return "五星";
        }
        if (roll < fiveStarRate + fourStarRate) {
            return "四星";
        }
        return "三星";
    }

    // ==================== 五星抽取 / 通用奖池抽取 ====================

    private GachaItem drawFiveStar(int distFromLast) {
        state.pullsSinceLastFiveStar = 0;

        String name;
        if (state.guaranteeNextLyn || random.nextDouble() < 0.5) {
            name = state.upCharacter;
            state.guaranteeNextLyn = false;
        } else {
            List<String> others = Arrays.asList("菲", "妮", "莫", "空", "维");
            name = others.get(random.nextInt(others.size()));
        }
        state.fiveStarRecords.add(new GoldRecord(name, distFromLast));
        return new GachaItem(name, "五星", distFromLast);
    }

    private GachaItem drawFromPool(List<String> pool, String rarity) {
        String name = pool.get(random.nextInt(pool.size()));
        return new GachaItem(name, rarity);
    }

    // ==================== 大保底追踪 ====================

    private void updateLynTracking(GachaItem item) {
        if (state.upCharacter.equals(item.getName())) {
            state.guaranteeNextLyn = false;
        } else if ("五星".equals(item.getRarity())) {
            state.guaranteeNextLyn = true;
        }
    }

    // ==================== 结果添加与UI状态刷新 ====================

    private void addResult(GachaItem item) {
        state.totalPulls++;
        pullCountLabel.setText("抽卡次数：" + state.totalPulls);
        refreshResourceLabel();
        resultListModel.addElement(item.toString());
        resultList.ensureIndexIsVisible(resultListModel.size() - 1);

        String raritySymbol;
        Color rarityColor;
        if ("五星".equals(item.getRarity())) {
            raritySymbol = "★★★★★";
            rarityColor = new Color(255, 200, 50);
        } else if ("四星".equals(item.getRarity())) {
            raritySymbol = "★★★★";
            rarityColor = new Color(180, 130, 240);
        } else {
            raritySymbol = "★★★";
            rarityColor = new Color(130, 170, 220);
        }
        statusLabel.setText(raritySymbol + "  " + item.getName());
        statusLabel.setForeground(rarityColor);

        updatePityDisplay();
    }

    // ==================== 保底计数显示更新（直接引用标签） ====================

    private void updatePityDisplay() {
        fivePityLabel.setText("五星保底: " + state.pullsSinceLastFiveStar + "/" + config.hardPityPulls);
        fourPityLabel.setText("四星保底: " + state.pullsSinceLastFourStar + "/10");
    }
}
