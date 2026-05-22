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
 * 核心机制：稀有度概率 + 软/硬保底 + 大保底（歪了下次UP必出）。
 */
public class GachaSimulator extends JFrame {

    // ==================== 数据模型与UI组件 ====================

    private final GameState state = new GameState();
    private final DefaultListModel<String> resultListModel = new DefaultListModel<>();
    private final JList<String> resultList = new JList<>(resultListModel);
    private final JLabel statusLabel = new JLabel("请点击抽卡按钮");
    private final Random random = new Random();

    // ==================== 卡池定义 ====================

    private final List<String> fiveStarPool = Arrays.asList(state.upCharacter, "菲", "妮", "莫", "空", "维");
    private final List<String> fourStarPool = Arrays.asList("桃", "秧", "卜");
    private final List<String> threeStarPool = Arrays.asList("风车1", "风车2", "风车3");

    // ==================== 概率常量 ====================

    private static final double BASE_FIVE_STAR_RATE = 0.008;
    private static final double BASE_FOUR_STAR_RATE = 0.05;
    private static final double SOFT_PITY_INCREMENT = 0.01;
    private static final int SOFT_PITY_START = 70;
    private static final int HARD_PITY_PULLS = 80;

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

    // ==================== 结果列表渲染器：按稀有度显示不同颜色与星标 ====================

    private static class GachaResultRenderer extends DefaultListCellRenderer {
        private final Color fiveStarBg = new Color(60, 50, 20);
        private final Color fiveStarFg = new Color(255, 200, 50);
        private final Color fourStarBg = new Color(40, 30, 55);
        private final Color fourStarFg = new Color(180, 130, 240);
        private final Color threeStarBg = new Color(35, 40, 55);
        private final Color threeStarFg = new Color(130, 170, 220);

