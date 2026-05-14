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

// 【改动1】类名修正: GachaTtem → GachaSimulator（Ttem无意义）
public class GachaSimulator extends JFrame {

    //运行主函数
    public static void main(String[] args) {
        // 
        SwingUtilities.invokeLater(GachaSimulator::new);
    }


    
   //作为 JList 的数据容器，负责存储和管理列表中的所有条目。
    private final DefaultListModel<String> resultListModel = new DefaultListModel<>();
    //调用 JList 的构造方法，传入数据模型，让两者绑定
    private final JList<String> resultList = new JList<>(resultListModel);
    //抽卡的按钮名
    private final JLabel statusLabel = new JLabel("请点击抽卡按钮");
    //随机数生成器，用于模拟抽卡的随机性
    private final Random random = new Random();
    //抽卡统计变量
    private int pullsSinceLastFiveStar = 0;
    // 【改动11】大保底逻辑修正：改为"歪了常驻五星 → 下次UP必出"
    // 【原代码】private int pullsSinceLastLyn = 0;（已废弃）
    private boolean guaranteeNextLyn = false;
    //抽卡池
    private final String upCharacter = "琳";
    private final List<String> fiveStarPool = Arrays.asList( upCharacter, "菲","妮","莫","空","维");
    private final List<String> fourStarPool = Arrays.asList("桃", "秧", "卜");
    private final List<String> threeStarPool = Arrays.asList("风车1", "风车2", "风车3");
    //抽卡概率设置
    private static final double BASE_FIVE_STAR_RATE = 0.008;
    private static final double BASE_FOUR_STAR_RATE = 0.05;
    // 【改动2】删除原 BASE_THREE_STAR_RATE 常量，三星概率改为动态计算（1-五星-四星），保证总概率=100%
    //保底机制设置
    // 【改动3】软保底递增幅度调整
    // 【原代码】private static final double SOFT_PITY_INCREMENT = 0.05;
    // 【原问题】每次+5%太陡峭，70抽时5.8%，75抽已超30%，几乎必出五星
    // 【新方案】改为每抽+1%，更平滑：70抽=1.8%, 75抽=6.8%, 79抽=10.8%
    private static final double SOFT_PITY_INCREMENT = 0.01;
    private static final int SOFT_PITY_START = 70;
    private static final int HARD_PITY_PULLS = 80;
    // 【改动4】大保底逻辑修正：不再基于抽数计数，改为"歪了即触发"
    // 【原代码】private static final int LYN_GUARANTEE_PULLS = 80;（基于抽数的错误逻辑，已废弃）
    // 【改动2】四星保底独立计数器
    // 【原代码】private int pullsSinceLastHighStar = 0;
    // 【原问题】五星和四星共用 pullsSinceLastHighStar，导致出五星时四星保底也被重置
    private int pullsSinceLastFourStar = 0; // 新增：记录距离上次四星及以上的抽数（四星保底用）
    private int totalPulls = 0;
    private int xingShengCount = 16000;
    // 【改动12】无限星声模式：跳过资源检查、不扣星声、不加奖励、界面显示∞
    private boolean infiniteMode = false;
    //显示抽卡次数的标签
    private final JLabel pullCountLabel = new JLabel("抽卡次数："+ totalPulls);
    private final JLabel resourceLabel = new JLabel("星声：" + xingShengCount);


    public GachaSimulator() {
        super("抽卡模拟器");
        initUI();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        // 【改动10】添加界面美化方法，统一管理所有组件的颜色、字体、边框等样式
        applyUIStyle();

       //添加组件
        add(createTopPanel(), BorderLayout.NORTH);
        add(createResultPanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);

        setVisible(true);
    }

