/** author nxyuruk@ualr.edu
 *
 *  September 6, 2007
 *
 *
 */



package StructuralClusteringAlgorithmsRelease;

import java.util.*;
import java.math.*;

public class Evaluate implements Constants{

	private Network net;

	public Evaluate(Network network){
			this.net = network;

	}


	//
	// gives the number of intra-cluster edges (edges that fall within  clusters)
	// or inter-cluster edges (edges across the clusters)
	//
	@SuppressWarnings("unchecked")
	public int numEdges(int type){

			// type = 0, intra-cluster edges
			// type = 1, inter-cluster edges

			Edge edge;
			Vertex vertexA, vertexB;
			int totalEdges = 0;
			int total = 0;

			Iterator<Edge> itEdge = net.getEdgeIterator();
			while (itEdge.hasNext()) {
				edge = (Edge) itEdge.next();
				vertexA = net.getVertex(edge.getVertexA() );
				vertexB = net.getVertex(edge.getVertexB() );

				total++;

				if (type == 0 && vertexA.getClusterId() == vertexB.getClusterId() ){ // intra-cluster edge
						totalEdges++;
				}
				if (type == 1 && vertexA.getClusterId() != vertexB.getClusterId() ){ // inter-cluster edge
						totalEdges++;
				}
			}

			System.out.println(total);
			return totalEdges;

	}

	// Three modes defined by optFuction for calculateDirectedModularity and calculateUndirectedModularity:
	// 1. NEWMAN_MOD
	// 2. NEWMAN_WEIGHTED_MOD
	// 3. SIM_MOD = similarity-based-modularity
	//
	// Modularity for directed networks. works for directed&weighted and directed&unweighted networks
	//
		@SuppressWarnings("unchecked")
		public double calculateDirectedModularity(int optFunction)
		{
			// This method calculate directed modularity as described in Community structure in directed networks, EA leicht and MEJ Newman.
			// Formula : Q(directed)= 1/m Sum(i,j){Aij - (K(i)in-K(j)out)/m } Delta(i,j)
			// where m is number of links,
			// K(i)in is # of in links of node i
			// Delta(i,j) is one if i and j are in same cluster, otherwise 0
			// Aij is 1 for unweighted networks
			// Aij can be any number for weighted networks

			double m = net.getSumEdgeFactor(OUT, optFunction);
			double DirQ=0.0;
			double Aij=0.0;
			double local_minus=0.0;

			int vertexClust_fr;
			int vertexClust_to;

			Vertex vertex_fr;
			Vertex vertex_to;
			Vertex Op_vertex;
			Iterator<Vertex> itVertex_fr = net.getVertexIterator();

			while (itVertex_fr.hasNext())
			{ // Loop factor initialization
				Op_vertex = (Vertex) itVertex_fr.next();

				Op_vertex.setINDegreeFactor( Op_vertex.getDegreeFactor(IN, optFunction) );

				Op_vertex.setOUTDegreeFactor( Op_vertex.getDegreeFactor(OUT, optFunction) );

				//System.out.println("Vertex "+ Op_vertex.getLabel() +" has "+ Op_vertex.getINDegreeFactor()+ " in and "+  Op_vertex.getOUTDegreeFactor() + " out degree (F:"+optFunction+")");
			}

			int loop_cnt =0;
			int outDeg = 0;

			itVertex_fr = net.getVertexIterator();

			while (itVertex_fr.hasNext())
			{ // first loop
				vertex_fr = (Vertex) itVertex_fr.next();

				loop_cnt++;

				if ( loop_cnt % (1000) == 0) { System.out.println("Loop_cnt:"+ loop_cnt);}

				outDeg = vertex_fr.getOutDegree();

				Iterator<Vertex> itVertex_to = net.getVertexIterator();

				while (itVertex_to.hasNext())
				{ // second loop
					vertex_to = (Vertex) itVertex_to.next();

					vertexClust_fr = vertex_fr.getClusterId();
					vertexClust_to = vertex_to.getClusterId();

					//System.out.println(vertex_fr.getLabel()+"(" + vertexClust_fr + ") -> "+ vertex_to.getLabel()+"(" + vertexClust_to+ ")");

					// If both vertices are in different cluster, this part of calculation would be zero, so go to next calculation
					if ( vertexClust_fr <= 0 ||  vertexClust_to <= 0 || vertexClust_fr != vertexClust_to ) continue;

					//Set out_neighbors = vertex_fr.getNeighborhood(OUT);

					if ( vertex_fr.isNeighbor(vertex_to.getLabel(), OUT) && outDeg > 0 )	// outDeg >0, if left any out Degree to process
					{
						// optFunction can be NEWMAN_MOD, SIMMOD, NEWMAN_WEIGHTED_MOD
						// edgeFactor will be 1, sim, weight, respectively.
						Aij = vertex_fr.getEdgeFactor(vertex_to.getLabel(), OUT, optFunction);
						outDeg--; // keep how many out degree left to process
					}
					else
					{
						//System.out.println("Not an  out neighbor\n");
						Aij=0.0;
				    }

					local_minus = vertex_fr.getOUTDegreeFactor() * vertex_to.getINDegreeFactor();
					local_minus= (double) (local_minus / m);

					DirQ= DirQ + Aij - local_minus;

					//System.out.println("V out:" + vertex_fr.getOutDegree() + " In "+ vertex_to.getInDegree()+ " m:"+ m + " Aij:"+ Aij +" Local minus:" + local_minus+ " D_Q:"+ DirQ);


				} // second loop
			} // first loop
			DirQ = DirQ / m;

			BigDecimal bd = new BigDecimal(DirQ);
			bd = bd.setScale(4, RoundingMode.HALF_UP);
    		DirQ = bd.doubleValue();

			return DirQ;
	}