        @Override
        public Component getListCellRendererComponent(
                JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {

            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            String text = value.toString();

            if (text.contains("五星")) {
                setText(convertToStars(text));
                setForeground(fiveStarFg);
                if (!isSelected) setBackground(fiveStarBg);
                setFont(new Font("Microsoft YaHei", Font.BOLD, 13));
            } else if (text.contains("四星")) {
                setText(convertToStars(text));
                setForeground(fourStarFg);
                if (!isSelected) setBackground(fourStarBg);
                setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
            } else {
                setText(convertToStars(text));
                setForeground(threeStarFg);
                if (!isSelected) setBackground(threeStarBg);
                setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
            }

            setBorder(new EmptyBorder(2, 8, 2, 8));
            return this;
        }

        private String convertToStars(String text) {
            if (text.startsWith("五星")) {
                return "★★★★★  " + text.substring(text.indexOf("-") + 2);
            } else if (text.startsWith("四星")) {
                return "★★★★    " + text.substring(text.indexOf("-") + 2);
            } else if (text.startsWith("三星")) {
                return "★★★      " + text.substring(text.indexOf("-") + 2);
            }
            return text;
        }
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
        fivePityLabel = new JLabel("五星保底: 0/80");
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

    // ==================== 按钮工厂方法（大号主按钮 / 小号功能按钮） ====================

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

    // ==================== 单抽 / 十连抽事件处理 ====================

    private void onDrawClick(ActionEvent e) {
        GachaItem item = drawGacha();
        if (item != null) {
            addResult(item);
        }
    }

    private void onTenDrawClick(ActionEvent e) {
        if (!state.infiniteMode && state.xingShengCount < 1600) {
            JOptionPane.showMessageDialog(this,
                "星声不足，无法十连！\n需要 1600 星声，当前：" + state.xingShengCount,
                "资源不足",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        for (int i = 0; i < 10; i++) {
            GachaItem item = drawGacha();
            if (item == null) {
                break;
            }
            addResult(item);
        }
    }

    // ==================== 功能按钮事件处理：重置星声、重置抽数、改UP名、金记录 ====================

    private void onResetXingSheng() {
        state.xingShengCount = 16000;
        refreshResourceLabel();
        statusLabel.setText("星声已重置为 16000");
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

    // ==================== 辅助：刷新资源标签 ====================

    private void refreshResourceLabel() {
 resourceLabel.setText(state.infiniteMode ? "星声：∞" : ("星声：" + state.xingShengCount));
        resourceLabel.setForeground(state.infiniteMode
            ? new Color(100, 220, 150)
            : new Color(212, 175, 55));
    }

    // ==================== 核心抽卡逻辑：资源扣除 → 稀有度判定 → 奖池抽取 → 奖励结算 ====================

    private GachaItem drawGacha() {
        if (!state.infiniteMode && state.xingShengCount < 160) {
            JOptionPane.showMessageDialog(this,
                "星声不足！当前剩余：" + state.xingShengCount,
                "无法抽卡",
                JOptionPane.WARNING_MESSAGE);
            return null;
        }
        state.pullsSinceLastFiveStar++;
        state.pullsSinceLastFourStar++;

        if (!state.infiniteMode) {
            state.xingShengCount -= 160;
        }

        String rarity = determineRarity();
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
        if (state.pullsSinceLastFiveStar >= HARD_PITY_PULLS) {
            state.pullsSinceLastFourStar = 0;
            return "五星";
        }

        if (state.pullsSinceLastFourStar >= 10) {
            state.pullsSinceLastFourStar = 0;
            return "四星";
        }

        double fiveStarRate = BASE_FIVE_STAR_RATE;
        if (state.pullsSinceLastFiveStar >= SOFT_PITY_START) {
            int pityCount = state.pullsSinceLastFiveStar - SOFT_PITY_START + 1;
            fiveStarRate = BASE_FIVE_STAR_RATE + SOFT_PITY_INCREMENT * pityCount;
            if (fiveStarRate > 1.0) fiveStarRate = 1.0;
        }

        double fourStarRate = BASE_FOUR_STAR_RATE;
        double threeStarRate = 1.0 - fiveStarRate - fourStarRate;
        if (threeStarRate < 0) {
            fourStarRate = 1.0 - fiveStarRate;
            threeStarRate = 0;
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

    // ==================== 五星抽取：50%概率出UP + 大保底强制UP ====================

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

    // ==================== 大保底追踪：歪了常驻五星 → 下次五星必出UP ====================

    private void updateLynTracking(GachaItem item) {
        if (state.upCharacter.equals(item.name)) {
            state.guaranteeNextLyn = false;
        } else if ("五星".equals(item.rarity)) {
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
        if ("五星".equals(item.rarity)) {
            raritySymbol = "★★★★★";
            rarityColor = new Color(255, 200, 50);
        } else if ("四星".equals(item.rarity)) {
            raritySymbol = "★★★★";
            rarityColor = new Color(180, 130, 240);
        } else {
            raritySymbol = "★★★";
            rarityColor = new Color(130, 170, 220);
        }
        statusLabel.setText(raritySymbol + "  " + item.name);
        statusLabel.setForeground(rarityColor);

        updatePityDisplay();
    }

    // ==================== 保底计数显示更新（直接引用标签，不再递归搜索组件树） ====================

    private void updatePityDisplay() {
        fivePityLabel.setText("五星保底: " + state.pullsSinceLastFiveStar + "/80");
        fourPityLabel.setText("四星保底: " + state.pullsSinceLastFourStar + "/10");
    }

    // ==================== 游戏状态数据类（所有可变状态集中管理） ====================

    private static class GameState {
        int pullsSinceLastFiveStar = 0;
        boolean guaranteeNextLyn = false;
        int pullsSinceLastFourStar = 0;
        int totalPulls = 0;
        int xingShengCount = 16000;
        boolean infiniteMode = false;
        String upCharacter = "琳";
        final java.util.List<GoldRecord> fiveStarRecords = new java.util.ArrayList<>();
    }

    // ==================== 抽卡结果数据类 ====================

    private static class GachaItem {
        private final String name;
        private final String rarity;
        private final int pullsFromLast;

        public GachaItem(String name, String rarity) {
            this(name, rarity, -1);
        }

        public GachaItem(String name, String rarity, int pullsFromLast) {
            this.name = name;
            this.rarity = rarity;
            this.pullsFromLast = pullsFromLast;
        }

        @Override
        public String toString() {
            return rarity + " - " + name;
        }
    }

    // ==================== 五星记录数据类 ====================

    private static class GoldRecord {
        final String name;
        final int pulls;
        GoldRecord(String name, int pulls) { this.name = name; this.pulls = pulls; }
    }
}
