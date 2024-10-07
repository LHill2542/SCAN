// Batch mode SCAN algorithm that runs for different eps values in [0,1]
// mu = 2 by default


package StructuralClusteringAlgorithmsRelease;

import java.util.*;
import java.io.*;
import java.lang.*;
import java.math.*;


public class Main implements Constants{

		private OpenFile openFile;
		private SaveFile saveFile;
		private Network network;
		private SCAN scan;
		private Evaluate evaluate;


    	public static void main(String[] args) {
			Main main = new Main();

			if (args.length != 3){
				System.out.println("Three parameters are required:");
				System.out.println("<filename without extension> <d=true/false> <w=true/false>");
				return;
			}
			else {
				main.run(args);
			}

		}


		public void run(String[] args){


			String filename = args[0];
			boolean b_directed = false;
			boolean b_weighted = false;

			if ( (  (String) args[1]).equals("d=true"))
			{
				b_directed = true;
			}

			if ( (  (String) args[2]).equals("w=true"))
						{
				b_weighted = true;
			}

			network   = new Network(b_directed, b_weighted);
			openFile  = new OpenFile(network);
			saveFile  = new SaveFile(network);
			scan      = new SCAN(network);
			evaluate  = new Evaluate(network);


			openFile.openPairsFile(filename + ".pairs");		// open file and load network
			network.setSimilarityFunction(COS);
			network.calculateSimilarities();


			// SCAN algorithm
			double modularity = 0.0;
			double sim_modularity = 0.0;
			double weighted_modularity = 0.0;
			long start = System.currentTimeMillis();


			for (double eps_iterator=0.1; eps_iterator<=1.0; eps_iterator+=0.1){

				 //round eps so that the values are 0.1, 0.2, 0.3, 0.4 etc.
				 BigDecimal bd = new BigDecimal(eps_iterator);
				 bd = bd.setScale(2,BigDecimal.ROUND_HALF_UP);
    			 double eps = bd.doubleValue();

				scan.run(eps, 2);

				long elapsedTimeMillis = System.currentTimeMillis()-start;
				float elapsedTimeSec = elapsedTimeMillis/1000F;

				System.out.println("\n\neps= " + eps + " mu= " + 2);
				System.out.println("Elapsed time in milliseconds: " + elapsedTimeMillis );
				System.out.println("Elapsed time in seconds: " + elapsedTimeSec );

				if (b_directed){
					modularity= evaluate.calculateDirectedModularity(NEWMAN_MOD);
					//weighted_modularity= evaluate.calculateDirectedModularity(NEWMAN_WEIGHTED_MOD);
					//sim_modularity = evaluate.calculateDirectedModularity(SIM_MOD);
				}
				else{

					modularity= evaluate.calculateUndirectedModularity(NEWMAN_MOD);
					//weighted_modularity= evaluate.calculateUndirectedModularity(NEWMAN_WEIGHTED_MOD);
					//sim_modularity = evaluate.calculateUndirectedModularity(SIM_MOD);
				}


				saveFile.clustResultsCommaDelimited(filename+"_"+ b_directed+"_"+b_weighted, eps, 2, modularity);


				/*
				int p = evaluate.numEdges(0); // number of edges within clusters
				int q = evaluate.numEdges(1); // number of edges between clusters
				System.out.println("p = " + p);
				System.out.println("q = " + q);
				*/
			}

		}



}