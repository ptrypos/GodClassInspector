package godclassinspector.model;

public class MetricsThresholdDTO {
	private static final int DEFAULT_WMC_THRESHOLD = 47;
	private static final int DEFAULT_ATFD_THRESHOLD = 5;
	private static final double DEFAULT_TCC_THRESHOLD = 0.33;

	private static int wmcThreshold = DEFAULT_WMC_THRESHOLD;
	private static int atfdThreshold = DEFAULT_ATFD_THRESHOLD;
	private static double tccThreshold = DEFAULT_TCC_THRESHOLD;

	public static int getWmcThreshold() {
		return wmcThreshold;
	}

	public static void setWmcThreshold(int wmcThreshold) {
		MetricsThresholdDTO.wmcThreshold = wmcThreshold;
	}

	public static int getAtfdThreshold() {
		return atfdThreshold;
	}

	public static void setAtfdThreshold(int atfdThreshold) {
		MetricsThresholdDTO.atfdThreshold = atfdThreshold;
	}

	public static double getTccThreshold() {
		return tccThreshold;
	}

	public static void setTccThreshold(double tccThreshold) {
		MetricsThresholdDTO.tccThreshold = tccThreshold;
	}
}
