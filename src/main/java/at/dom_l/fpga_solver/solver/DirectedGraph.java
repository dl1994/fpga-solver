package at.dom_l.fpga_solver.solver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DirectedGraph {
    
    private final List<Set<Integer>> directConnections;
    private final List<Set<Integer>> forwardConnections;
    private final List<Set<Integer>> backwardConnections;
    
    public DirectedGraph(int numOfNodes) {
        directConnections = new ArrayList<>(numOfNodes);
        forwardConnections = new ArrayList<>(numOfNodes);
        backwardConnections = new ArrayList<>(numOfNodes);
        
        for (int i = 0; i < numOfNodes; i++) {
            directConnections.add(new HashSet<>());
            forwardConnections.add(new HashSet<>());
            backwardConnections.add(new HashSet<>());
        }
    }
    
    public void connect(int from, int to) {
        directConnections.get(from).add(to);
        forwardConnections.get(from).add(to);
        forwardConnections.get(from).addAll(forwardConnections.get(to));
        backwardConnections.get(to).add(from);
        backwardConnections.get(to).addAll(backwardConnections.get(from));
        
        Set<Integer> forward = forwardConnections.get(from);
        
        backwardConnections.get(from).forEach(connection -> {
            forwardConnections.get(connection).addAll(forward);
        });
        
        Set<Integer> backward = backwardConnections.get(to);
        
        forwardConnections.get(to).forEach(connection -> {
            backwardConnections.get(connection).addAll(backward);
        });
    }
    
    public Set<Integer> getForwardConnections(int index) {
        return forwardConnections.get(index);
    }
    
    public void disconnect(int from, int to) {
        directConnections.get(from).remove(to);
    }
}
