package StructuralClusteringAlgorithmsRelease;

interface Constants
{
    public static final int COS = 0;
    public static final int MIN = 1;
    public static final int MAX = 2;
    public static final int JAC = 3;
    public static final int AFF = 4;			// Affinity(v,w) = num-shared-neighbors(v,w) + 1 / num-neighbors(w)

	public static final int UNDEFINED = 0;
    public static final int UNCLASSIFIED = -1;
    public static final int NONMEMBER = -2;
  	public static final int OUTLIER = -3;
    public static final int HUB = -4;


	public static final int IN = 1;
	public static final int OUT = -1;
	public static final int BIDIRECTIONAL = 0;


	public static final int NEWMAN_MOD = 0;
	public static final int SIM_MOD = 1;
	public static final int NEWMAN_WEIGHTED_MOD = 2;


}
