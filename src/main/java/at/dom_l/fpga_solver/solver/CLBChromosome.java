package at.dom_l.fpga_solver.solver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import at.dom_l.fpga_solver.logic.ConfigurableLogicBlock;
import at.dom_l.fpga_solver.expressions.interfaces.IBooleanExpression;

public class CLBChromosome implements Comparable<CLBChromosome> {
    
    private int error;
    private int clbs;
    private final int inputsPerBlock;
    private final int tableSize;
    private final Random random;
    private final DirectedGraph connections;
    private final ConfigurableLogicBlock[] logicBlocks;
    private final Set<Integer> taggedClbs;
    private final List<IBooleanExpression> variables;
    private final Map<Integer, CLBInputs> blockInputs;
    private final Map<String, Integer> functionOutputs;
    
    public CLBChromosome(Random random, int numOfLogicBlocks, int inputsPerBlock, List<IBooleanExpression> variables,
            Set<String> functionNames) {
        this.random = random;
        this.inputsPerBlock = inputsPerBlock;
        this.variables = variables;
        
        taggedClbs = new HashSet<>();
        tableSize = (int) Math.pow(2.0, inputsPerBlock);
        functionOutputs = new HashMap<>();
        blockInputs = new HashMap<>();
        connections = new DirectedGraph(numOfLogicBlocks);
        logicBlocks = new ConfigurableLogicBlock[numOfLogicBlocks];
        
        for (int i = 0; i < numOfLogicBlocks; i++) {
            logicBlocks[i] = new ConfigurableLogicBlock(inputsPerBlock);
            logicBlocks[i].setName(Integer.toString(i + 1));
            blockInputs.put(i, new CLBInputs(inputsPerBlock));
            
            for (int j = 0; j < inputsPerBlock; j++) {
                logicBlocks[i].setInput(j, randomVariable());
            }
            
            for (int j = 0; j < tableSize; j++) {
                logicBlocks[i].setOutput(j, random.nextBoolean());
            }
        }
        
        functionNames.forEach(functionName -> {
            functionOutputs.put(functionName, randomBlockIndex());
        });
    }
    
    public CLBChromosome(CLBChromosome original) {
        this.error = original.error;
        this.clbs = original.clbs;
        this.inputsPerBlock = original.inputsPerBlock;
        this.tableSize = original.tableSize;
        this.random = original.random;
        this.variables = original.variables;
        this.taggedClbs = new HashSet<>();
        this.connections = new DirectedGraph(original.logicBlocks.length);
        this.functionOutputs = new HashMap<>(original.functionOutputs);
        
        this.logicBlocks = new ConfigurableLogicBlock[original.logicBlocks.length];
        
        for (int i = 0; i < this.logicBlocks.length; i++) {
            this.logicBlocks[i] = new ConfigurableLogicBlock(original.logicBlocks[i]);
            this.logicBlocks[i].setName(Integer.toString(i + 1));
        }
        
        this.blockInputs = new HashMap<>();
        
        original.blockInputs.forEach((key, value) -> {
            this.blockInputs.put(key, new CLBInputs(value));
            
            for (int input : value.getInputs()) {
                if (input != -1) {
                    this.connections.connect(input, key);
                }
            }
        });
        
        this.blockInputs.forEach((block, inputs) -> {
            int[] blockInputs = inputs.getInputs();
            for (int i = 0; i < blockInputs.length; i++) {
                if (blockInputs[i] != -1) {
                    this.logicBlocks[block].setInput(i, logicBlocks[blockInputs[i]]);
                }
            }
        });
    }
    
    public IBooleanExpression getFunctionOutput(String functionName) {
        return logicBlocks[functionOutputs.get(functionName)];
    }
    
    public boolean mutate(double mutationChance) {
        Set<Integer> mutatedBlocks = new HashSet<>();
        
        for (int i = 0; i < logicBlocks.length; i++) {
            boolean blockMutated = false;
            
            blockMutated |= mutateTable(logicBlocks[i], mutationChance);
            blockMutated |= mutateInputs(logicBlocks[i], i, mutationChance);
            
            if (blockMutated) {
                mutatedBlocks.add(i);
            }
        }
        
        return mutateFunctionOutputs(mutationChance, mutatedBlocks);
    }
    
    private boolean mutateTable(ConfigurableLogicBlock logicBlock, double mutationChance) {
        boolean mutated = false;
        
        for (int i = 0; i < tableSize; i++) {
            if (mutates(mutationChance)) {
                logicBlock.flipOutput(i);
                mutated = true;
            }
        }
        
        return mutated;
    }
    
