/* 
 * The MIT License (MIT)
 * 
 * Copyright © 2016 Domagoj Latečki
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package hr.dlatecki.clb.solver;

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
