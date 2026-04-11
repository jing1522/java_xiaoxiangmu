import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class GachaTtem extends JFrame {

    //运行主函数
    public static void main(String[] args) {
        SwingUtilities.invokeLater(GachaTtem::new);
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
    private int pullsSinceLastLyn = 0;
    private boolean guaranteeNextLyn = false;
    //抽卡池
    private final String upCharacter = "琳";
    private final List<String> fiveStarPool = Arrays.asList( upCharacter, "菲","妮","莫","空","维");
    private final List<String> fourStarPool = Arrays.asList("桃", "秧", "卜");
    private final List<String> threeStarPool = Arrays.asList("风车1", "风车2", "风车3");
    //抽卡概率设置
    private static final double BASE_FIVE_STAR_RATE = 0.008;
    private static final double BASE_FOUR_STAR_RATE = 0.05;
    private static final double BASE_THREE_STAR_RATE = 0.942;
    //保底机制设置
    private static final double SOFT_PITY_INCREMENT = 0.05;
    private static final int SOFT_PITY_START = 70;
    private static final int HARD_PITY_PULLS = 80;
    private static final int LYN_GUARANTEE_PULLS = 80;
    //抽卡统计变量
    private int pullsSinceLastHighStar =0;
    private int totalPulls = 0;
    private int xingShengCount = 16000;
    //显示抽卡次数的标签
    private final JLabel pullCountLabel = new JLabel("抽卡次数："+ totalPulls);
    private final JLabel resourceLabel = new JLabel("星声：" + xingShengCount);

    public GachaTtem() {
        super("抽卡模拟器");
        initUI();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 520);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
       //添加组件
        add(createTopPanel(), BorderLayout.NORTH);
        add(createResultPanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);

        setVisible(true);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        //给面板设置一个空白边框，用于在面板内部创建留白边距。
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        panel.add(new JLabel("Up角色:"+ upCharacter), BorderLayout.WEST);
        panel.add(statusLabel, BorderLayout.SOUTH);
        panel.add(pullCountLabel, BorderLayout.EAST);
        panel.add(resourceLabel, BorderLayout.NORTH);
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
        scrollPane.setBorder(BorderFactory.createTitledBorder("抽卡记录"));
        return scrollPane;
    }

    private JPanel createButtonPanel() {
        JButton drawButton = new JButton("抽卡");
        drawButton.addActionListener(this::onDrawClick);

        JButton tenDrawButton = new JButton("十连抽");
        tenDrawButton.addActionListener(this::onTenDrawClick);

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        panel.add(drawButton);
        panel.add(tenDrawButton);
        panel.add(statusLabel);
        return panel;
    }

    private void onDrawClick(ActionEvent e) {
        GachaItem item = drawGacha();     //  先用变量接收返回值
       if (item != null) {               //  判断变量是否为空
        addResult(item);              //  不为空才添加
       }
    }
    

    private void onTenDrawClick(ActionEvent e) {
        if (xingShengCount < 1600) {
        JOptionPane.showMessageDialog(this, 
            "星声不足，无法十连！\n需要 1600 星声，当前：" + xingShengCount, 
            "资源不足", 
            JOptionPane.WARNING_MESSAGE);
        return;
    }
    
    for (int i = 0; i < 10; i++) {
        GachaItem item = drawGacha();
        if (item != null) {
            addResult(item);
        }
    }
    }

    private GachaItem drawGacha() {
         if (xingShengCount < 160) {
        JOptionPane.showMessageDialog(this, 
            "星声不足！当前剩余：" + xingShengCount, 
            "无法抽卡", 
            JOptionPane.WARNING_MESSAGE);
        return null;  // 资源不足，不执行抽卡
        }
        pullsSinceLastFiveStar++;
        pullsSinceLastHighStar++;
        xingShengCount -= 160; // 每抽扣除160星声
        String rarity = determineRarity();
        GachaItem item;

        if ("五星".equals(rarity)) {
            item = drawFiveStar();
            xingShengCount += 1000; // 抽到五星角色奖励1000星声
        } else if ("四星".equals(rarity)) {
            item = drawFromPool(fourStarPool, "四星");
            xingShengCount += 300; // 抽到四星角色奖励300星声
        } else {
            item = drawFromPool(threeStarPool, "三星");
            xingShengCount += 50; // 抽到三星角色奖励500星声
        }
        if (!"三星".equals(item.rarity)) {
            pullsSinceLastHighStar = 0;
        }     

        updateLynTracking(item);
        return item;
    }

        private String determineRarity() {
        if (pullsSinceLastFiveStar >= HARD_PITY_PULLS) {
            return "五星";
        }
    
        if (pullsSinceLastHighStar >= 10) {
            return "四星";
        }
         
        double fiveStarRate = BASE_FIVE_STAR_RATE;
        // 软保底机制：当抽卡次数达到一定数量后，五星角色的概率逐渐增加
        if (pullsSinceLastFiveStar >= SOFT_PITY_START) {
            fiveStarRate += SOFT_PITY_INCREMENT;
        }
        // 四星角色的概率会根据五星角色的概率进行调整，确保总概率为100%
        double fourStarRate = BASE_FOUR_STAR_RATE - (fiveStarRate - BASE_FIVE_STAR_RATE);
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

    private void updateLynTracking(GachaItem item) {
        if (upCharacter.equals(item.name)) {
            pullsSinceLastLyn = 0;
        } else {
            pullsSinceLastLyn++;
            if (pullsSinceLastLyn >= LYN_GUARANTEE_PULLS) {
                guaranteeNextLyn = true;
            }
        }
    }

    private void addResult(GachaItem item) {
        totalPulls++;
        pullCountLabel.setText("抽卡次数："+ totalPulls);
        resourceLabel.setText("星声：" + xingShengCount); 
        resultListModel.addElement(item.toString());
        resultList.ensureIndexIsVisible(resultListModel.size() - 1);
        statusLabel.setText("本次抽到：" + item.toString());
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