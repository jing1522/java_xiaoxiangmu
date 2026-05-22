package gacha;

/**
 * 五星记录 —— 记录一次五星抽卡的角色名和距离上个金的抽数。
 */
public class GoldRecord {
    public final String name;
    public final int pulls;

    public GoldRecord(String name, int pulls) {
        this.name = name;
        this.pulls = pulls;
    }
}
