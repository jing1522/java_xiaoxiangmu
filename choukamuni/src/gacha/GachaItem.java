package gacha;

/**
 * 抽卡结果 —— 单次抽卡的名称、稀有度、距上个金的抽数。
 */
public class GachaItem {
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

    public String getName() { return name; }
    public String getRarity() { return rarity; }
    public int getPullsFromLast() { return pullsFromLast; }

    @Override
    public String toString() {
        return rarity + " - " + name;
    }
}