	//
	// Modularity for undirected networks. works for undirected&weighted and undirected&unweighted networks
	//
	@SuppressWarnings("unchecked")
	public double calculateUndirectedModularity(int optFunction)
	{
		// Traditional Newman's modularity
		// The formula is renewed based on definition in Community structure in directed networks, EA leicht and MEJ Newman.
		// Formula : Q(undirected)= 1/2m Sum(i,j){Aij - (K(i)-K(j))/2m } Delta(i,j)
		// where m is number of links,
		// K(i) is degree of node i: # of links for node i
		// Delta(i,j) is one if i and j are in the same cluster, otherwise 0
		// Aij is 1 for unweighted networks
		// Aij can be any number for weighted networks

		double m = net.getSumEdgeFactor(BIDIRECTIONAL, optFunction);
		double Q=0.0;
		double Aij=0.0;
		double local_minus=0.0;

		int vertexClust_fr;
		int vertexClust_to;

		Vertex vertex_fr;
		Vertex vertex_to;
		Iterator<Vertex> itVertex_fr = net.getVertexIterator();

		//System.out.println(optFunction+"m: "+m);

		while (itVertex_fr.hasNext())
		{ // first loop
			vertex_fr = (Vertex) itVertex_fr.next();

			Iterator<Vertex> itVertex_to = net.getVertexIterator();
			while (itVertex_to.hasNext())
			{ // second loop
				vertex_to = (Vertex) itVertex_to.next();

				vertexClust_fr = vertex_fr.getClusterId();
				vertexClust_to = vertex_to.getClusterId();

				//System.out.println(vertex_fr.getLabel()+"(" + vertexClust_fr + ") -> "+ vertex_to.getLabel()+"(" + vertexClust_to+ ")");

				// If both vertices are in different cluster, this part of calculation would be zero, so go to next calculation
				if ( vertexClust_fr <= 0 ||  vertexClust_to <= 0 || vertexClust_fr != vertexClust_to ) continue;

				if ( vertex_fr.isNeighbor(vertex_to.getLabel()) )	{
					// optFunction can be NEWMAN_MOD, SIMMOD, NEWMAN_WEIGHTED_MOD
					// edgeFactor will be 1, sim, weight, respectively.
					Aij = vertex_fr.getEdgeFactor(vertex_to.getLabel(), BIDIRECTIONAL, optFunction);
				}
				else { Aij = 0.0; }


				local_minus = vertex_fr.getDegreeFactor(BIDIRECTIONAL, optFunction)*vertex_to.getDegreeFactor(BIDIRECTIONAL, optFunction);
				local_minus= (double) (local_minus / (2*m));

				Q= Q + Aij - local_minus;

				//System.out.println("V out:" + vertex_fr.getOutDegree() + " In "+ vertex_to.getInDegree()+ " m:"+ m + " Aij:"+ Aij +" Local minus:" + local_minus+ " Q:"+ Q);


			} // second loop
		} // first loop
		Q = Q / (2*m);


		BigDecimal bd = new BigDecimal(Q);
		bd = bd.setScale(4, RoundingMode.HALF_UP);
    	Q = bd.doubleValue();

		return Q;


	}






