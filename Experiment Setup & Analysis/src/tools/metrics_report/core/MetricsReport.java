package tools.metrics_report.core;

import java.text.DecimalFormat;

import util.DoubleList;


/**
 * Calculate metrics based on the intervals between 
 * consecutive visits.
 * 
 * @author Pablo A. Sampaio
 */
public class MetricsReport {
	private int numNodes;
	private int startTime, endTime;  // closed interval

	private VisitsList visits;
	
	private DoubleList[] intervalsByNode; // each list has the intervals of a node
	private DoubleList   allIntervals;
	private DoubleList   allWeights; 

	private DoubleList nodesVisitsCount;
	private DoubleList nodesIdlenesses;
	private DoubleList nodesRelativeIdlenesses;
	private DoubleList nodePriorities;
	
	private Double[] instantsAvgIdlenesses;
	


	public MetricsReport(int nodes, int initialTime, int finalTime, VisitsList list,
			DoubleList nodePriorities) {
		init(nodes, initialTime, finalTime, list, nodePriorities);
	}

	public MetricsReport(int nodes, int initialTime, int finalTime, VisitsList list) {
		DoubleList priorities = new DoubleList();
		for (int v = 0; v < nodes; v++) {
			priorities.add(1.0d);
		}
		init(nodes, initialTime, finalTime, list, priorities);
	}
	
	private void init(int nodes, int initialTime, int finalTime, VisitsList list,
			DoubleList nodePriorities) {
		numNodes = nodes;
		startTime = initialTime;
		endTime = finalTime;
		this.nodePriorities = nodePriorities; 
		
		visits = list.filterByTime(startTime, endTime);
		
		intervalsByNode = new DoubleList[nodes];
		nodesVisitsCount = new DoubleList(nodes);
		
		int sumSize = 0;
		
		for (int v = 0; v < numNodes; v++) {
			intervalsByNode[v] = calculateIntervals(v);
			sumSize += intervalsByNode[v].size();
		}

		allIntervals = new DoubleList(sumSize);
		allWeights = new DoubleList(sumSize);
		
		for (int v = 0; v < numNodes; v++) {
			allIntervals.addAll(intervalsByNode[v]);
			
			for (int i = 0; i < intervalsByNode[v].size(); i++) {
				allWeights.add(nodePriorities.get(v));
			}
		}

		nodesIdlenesses = calculateNodeAvgIdlenessesPerNode();
		instantsAvgIdlenesses = calculateNodeIdlenessesPerTimeUnit();
		
		nodesRelativeIdlenesses = calculateNodeRelativeIdlenesses(nodePriorities);
	}
	
	private VisitPriority[] calculateVisitsByPriority(int maxPriority) {
		
		VisitPriority[] vp = new VisitPriority[maxPriority];
		
		for(int i = 0; i < vp.length; i++){
			VisitPriority v = new VisitPriority(i+1, 0);
			vp[i] = v;
		}
				
		for(int i = 0; i < visits.getNumVisits(); i++){
			Visit visit = visits.getVisit(i);
			int vertex = visit.vertex;
			int priority = (int) Math.round(nodePriorities.get(vertex));
			if( priority > maxPriority ){
				continue;
			}
			vp[priority-1].incCount();
			vp[priority-1].putNode(vertex);
		}
		for(int i = 0; i < vp.length; i++){
			vp[i].calcVisitRate();
			vp[i].calcRelativeVisitRate(vp[0].getVisitRate());
		}
		return vp;
	}
	
	public String getRelativeVisitRates(int maxPriority) {
		VisitPriority[] priorityVisits = calculateVisitsByPriority(10);
		String relativesVisitRates = "";
		DecimalFormat df= new DecimalFormat("#0.000");
		for(int i = 0; i < maxPriority; i++){
			relativesVisitRates += df.format(priorityVisits[i].getRelativeVisitRate())+";";
		}
		return relativesVisitRates;
	}

	
	private DoubleList calculateIntervals(int node) {
		DoubleList intervals = new DoubleList();
		
		VisitsList nodeVisits = visits.filterByVertex(node);
		
		int lastVisitTime = startTime;
		double interval;
		
		Visit v;
		
		nodesVisitsCount.add(nodeVisits.getNumVisits());
		
		for (int i = 0; i < nodeVisits.getNumVisits(); i ++) {
			v = nodeVisits.getVisit(i);
			
			interval = (v.time - lastVisitTime);
			intervals.add(interval);			
			lastVisitTime = v.time;
		}
		
		interval = (endTime + 1 - lastVisitTime);
		intervals.add(interval);
		
		return intervals;
	}

	// for each time unit, average per node
	private Double[] calculateNodeIdlenessesPerTimeUnit() {
		return getAverageIdleness_curb(1);
	}
	
