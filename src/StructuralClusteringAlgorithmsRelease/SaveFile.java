/* author nxyuruk@ualr.edu
 *
 * September 19, 2007
 *
 * This class contains methods to save clustering results into files
 *
 */

package StructuralClusteringAlgorithmsRelease;

import java.io.*;
import java.util.*;

public class SaveFile implements Constants{

		private Network net;

		public SaveFile(Network network){
			this.net = network;
		}

		//
		// save clustering results into file
		// file format:
		// cluster1: vertex11, vertex12, vertex13
		// cluster2: vertex21, vertex22, vertex23
		// ...
		@SuppressWarnings("unchecked")
		public void clustResultsCommaDelimited(String filename, double eps, int mu, double modularity){

				int nonmembers = 0;
				String outFile = filename + "__SCAN_clusters.txt";
				try {

					// BufferedWriter out = new BufferedWriter(new FileWriter(outFile));
					BufferedWriter out = new BufferedWriter(new FileWriter(outFile, true));
					out.write("\n\n---------- SCAN result ----------");
					out.write("\n" + filename + ", epsilon=" + eps + ", mu=" + mu);

					//iterate through vertices, build list of vertices for each cluster
					TreeMap <Integer, TreeSet> clusterMembers = new TreeMap<Integer, TreeSet>();
					Iterator itVertex = net.getVertexIterator();
					while (itVertex.hasNext()) {
							Vertex vertex = (Vertex) itVertex.next();
							if ( ! clusterMembers.containsKey(vertex.getClusterId() ) ) {
								clusterMembers.put (vertex.getClusterId(), new TreeSet<String>());
							}
							clusterMembers.get( vertex.getClusterId() ).add( vertex.getLabel() );
							vertex.setClusterId(UNCLASSIFIED);
					}



					// iterate through found clusters, print members of each cluster
					Iterator<Integer> itCluster = clusterMembers.keySet().iterator();
					while (itCluster.hasNext()) {
						int cluster = (Integer) itCluster.next();
						TreeSet<String> members  = (TreeSet<String>) clusterMembers.get( cluster );
						if ( members.size() == 0 ) { continue; }
						else {
							if (cluster == OUTLIER)  { out.write("\nOUTLIERS:");  }
							else if (cluster == HUB) { out.write("\nHUBS:");  }
							else 				     { out.write("\nCluster[" + cluster + "]:"); }
						}
						//out.write(members.size() + "\t");

						Iterator<String> itMembers = members.iterator();
						while ( itMembers.hasNext() ) {
								String member = itMembers.next();
								out.write(member + ",");
								if (cluster == OUTLIER || cluster == HUB) {nonmembers++;}
						}
					}
					out.write("\nModularity: " + modularity);
					out.close();
				}
				catch (IOException e) { System.out.println("Error in writing to output file!"); }

				System.out.println("outliers: " + nonmembers);
		}




		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void clustResultsCommaDelimited(String filename){

						String outFile = filename + "__AHSCAN_clusters.txt";
						try {

							BufferedWriter out = new BufferedWriter(new FileWriter(outFile));
							out.write("###");


							//iterate through vertices, build list of vertices for each cluster
							TreeMap <Integer, TreeSet> clusterMembers = new TreeMap<Integer, TreeSet>();
							Iterator itVertex = net.getVertexIterator();
							while (itVertex.hasNext()) {
									Vertex vertex = (Vertex) itVertex.next();
									if ( ! clusterMembers.containsKey(vertex.getClusterId() ) ) {
										clusterMembers.put (vertex.getClusterId(), new TreeSet<String>());
									}
									clusterMembers.get( vertex.getClusterId() ).add( vertex.getLabel() );
							}
							// iterate through found clusters, print members of each cluster
							Iterator itCluster = clusterMembers.keySet().iterator();
							while (itCluster.hasNext()) {
								int cluster = (Integer) itCluster.next();
								TreeSet<String> members  = (TreeSet) clusterMembers.get( cluster );
								if ( members.size() == 0 ) { continue; }
								else {
									 out.write("\n" + cluster + ":");
								}
								//out.write(members.size() + "\t");

								Iterator<String> itMembers = members.iterator();
								while ( itMembers.hasNext() ) {
										String member = itMembers.next();
										out.write(member + ",");
								}
							}
							out.close();
						}
						catch (IOException e) { System.out.println("Error in writing to output file!"); }


		}


		//
		// save clustering results into file
		// file format:
		// vertex \t cluster \n
		@SuppressWarnings("unchecked")
		public void clustResultsTabDelimited(String filename){

				String outFile = filename + "__AHSCAN_clusters.tc";
				try {

					BufferedWriter out = new BufferedWriter(new FileWriter(outFile));

					//iterate through vertices, print vertex label and clusterId
					Iterator<Vertex> itVertex = net.getVertexIterator();
					while (itVertex.hasNext()) {
							Vertex vertex = (Vertex) itVertex.next();
							int cluster = vertex.getClusterId();
							out.write(vertex.getLabel() + "\t" + cluster + "\n");
					}
					out.close();
				} catch (IOException e) { System.out.println("Error in writing to output file!"); }
		}


}