	@SuppressWarnings("unchecked")
	public double calculateHomogeneity(){

				TreeMap<Integer, Double> clusterTotalHomogeneity = new  TreeMap<Integer, Double>();
				TreeMap<Integer, Integer> clusterTotalSize = new  TreeMap<Integer, Integer>();

				int vertexClust;
				int ls, ds;
				double vertexHomogeneity = 0.0;
				double homogeneity = 0.0;


				Vertex vertex;
				Iterator<Vertex> itVertex = net.getVertexIterator();
				while (itVertex.hasNext()) {
					vertex = (Vertex) itVertex.next();
					vertexClust = vertex.getClusterId();

					ls = 0;
					ds = 0;


					Set<String> neighbors = vertex.getNeighborhood();
					Iterator<String> itNeighbors = neighbors.iterator();
					while (itNeighbors.hasNext() ){
						String neighbor_s = (String) itNeighbors.next();
						Vertex neighbor = net.getVertex(neighbor_s);

						if (vertexClust == neighbor.getClusterId() ) {	// vertices that belong to the same cluster
							ls++;
						}
					}
					ds = neighbors.size();
					vertexHomogeneity = (double) ls / ds;

					add(clusterTotalHomogeneity, vertexClust, vertexHomogeneity);
					add(clusterTotalSize, vertexClust, 1);

					//System.out.println(vertex.getLabel() + " -- " + vertexHomogeneity);

				
			  	} // while


				Iterator<Integer> itClusters = clusterTotalHomogeneity.keySet().iterator();
				while ( itClusters.hasNext() ) {
						int clusterId = itClusters.next();
						homogeneity += clusterTotalHomogeneity.get(clusterId) /  clusterTotalSize.get(clusterId);
				}

				homogeneity = (double) homogeneity / clusterTotalHomogeneity.keySet().size();

			  	return homogeneity;


		} // calculateHomogeneity