	// for each node, the idlenesse averaged along the time units
	private DoubleList calculateNodeAvgIdlenessesPerNode() {
		DoubleList list = new DoubleList(numNodes);
			
		double vertexSumIdlenesses = 0.0d;
		int interval;
			
		for (int node = 0; node < numNodes; node++) {
			
			for (int i = 0; i < intervalsByNode[node].size(); i ++) {
				interval = (int)intervalsByNode[node].get(i);
					
				// during consecutive visits, the idleness grows in an arithmetic progression
				// this is the formula of the sum of this arithmetic progression  
				vertexSumIdlenesses += (interval*interval + interval) / 2;				
			}

			list.add(vertexSumIdlenesses / (endTime - startTime + 1));
			
		}

		return list;
	}
	
	private DoubleList calculateNodeRelativeIdlenesses(DoubleList priorities) {
		DoubleList list = new DoubleList(numNodes);
			
		double vertexSumIdlenesses = 0.0d;
		int interval;
			
		for (int node = 0; node < numNodes; node++) {
			
			for (int i = 0; i < intervalsByNode[node].size(); i ++) {
				interval = (int)intervalsByNode[node].get(i);
					
				// during consecutive visits, the idleness grows in an arithmetic progression
				// this is the formula of the sum of this arithmetic progression  
				vertexSumIdlenesses += (interval*interval + interval) / 2;				
			}

			list.add((vertexSumIdlenesses*priorities.get(node)) / (endTime - startTime + 1));
			
		}

		return list;
	}
		
	

	/***** Metrics based on intervals *****/
	
	/**
	 * Maximum interval between consecutive visits, considering all 
	 * intervals from all nodes.
	 */
	public double getMaxInterval() {
		return allIntervals.max();
	}
	
	/**
	 * Maximum interval between consecutive visits, considering all 
	 * intervals from all nodes.
	 */
	public double getMaxRelativeInterval() {
		return allIntervals.max(this.allWeights);
	}

	/**
	 * Average interval between consecutive visits, considering all 
	 * intervals from all nodes.
	 */
	public double getAverageInterval() {
		return allIntervals.mean();
	}
	
	/**
	 * Average interval between consecutive visits considering the weights, considering all 
	 * intervals from all nodes.
	 */
	public double getAverageRelativeInterval() {
		return allIntervals.generalizedMean(1.0d, this.allWeights);
		//return allIntervals.generalizedMean2(1.0d, this.allWeights); //sem sentido
	}

	/**
	 * Standard deviation of the intervals between consecutive visits, 
	 * considering all intervals from all nodes.
	 */
	public double getStdDevOfIntervals() {
		return allIntervals.standardDeviation();
	}
	
	/**
	 * Quadratic mean of the intervals between consecutive visits,
	 * considering all intervals from all nodes.
	 */
	public double getQuadraticMeanOfIntervals() {
		return allIntervals.generalizedMean(2.0d, this.allWeights);
	}


	public double getSpecialMeanOfIntervals() {
		return allIntervals.specialMean();
	}
	
	/**
	 * Generalized mean of the intervals between consecutive visits,
	 * considering all intervals from all nodes.
	 * The priorities of the nodes are used as weights for each interval.
	 */
	public double getGeneralizedMeanOfIntervals(double p) {
		return allIntervals.generalizedMean(p, this.allWeights);
	}
	
	
	/***** Metrics based on idleness *****/

	/**
	 * Maximum instantaneous idleness of all nodes in all time instants.
	 * (It is the same as the maximum interval). 
	 */
	public double getMaxInstantaeousIdleness() {
		return getMaxInterval();
	}
	
	/**
	 * Average idlenesses along the simulation, averaged by the number of nodes
	 * (average of nodes of average in time or vice-versa).  
	 */
	public double getAverageIdleness() {
		return nodesIdlenesses.mean();
	}
	public double getAverageIdleness2() {
		int time = instantsAvgIdlenesses.length;
		System.out.println("TIME: " + time);
		
		double sum = 0.0d;
		for (int i = 0; i < instantsAvgIdlenesses.length; i++) {
			sum += instantsAvgIdlenesses[i];			
		}
		
		return sum/time;
	}
	
	
	public double getAverageRelativeIdleness() {
		return nodesRelativeIdlenesses.mean();
	}
	
	
	/***** Metrics based on the number of visits per node *****/
	
	/**
	 * Total number of visits, considering all nodes.
	 */
	public double getTotalVisits() {
		return nodesVisitsCount.sum();
	}
	
	/**
	 * Average number of visits per node.
	 */
	public double getAverageVisits() {
		return nodesVisitsCount.mean();
	}
	
