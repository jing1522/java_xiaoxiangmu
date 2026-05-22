package gacha;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * 抽卡结果列表渲染器 —— 按稀有度显示不同颜色背景和星星图标。
 */
public class GachaResultRenderer extends DefaultListCellRenderer {
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
