import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

// represent the Graph class
// this class is used for kruskal
class Graph {
  HashMap<Integer, Integer> representatives = new HashMap<Integer, Integer>();
  ArrayList<Edge> edgesInTree;
  ArrayList<Edge> workList; // all edges in graph, sorted by edge weights
  boolean bfs;
  HashMap<Integer, Integer> cameFromEdge;

  // constructor
  Graph(ArrayList<Edge> edges, ArrayList<Edge> workList) {
    this.edgesInTree = edges;
    this.workList = workList;

    // this.workList.sort((e1, e2) -> e1.compareTo(e2));;
  }

  // adds edge to graph
  // EFFECT: adds edge to this edgesInTree
  public void addEdge(Cell n1, Cell n2, Random r) {
    Edge edge = new Edge(n1, n2, r);
    edgesInTree.add(edge);
  }

  // unions two sets together
  // EFFECT: id mapping is modified, everything in set2 will map to set1
  public void union(HashMap<Integer, Integer> representatives2, Integer set1, Integer set2) {

    for (Map.Entry<Integer, Integer> entry : representatives2.entrySet()) {
      if (entry.getValue() == set2) {
        entry.setValue(set1);
      }
    }

  }

  // finds the set this node is in
  public Integer find(HashMap<Integer, Integer> representatives2, int id) {
    return representatives2.get(id);
  }

  // kruskal creates minimum spanning tree
  // EFFECT: modifies worklist and outputs minimum spanning tree
  public ArrayList<Edge> kruskal() {
    // initialize every node's representative to itself
    // sets
    for (Edge edge : workList) {
      this.representatives.put(edge.n1.id, edge.n1.id);
      this.representatives.put(edge.n2.id, edge.n2.id);
    }
    // if AB, BC
    // Node : A B C
    // Set: A B C

    while (workList.size() > 0) {
      // Pick the next cheapest edge of the graph: suppose it connects X and Y.
      Edge current = workList.get(0);

      // if set is the same, dont add
      if (find(representatives, current.n1.id).equals(find(representatives, current.n2.id))) {
        // already connected
        // discard this edge
        current.setConnected(false);
        workList.remove(0);
      }
      // add
      else {
        // record this edge in edgesInTree
        edgesInTree.add(current);
        // make same set
        union(representatives, find(representatives, current.n1.id),
            find(representatives, current.n2.id));
        workList.remove(0);

      }
    }
    return edgesInTree;
  }

  // ==========P2===============

  // finds destination cell and builds hashmap along the way
  // accumulates hashmap, worklist, and seen cell list
  // EFFECT: hashmap is updated and worklist is modified
  // used for both dfs and bfs, only differnce is how items are added to worklist
  boolean search(Cell source, Cell destination, ArrayList<Cell> workList, ArrayList<Cell> seenList,
      HashMap<Integer, Integer> cameFromEdge, int length, int index) {

    if (workList.size() > 0) {
      Cell next = workList.remove(0);
      next.highlight(Color.gray);

      // already been seen (maybe not necessary)
      if (seenList.contains(next)) {
        return true; // keep going
      }
      // reached destination
      if (next.equals(destination)) {
        this.cameFromEdge = cameFromEdge;
        return false; // finished
      }
      // add neighbors to worklist, and mapping
      else {
        for (Edge e : next.getEdges(length)) {
          Cell neighborNode = e.getOtherNode(next);
          if (!seenList.contains(neighborNode)) {

            if (bfs) {
              workList.add(neighborNode);
            }
            else {
              workList.add(index, neighborNode);

            }
            cameFromEdge.put(neighborNode.id, next.id); // fix field of field

          }
        }
        // add current to seen
        seenList.add(next);
      }

      return true;
    }
    else {
      return false; // workList is empty
    }
  }

  // makes pathway of edges from cell to starting cell
  // using the hashmap
  public ArrayList<Integer> reconstruct(HashMap<Integer, Integer> cameFromEdge, int c) {
    // for new
    // hashmap
    ArrayList<Integer> path = new ArrayList<>();
    int current = c;

    while (cameFromEdge.containsKey(current)) {
      // gets edge that maps to current
      int prev = cameFromEdge.get(current);
      path.add(0, current);
      current = prev;
    }
    path.add(0, current); // add start node to the path
    return path;
  }

  // EFFECT: set bfs boolean
  void setBFS(boolean b) {
    this.bfs = true;
  }

  // gets the hashmap of the cameFromEdge
  public HashMap<Integer, Integer> getCameFromEdge() {
    return this.cameFromEdge;
  }

  // moves cell up
  // EFFECT: sets current cell and changes color
  public Cell moveUp(Cell currentCell) {
    if (currentCell.hasTop() && currentCell.topConnected()) {
      currentCell.highlight(Color.gray);
      currentCell = currentCell.getOtherNodeTop();
      currentCell.highlight(Color.red);

    }
    return currentCell;

  }

  // moves cell down
  // EFFECT: sets current cell and changes color
  public Cell moveDown(Cell currentCell, int length) {
    if (currentCell.hasBottom(length) && currentCell.bottomConnected()) {
      currentCell.highlight(Color.gray);
      currentCell = currentCell.getOtherNodeBottom();
      currentCell.highlight(Color.red);
    }
    return currentCell;
  }

  // moves cell left
  // EFFECT: sets current cell and changes color
  public Cell moveLeft(Cell currentCell) {
    if (currentCell.hasLeft() && currentCell.leftConnected()) {
      currentCell.highlight(Color.gray);
      currentCell = currentCell.getOtherNodeLeft();
      currentCell.highlight(Color.red);

    }
    return currentCell;
  }

  // moves cell right
  // EFFECT: sets current cell and changes color
  public Cell moveRight(Cell currentCell, int length) {
    if (currentCell.hasRight(length) && currentCell.rightConnected()) {
      currentCell.highlight(Color.gray);
      currentCell = currentCell.getOtherNodeRight();
      currentCell.highlight(Color.red);

    }
    return currentCell;
  }

}