	/**
	 * Standard deviation of the numbers of visits per node. 
	 */
	public double getStdDevVisits() {
		return nodesVisitsCount.standardDeviation();
	}
	
	
	/**
	 * Exploration time (time at which each node has been visited at least once)
	 * 
	 *  returns -1 if the graph is not explored at the end of the simulation
	 */
	public double getExplorationTime() {
		boolean[] explored = new boolean[numNodes]; //all false, initially
		
		int numVis = 0;
		Visit visit ;
		boolean test = true;
		
		while(numVis < visits.getNumVisits()){
			visit = visits.getVisit(numVis);
			if(!explored[visit.vertex]){
				explored[visit.vertex] = true;
				test = true;
				for(boolean exp : explored)
					test &= exp;
				if(test)
					return visit.time;			
			}
			numVis++;
		}
		
		return -1;
	}
	
	/**
	 *  Normalized Exploration time ( exp_time * nbAgents/nbNodes)
	 * 
	 */
	public double getNormExplorationTime(int numAgents) {
		double exp_time = getExplorationTime();
		if(exp_time == -1)
			return -1;
		
		return exp_time * numAgents / numNodes;
	}
	
	public int[] getIntervalsHistogram() {
		int[] histogram = new int[(int)this.allIntervals.max() + 1];
		int interval;
		
		for (int i = 0; i < this.allIntervals.size(); i ++) {
			interval = (int)this.allIntervals.get(i);
			histogram[interval] ++;
		}
		
		return histogram;
	}
	
	
	/***** Metrics for curb representation *****/
	
	/**
	 * Average idleness as a function of time, considering all nodes.
	 * @param freq 
	 * 			The average idleness is given every freq turn/seconds 
	 * 			(depending on type of simulation)
	 * 
	 * @author Cyril Poulet
	 */
	public Double[] getAverageIdleness_curb(int freq) {
		int[] intervals = new int[numNodes];		
		Double[] values = new Double[(endTime - startTime)/freq + 1];
		
		int numVis = 0;
		Visit visit = visits.getVisit(numVis);
		
		for(int i = startTime; i<= endTime; i++){
			for(int j = 0; j < numNodes; j++)
				intervals[j]++;
			while(visit.time == i){
				intervals[visit.vertex] = 0;
				numVis++;
				if(numVis < visits.getNumVisits())
					visit = visits.getVisit(numVis);
				else
					break;
			}
			
			if(i % freq == 0){
				double sum = 0.0;
				for(int interval : intervals)
					sum += interval;
				
				values[i/freq] = sum/numNodes;
			}			
		}
		
		return values;
	}
	
	
	/**
	 * Maximum idleness as a function of time, considering all nodes.
	 * @param freq 
	 * 			The maximum idleness is given every freq turn/seconds 
	 * 			(depending on type of simulation)
	 * 
	 * @author Cyril Poulet
	 */
	public Double[] getMaxIdleness_curb(int freq) {
		int[] intervals = new int[numNodes];		
		Double[] values = new Double[(endTime - startTime)/freq + 
		                             (((endTime - startTime) % freq ==0)? 1 : 0)];
		
		int numVis = 0;
		Visit visit = visits.getVisit(numVis);
		
		for(int i = startTime; i<= endTime; i++){
			for(int j = 0; j < numNodes; j++)
				intervals[j]++;
			while(visit.time == i){
				intervals[visit.vertex] = 0;
				numVis++;
				if(numVis < visits.getNumVisits())
					visit = visits.getVisit(numVis);
				else
					break;
			}
			
			if(i % freq == 0){
				double max = intervals[0];
				for(int interval : intervals)
					if(interval > max)
						max = interval;
				
				values[i/freq] = max;
			}			
		}
		
		return values;
	}
	
	/**
	 * Standart Deviation as a function of time, considering all nodes.
	 * @param freq 
	 * 			The standart deviation in idleness is given every freq turn/seconds 
	 * 			(depending on type of simulation)
	 * 
	 * @author Cyril Poulet
	 */
	public Double[] getStdDev_curb(int freq) {
		int[] intervals = new int[numNodes];		
		Double[] values = new Double[(endTime - startTime)/freq + 
		                             (((endTime - startTime) % freq ==0)? 1 : 0)];
		
		int numVis = 0;
		Visit visit = visits.getVisit(numVis);
		
		for(int i = startTime; i<= endTime; i++){
			for(int j = 0; j < numNodes; j++)
				intervals[j]++;
			while(visit.time == i){
				intervals[visit.vertex] = 0;
				numVis++;
				if(numVis < visits.getNumVisits())
					visit = visits.getVisit(numVis);
				else
					break;
			}
			
			if(i % freq == 0){
				double sum = 0.0;
				for(int interval : intervals)
					sum += interval;
				
				double stddev = 0;
				double diff = 0;
				for(int interval : intervals){
					diff = (interval - sum/numNodes);
					stddev += diff*diff;
				}
					
				
				values[i/freq] = Math.sqrt(stddev/numNodes);
			}			
		}
		
		return values;
	}
	