    /*
	// based on the paper entitled
	// "Local modularity measure for network clusterizations"
	// Stefanie Muff, Francesco Rao, and Amedeo Caflisch, PHYSICAL REVIEW E 72, 056107 (2005)
	public double localModularity(){

				TreeMap <Integer, Integer> ls = new  TreeMap<Integer, Integer>();
				TreeMap <Integer, Integer> ds = new  TreeMap<Integer, Integer>();
				TreeMap <String, Integer> ds_NM = new  TreeMap<String, Integer>();
				TreeMap <Integer, TreeSet> neighborClusters = new TreeMap<Integer, TreeSet>();
				TreeMap <String, TreeSet> neighborClusters_NM = new TreeMap<String, TreeSet>();
				TreeMap <Integer, Double> clusterSizes = new TreeMap<Integer, Double>();
				TreeMap <Integer, Integer> neighborNonMembers = new TreeMap<Integer, Double>();
				TreeMap <String, Integer> neighborNonMembers_NM = new TreeMap<String, Double>();

				int nonmembers = 0;
				Iterator itVertex = net.getVertexIterator();
				while (itVertex.hasNext()) {
					Vertex vertexA = (Vertex) itVertex.next();
					int clusterA = vertexA.getClusterId();

					Set neighbors = vertexA.getNeighborhood();
					Iterator itNeighbors = neighbors.iterator();
					while (itNeighbors.hasNext() ){
						String neighbor_s = (String) itNeighbors.next();
						Vertex vertexB = net.getVertex(neighbor_s);
						int clusterB = vertexB.getClusterId();


						// properly calculate ls and ds
						if (clusterA >= 0) {							// vertices that belong to a cluster
							if (clusterA == clusterB ){
								add(ls, clusterA, 1);

							}
							add(ds, clusterA, 1);

							if (clusterB >= 0){
								// build a list of neighbor clusters for each cluster
								if (!neighborClusters.containsKey(clusterA)){
									neighborClusters.put(clusterA, new TreeSet<Integer>());
								}
								neighborClusters.get( clusterA ).add( clusterB );
							}
							else{
								add(neighborNonMembers, clusterA, 1);
							}

						}
						else{											// nonmembers, hubs and outliers
							add(ds_NM, vertexA, 1);						// ls will be zero for them
																		// ds will still count
							if (clusterB >= 0){
								// build a list of neighbor clusters for each nonmember
								if (!neighborClusters_NM.containsKey(vertexA)){
									neighborClusters_NM.put(vertexA, new TreeSet<Integer>());
								}
								neighborClusters_NM.get( vertexA ).add( clusterB );
							}
							else{
								add(neighborNonMembers_NM, vertexA, 1);
							}

						}

					} // while neighbors iterator

			  	} // while vertex iterator


				double LQ = 0.0;
				Iterator<Integer> itClusters = ls.keySet().iterator();
				while ( itClusters.hasNext() ) {
						int clusterId = itClusters.next();
						int LiN = 0;
						Set neighborClusters = neighborClusters.get(clusterId);
						Iterator<Integer> itNeighborClusters = neighborClusters.iterator();
						while (itNeighborClusters.hasNext()) {
							int neighborCluster = itNeighborClusters.next();
							LiN += ls.get(neighborCluster);
						}
						LiN += ds.get( clusterId );
						LQ += ( ls.get(clusterId) / LiN) -  (ds.get(clusterId) / Math.pow(LiN, 2));
				}



				Iterator<String> itNonMembers = ds_NM.keySet().iterator();
				while ( itNonMembers.hasNext() ) {
						String nonMember = itNonMembers.next();
						double LiN = 0.0;
						Set neighborClusters_NM = neighborClusters_NM.get(nonMember);
						Iterator<Integer> itNeighborClusters_NM = neighborClusters.iterator();
						while (itNeighborClusters_NM.hasNext()) {
							int neighborCluster = itNeighborClusters_NM.next();
							LiN += clusterSizes.get(neighborCluster);
						}
						LiN += neighborNonMembers_NM.get( nonMember );
						LQ += ( ls.get(nonMember) / LiN) -  (ds.get(nonMember) / Math.pow(LiN, 2));
				}

				return LQ;


	} // localModularity
	*/


