package godclassinspector.models;

public class MetricsThresholdDTO {
	private static final int DEFAULT_WMC_THRESHOLD = 47;
	private static final int DEFAULT_ATFD_THRESHOLD = 5;
	private static final double DEFAULT_TCC_THRESHOLD = 0.33;
	private static final double DEFAULT_LAA_THRESHOLD = 0.33;
	private static final int EXTRACT_METHOD_COMPLEXITY_VALUE = 3;

	private static int wmcThreshold = DEFAULT_WMC_THRESHOLD;
	private static int atfdThreshold = DEFAULT_ATFD_THRESHOLD;
	private static double tccThreshold = DEFAULT_TCC_THRESHOLD;
	private static double laaThreshold = DEFAULT_LAA_THRESHOLD;

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

	public static double getLaaThreshold() {
		return laaThreshold;
	}

	public static void setLaaThreshold(double laaThreshold) {
		MetricsThresholdDTO.laaThreshold = laaThreshold;
	}
	
	public static int getExtractMethodComplexityThreshold() {
		return EXTRACT_METHOD_COMPLEXITY_VALUE;
	}
}