	/**
	 * Number of visits as a function of time, considering all nodes.
	 * @param freq 
	 * 			The number of visits so far is given every freq turn/seconds 
	 * 			(depending on type of simulation)
	 * 
	 * @author Cyril Poulet
	 */
	public Double[] getVisitsNum_curb(int freq) {		
		Double[] values = new Double[(endTime - startTime)/freq + 
		                             (((endTime - startTime) % freq ==0)? 1 : 0)];
		
		int numVis = 0;
		Visit visit = visits.getVisit(numVis);
		
		for(int i = startTime; i<= endTime; i++){
			while(visit.time == i){
				numVis++;
				if(numVis < visits.getNumVisits())
					visit = visits.getVisit(numVis);
				else
					break;
			}
			
			if(i % freq == 0){
				values[i/freq] = numVis/1.0;
			}			
		}
		
		return values;
	}
	
	/**
	 * Average Number of visits as a function of time, considering all nodes.
	 * @param freq 
	 * 			The average number of visits so far is given every freq turn/seconds 
	 * 			(depending on type of simulation)
	 * 
	 * @author Cyril Poulet
	 */
	public Double[] getVisitsAvg_curb(int freq) {	
		int[] visitnums = new int[numNodes];
		Double[] values = new Double[(endTime - startTime)/freq + 
		                             (((endTime - startTime) % freq ==0)? 1 : 0)];
		
		int numVis = 0;
		Visit visit = visits.getVisit(numVis);
		
		for(int i = startTime; i<= endTime; i++){
			while(visit.time == i){
				visitnums[visit.vertex]++;
				numVis++;
				if(numVis < visits.getNumVisits())
					visit = visits.getVisit(numVis);
				else
					break;
			}
			
			if(i % freq == 0){
				Double sum = 0.0;
				for(int visitnum : visitnums)
					sum += visitnum;
				values[i/freq] = sum/numNodes;
					
			}			
		}
		
		return values;
	}
	
	/**
	 * Standard Deviation of the Number of visits as a function of time, considering all nodes.
	 * @param freq 
	 * 			The stdDev of the number of visits so far is given every freq turn/seconds 
	 * 			(depending on type of simulation)
	 * 
	 * @author Cyril Poulet
	 */
	public Double[] getVisitStdDev_curb(int freq) {	
		int[] visitnums = new int[numNodes];
		Double[] values = new Double[(endTime - startTime)/freq + 
		                             (((endTime - startTime) % freq ==0)? 1 : 0)];
		
		int numVis = 0;
		Visit visit = visits.getVisit(numVis);
		
		for(int i = startTime; i<= endTime; i++){
			while(visit.time == i){
				visitnums[visit.vertex]++;
				numVis++;
				if(numVis < visits.getNumVisits())
					visit = visits.getVisit(numVis);
				else
					break;
			}
			
			if(i % freq == 0){
				double sum = 0.0;
				for(int visitnum : visitnums)
					sum += visitnum;
				
				double stddev = 0;
				double diff = 0;
				for(int visitnum : visitnums){
					diff = (visitnum - sum/numNodes);
					stddev += diff*diff;
				}
				
				
				values[i/freq] = Math.sqrt(stddev/numNodes);
					
			}			
		}
		
		return values;
	}
	
	/**
	 * Number of visits as a function of time, node by node
	 * @param freq 
	 * 			The numbers of visits per node are given every freq turn/seconds 
	 * 			(depending on type of simulation)
	 * 
	 * @return Double[][] 
	 * 			first dim : nodes
	 * 			second dim : visits by time on considered node
	 * 
	 * @author Cyril Poulet
	 */
	public Double[][] getVisitsNum_bynode_curb(int freq) {	
		int[] visitnums = new int[numNodes];
		Double[][] values = new Double[numNodes][(endTime - startTime)/freq + 
		                             (((endTime - startTime) % freq ==0)? 1 : 0)];
		
		int numVis = 0;
		Visit visit = visits.getVisit(numVis);
		
		for(int i = startTime; i<= endTime; i++){
			while(visit.time == i){
				visitnums[visit.vertex]++;
				numVis++;
				if(numVis < visits.getNumVisits())
					visit = visits.getVisit(numVis);
				else
					break;
			}
			
			if(i % freq == 0){
				for(int j = 0; j < numNodes; j++)
					values[j][i/freq] = visitnums[j]/1.0;
			}			
		}		
		return values;
	}
	
}
