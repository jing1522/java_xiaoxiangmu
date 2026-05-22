package gacha;

/**
 * 概率配置 —— 所有可调整的抽卡概率参数。
 */
public class ProbabilityConfig {
    public double baseFiveStarRate = 0.008;
    public double baseFourStarRate = 0.05;
    public double softPityIncrement = 0.01;
    public int softPityStart = 70;
    public int hardPityPulls = 80;
    public int initialXingSheng = 16000;
    public int costPerPull = 160;
}
