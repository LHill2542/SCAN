/* author nxyuruk@ualr.edu
 *
 * August 02, 2007
 *
 */


package StructuralClusteringAlgorithmsRelease;

import java.util.*;
import java.math.*;

public class Vertex implements Constants {

    private final String label;
    private int clusterID;
    private double IN_DegreeFactor, OUT_DegreeFactor;

	// labels of neighbors and similarities with those neighbors
    private final TreeMap<String, Double[]> neighborhood, neighborhoodIN, neighborhoodOUT;

    public Vertex( String label ) {
        this.label = label;
        this.clusterID    = UNCLASSIFIED;
		this.neighborhood = new TreeMap<>();
		this.neighborhoodIN = new TreeMap<>();
		this.neighborhoodOUT = new TreeMap<>();
		this.IN_DegreeFactor = 0.0;
		this.OUT_DegreeFactor = 0.0;
    }


    public void addNeighbor(String neighbor, double weight) {
        if (!neighborhood.containsKey(neighbor)) {
			Double[] ws = new Double[2];
			ws[0] = weight;
			ws[1] = 0.0;										// similarity=0 tentatively
			neighborhood.put (neighbor, ws);
		}
    }

	public void addNeighbor(String neighbor, int direction, double weight) {

		Double[] ws = new Double[2];
		ws[0] = weight;
		ws[1] = 0.0;                                        // similarity=0 tentatively

		if (direction == IN) {
			neighborhoodIN.put(neighbor, ws);
		} else if (direction == OUT) {
			neighborhoodOUT.put(neighbor, ws);
		}

		// for directed networks we only need names of neighbors in neighborhood, similarity and weights are stored in neighborhoodIN and neighborhoodOUT
		if (!neighborhood.containsKey(neighbor)) {
			neighborhood.put(neighbor, ws);
		}
	}


	public boolean isNeighbor(String neighbor){
		return neighborhood.containsKey(neighbor);
	}

	public boolean isNeighbor(String neighbor, int direction){
		return switch (direction) {
			case IN -> neighborhoodIN.containsKey(neighbor);
			case OUT -> neighborhoodOUT.containsKey(neighbor);
			default -> neighborhood.containsKey(neighbor);
		};
	}

	public String getLabel(){
			return label;
	}

    public int getDegree(){
			return neighborhood.size();
	}
	public int getInDegree(){
			return neighborhoodIN.size();
	}
	public int getOutDegree(){
			return neighborhoodOUT.size();
	}

	public void setINDegreeFactor(double DegFactor){
			this.IN_DegreeFactor = DegFactor;
	}

	public void setOUTDegreeFactor(double DegFactor){
			this.OUT_DegreeFactor = DegFactor;
	}

	public double getOUTDegreeFactor(){
			return OUT_DegreeFactor;
	}

	public double getINDegreeFactor(){
			return IN_DegreeFactor;
	}

    public int getClusterId() {
			return clusterID;
    }

	public void setClusterId(int clusterId){
			this.clusterID = clusterId;
	}

	public void setSimilarity(String toVertex, double similarity){
			Double[] ws = neighborhood.get(toVertex);
			ws[1] = similarity;
			// neighborhood.get(toVertex) returns a pointer to Double class. Any updates will be immediately shown in treemap
	}

	public double getSimilarity(String toVertex) {
		Double[] ws = neighborhood.get(toVertex);
		return ws[1];
	}


	public double getWeight(String toVertex, int direction) {

		//System.out.println("direction:" + direction);
		Double[] ws = switch (direction) {
			case IN -> neighborhoodIN.get(toVertex);
			case OUT -> neighborhoodOUT.get(toVertex);
			default -> neighborhood.get(toVertex);       // BIDIRECTIONAL, for undirected networks
		};

		//System.out.println("ws[0]:" + ws[0] + "ws[1]:" + ws[1] );
		return ws[0];
	}


    public Set<String> getNeighborhood(int direction) {
		return switch (direction) {
			case IN -> neighborhoodIN.keySet();
			case OUT ->  neighborhoodOUT.keySet();
			default -> neighborhood.keySet();
		};
	}

	public Set<String> getNeighborhood() {
		return neighborhood.keySet();
	}

	public double getEdgeFactor(String toVertex, int direction, int function){
		// consider Aii entries of adjacency matrix A as 0
		if (toVertex.equals(this.getLabel()) ) {
			return 0.0;
		}

		return switch (function) {
            case NEWMAN_WEIGHTED_MOD -> getWeight(toVertex, direction);
			case SIM_MOD -> getSimilarity(toVertex);
			default -> 1.0;
		};
	}

	public double getDegreeFactor(int direction, int function) { // sum of weights/similarities for incoming/outgoing/all links based on direction
		double degreeFactor = 0.0;
		for (String neighbor : getNeighborhood(direction)) {
			degreeFactor += this.getEdgeFactor(neighbor, direction, function);
		}
		return degreeFactor;
	}



	public double calculateSimilarity(Vertex toVertex, int similarityFunc, boolean directed, boolean weighted) {


		// general cosine similarity (dot product of two vectors / product of two vector lenghts)
		// vectors are rows in adjacency matrix A (where element Aij=1 if there is an edge between nodes i and j, 0 otherwise)
		// this formula is identical to the "Definition 2: Structural Similarity" formula given in KDD'07 paper

		int direction = BIDIRECTIONAL; // default;
		if (directed) {
			direction = OUT;
		}

		HashSet<String> neighborhood1 = new HashSet<>(this.getNeighborhood(direction));
		HashSet<String> neighborhood2 = new HashSet<>(toVertex.getNeighborhood(direction));

		double dot_product = 0.0;
		double vec_len1 = 0.0;
		double vec_len2 = 0.0;
		double sim = 0.0;

        for (String neighbor : neighborhood1) {
            double weight1 = this.getWeight(neighbor, direction);
            vec_len1 += (weight1 * weight1);

            // if the compared vertex has the same neighbor
            if (toVertex.isNeighbor(neighbor, direction)) {
                double weight2 = toVertex.getWeight(neighbor, direction);
                dot_product += (weight1 * weight2);
            }
        }

        for (String neighbor2 : neighborhood2) {
            double weight2 = toVertex.getWeight(neighbor2, direction);
            vec_len2 += (weight2 * weight2);
        }

		vec_len1 = Math.sqrt(vec_len1);
		vec_len2 = Math.sqrt(vec_len2);

		if (vec_len1 == 0.0 || vec_len2 == 0.0) {
			return 0.0;
		}

		//System.out.println(dot_product+"\t"+vec_len1+"\t"+vec_len2);


		sim = (double) (dot_product / (vec_len1 * vec_len2));

		BigDecimal bd = new BigDecimal(sim);
		bd = bd.setScale(2, RoundingMode.HALF_UP);
		sim = bd.doubleValue();

		return sim;
	}
}