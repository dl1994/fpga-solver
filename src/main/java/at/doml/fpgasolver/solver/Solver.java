package at.doml.fpgasolver.solver;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import at.doml.fpgasolver.args.ArgumentParser;
import at.doml.fpgasolver.args.FieldKey;
import at.doml.fpgasolver.args.LongArgument;
import at.doml.fpgasolver.args.ShortArgument;
import at.doml.fpgasolver.args.abstracts.AbstractArgument;
import at.doml.fpgasolver.args.exceptions.MissingPropertyException;
import at.doml.fpgasolver.args.exceptions.ParameterException;
import at.doml.fpgasolver.args.exceptions.UnknownArgumentException;
import at.doml.fpgasolver.expressions.FunctionExpression;
import at.doml.fpgasolver.parsing.exceptions.LexicalException;
import at.doml.fpgasolver.parsing.exceptions.SyntaxException;
import at.doml.fpgasolver.parsing.lexical.LexicalAnalyser;
import at.doml.fpgasolver.parsing.lexical.LexicalToken;
import at.doml.fpgasolver.parsing.lexical.LexicalUnit;
import at.doml.fpgasolver.parsing.syntax.SyntaxAnalyser;

public class Solver {
    
    private static final FieldKey<Double> MUTATION_CHANCE = new FieldKey<>("mutationChance", 0.025);
    private static final FieldKey<Integer> CLBS = new FieldKey<>("clbs", 0);
    private static final FieldKey<Integer> INPUTS = new FieldKey<>("inputs", 0);
    private static final FieldKey<Integer> POP_SIZE = new FieldKey<>("popSize", 50);
    private static final FieldKey<Integer> GENERATIONS = new FieldKey<>("generations", 500);
    private static final FieldKey<Integer> MUTATIONS_PER_PARENT = new FieldKey<>("mutationsPerParent", 4);
    private static final FieldKey<Boolean> PRINT_TIME = new FieldKey<>("printTime", Boolean.FALSE);
    private static final FieldKey<Boolean> PRINT_MESSAGES = new FieldKey<>("printMessages", Boolean.FALSE);
    private static final FieldKey<Boolean> ITERATIVE_MODE = new FieldKey<>("iterativeMode", Boolean.FALSE);
    private static final FieldKey<Boolean> FIRST_ACCEPTABLE = new FieldKey<>("firstAcceptable", Boolean.FALSE);
    
    public static void main(String[] args) {
        ArgumentParser argumentParser = null;
        
        try {
            argumentParser = parseArguments(args);
        } catch (MissingPropertyException e) {
            String propertyName = e.getProperty();
            
            if (propertyName.equals(CLBS.getName())) {
                System.err.println("Number of CLBs wasn't specified.");
            }
            
            if (propertyName.equals(INPUTS.getName())) {
                System.err.println("Number of inputs per CLB wasn't specified.");
            }
            
            System.err.println("Exiting...");
            System.exit(-1);
        } catch (ParameterException e) {
            printErrorAndExit(e.getMessage(), -1);
        } catch (UnknownArgumentException e) {
            printErrorAndExit(e.getMessage(), -1);
        }
        
        String[] remainingArgs = argumentParser.getRemainingArgs();
        
        List<Path> files = new ArrayList<>();
        
        for (String arg : remainingArgs) {
            Path path = Paths.get(arg);
            
            File file = path.toFile();
            
            if (!file.exists()) {
                printErrorAndExit("Specified file doesn't exist: " + path.toString(), -2);
            }
            
            if (file.isDirectory()) {
                printErrorAndExit("Specified file is a directory: " + path.toString(), -2);
            }
            
            if (!file.canRead()) {
                printErrorAndExit("Unable to read specified file: " + path.toString(), -2);
            }
            
            files.add(path);
        }
        
        Set<String> functionNames = new HashSet<>();
        List<FunctionExpression> functions = new ArrayList<>();
        
        if (files.size() == 0) {
            List<String> lines = new ArrayList<>();
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
                String line;
                
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
            } catch (IOException e) {
                printErrorAndExit("Error reading from standard input.", -2);
            }
            lexicalAndSyntaxAnalysis(functionNames, functions, lines, "standard input");
        } else {
            files.forEach(file -> {
                String fileName = file.toString();
                
                try {
                    lexicalAndSyntaxAnalysis(functionNames, functions, Files.readAllLines(file), "file " + fileName);
                } catch (IOException e) {
                    printErrorAndExit("Error reading from file: " + file.toString(), -2);
                }
            });
        }
        
        if (functions.isEmpty()) {
            printErrorAndExit("No functions were specified.", -5);
        }
        
        System.out.println("Input functions:");
        
        functions.forEach(System.out::println);
        
        System.out.println();
        
        checkForCycles(functions);
        