    // 【改动10】统一美化方法：集中管理颜色方案、字体、边框等，方便后续调整
    // 配色灵感来自抽卡游戏常见的深色主题 + 金色点缀
    private void applyUIStyle() {
        // --- 全局字体设置：使用系统默认字体，微调大小 ---
        Font defaultFont = new Font("Microsoft YaHei", Font.PLAIN, 13);
        Font titleFont = new Font("Microsoft YaHei", Font.BOLD, 14);
        Font buttonFont = new Font("Microsoft YaHei", Font.BOLD, 14);
        Font starFont = new Font("Microsoft YaHei", Font.BOLD, 16);

        // 将默认字体应用到所有组件（会被后续单独设置覆盖）
        UIManager.put("Label.font", defaultFont);
        UIManager.put("Button.font", buttonFont);
        UIManager.put("List.font", defaultFont);

        // --- 颜色方案 ---
        // 背景色：深灰蓝
        Color bgColor = new Color(30, 34, 45);
        // 面板色：略浅的深色
        Color panelColor = new Color(42, 47, 58);
        // 金色（用于五星和按钮主色）
        Color goldColor = new Color(212, 175, 55);
        // 紫色（四星）
        Color purpleColor = new Color(160, 100, 220);
        // 蓝色（三星）
        Color blueColor = new Color(80, 150, 220);

        // --- 窗口背景 ---
        getContentPane().setBackground(bgColor);

        // --- 状态标签字体加大 ---
        statusLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 13));
        statusLabel.setForeground(new Color(200, 200, 200));

        // --- 抽卡次数和资源标签 ---
        pullCountLabel.setFont(titleFont);
        pullCountLabel.setForeground(new Color(180, 200, 220));
        resourceLabel.setFont(titleFont);
        resourceLabel.setForeground(goldColor);

        // 【改动10】设置JList自定义渲染器：按稀有度显示彩色背景和星星图标
        resultList.setCellRenderer(new GachaResultRenderer());

        // 【改动10】设置列表固定宽度和行高
        resultList.setFixedCellHeight(28);
        resultList.setBackground(panelColor);
        resultList.setForeground(new Color(220, 220, 220));
        resultList.setSelectionBackground(new Color(60, 70, 90));
        resultList.setSelectionForeground(Color.WHITE);
    }

    // 【改动10】自定义列表渲染器：每条抽卡记录按稀有度显示不同颜色
    private static class GachaResultRenderer extends DefaultListCellRenderer {
        // 五星：金色背景 + ★★★★★
        private final Color fiveStarBg = new Color(60, 50, 20);
        private final Color fiveStarFg = new Color(255, 200, 50);
        // 四星：紫色背景 + ★★★★
        private final Color fourStarBg = new Color(40, 30, 55);
        private final Color fourStarFg = new Color(180, 130, 240);
        // 三星：蓝灰 + ★★★
        private final Color threeStarBg = new Color(35, 40, 55);
        private final Color threeStarFg = new Color(130, 170, 220);

        @Override
        public Component getListCellRendererComponent(
                JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {

            // 调用父类获取默认渲染组件
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            String text = value.toString();

            // 按稀有度设置文字和背景颜色
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

        // 【改动10】将 "五星 - 琳" 格式转为 "★★★★★  琳"，直观展示稀有度
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

    // 【改动7】顶部面板布局重构
    // 【原代码】直接用 BorderLayout 的四个方向放组件，后加会覆盖先加的
    // 【原问题】WEST/EAST/NORTH/SOUTH 都占满会导致部分组件不可见
    // 【新方案】嵌套面板：第一行(Up角色 + 资源 + 抽数)，第二行(状态)
    private JPanel createTopPanel() {
        // 【改动10】顶部面板背景色和边框美化
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false); // 透明，让窗口背景色透出
        panel.setBorder(BorderFactory.createEmptyBorder(12, 15, 5, 15));

        // 标题行（居中，大字）
        JLabel titleLabel = new JLabel("⬥ 抽 卡 模 拟 器 ⬥", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 20));
        titleLabel.setForeground(new Color(212, 175, 55));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // 信息行：Up角色(左) + 抽卡次数(中) + 星声资源(右)
        JPanel infoRow = new JPanel(new BorderLayout());
        infoRow.setOpaque(false);

        JLabel upLabel = new JLabel("✦ UP: " + upCharacter + " ✦");
        upLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 16));
        upLabel.setForeground(new Color(255, 180, 80));

        infoRow.add(upLabel, BorderLayout.WEST);
        infoRow.add(pullCountLabel, BorderLayout.CENTER);
        infoRow.add(resourceLabel, BorderLayout.EAST);

        // 保底信息行
        JPanel pityRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        pityRow.setOpaque(false);
        JLabel fivePityLabel = new JLabel("五星保底: 0/80");
        fivePityLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 11));
        fivePityLabel.setForeground(new Color(180, 160, 120));
        JLabel fourPityLabel = new JLabel("四星保底: 0/10");
        fourPityLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 11));
        fourPityLabel.setForeground(new Color(160, 140, 200));
        pityRow.add(fivePityLabel);
        pityRow.add(fourPityLabel);
        // 保存引用以便后续更新（放在面板的 clientProperty 中）
        infoRow.putClientProperty("fivePity", fivePityLabel);
        infoRow.putClientProperty("fourPity", fourPityLabel);

        // 状态行
        JPanel statusRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        statusRow.setOpaque(false);
        statusRow.add(statusLabel);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(infoRow, BorderLayout.CENTER);
        panel.add(pityRow, BorderLayout.SOUTH);
        // 状态标签单独放一个嵌套层
        JPanel bottomWrap = new JPanel(new BorderLayout());
        bottomWrap.setOpaque(false);
        bottomWrap.add(pityRow, BorderLayout.NORTH);
        bottomWrap.add(statusRow, BorderLayout.SOUTH);
        panel.add(bottomWrap, BorderLayout.SOUTH);

        /* 【原代码】
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        panel.add(new JLabel("Up角色:"+ upCharacter), BorderLayout.WEST);
        panel.add(statusLabel, BorderLayout.SOUTH);
        panel.add(pullCountLabel, BorderLayout.EAST);
        panel.add(resourceLabel, BorderLayout.NORTH);
        */
        return panel;
    }


    private JScrollPane createResultPanel() {
        resultList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultList.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            // 获取点击位置的索引
            int index = resultList.locationToIndex(e.getPoint());
            if (index >= 0) {  // 确保点到了条目上
                // 获取点击的条目内容
                String itemText = resultListModel.getElementAt(index);
                // 计算这是第几抽（索引 + 1）
                int drawNumber = index + 1;

                // 弹出消息窗口
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
        // 【改动10】美化滚动面板边框，金色细线 + 标题
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
        // 滚动面板背景
        scrollPane.getViewport().setBackground(new Color(42, 47, 58));
        scrollPane.setBackground(new Color(30, 34, 45));
        return scrollPane;
    }

    private JPanel createButtonPanel() {
        // 【改动10】按钮美化：圆角边框、金色/紫色配色、悬停效果
        JButton drawButton = createStyledButton("单 抽", new Color(212, 175, 55));
        drawButton.addActionListener(this::onDrawClick);

        JButton tenDrawButton = createStyledButton("十 连 抽", new Color(180, 130, 240));
        tenDrawButton.addActionListener(this::onTenDrawClick);

        // 【改动12】无限星声模式复选框
        JCheckBox infiniteCheck = new JCheckBox("无限星声");
        infiniteCheck.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        infiniteCheck.setForeground(new Color(180, 200, 220));
        infiniteCheck.setOpaque(false);
        infiniteCheck.setFocusPainted(false);
        infiniteCheck.addActionListener(e -> {
            infiniteMode = infiniteCheck.isSelected();
            // 切换模式时立即更新资源显示
            resourceLabel.setText(infiniteMode ? "星声：∞" : ("星声：" + xingShengCount));
            resourceLabel.setForeground(infiniteMode
                ? new Color(100, 220, 150)
                : new Color(212, 175, 55));
        });

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonRow.setOpaque(false);
        buttonRow.add(drawButton);
        buttonRow.add(tenDrawButton);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 15, 10));
        panel.add(buttonRow, BorderLayout.CENTER);
        panel.add(infiniteCheck, BorderLayout.EAST);
        // 【改动7】删除 panel.add(statusLabel);
        // 【原问题】statusLabel 已在 createTopPanel() 中添加，Swing 组件只能有一个父容器
        // 重复添加会导致顶部面板的 statusLabel 显示空白，只有底部按钮面板能显示
        return panel;
    }

    // 【改动10】创建统一样式的按钮：自定义背景色、圆角、悬停时变亮
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

        // 悬停效果：鼠标移入变亮，移出恢复
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(baseColor.brighter());
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(baseColor);
            }
        });

        return button;
    }

    private void onDrawClick(ActionEvent e) {
        GachaItem item = drawGacha();     //  先用变量接收返回值
       if (item != null) {               //  判断变量是否为空
        addResult(item);              //  不为空才添加
       }
    }


    // 【改动4】十连抽中途资源不足时提前终止
    private void onTenDrawClick(ActionEvent e) {
        // 【改动12】无限模式跳过资源检查
        if (!infiniteMode && xingShengCount < 1600) {
        JOptionPane.showMessageDialog(this,
            "星声不足，无法十连！\n需要 1600 星声，当前：" + xingShengCount,
            "资源不足",
            JOptionPane.WARNING_MESSAGE);
        return;
    }

    for (int i = 0; i < 10; i++) {
        GachaItem item = drawGacha();
        // 【改动4】drawGacha 返回 null 表示资源不足，提前终止十连
        // 【原代码】没有判断 null，资源不够时仍继续循环但实际没抽卡
        if (item == null) {
            break;
        }
        addResult(item);
    }
    }

    private GachaItem drawGacha() {
        // 【改动12】无限模式跳过资源检查和扣除；普通模式必须检查
        if (!infiniteMode && xingShengCount < 160) {
        JOptionPane.showMessageDialog(this,
            "星声不足！当前剩余：" + xingShengCount,
            "无法抽卡",
            JOptionPane.WARNING_MESSAGE);
        return null;  // 资源不足，不执行抽卡
        }
        pullsSinceLastFiveStar++;
        // 【改动2】四星保底独立计数
        // 【原代码】pullsSinceLastHighStar++;
        // 【原问题】pullsSinceLastHighStar 既做四星保底又受五星重置影响
        pullsSinceLastFourStar++;
        // 【改动12】无限模式不扣除星声
        if (!infiniteMode) {
            xingShengCount -= 160; // 每抽扣除160星声
        }
        String rarity = determineRarity();
        GachaItem item;

        if ("五星".equals(rarity)) {
            item = drawFiveStar();
            // 【改动12】无限模式不加奖励星声
            if (!infiniteMode) xingShengCount += 1000; // 抽到五星角色奖励1000星声
            // 【改动2】非保底出五星时重置四星保底计数（保底触发已在 determineRarity 中重置）
            pullsSinceLastFourStar = 0;
        } else if ("四星".equals(rarity)) {
            item = drawFromPool(fourStarPool, "四星");
            if (!infiniteMode) xingShengCount += 300; // 抽到四星角色奖励300星声
            // 【改动2】非保底出四星时重置四星保底计数（保底触发已在 determineRarity 中重置）
            pullsSinceLastFourStar = 0;
        } else {
            item = drawFromPool(threeStarPool, "三星");
            // 【改动6】注释修正: 实际加50星声，原注释错误地写"500星声"
            if (!infiniteMode) xingShengCount += 50; // 抽到三星角色奖励50星声
        }
        // 【改动2】删除原代码
        // 【原代码】
        // if (!"三星".equals(item.rarity)) {
        //     pullsSinceLastHighStar = 0;
        // }
        // 【原问题】五星和四星共用 pullsSinceLastHighStar，出五星时同样重置了四星保底计数
        // 【新方案】已在各自分支内用 pullsSinceLastFourStar 独立重置

        updateLynTracking(item);
        return item;
    }

        // 【改动1 + 改动3】概率计算重写
        private String determineRarity() {
        // 硬保底：80抽必出五星
        if (pullsSinceLastFiveStar >= HARD_PITY_PULLS) {
            // 【改动8】五星硬保底触发时也重置四星保底计数器，避免下一抽立即触发四星保底
            pullsSinceLastFourStar = 0;
            return "五星";
        }

        // 【改动2】四星保底：10抽内必出四星及以上，使用独立计数器
        if (pullsSinceLastFourStar >= 10) {
            // 【改动8】在触发处立即重置计数器，不再依赖 drawGacha 中的外部重置
            // 这样即使后续逻辑有变化，保底重置也不会遗漏
            pullsSinceLastFourStar = 0;
            return "四星";
        }

        // 【改动3】软保底递增：70抽后每抽递增一次，直到80抽100%
        // 【原代码】fiveStarRate += SOFT_PITY_INCREMENT; 只加一次，之后不变
        // 【原问题】70抽后概率只从0.8%跳到5.8%，然后每抽都一样，不是递增
        double fiveStarRate = BASE_FIVE_STAR_RATE;
        if (pullsSinceLastFiveStar >= SOFT_PITY_START) {
            int pityCount = pullsSinceLastFiveStar - SOFT_PITY_START + 1; // 第1抽到第10抽
            fiveStarRate = BASE_FIVE_STAR_RATE + SOFT_PITY_INCREMENT * pityCount;
            // 上限100%（80抽时 pityCount=11，0.008+0.05*11=0.558，实际硬保底已处理）
            if (fiveStarRate > 1.0) fiveStarRate = 1.0;
        }

        // 【改动1】概率计算方式修正
        // 【原代码】
        // double fourStarRate = BASE_FOUR_STAR_RATE - (fiveStarRate - BASE_FIVE_STAR_RATE);
        // 【原问题】从四星概率中减去五星增加的部分，不严谨且可能破坏总概率=100%
        // 【新方案】先定五星和四星，三星 = 1 - 五星 - 四星，保证总和=100%
        double fourStarRate = BASE_FOUR_STAR_RATE;
        double threeStarRate = 1.0 - fiveStarRate - fourStarRate;
        // 容错：如果五星+四星超过1，调整四星
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
    private GachaItem drawFiveStar() {
        pullsSinceLastFiveStar = 0;

        String name;
        if (guaranteeNextLyn || random.nextDouble() < 0.5) {
            name = upCharacter;
            guaranteeNextLyn = false;
        } else {
            List<String> others = Arrays.asList("菲","妮","莫","空","维");
            name = others.get(random.nextInt(others.size()));
        }

        return new GachaItem(name, "五星");
    }

    private GachaItem drawFromPool(List<String> pool, String rarity) {
        String name = pool.get(random.nextInt(pool.size()));
        return new GachaItem(name, rarity);
    }

    // 【改动11】大保底逻辑：歪了常驻五星 → 标记下次UP必出
    // 【原逻辑】统计"距离上次UP的抽数"，80抽没UP才触发——错误
    // 【新逻辑】只要抽出五星但不是UP（歪了），立即标记下次五星必出UP
    private void updateLynTracking(GachaItem item) {
        if (upCharacter.equals(item.name)) {
            // 抽到UP角色 → 大保底已使用，重置标记
            guaranteeNextLyn = false;
        } else if ("五星".equals(item.rarity)) {
            // 抽到五星但不是UP（歪了常驻）→ 下次五星必定是UP
            guaranteeNextLyn = true;
        }
        // 四星和三星不触发大保底逻辑
        /* 【原代码】
        if (upCharacter.equals(item.name)) {
            pullsSinceLastLyn = 0;
        } else {
            pullsSinceLastLyn++;
            if (pullsSinceLastLyn >= LYN_GUARANTEE_PULLS) {
                guaranteeNextLyn = true;
            }
        }
        */
    }

    private void addResult(GachaItem item) {
        totalPulls++;
        pullCountLabel.setText("抽卡次数："+ totalPulls);
        // 【改动12】无限模式下显示∞，不显示星声数字
        resourceLabel.setText(infiniteMode ? "星声：∞" : ("星声：" + xingShengCount));
        resultListModel.addElement(item.toString());
        resultList.ensureIndexIsVisible(resultListModel.size() - 1);
        // 【改动10】状态栏按稀有度显示不同颜色
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

        // 【改动10】更新保底计数显示
        updatePityDisplay();
    }

    // 【改动10】更新顶部面板的保底计数显示
    private void updatePityDisplay() {
        // 通过遍历组件树找到保底标签（简单方案：直接遍历顶层面板的子组件）
        Component northComp = ((BorderLayout) getContentPane().getLayout()).getLayoutComponent(BorderLayout.NORTH);
        if (northComp instanceof JPanel) {
            JPanel topPanel = (JPanel) northComp;
            updatePityInPanel(topPanel);
        }
    }

    private void updatePityInPanel(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                Object fivePity = panel.getClientProperty("fivePity");
                Object fourPity = panel.getClientProperty("fourPity");
                if (fivePity instanceof JLabel) {
                    ((JLabel) fivePity).setText("五星保底: " + pullsSinceLastFiveStar + "/80");
                }
                if (fourPity instanceof JLabel) {
                    ((JLabel) fourPity).setText("四星保底: " + pullsSinceLastFourStar + "/10");
                }
                // 递归搜索嵌套面板
                if (fivePity == null && fourPity == null) {
                    updatePityInPanel(panel);
                }
            }
        }
    }


    private static class GachaItem {
        private final String name;
        private final String rarity;

        public GachaItem(String name, String rarity) {
            this.name = name;
            this.rarity = rarity;
        }

        @Override
        public String toString() {
            return rarity + " - " + name;
        }
    }
}
