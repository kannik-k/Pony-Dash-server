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

    /**
     * Find path from source to destination.
     * @param srcX source x
     * @param srcY source y
     * @param dstX destination x
     * @param dstY destination y
     * @return List of path to destination
     */
    public ArrayList<Node> findPath(int srcX, int srcY, int dstX, int dstY) {
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingInt(Node::getFScore));
        openSet.add(new Node(srcX, srcY));
        Set<Node> closedSet = new HashSet<>(); // processed nodes

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            if (current.x == dstX && current.y == dstY) {
                return reconstructPath(current);
            }
            closedSet.add(current);
            updateNeighbours(current, dstX, dstY, openSet, closedSet);
        }
        return new ArrayList<>(); // Return empty path if none was found
    }

    /**
     * Update neighbours based on the tile that is being currently analyzed.
     * @param current node
     * @param dstX destination x
     * @param dstY destination y
     * @param openSet nodes that can still be processed
     * @param closedSet processed nodes
     */
    private void updateNeighbours(Node current, int dstX, int dstY, PriorityQueue<Node> openSet, Set<Node> closedSet) {
        for (int[] neighbour : neighbours) {
            int x = current.x + neighbour[0];
            int y = current.y + neighbour[1];
            Node neighbourNode = new Node(x, y);
            if (isOutOfBounds(x, y) || isCollision(x, y) || closedSet.contains(neighbourNode)) {
                continue;
            }
            int newGScore = current.gScore + 1;
            if (!openSet.contains(neighbourNode) || newGScore < neighbourNode.gScore) {
                neighbourNode.parent = current;
                neighbourNode.gScore = newGScore;
                neighbourNode.updateHScore(dstX, dstY);
                openSet.add(neighbourNode);
            }
        }
    }

    private boolean isOutOfBounds(int x, int y) {
        return x < 0 || x >= maxX || y < 0 || y >= maxY;
    }

    private boolean isCollision(int x, int y) {
        return grid[y][x] == 1; // 1 is collision
    }

    private ArrayList<Node> reconstructPath(Node current) {
        ArrayList<Node> path = new ArrayList<>();
        while (current != null) {
            path.add(current);
            current = current.parent;
        }
        Collections.reverse(path); // Reversing to get the correct order
        return path;
    }
}
