package open.CooperativeAuctions.SumEdges;

import java.util.LinkedList;

import open.CooperativeAuctions.PathLength.Minimax_Agent;

import util.graph.Node;

public class Minimax2_Agent extends Minimax_Agent {

	
	public Minimax2_Agent(String id, int number_of_agents,
			LinkedList<String> nodes, double idleness_rate_for_path, double max_dist) {
		super(id, number_of_agents, nodes, idleness_rate_for_path, max_dist);
	}
	
	public Minimax2_Agent(String id, double entering_time,
			double quitting_time, int number_of_agents,
			LinkedList<String> nodes, double idleness_rate_for_path,
			double max_dist, String society_id) {
		super(id, entering_time, quitting_time, number_of_agents, nodes,
				idleness_rate_for_path, max_dist, society_id);
	}
	
	
	@Override
	protected double CalculateUtility(LinkedList<String> nodes) {
		double sum = 0;
		for(int i = 0; i < nodes.size(); i++){
			Node n1 = this.graph.getNode(nodes.get(i));
			for(int j = i+1; j < nodes.size(); j++){
				Node n2 = this.graph.getNode(nodes.get(j));
				double current_length = this.graph.getDistance(n1, n2);
				if(current_length != Double.MAX_VALUE)
					sum += current_length;
			}
		}
		
		return sum;
	}

}