	@SuppressWarnings({ "unchecked", "rawtypes" })
	public double calculateDensity(){

		TreeMap <Integer, Integer> edgesWithinCluster = new TreeMap<Integer, Integer>();
		TreeMap <Integer, TreeSet> verticesWithinCluster = new TreeMap<Integer, TreeSet>();
		Vertex vertexA, vertexB;
		Edge edge;

		Iterator<Edge> itEdge = net.getEdgeIterator();
		while (itEdge.hasNext()) {
			edge = (Edge) itEdge.next();
			vertexA = net.getVertex(edge.getVertexA() );
			vertexB = net.getVertex(edge.getVertexB() );
			int clusterA = vertexA.getClusterId();
			int clusterB = vertexB.getClusterId();

			if ( clusterA >= 0 && clusterB >= 0){ // if they are both non-outliers

				// add vertexA into its cluster
				if ( ! verticesWithinCluster.containsKey(clusterA ) ) {
					verticesWithinCluster.put (clusterA, new TreeSet<String>());
				}
				verticesWithinCluster.get( clusterA ).add( vertexA.getLabel() );

				// add vertexB into its cluster
				if ( ! verticesWithinCluster.containsKey(clusterB ) ) {
					verticesWithinCluster.put (clusterB, new TreeSet<String>());
				}
				verticesWithinCluster.get( clusterB ).add( vertexB.getLabel() );


				if ( clusterA == clusterB ){ // intra-cluster edge
					if ( ! edgesWithinCluster.containsKey(clusterA) ) {
						edgesWithinCluster.put (clusterA, 1);  			// one edge at the beginning
					}
					else { edgesWithinCluster.put (clusterA, edgesWithinCluster.get(clusterA)+1); } // increase number of edges by one
				}
			}

		}

		// density calculation
		double density          = 0.0;
		double weighted_density = 0.0;

		Iterator itCluster = verticesWithinCluster.keySet().iterator();
		while (itCluster.hasNext()) {
			int cluster = (Integer) itCluster.next();
			TreeSet<String> vertices  = (TreeSet) verticesWithinCluster.get( cluster );
			int clusterSize = vertices.size();
			//System.out.println("edges within cluster: " + edgesWithinCluster.get(cluster) + " vertices within cluster: " + clusterSize);
			density = (double) edgesWithinCluster.get(cluster) / (clusterSize * (clusterSize - 1) / 2); // nominator: number of edges within the cluster
																											// denominator: possible number of edges
			weighted_density += (double) density * clusterSize;

		}


		// denominator: number of vertices
		double weighted_average_density = weighted_density / net.getNumVertices();

		//System.out.println("num-vertices: " + net.getNumVertices());
		System.out.println("WAD: " + weighted_average_density);
		//System.out.println("AD: " + average_density);
		return weighted_average_density;

	}


	// local clustering coefficient: calculated for each vertex in the network/group
	// local-clust-coefficient(v1) = edges between nearest neighbors of v1 / possible number of connections betweeen neighbors of v1
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public double calculateLocalClustCoef(){

			double clust_coef = 0.0; double total_clust_coef = 0.0;
			Vertex vertex;
			Iterator<Vertex> itVertex = net.getVertexIterator();
			while (itVertex.hasNext()) {
				vertex = (Vertex) itVertex.next();
				int vertexClust = vertex.getClusterId();

				Set<String> neighbors = vertex.getNeighborhood();
				Set<String> neighbors_of_same_group = new HashSet();
				Iterator<String> itNeighbors = neighbors.iterator();
				while (itNeighbors.hasNext() ){
					String neighbor_s = (String) itNeighbors.next();
					Vertex neighbor = net.getVertex(neighbor_s);
					if (vertexClust == neighbor.getClusterId() ){
						neighbors_of_same_group.add(neighbor.toString());
					}
				}

				Object[] neighbors_sg = neighbors_of_same_group.toArray();

				int edges_between_neighbors = 0;
				for (int i=0; i<neighbors_sg.length; i++){
					for (int j=i+1; j<neighbors_sg.length; j++){

						Vertex vertexA = (Vertex) neighbors_sg[i];
						Vertex vertexB = (Vertex) neighbors_sg[j];
						if ( vertexA.getNeighborhood().contains(vertexB.getLabel()) ){
							edges_between_neighbors++;
						}
					}
				}

				if (neighbors_sg.length > 1){
					clust_coef = (double) edges_between_neighbors / (neighbors_sg.length * (neighbors_sg.length-1) / 2);
				}
				else {
					clust_coef = 0.0;
				}

				total_clust_coef += clust_coef;

				//System.out.println("vertex: " + vertex.getLabel() + "\t" + edges_between_neighbors + "\t" + neighbors_sg.length + "\t" + clust_coef);

			}


			return (double) total_clust_coef / net.getNumVertices() ;

	}




	private void add(TreeMap<Integer, Double> X, int key, double sim){

		if (X.containsKey(key)){
			X.put(key, X.get(key) + sim);
		}
		else{
			X.put(key, sim);
		}
	}

	private void add(TreeMap<Integer, Integer> X, int key, int increment){

		if (X.containsKey(key)){
			X.put(key, X.get(key) + increment);
		}
		else{
			X.put(key, increment);
		}
	}

	@SuppressWarnings("unused")
	private void add(TreeMap<String, Integer> X, String key, int increment){

			if (X.containsKey(key)){
				X.put(key, X.get(key) + increment);
			}
			else{
				X.put(key, increment);
			}
	}






}