        Evaluator evaluator = new Evaluator(functions.toArray(new FunctionExpression[functions.size()]));
        FPGAGeneticAlgorithm algorithm = FPGAGeneticAlgorithm.builder()
                .popSize(argumentParser.getProperty(POP_SIZE))
                .numOfClbs(argumentParser.getProperty(CLBS))
                .clbInputs(argumentParser.getProperty(INPUTS))
                .mutationsPerParent(argumentParser.getProperty(MUTATIONS_PER_PARENT))
                .maxGenerations(argumentParser.getProperty(GENERATIONS))
                .mutationChance(argumentParser.getProperty(MUTATION_CHANCE))
                .printMessages(argumentParser.getProperty(PRINT_MESSAGES))
                .printTime(argumentParser.getProperty(PRINT_TIME))
                .firstAcceptable(argumentParser.getProperty(FIRST_ACCEPTABLE))
                .iterativeMode(argumentParser.getProperty(ITERATIVE_MODE))
                .random(new Random())
                .evaluator(evaluator)
                .inputStream(System.in)
                .outputStream(System.out)
                .build();
        
        CLBChromosome solution = algorithm.findSolution();
        
        if (argumentParser.getProperty(PRINT_MESSAGES)) {
            System.out.println();
        }
        
        System.out.println("Found solution in " + algorithm.getTotalTime() + "ms.");
        System.out.println("Solution - errors: " + solution.getError() + ", number of CLBs: " + solution.getClbs());
        System.out.println();
        System.out.println(solution);
    }
    
    private static void checkForCycles(List<FunctionExpression> functions) {
        Map<String, FunctionExpression> functionsMap = functions.stream()
                .collect(Collectors.toMap(FunctionExpression::getName, f -> f));
        Set<String> functionNames = functionsMap.keySet();
        
        for (FunctionExpression function : functions) {
            String name = function.getName();
            
            Set<String> activeFunctions = new HashSet<>();
            Set<String> iteratedFunctions = new HashSet<>();
            
            activeFunctions.add(name);
            
            while (activeFunctions.size() != 0) {
                Set<String> newActiveFunctions = new HashSet<>();
                
                for (String activeFunction : activeFunctions) {
                    newActiveFunctions.addAll(functionsMap.get(activeFunction).getExpressionVariables().stream()
                            .filter(variable -> functionNames.contains(variable))
                            .collect(Collectors.toSet()));
                    if (newActiveFunctions.contains(name)) {
                        printErrorAndExit("Definition for function " + name + " contains a cycle.", -5);
                    }
                }
                
                iteratedFunctions.addAll(activeFunctions);
                activeFunctions = newActiveFunctions;
                activeFunctions.removeAll(iteratedFunctions);
            }
            
            function.getVariables().stream()
                    .filter(variable -> functionNames.contains(variable));
            
        }
    }
    
    private static void lexicalAndSyntaxAnalysis(Set<String> functionNames, List<FunctionExpression> functions,
            List<String> lines, String streamName) {
        List<LexicalUnit> lexicalUnits = lexicalAnalysis(lines, streamName);
        
        for (String functionName : syntaxAnalysis(lexicalUnits, functions, streamName)) {
            if (functionNames.contains(functionName)) {
                printErrorAndExit(
                        "Found duplicate function \"" + functionName + "\" in " + streamName + "."
                                + (streamName.startsWith("file") ? " Check other files for a function with same name."
                                        : ""),
                        -4);
            } else {
                functionNames.add(functionName);
            }
        }
    }
    
    private static void printErrorAndExit(String errorMessage, int statusCode) {
        System.err.println(errorMessage);
        System.err.println("Exiting...");
        System.exit(statusCode);
    }
    
    private static List<LexicalUnit> lexicalAnalysis(List<String> lines, String streamName) {
        LexicalAnalyser analyser = new LexicalAnalyser(lines);
        
        try {
            return analyser.getLexicalUnits();
        } catch (LexicalException e) {
            List<LexicalUnit> errorUnits = e.getLexicalUnits();
            
            System.err.println("In " + streamName + ":");
            
            errorUnits.forEach(errorUnit -> {
                System.err.println("Error at line: " + errorUnit.getLineNumber());
                System.err.println(errorUnit.highlighted());
            });
            
            System.err.println("Exiting...");
            System.exit(-3);
            
            return null;
        }
    }
    
    private static List<String> syntaxAnalysis(List<LexicalUnit> lexicalUnits, List<FunctionExpression> functions,
            String streamName) {
        SyntaxAnalyser analyser = new SyntaxAnalyser(lexicalUnits);
        
        try {
            List<FunctionExpression> functionExpressions = analyser.parseTokens();
            
            functions.addAll(functionExpressions);
            
            return functionExpressions.stream()
                    .map(FunctionExpression::getName)
                    .collect(Collectors.toList());
        } catch (SyntaxException e) {
            LexicalUnit actualUnit = e.getActualUnit();
            LexicalToken[] expectedTypes = e.getExpectedTypes();
            
            System.err.println("In " + streamName + ":");
            
            if (actualUnit == null || expectedTypes == null) {
                printErrorAndExit(e.getMessage(), -4);
            } else {
                printErrorAndExit("Expected any of these lexical tokens: "
                        + Arrays.toString(expectedTypes) + ", but was: " + actualUnit.getType() + ". At line "
                        + actualUnit.getLineNumber() + ":\n" + actualUnit.highlighted(), -4);
            }
            
            return null;
        }
    }
    
    public static ArgumentParser parseArguments(String... args) {
        AbstractArgument generations = new ShortArgument('g', 1,
                createNumberAction("-g", "greater than or equal to 1", GENERATIONS, Integer::parseInt,
                        value -> value < 1));
        AbstractArgument mutationsPerParent = new ShortArgument('M', 1,
                createNumberAction("-M", "greater than or equal to 1", MUTATIONS_PER_PARENT, Integer::parseInt,
                        value -> value < 1));
        AbstractArgument popSize = new ShortArgument('s', 1,
                createNumberAction("-s", "greater than or equal to 1", POP_SIZE, Integer::parseInt,
                        value -> value < 1));
        AbstractArgument mutationChance = new ShortArgument('m', 1,
                createNumberAction("-m", "greater than or equal to 0 and less than or equal to 1",
                        MUTATION_CHANCE, Double::parseDouble, value -> value < 0.0 || value > 1.0));
        AbstractArgument printMessages = new ShortArgument('p', 0, (params, propertySetter) -> {
            activateBooleanProperty(PRINT_MESSAGES, propertySetter);
        });
        AbstractArgument printTime = new ShortArgument('P', 0, (params, propertySetter) -> {
            activateBooleanProperty(PRINT_TIME, propertySetter);
            activateBooleanProperty(PRINT_MESSAGES, propertySetter);
        });
        AbstractArgument iterativeMode = new ShortArgument('i', 0, (params, propertySetter) -> {
            activateBooleanProperty(ITERATIVE_MODE, propertySetter);
        });
        AbstractArgument firstAcceptable = new ShortArgument('f', 0, (params, propertySetter) -> {
            activateBooleanProperty(FIRST_ACCEPTABLE, propertySetter);
        });
        AbstractArgument fpga = new LongArgument("fpga", 2, (params, propertySetter) -> {
            try {
                Integer clbs = Integer.parseInt(params[0]);
                Integer inputs = Integer.parseInt(params[1]);
                
                if (clbs < 1 || inputs < 1) {
                    throw new ParameterException(
                            "Number of clbs and number of inputs must be a number greater than or equal to 1.");
                }
                
                propertySetter.setProperty(CLBS, clbs);
                propertySetter.setProperty(INPUTS, inputs);
                
            } catch (NumberFormatException e) {
                throw new ParameterException("Integer value was expected for number of clbs and inputs.");
            }
        });
        
        Set<FieldKey<?>> defaultProperties = new HashSet<>();
        
        defaultProperties.add(POP_SIZE);
        defaultProperties.add(PRINT_TIME);
        defaultProperties.add(GENERATIONS);
        defaultProperties.add(ITERATIVE_MODE);
        defaultProperties.add(PRINT_MESSAGES);
        defaultProperties.add(MUTATION_CHANCE);
        defaultProperties.add(FIRST_ACCEPTABLE);
        defaultProperties.add(MUTATIONS_PER_PARENT);
        
        ArgumentParser argumentParser = new ArgumentParser(defaultProperties, generations, popSize, mutationChance,
                printMessages, printTime, iterativeMode, firstAcceptable, fpga, mutationsPerParent);
        
        argumentParser.parse(args);
        argumentParser.getProperty(CLBS);
        argumentParser.getProperty(INPUTS);
        
        return argumentParser;
    }
    
    private static <T extends Number> BiConsumer<String[], ArgumentParser.PropertySetter> createNumberAction(
            String argName, String valueBoundMessage, FieldKey<T> property,
            Function<String, T> stringParser, Function<T, Boolean> boundChecker) {
        return (params, propertySetter) -> {
            String className = property.getDefaultValue().getClass().getSimpleName();
            
            try {
                T value = stringParser.apply(params[0]);
                
                if (boundChecker.apply(value)) {
                    throw new ParameterException("Value " + valueBoundMessage + " was expected for parameter of "
                            + argName + " argument.");
                }
                
                propertySetter.setProperty(property, value);
            } catch (NumberFormatException e) {
                throw new ParameterException(className + " value was expected as a paremeter of "
                        + argName + " argument.");
            }
        };
    }
    
    private static void activateBooleanProperty(FieldKey<Boolean> field, ArgumentParser.PropertySetter propertySetter) {
        propertySetter.setProperty(field, Boolean.TRUE);
    }
}
