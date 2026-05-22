package gacha;

import java.util.ArrayList;
import java.util.List;

/**
 * 游戏状态 —— 所有可变的抽卡状态集中管理。
 */
public class GameState {
    int pullsSinceLastFiveStar = 0;
    boolean guaranteeNextLyn = false;
    int pullsSinceLastFourStar = 0;
    int totalPulls = 0;
    int xingShengCount;
    boolean infiniteMode = false;
    String upCharacter = "琳";
    final List<GoldRecord> fiveStarRecords = new ArrayList<>();

    public GameState(int initialXingSheng) {
        this.xingShengCount = initialXingSheng;
    }
}
