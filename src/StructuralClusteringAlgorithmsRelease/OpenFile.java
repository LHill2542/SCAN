/* author nxyuruk@ualr.edu
 *
 * August 03, 2007
 *
 * This class contains methods to open and load different types of input files
 * Input files: .pairs, .gml, .graphml
 */

package StructuralClusteringAlgorithmsRelease;

import java.io.*;
import java.util.*;

public class OpenFile{

		private Network net;

		public OpenFile(Network network){
			this.net = network;
		}

		//
		// open and loads network from plain .pairs file
		// file format: vertex1 vertex2
		// builds the network in the memory
		//
		public void openPairsFile(String filename){

			String line = null;
			System.out.println (filename);

			String[] elements = new String[3];

			try {
				FileInputStream fstream = new FileInputStream(filename);
			 	DataInputStream in = new DataInputStream(fstream);
				while ( (line = in.readLine()) != null ) {

					//System.out.println (line);

					// <Added by MM>
					if (line.charAt(0) == '#') continue;
					// </Added by MM>

					//System.out.println (line);

					elements = line.split(" |\t"); // Split by space or tab // <Added by MM>

					// vertices = line.split(" "); // <Uncommented by MM>

					String vertexA = elements[0];
					String vertexB = elements[1];

					double weight = 1.0;
					if (net.isWeighted()){ weight = java.lang.Double.valueOf(elements[2]); }

					net.addEdge(vertexA, vertexB, weight);		// vertexA, vertexB are names for vertices, as read from the input file

				}
				in.close();
			} catch (Exception e) { System.err.println("File input error"); }


		}

		/**
		Open file for true classes of vertices
		File format: vertex-name \t cluster-id
		Set cluster ids for the vertices of the network
		*/
		public void openTrueClasses(String filename){

				String line = null;
				System.out.println (filename);

				int num_clusters = 0; int clusterId = 0;
				TreeMap<String, Integer> clusterIds = new  TreeMap<String, Integer>();
				try {
					FileInputStream fstream = new FileInputStream(filename);
					DataInputStream in = new DataInputStream(fstream);
					while ( (line = in.readLine()) != null ) {
						//System.out.println (line);
						String[] vertices = line.split("\t");
						String vertex = vertices[0];
						String cluster = vertices[1];

						if (!clusterIds.containsKey(cluster)) { clusterIds.put(cluster, num_clusters); num_clusters++; }
						else { clusterId = clusterIds.get(cluster); }

						net.getVertex(vertex).setClusterId(clusterId);

						//System.out.println(vertex + " = " + clusterId);

					}
					in.close();
				} catch (Exception e) { System.err.println("File input error --true classes file"); }

		}

}