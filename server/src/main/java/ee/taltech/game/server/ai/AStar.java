package ee.taltech.game.server.ai;

import java.util.*;

public class AStar {
    private final int maxX;
    private final int maxY;
    private final int[][] grid;
    private final int[][] neighbours = {{-1, 0}, {0, -1}, {1, 0}, {0, 1}};

    public AStar(int[][] grid) {
        this.grid = grid;
        this.maxX = grid[0].length;
        this.maxY = grid.length;
    }

    public class Node {
        int x;
        int y;
        int gScore; // distance to this node
        int hScore; // distance from this node to the finish
        Node parent;

        Node(int x, int y) {
            this.x = x;
            this.y = y;
            this.gScore = 0;
            this.hScore = 0;
            this.parent = null;
        }

        /**
         * Set hScore for this node.
         * @param dstX destination x coordinate
         * @param dstY destination y coordinate
         */
        void updateHScore(int dstX, int dstY) {
            this.hScore = Math.abs(x - dstX) + Math.abs(y - dstY);
        }

        /**
         * Get F score for this node.
         * <p>
         * This is the sum of the path distance to the node and the direct distance from the node to the final point.
         * The lower the F score, the better node for the final path it is.
         * </p>
         * @return F score
         */
        int getFScore() {
            return this.gScore + this.hScore;
        }

        /**
         * Check if given object is the same as this node.
         * @param o given object
         * @return boolean if object is the same as this node
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Node node)) return false;
            return x == node.x && y == node.y;
        }

        /**
         * Create hashcode for node.
         * @return created hashcode
         */
        @Override
        public int hashCode() {
            return Integer.hashCode(x + (y * maxY));
        }
    }

    public List<Node> findPath(int srcX, int srcY, int dstX, int dstY) {
        List<Node> path = new ArrayList<>();
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingInt(AStar.Node::getFScore));
        openSet.add(new Node(srcX, srcY));
        Set<Node> closedSet = new HashSet<>(); // processed nodes
        while(!openSet.isEmpty()) {
            AStar.Node current = openSet.poll(); // poll returns and removes the element at the front of the queue
            if (current.x == dstX && current.y == dstY) { // if node is destination point
                while (current != null) {
                    path.add(current);
                    current = current.parent; // goes up the tree to find path
                }
                return path;
            }
            closedSet.add(current);
            for (int[] neighbour: neighbours) {
                int x = current.x + neighbour[0];
                int y = current.y + neighbour[1];
                if (x < 0 || x >= maxX || y < 0 || y >= maxY || grid[y][x] == 1) { // 1 is collision
                    continue;
                }
                AStar.Node neighbourNode = new AStar.Node(x, y);
                int newGScore = current.gScore + 1;
                if (closedSet.contains(neighbourNode)) {
                    continue;
                }
                if (!openSet.contains(neighbourNode) || newGScore < neighbourNode.gScore) {
                    neighbourNode.parent = current;
                    neighbourNode.gScore = newGScore;
                    neighbourNode.updateHScore(dstX, dstY);
                    openSet.add(neighbourNode);
                }
            }
        }
        return null;
    }
}
