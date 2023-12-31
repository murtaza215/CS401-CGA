import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import soot.MethodOrMethodContext;
import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.jimple.toolkits.callgraph.CHATransformer;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.util.dot.DotGraph;
import soot.util.queue.QueueReader;

public class Main {

    // Utility method to check if a class should be included in the call graph
    private static boolean shouldIncludeClass(String className) {
        return !className.startsWith("<java.") &&
               !className.startsWith("<sun.") &&
               !className.startsWith("<org.") &&
               !className.startsWith("<com.") &&
               !className.startsWith("<jdk.") &&
               !className.startsWith("<javax.");
    }

    // Utility method to filter edges that involve excluded classes
    private static List<Edge> filterEdges(CallGraph graph) {
        QueueReader<Edge> listener = graph.listener();
        List<Edge> filteredEdges = new ArrayList<>();
        
        while (listener.hasNext()) {
            Edge edge = listener.next();
            if (shouldIncludeClass(edge.getSrc().toString()) && shouldIncludeClass(edge.getTgt().toString())) {
                filteredEdges.add(edge);
            }
        }

        return filteredEdges;
    }

    // Optimized method to serialize the call graph and count method calls
    public static void serializeCallGraph(CallGraph graph, String filename) {
        DotGraph canvas = new DotGraph("call-graph");
        List<Edge> filteredEdges = filterEdges(graph);

        // Keep track of method calls count
        Map<String, Integer> methodCallCounts = new HashMap<>();

        for (Edge edge : filteredEdges) {
            MethodOrMethodContext src = edge.getSrc();
            MethodOrMethodContext tgt = edge.getTgt();
            String srcString = src.toString();
            String tgtString = tgt.toString();

            canvas.drawNode(srcString);
            canvas.drawNode(tgtString);
            canvas.drawEdge(srcString, tgtString);

            // Count the method calls
            String callInfo = srcString + " -> " + tgtString;
            Integer count = methodCallCounts.get(callInfo);
            if (count == null) {
                count = 0;
            }
            methodCallCounts.put(callInfo, count + 1);
        }

        if (filename == null) {
            filename = "output.dot";
        }

        canvas.plot(filename);
        System.out.println("Call graph written to " + filename);

        // Print method call counts
        System.out.println("Method Call Counts:");
        for (String callInfo : methodCallCounts.keySet()) {
            int count = methodCallCounts.get(callInfo);
            System.out.println(callInfo + ": " + count + " times");
        }
    }

 // ... (previous code)

    public static void main(String[] args) {
        List<String> argsList = new ArrayList<>(Arrays.asList(args));
        argsList.addAll(Arrays.asList(new String[]{"-w", "-main-class", "testers.CallGraphs", "testers.CallGraphs", "testers.A"}));
        String[] args2 = argsList.toArray(new String[0]);

        PackManager.v().getPack("wjtp").add(new Transform("wjtp.myTrans", new SceneTransformer() {
            @Override
            protected void internalTransform(String phaseName, Map options) {
                CHATransformer.v().transform();
                SootClass a = Scene.v().getSootClass("testers.A");
                SootMethod src = Scene.v().getMainClass().getMethodByName("doStuff");
                CallGraph cg = Scene.v().getCallGraph();

                serializeCallGraph(cg, "output.dot");
                System.out.println("serializeCallGraph completed.");

                List<Edge> targets = new ArrayList<>();
                Iterator<Edge> edges = cg.edgesOutOf(src);
                while (edges.hasNext()) {
                    targets.add(edges.next());
                }

                int totalMethodCalls = 0;
                for (Edge edge : targets) {
                    SootMethod tgt = (SootMethod) edge.getTgt();
                    System.out.println(src + " may call " + tgt);
                    totalMethodCalls++;
                }

                // Print the total number of method calls
                System.out.println("---------------------------------------");
                System.out.println("Total Method Calls: " + totalMethodCalls);
                System.out.println("---------------------------------------");
            }
        }));

        args = argsList.toArray(new String[0]);
        soot.Main.main(args2);
    }

}