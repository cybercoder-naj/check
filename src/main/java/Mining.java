import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

class State {
    // resource is the sum of resources from parent and current node.
    private final int resource;

    // the point on the map.
    int x, y;

    public State(int resource, int x, int y) {
        this.resource = resource;
        this.x = x;
        this.y = y;
    }

    public int getResource() {
        return resource;
    }
}

enum Action {
    DOWN_LEFT, DOWN_RIGHT
}

class Result {
    // The point on the map.
    int x, y;

    // The action taken to get to that point on the map.
    private final Action action;

    public Result(int x, int y, Action action) {
        this.x = x;
        this.y = y;
        this.action = action;
    }

    public Action getAction() {
        return action;
    }

    // Defined for visual output.
    @Override
    public boolean equals(Object other) {
        if (other instanceof Result) {
            Result o = (Result) other;
            return this.x == o.x && this.y == o.y;
        }
        return false;
    }
}

class Node implements Comparable<Node> {
    private final State state;
    private final Node parent;
    private final Action action;

    public Node(State state, Node parent, Action action) {
        this.state = state;
        this.parent = parent;
        this.action = action;
    }

    public State getState() {
        return state;
    }

    public Node getParent() {
        return parent;
    }

    public Action getAction() {
        return action;
    }

    // Defined for sorting the goal states.
    @Override
    public int compareTo(@NotNull Node o) {
        return o.state.getResource() - this.state.getResource();
    }
}

// Frontier used for Depth-First Search Algorithm.
class Frontier {
    Stack<Node> frontier = new Stack<>();

    public void add(Node node) {
        frontier.push(node);
    }

    public Node remove() {
        return frontier.pop();
    }

    public boolean isNotEmpty() {
        return frontier.size() != 0;
    }
}

public class Mining {
    int[][] map;
    int n;
    List<Node> goalList = new ArrayList<>();

    public Mining(int n, String filename) {
        this.n = n;
        map = new int[n][n];
        try {
            // Reading from file and inputting into int[][] map.
            File file = new File(filename);
            Scanner sc = new Scanner(file);
            for (int i = 0; sc.hasNextLine(); i++) {
                String[] row = sc.nextLine().split("\\s+");
                for (int j = 0; j < row.length; j++) {
                    String element = row[j];
                    map[i][j] = Integer.parseInt(element);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private List<Result> getChildren(int x, int y) {
        List<Result> results = new ArrayList<>();
        if (x != n - 1 && y != 0) {
            results.add(new Result(x + 1, y - 1, Action.DOWN_LEFT));
        }
        if (x != n - 1 && y != n - 1) {
            results.add(new Result(x + 1, y + 1, Action.DOWN_RIGHT));
        }
        return results;
    }

    public void solve() {
        // Evaluating each starting cell at the surface.
        for (int i = 0; i < n; i++) {
            State initialState = new State(map[0][i], 0, i);
            Node initial = new Node(initialState, null, null);
            Frontier frontier = new Frontier();
            frontier.add(initial);

            while (frontier.isNotEmpty()) {
                Node node = frontier.remove();
                // If current node is one of many goal states.
                if (node.getState().x == n - 1) {
                    goalList.add(node);
                    continue;
                }

                // Finding the possible routes from the current node.
                List<Result> children = getChildren(node.getState().x, node.getState().y);

                // Adding the child nodes to the frontier.
                for (Result result : children) {
                    int newResource = node.getState().getResource() + map[result.x][result.y];
                    State childState = new State(newResource, result.x, result.y);
                    Node child = new Node(childState, node, result.getAction());
                    frontier.add(child);
                }
            }
        }

        // Sorting the list of goal states in descending order.
        Collections.sort(goalList);
        Node resultNode = goalList.get(0); // getting node with maximum resource in its state.
        printSolution(resultNode);
    }

    private void printSolution(Node solution) {
        // Getting all nodes from initial state to goal state.
        Stack<Node> path = new Stack<>();
        int total = solution.getState().getResource();
        while (solution.getParent() != null) {
            path.push(solution);
            solution = solution.getParent();
        }
        path.push(solution);

        // to register the points for visual output.
        List<Result> points = new ArrayList<>();

        // Printing the path from initial to goal state.
        State first = path.pop().getState();
        System.out.println("The driller starts at (" + first.x + ", " + first.y + ") and claims " + map[first.x][first.y] + ".");
        StringBuilder sb = new StringBuilder(String.valueOf(map[first.x][first.y]));
        points.add(new Result(first.x, first.y, null));
        while (path.size() > 0) {
            Node node = path.pop();
            State state = node.getState();
            Action action = node.getAction();
            System.out.println("Driller goes " + ((action == Action.DOWN_RIGHT) ? "down-right" : "down-left") + " and claims " + map[state.x][state.y] + ".");
            sb.append(" + ").append(map[state.x][state.y]);
            points.add(new Result(state.x, state.y, action));
        }
        sb.append(" = ").append(total);
        System.out.println("Driller retracts.\n\nTotal resources: " + sb.toString() + ".\n");
        printSolutionMap(points);
    }

    // Printing the visual output.
    private void printSolutionMap(List<Result> points) {
        row:
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (points.contains(new Result(i, j, null))) {
                    System.out.println(map[i][j]);
                    continue row;
                } else System.out.print("\t");
            }
        }
    }

    // Printing the input map from file.
    public void printMap() {
        System.out.println();
        for (int[] row : map) {
            for (int cell : row)
                System.out.print(cell + "\t");
            System.out.println();
        }
        System.out.println();
    }

    // Driver function
    public static void main(String[] args) {
        // TODO change file location based on your system.
        Mining obj = new Mining(7, "src\\main\\java\\mine1.txt");
        obj.printMap();
        obj.solve();
    }
}
