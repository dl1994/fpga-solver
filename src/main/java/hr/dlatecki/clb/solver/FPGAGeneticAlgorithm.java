package hr.dlatecki.clb.solver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;
import hr.dlatecki.clb.expressions.VariableExpression;
import hr.dlatecki.clb.expressions.interfaces.IBooleanExpression;

public class FPGAGeneticAlgorithm {
    
    private final int popSize;
    private final int numOfClbs;
    private final int clbInputs;
    private final int mutationsPerParent;
    private final int maxGenerations;
    private final double mutationChance;
    private final boolean printMessages;
    private final boolean printTime;
    private final boolean iterativeMode;
    private final boolean firstAcceptable;
    private final Random random;
    private final Evaluator evaluator;
    private final BufferedReader reader;
    private final BufferedWriter writer;
    private static final int THREADS_PER_PROCESSOR = 8;
    
    private FPGAGeneticAlgorithm(Builder builder) {
        this.popSize = checkAndSet("population size", builder.popSize, FPGAGeneticAlgorithm::isPositive);
        this.numOfClbs = checkAndSet("number of CLBs", builder.numOfClbs, FPGAGeneticAlgorithm::isPositive);
        this.clbInputs = checkAndSet("number of CLB inputs", builder.clbInputs, FPGAGeneticAlgorithm::isPositive);
        this.mutationsPerParent = checkAndSet("number of mutations per parent",
                builder.mutationsPerParent, FPGAGeneticAlgorithm::isPositive);
        this.maxGenerations = checkAndSet("number of generations",
                builder.maxGenerations, FPGAGeneticAlgorithm::isPositive);
        this.mutationChance = checkAndSet("mutation chance", builder.mutationChance,
                chance -> chance >= 0.0 && chance <= 1.0);
        this.printMessages = builder.printMessages;
        this.printTime = builder.printTime;
        this.iterativeMode = builder.iterativeMode;
        this.firstAcceptable = builder.firstAcceptable;
        this.random = checkIfNull("Random object", builder.random);
        this.evaluator = checkIfNull("Evaluator object", builder.evaluator);
        
        reader = new BufferedReader(new InputStreamReader(checkIfNull("InputStream object", builder.inputStream)));
        writer = new BufferedWriter(new OutputStreamWriter(checkIfNull("OutputStream object", builder.outputStream)));
    }
    
    private int generation;
    private long time;
    private long totalTime;
    private CLBChromosome best;
    private List<Future<?>> futures;
    private ExecutorService executor;
    private SortedSet<CLBChromosome> population;
    
    private static <T> T checkAndSet(String argName, T value, Function<T, Boolean> validator) {
        if (validator.apply(value)) {
            return value;
        } else {
            throw new IllegalArgumentException("Invalid value was provided for " + argName + ". Value: " + value + ".");
        }
    }
    
    private static <T> T checkIfNull(String argName, T value) {
        try {
            return checkAndSet(argName, value, Objects::nonNull);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(argName + " cannot be null!");
        }
    }
    
    private static boolean isPositive(int value) {
        return value > 0;
    }
    
    public CLBChromosome findSolution() {
        totalTime = 0L;
        
        long tTime = System.currentTimeMillis();
        
        generation = 0;
        executor = Executors.newFixedThreadPool(THREADS_PER_PROCESSOR * Runtime.getRuntime().availableProcessors());
        population = generatePopulation();
        futures = new ArrayList<>(popSize);
        time = System.currentTimeMillis();
        
        population.forEach(pop -> {
            futures.add(executor.submit(() -> {
                evaluator.evaluate(pop);
            }));
        });
        
        time = System.currentTimeMillis() - time;
        
        printMessage(time, generation, population, population.first());
        
        synchronize(futures);
        best = population.first();
        totalTime += System.currentTimeMillis() - tTime;
        generation++;
        
        if (firstAcceptable && best.getError() == 0) {
            executor.shutdown();
            
            return best;
        }
        
        if (iterativeMode) {
            boolean continueIterating = true;
            
            while (continueIterating) {
                tTime = System.currentTimeMillis();
                
                iteration();
                
                totalTime += System.currentTimeMillis() - tTime;
                
                writeLine("Continue iterating? (Y/n): ");
                
                String line = readLine().trim().toUpperCase();
                
                if (!line.equals("Y")) {
                    continueIterating = false;
                }
            }
        } else {
            tTime = System.currentTimeMillis();
            
            iteration();
            
            totalTime += System.currentTimeMillis() - tTime;
        }
        
        executor.shutdown();
        
        return best;
    }
    
