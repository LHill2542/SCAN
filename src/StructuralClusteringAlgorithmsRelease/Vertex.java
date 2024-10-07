/* author nxyuruk@ualr.edu
 *
 * August 02, 2007
 *
 */


package StructuralClusteringAlgorithmsRelease;

import java.util.*;
import java.math.*;

public class Vertex implements Constants {

    private String label;
    private int clusterID;
    private double IN_DegreeFactor;
    private double OUT_DegreeFactor;

    private TreeMap<String, Double[]> neighborhood;   // labels of neighbors and similarities with those neighbors
    private TreeMap<String, Double[]> neighborhoodIN;
    private TreeMap<String, Double[]> neighborhoodOUT;


    public Vertex( String label ) {
        this.label = label;
        this.clusterID    = UNCLASSIFIED;
		this.neighborhood = new  TreeMap<String, Double[]>();
		this.neighborhoodIN = new  TreeMap<String, Double[]>();
		this.neighborhoodOUT = new  TreeMap<String, Double[]>();
		this.IN_DegreeFactor = 0.0;
		this.OUT_DegreeFactor = 0.0;
    }


    public void addNeighbor(String neighbor, double weight) {

        if ( ! neighborhood.containsKey(neighbor)) {
			Double ws[] = new Double[2];
			ws[0] = weight;
			ws[1] = 0.0;										// similarity=0 tentatively
			neighborhood.put (neighbor, ws);
		}

    }

	public void addNeighbor(String neighbor, int direction, double weight) {

	        Double ws[] = new Double[2];
			ws[0] = weight;
			ws[1] = 0.0;                               			// similarity=0 tentatively

	        if (direction == IN) { neighborhoodIN.put (neighbor, ws); }
	        if (direction == OUT){ neighborhoodOUT.put (neighbor, ws); }


	    	// for directed networks we only need names of neighbors in neighborhood, similarity and weights are stored in neighborhoodIN and neighborhoodOUT
	    	if ( ! neighborhood.containsKey(neighbor)) {
	        	neighborhood.put (neighbor,ws);
			}
    }


	public boolean isNeighbor(String neighbor){
		return neighborhood.containsKey(neighbor);
	}

	public boolean isNeighbor(String neighbor, int direction){
		if (direction == IN) 	   { return neighborhoodIN.containsKey(neighbor); }
		else if (direction == OUT) { return neighborhoodOUT.containsKey(neighbor); }
		else 					   { return neighborhood.containsKey(neighbor); }
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

			Double ws[] = neighborhood.get(toVertex);
			ws[1] = similarity;
			// neighborhood.get(toVertex) returns a pointer to Double class. Any updates will be immediately shown in treemap

	}


	public double getSimilarity(String toVertex){

				Double ws[] = neighborhood.get(toVertex);
				return ws[1];
	}


	public double getWeight(String toVertex, int direction){

			//System.out.println("direction:" + direction);
			Double ws[] = new Double[2];

			switch (direction) {
				case IN:  ws = (Double[]) neighborhoodIN.get(toVertex); break;
				case OUT: ws = (Double[]) neighborhoodOUT.get(toVertex); break;
				default:  ws = (Double[]) neighborhood.get(toVertex);break;       // BIDIRECTIONAL, for undirected networks
		    }
		    //System.out.println("ws[0]:" + ws[0] + "ws[1]:" + ws[1] );
			return ws[0];
	}


    public Set getNeighborhood(int direction) {
		    switch (direction) {
				case IN: return neighborhoodIN.keySet();
				case OUT: return neighborhoodOUT.keySet();
				default:  return neighborhood.keySet();
		    }
    }
	public Set getNeighborhood() {
			return neighborhood.keySet();
	}



	public double getEdgeFactor(String toVertex, int direction, int function){

		double Aij = 0.0;

		if (toVertex.equals(this.getLabel()) ){ Aij = 0.0; return Aij;}			// consider Aii entries of adjacency matrix A as 0


		switch (function)
		{
			case NEWMAN_MOD: 			Aij = 1.0;  break;
			case NEWMAN_WEIGHTED_MOD: 	Aij = this.getWeight(toVertex, direction); break;
			case SIM_MOD: 				Aij = this.getSimilarity(toVertex); break; // similarity-based modularity
			default:                    Aij = 1.0;  break;
		}
		//System.out.println("Aij: "+Aij);
		return Aij;

	}

	public double getDegreeFactor(int direction, int function){ // sum of weights/similarities for incoming/outgoing/all links based on direction

			double degreeFactor = 0.0;
			HashSet<String> neighborhood = new HashSet<String>( this.getNeighborhood(direction) );
			Iterator itNeighbors = neighborhood.iterator();
			while (itNeighbors.hasNext()) {
				String neighbor = (String) itNeighbors.next();
				degreeFactor += this.getEdgeFactor(neighbor, direction, function);

			}
			return degreeFactor;
	}



	public double calculateSimilarity(Vertex toVertex, int similarityFunc, boolean directed, boolean weighted){


			// general cosine similarity (dot product of two vectors / product of two vector lenghts)
			// vectors are rows in adjacency matrix A (where element Aij=1 if there is an edge between nodes i and j, 0 otherwise)
			// this formula is identical to the "Definition 2: Structural Similarity" formula given in KDD'07 paper

			int direction=BIDIRECTIONAL; // default;
			if (directed){	direction = OUT;	}

			HashSet<String> neighborhood1 = new HashSet<String>( this.getNeighborhood(direction) );
			HashSet<String> neighborhood2 = new HashSet<String>( toVertex.getNeighborhood(direction) );


			double dot_product = 0.0;
			double vec_len1 = 0.0;
			double vec_len2 = 0.0;
			double sim = 0.0;

			Iterator itNeighbors = neighborhood1.iterator();
			while (itNeighbors.hasNext()) {
				String neighbor = (String) itNeighbors.next();
				double weight1 = this.getWeight(neighbor, direction);
				vec_len1 += (weight1 * weight1);

				//System.out.print(neighbor+",");
				// if the compared vertex has the same neighbor
				if ( toVertex.isNeighbor(neighbor, direction) ){
					double weight2 = toVertex.getWeight(neighbor, direction);
					dot_product += (weight1 * weight2);
				}

			}
			//System.out.print("\n");

			Iterator itNeighbors2 = neighborhood2.iterator();
			while (itNeighbors2.hasNext()) {
				String neighbor2 = (String) itNeighbors2.next();
				double weight2 = toVertex.getWeight(neighbor2, direction);
				vec_len2 += (weight2 * weight2);
				//System.out.print(neighbor2+",");
			}


			vec_len1 = Math.sqrt(vec_len1);
			vec_len2 = Math.sqrt(vec_len2);

			if (vec_len1 == 0.0 ||  vec_len2 == 0.0) { return 0.0; }

			//System.out.println(dot_product+"\t"+vec_len1+"\t"+vec_len2);


			sim =  (double) (dot_product / ( vec_len1 * vec_len2 ));

			BigDecimal bd = new BigDecimal(sim);
			bd = bd.setScale(2,BigDecimal.ROUND_HALF_UP);
    		sim = bd.doubleValue();

			return sim;



	}







}
