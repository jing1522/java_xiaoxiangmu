import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class GachaTtem extends JFrame {

    //运行主函数
    public static void main(String[] args) {
        SwingUtilities.invokeLater(GachaTtem::new);
    }

   //成员变量
    private final DefaultListModel<String> resultListModel = new DefaultListModel<>();
    private final JList<String> resultList = new JList<>(resultListModel);
    private final JLabel statusLabel = new JLabel("请点击抽卡按钮");
    private final Random random = new Random();

    private int pullsSinceLastFiveStar = 0;
    private int pullsSinceLastLyn = 0;
    private boolean guaranteeNextLyn = false;

    private final List<String> fiveStarPool = Arrays.asList("维", "琳", "菲");
    private final List<String> fourStarPool = Arrays.asList("桃", "秧", "卜");
    private final List<String> threeStarPool = Arrays.asList("风车1", "风车2", "风车3");

    private static final double BASE_FIVE_STAR_RATE = 0.008;
    private static final double BASE_FOUR_STAR_RATE = 0.05;
    private static final double BASE_THREE_STAR_RATE = 0.942;
    private static final double SOFT_PITY_INCREMENT = 0.05;
    private static final int SOFT_PITY_START = 70;
    private static final int HARD_PITY_PULLS = 80;
    private static final int LYN_GUARANTEE_PULLS = 80;

    private int pullsSinceLastHighStar =0;
    private int totalPulls = 0;
    private final JLabel pullCountLabel = new JLabel("抽卡次数："+ totalPulls);

    public GachaTtem() {
        super("抽卡模拟器");
        initUI();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 520);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        add(createTopPanel(), BorderLayout.NORTH);
        add(createResultPanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);

        setVisible(true);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        panel.add(new JLabel("预置角色：五星/四星/三星"), BorderLayout.NORTH);
        panel.add(statusLabel, BorderLayout.SOUTH);
        panel.add(pullCountLabel, BorderLayout.SOUTH);
        return panel;
    }

    private JScrollPane createResultPanel() {
        resultList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
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
        addResult(drawGacha());
    }

    private void onTenDrawClick(ActionEvent e) {
        for (int i = 0; i < 10; i++) {
            addResult(drawGacha());
        }
    }

    private GachaItem drawGacha() {
        pullsSinceLastFiveStar++;
        pullsSinceLastHighStar++;
        String rarity = determineRarity();
        GachaItem item;

        if ("五星".equals(rarity)) {
            item = drawFiveStar();
        } else if ("四星".equals(rarity)) {
            item = drawFromPool(fourStarPool, "四星");
        } else {
            item = drawFromPool(threeStarPool, "三星");
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
        if (pullsSinceLastFiveStar >= SOFT_PITY_START) {
            fiveStarRate += SOFT_PITY_INCREMENT;
        }
    
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
            name = "琳";
            guaranteeNextLyn = false;
        } else {
            List<String> others = Arrays.asList("维", "菲");
            name = others.get(random.nextInt(others.size()));
        }

        return new GachaItem(name, "五星");
    }

    private GachaItem drawFromPool(List<String> pool, String rarity) {
        String name = pool.get(random.nextInt(pool.size()));
        return new GachaItem(name, rarity);
    }

    private void updateLynTracking(GachaItem item) {
        if ("琳".equals(item.name)) {
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