    private boolean mutateInputs(ConfigurableLogicBlock logicBlock, int blockIndex, double mutationChance) {
        int numVars = variables.size();
        boolean mutated = false;
        
        for (int i = 0; i < inputsPerBlock; i++) {
            if (mutates(mutationChance)) {
                List<Integer> validBlocks = findValidBlocks(blockIndex);
                
                int numBlocks = validBlocks.size();
                int total = numVars + numBlocks;
                int value = random.nextInt(total);
                
                if (value < numVars) {
                    logicBlock.setInput(i, randomVariable());
                    
                    setInputAndUpdateGraph(blockIndex, i, -1);
                } else {
                    int block = validBlocks.get(random.nextInt(validBlocks.size()));
                    
                    logicBlock.setInput(i, logicBlocks[block]);
                    
                    setInputAndUpdateGraph(blockIndex, i, block);
                }
                
                mutated = true;
            }
        }
        
        return mutated;
    }
    
    public void setInputAndUpdateGraph(int blockIndex, int inputIndex, int newInput) {
        CLBInputs inputs = blockInputs.get(blockIndex);
        
        int oldInput = inputs.getInput(inputIndex);
        
        if (oldInput != -1) {
            connections.disconnect(oldInput, blockIndex);
        }
        
        if (newInput != -1) {
            connections.connect(newInput, blockIndex);
        }
        
        inputs.setInput(inputIndex, newInput);
    }
    
    private List<Integer> findValidBlocks(int blockIndex) {
        Set<Integer> validBlocks = new HashSet<>();
        
        for (int i = 0; i < logicBlocks.length; i++) {
            validBlocks.add(i);
        }
        
        validBlocks.remove(blockIndex);
        validBlocks.removeAll(connections.getForwardConnections(blockIndex));
        
        return new ArrayList<>(validBlocks);
    }
    
    private boolean mutateFunctionOutputs(double mutationChance, Set<Integer> mutatedBlocks) {
        boolean mutated = false;
        
        for (Map.Entry<String, Integer> functionOutput : functionOutputs.entrySet()) {
            if (mutates(mutationChance)) {
                functionOutput.setValue(randomBlockIndex());
                mutated = true;
            }
            
            tagClbs(functionOutput.getKey());
        }
        
        if (!mutated) {
            for (Integer tag : taggedClbs) {
                if (mutatedBlocks.contains(tag)) {
                    taggedClbs.clear();
                    
                    return true;
                }
            }
        }
        
        return mutated;
    }
    
    private boolean mutates(double mutationChance) {
        return mutationChance >= random.nextDouble();
    }
    
    private IBooleanExpression randomVariable() {
        return variables.get(random.nextInt(variables.size()));
    }
    
    private int randomBlockIndex() {
        return random.nextInt(logicBlocks.length);
    }
    
    @Override
    public int compareTo(CLBChromosome o) {
        if (error > o.error) {
            return 1;
        } else if (error < o.error) {
            return -1;
        } else if (clbs > o.clbs) {
            return 1;
        } else {
            return -1;
        }
    }
    
    public String getFitnessString() {
        return "(" + error + ", " + clbs + ")";
    }
    
    public void setError(int error) {
        this.error = error;
    }
    
    public int getError() {
        return error;
    }
    
    public void setClbs(int clbs) {
        this.clbs = clbs;
    }
    
    public int getClbs() {
        return clbs;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        
        for (int i = 0; i < logicBlocks.length; i++) {
            builder.append("Block ").append(i + 1).append(":\n");
            builder.append("-------------------\n");
            builder.append(logicBlocks[i]);
            builder.append("-------------------\n");
        }
        
        functionOutputs.forEach((function, output) -> {
            builder.append(function).append(" => ").append(output + 1).append("\n");
        });
        
        return builder.toString();
    }
    
    public void tagClbs(String functionName) {
        int clbIndex = functionOutputs.get(functionName);
        
        Set<Integer> clbsToIterate = new HashSet<>();
        Set<Integer> newClbsToIterate = new HashSet<>();
        
        taggedClbs.add(clbIndex);
        newClbsToIterate.add(clbIndex);
        
        while (!newClbsToIterate.isEmpty()) {
            clbsToIterate.addAll(newClbsToIterate);
            newClbsToIterate.clear();
            
            for (int clb : clbsToIterate) {
                for (int input : blockInputs.get(clb).getInputs()) {
                    if (input != -1) {
                        newClbsToIterate.add(input);
                        taggedClbs.add(input);
                    }
                }
            }
            
            newClbsToIterate.removeAll(clbsToIterate);
            clbsToIterate.clear();
        }
    }
    
    public void setNumOfTaggedClbs() {
        clbs = taggedClbs.size();
        taggedClbs.clear();
    }
}