    private void iteration() {
        int iteration = 0;
        
        while (iteration <= maxGenerations) {
            time = System.currentTimeMillis();
            
            SortedSet<CLBChromosome> newPopulation = Collections.synchronizedSortedSet(new TreeSet<>());
            
            futures.clear();
            
            population.forEach(pop -> {
                futures.add(executor.submit(() -> {
                    SortedSet<CLBChromosome> children = new TreeSet<>();
                    
                    for (int i = 0; i < mutationsPerParent; i++) {
                        CLBChromosome child = new CLBChromosome(pop);
                        
                        if (child.mutate(mutationChance)) {
                            evaluator.evaluate(child);
                        } else {
                            child.setError(pop.getError());
                            child.setClbs(pop.getClbs());
                        }
                        
                        children.add(child);
                    }
                    
                    CLBChromosome bestChild = children.first();
                    
                    newPopulation.add(getBetter(bestChild, pop));
                }));
            });
            
            synchronize(futures);
            best = getBetter(newPopulation.first(), best);
            population = newPopulation;
            time = System.currentTimeMillis() - time;
            
            printMessage(time, generation, population, best);
            
            iteration++;
            generation++;
            
            if (firstAcceptable && best.getError() == 0) {
                return;
            }
        }
    }
    
    public long getTotalTime() {
        return totalTime;
    }
    
    private void printMessage(long time, int generation, SortedSet<CLBChromosome> population, CLBChromosome best) {
        String prefix = "";
        
        if (printTime) {
            prefix = "[" + time + " ms] ";
        }
        
        if (printMessages) {
            int errorSum = 0;
            int clbsSum = 0;
            
            for (CLBChromosome pop : population) {
                errorSum += pop.getError();
                clbsSum += pop.getClbs();
            }
            
            writeLine(prefix + "Generation: " + generation + ", current best: " + best.getFitnessString()
                    + ", generation best: " + population.first().getFitnessString() + ", average: ("
                    + (double) errorSum / popSize + ", " + (double) clbsSum / popSize + ").");
        }
    }
    
    private void writeLine(String line) {
        try {
            writer.write(line);
            writer.newLine();
            writer.flush();
        } catch (IOException ignorable) {}
    }
    
    private String readLine() {
        try {
            String line = reader.readLine();
            
            if (line == null) {
                return "";
            } else {
                return line;
            }
        } catch (IOException ignorable) {
            return "";
        }
    }
    
    private static void synchronize(List<Future<?>> futures) {
        futures.forEach(future -> {
            try {
                future.get();
            } catch (Exception ignorable) {}
        });
    }
    
    private CLBChromosome getBetter(CLBChromosome first, CLBChromosome second) {
        int comparisonValue = first.compareTo(second);
        
        if (comparisonValue >= 1) {
            return second;
        } else if (comparisonValue <= -1) {
            return first;
        } else {
            return random.nextBoolean() ? first : second;
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    private SortedSet<CLBChromosome> generatePopulation() {
        SortedSet<CLBChromosome> population = new TreeSet<>();
        List<IBooleanExpression> variables = evaluator.getVariables().stream()
                .map(variableName -> new VariableExpression(variableName))
                .collect(Collectors.toList());
        
        for (int i = 0; i < popSize; i++) {
            population.add(new CLBChromosome(random, numOfClbs, clbInputs, variables, evaluator.getFunctionNames()));
        }
        
        return population;
    }
    
    public static class Builder {
        
        private int popSize;
        private int numOfClbs;
        private int clbInputs;
        private int mutationsPerParent;
        private int maxGenerations;
        private double mutationChance;
        private boolean printMessages;
        private boolean printTime;
        private boolean iterativeMode;
        private boolean firstAcceptable;
        private Random random;
        private Evaluator evaluator;
        private InputStream inputStream;
        private OutputStream outputStream;
        
        private Builder() {
            popSize = 100;
            mutationsPerParent = 4;
            maxGenerations = 100;
            mutationChance = 0.05;
            random = new Random();
            inputStream = System.in;
            outputStream = System.out;
        }
        
        public Builder popSize(int popSize) {
            this.popSize = popSize;
            return this;
        }
        
        public Builder numOfClbs(int numOfClbs) {
            this.numOfClbs = numOfClbs;
            return this;
        }
        
        public Builder clbInputs(int clbInputs) {
            this.clbInputs = clbInputs;
            return this;
        }
        
        public Builder mutationsPerParent(int mutationsPerParent) {
            this.mutationsPerParent = mutationsPerParent;
            return this;
        }
        
        public Builder maxGenerations(int maxGenerations) {
            this.maxGenerations = maxGenerations;
            return this;
        }
        
        public Builder mutationChance(double mutationChance) {
            this.mutationChance = mutationChance;
            return this;
        }
        
        public Builder printMessages(boolean printMessages) {
            this.printMessages = printMessages;
            return this;
        }
        
        public Builder printTime(boolean printTime) {
            this.printTime = printTime;
            return this;
        }
        
        public Builder iterativeMode(boolean iterativeMode) {
            this.iterativeMode = iterativeMode;
            return this;
        }
        
        public Builder firstAcceptable(boolean firstAcceptable) {
            this.firstAcceptable = firstAcceptable;
            return this;
        }
        
        public Builder random(Random random) {
            this.random = random;
            return this;
        }
        
        public Builder evaluator(Evaluator evaluator) {
            this.evaluator = evaluator;
            return this;
        }
        
        public Builder inputStream(InputStream inputStream) {
            this.inputStream = inputStream;
            return this;
        }
        
        public Builder outputStream(OutputStream outputStream) {
            this.outputStream = outputStream;
            return this;
        }
        
        public FPGAGeneticAlgorithm build() {
            return new FPGAGeneticAlgorithm(this);
        }
    }
}
