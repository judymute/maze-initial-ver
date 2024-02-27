import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

// Represents a single square of the game area
class Cell {

  Posn pos;
  int id;
  Edge left;
  Edge right;
  Edge top;
  Edge bottom;

  Color color;

  // constructor of cell
  Cell(int id, int x, int y) {
    this.id = id;
    this.pos = new Posn(x, y);
    this.color = Color.white;
  }

  // draws the cell
  WorldImage drawCell() {
    return new RectangleImage(20, 20, OutlineMode.SOLID, color);
  }

  // checks if cell should have a left
  boolean hasLeft() {
    return (this.pos.x != 10);
  }

  // checks if cell should have a right
  boolean hasRight(int length) {
    return (this.pos.x != (length - 1) * 20 + 10);
  }

  // checks if cell should have a top
  boolean hasTop() {
    return (this.pos.y != 10);
  }

  // checks if cell should have a bottom
  boolean hasBottom(int length) {
    return (this.pos.y != (length - 1) * 20 + 10);
  }

  // places the drawn cell onto a given image
  WorldScene placeCell(WorldScene image) {
    image.placeImageXY(this.drawCell(), this.pos.x, this.pos.y);
    return image;
  }

  // get other node top
  Cell getOtherNodeTop() {
    return this.top.getOtherNode(this);
  }

  // get other node bottom
  Cell getOtherNodeBottom() {
    return this.bottom.getOtherNode(this);
  }

  // get other node bottom
  Cell getOtherNodeLeft() {
    return this.left.getOtherNode(this);
  }

  // get other node bottom
  Cell getOtherNodeRight() {
    return this.right.getOtherNode(this);
  }

  // get id
  int getId() {
    return this.id;
  }

  // ==========P2============

  // set left
  // EFFECT: left field
  void setLeft(Edge left) {
    this.left = left;
  }

  // set right
  // EFFECT: right field is set
  void setRight(Edge right) {
    this.right = right;
  }

  // set top
  // EFFECT: top field is set
  void setTop(Edge top) {
    this.top = top;
  }

  // set bottom
  // EFFECT: bottom field is set
  void setBottom(Edge bottom) {
    this.bottom = bottom;
  }

  // returns a list of the cells edges //TEST
  public ArrayList<Edge> getEdges(int length) {
    ArrayList<Edge> cellEdges = new ArrayList<Edge>();

    if (this.hasLeft() && this.left.isConnected()) {
      cellEdges.add(this.left);
    }
    if (this.hasRight(length) && this.right.isConnected()) {
      cellEdges.add(this.right);
    }
    if (this.hasTop() && this.top.isConnected()) {
      cellEdges.add(this.top);
    }
    if (this.hasBottom(length) && this.bottom.isConnected()) {
      cellEdges.add(this.bottom);
    }
    return cellEdges;

  }

  // highlights cell to given color
  // EFFECT: cell color is set to given
  public void highlight(Color color) {
    this.color = color;

  }

  // changes visibility of cell
  // EFFECT: cell color is changed
  public void changeVisibility() {
    if (this.color.equals(Color.white)) {
      this.highlight(Color.gray);
    }
    else {
      this.highlight(Color.white);
    }
  }

  // is top connected?
  boolean topConnected() {
    return this.top.isConnected();
  }

  // is bottom connected?
  boolean bottomConnected() {
    return this.bottom.isConnected();
  }

  // is left connected?
  boolean leftConnected() {
    return this.left.isConnected();
  }

  // is right connected?
  boolean rightConnected() {
    return this.right.isConnected();
  }

}

// represents a MazeWorld class
class MazeWorld extends World {
  // All the cells of the game
  ArrayList<ArrayList<Cell>> board = new ArrayList<ArrayList<Cell>>();
  ArrayList<Edge> edgesWorkList = new ArrayList<Edge>(); // this list gets modified
  ArrayList<Edge> edgesAll = new ArrayList<Edge>(); // all edges here
  Random r = new Random();
  Graph spanningTree;
  ArrayList<Edge> edgesSpanningTree = new ArrayList<Edge>(); // only edges in spanning tree
  int length;
  public static final int SCENE_SIZE = 520;
  WorldScene background = new WorldScene(SCENE_SIZE, SCENE_SIZE);

  boolean animate;
  Cell cellFirst;
  Cell cellLast;
  Cell currentCell;
  ArrayList<Cell> searchWorkList = new ArrayList<Cell>();
  ArrayList<Cell> seenList = new ArrayList<Cell>();
  HashMap<Integer, Integer> searchMap = new HashMap<Integer, Integer>();
  int addIndex;
  int wrongMoves;
  boolean win;

  // constructor for maze world
  // creates cells, creates edges,
  // sorts edges, then runs kruskal
  // EFFECT: cells added and removed from worklist
  // edges created in edges lists
  MazeWorld(int length, Random rand) {
    animate = false;
    this.length = length;
    this.r = rand;
    this.createCells(length);
    this.createEdges(length, r);
    this.edgesWorkList.sort((e1, e2) -> e1.compareTo(e2));
    spanningTree = new Graph(new ArrayList<Edge>(), edgesWorkList);
    edgesSpanningTree = spanningTree.kruskal();

    // init search variables
    cellFirst = board.get(0).get(0);
    cellLast = board.get(length - 1).get(length - 1);
    currentCell = board.get(0).get(0);
    searchWorkList.add(cellFirst);
    wrongMoves = 0;
    win = false;
    this.makeScene();
  }

  // creates the array list of array list of cells
  // EFFECT: adds array list of cells to board
  // adds cells to each array list
  void createCells(int length) {
    int id = 0;
    // create the ArrayList with a loop
    for (int row = 0; row < length; row++) {
      // row
      ArrayList<Cell> newRow = new ArrayList<Cell>();
      // nested for-loop that iterates over each column in the current row
      // col is initialized to 0 and increments by 1 on each iteration
      // until it reaches the values of 'length'
      for (int col = 0; col < length; col++) { // index in row
        newRow.add(new Cell(id, col * 20 + 10, row * 20 + 10));
        id++;
      }
      board.add(newRow);
    }
  }

  // creates all edges and assigns them to cells
  // EFFECT: sets the edge fields for each cell
  void createEdges(int length, Random rand) {
    // loop that checks and assigns adjacent cells
    for (int row = 0; row < length; row++) {
      for (int col = 0; col < length; col++) {
        Cell cell = board.get(row).get(col);
        if (board.get(row).get(col).hasRight(length)) {
          Edge e = new Edge(cell, board.get(row).get(col + 1), r.nextInt(1000));
          cell.setRight(e);
          board.get(row).get(col + 1).setLeft(e);
          edgesWorkList.add(e);
          edgesAll.add(e);
        }
        if (board.get(row).get(col).hasBottom(length)) {
          Edge e = new Edge(cell, board.get(row + 1).get(col), r.nextInt(1000));
          cell.setBottom(e);
          board.get(row + 1).get(col).setTop(e);
          edgesWorkList.add(e);
          edgesAll.add(e);
        }
      }
    }
  }

  // make scene for maze
  public WorldScene makeScene() {
    for (int row = 0; row < length; row++) {
      for (int col = 0; col < length; col++) {
        board.get(row).get(col).placeCell(background);
      }
    }

    for (Edge e : edgesAll) {
      // if edge e is not in the spanning tree, draw a wall
      if (!edgesSpanningTree.contains(e)) {
        if (e.n1.pos.x == e.n2.pos.x) {
          // background.placeImageXY(e.drawEdgeHorizontal(), e.n1.pos.x, e.n1.pos.y + 10);
          // fof fixed
          e.placeEdgeHorizontal(background);
        }
        else {
          // background.placeImageXY(e.drawEdgeVertical(), e.n1.pos.x + 10, e.n1.pos.y);
          // fof fixed
          e.placeEdgeVertical(background);

        }
      }
    }
    WorldImage border = new RectangleImage(length * 40, length * 40, OutlineMode.OUTLINE,
        Color.black);
    background.placeImageXY(border, 0, 0);

    String moves = "Wrong moves: " + wrongMoves;
    WorldImage movesImage = new TextImage(moves, 20, Color.black);
    WorldImage box = new RectangleImage(200, 50, OutlineMode.SOLID, Color.white);
    background.placeImageXY(box, SCENE_SIZE / 2, SCENE_SIZE - 20);
    background.placeImageXY(movesImage, SCENE_SIZE / 2, SCENE_SIZE - 10);

    if (win) {
      String win = "You win omg";
      WorldImage winImage = new TextImage(win, 20, Color.black);
      background.placeImageXY(winImage, SCENE_SIZE / 2, SCENE_SIZE - 30);

    }
    return background;
  }

  // gets cell given an id
  Cell getCellFromId(int id) {
    int row = id / length;
    int col = id % length;

    return board.get(row).get(col);
  }

  // initialized all variables to start the maze
  // EFFECT: changes all the fields to the default
  public void initialize() {
    board = new ArrayList<ArrayList<Cell>>();
    edgesWorkList = new ArrayList<Edge>(); // this list gets modified
    edgesAll = new ArrayList<Edge>(); // all edges here

    edgesSpanningTree = new ArrayList<Edge>(); // only edges in spanning tree

    searchWorkList = new ArrayList<Cell>();
    seenList = new ArrayList<Cell>();
    searchMap = new HashMap<Integer, Integer>();

    animate = false;
    this.r = new Random();
    this.createCells(length);
    this.createEdges(length, r);
    this.edgesWorkList.sort((e1, e2) -> e1.compareTo(e2));
    spanningTree = new Graph(new ArrayList<Edge>(), edgesWorkList);
    edgesSpanningTree = spanningTree.kruskal();

    // init search variables
    cellFirst = board.get(0).get(0);
    cellLast = board.get(length - 1).get(length - 1);
    currentCell = board.get(0).get(0);
    searchWorkList.add(cellFirst);
    wrongMoves = 0;
    win = false;
    this.makeScene();
  }

  // on key
  // key event resets board r is pressed
  // EFFECT: depending on key,
  // changes animation field, colors of cells, and initial fields
  public void onKeyEvent(String key) {
    // bfs
    if (key.equals("b")) {
      this.animate = true;
      spanningTree.setBFS(true);
      // this.addIndex = searchWorkList.size() - 1;
    }

    // dfs
    if (key.equals("d")) {
      this.animate = true;
      this.addIndex = 0;
    }

    // visuals
    if (key.equals("v")) {
      for (Cell c : seenList) {
        if (!spanningTree.reconstruct(searchMap, cellLast.getId()).contains(c.getId())) {
          c.changeVisibility();
        }
      }
    }

    // reset board
    if (key.equals("r")) {
      this.initialize();
    }

    if (key.equals("up")) {
      currentCell = this.spanningTree.moveUp(currentCell);

      if (!spanningTree.reconstruct(searchMap, cellLast.getId()).contains(currentCell.getId())) {
        wrongMoves++;
      }

    }

    if (key.equals("down")) {
      currentCell = this.spanningTree.moveDown(currentCell, length);

      if (!spanningTree.reconstruct(searchMap, cellLast.getId()).contains(currentCell.getId())) {
        wrongMoves++;
      }
    }

    if (key.equals("left")) {
      currentCell = this.spanningTree.moveLeft(currentCell);

      if (!spanningTree.reconstruct(searchMap, cellLast.getId()).contains(currentCell.getId())) {
        wrongMoves++;
      }
    }

    if (key.equals("right")) {
      currentCell = this.spanningTree.moveRight(currentCell, length);

      if (!spanningTree.reconstruct(searchMap, cellLast.getId()).contains(currentCell.getId())) {
        wrongMoves++;
      }
    }
  }

  // updates world scene every tick if animate is true
  // EFFECT: changes animate field, changes color of
  // cells in the path, changes color of cell based on search
  public void onTick() {
    if (animate) {
      animate = this.spanningTree.search(cellFirst, cellLast, searchWorkList, seenList, searchMap,
          length, addIndex);
      if (!animate) {
        this.darkHighLightAll(spanningTree.reconstruct(searchMap, cellLast.getId()));
      }

    }

    if (currentCell.equals(cellLast)) {
      win = true;
    }

    makeScene();

  }

  // highlights the solved maze path
  // EFFECT: changes color of all cells in reconstructed list
  void darkHighLightAll(ArrayList<Integer> reconstructedList) {

    for (int id : reconstructedList) {
      Cell c = this.getCellFromId(id);
      c.highlight(Color.black);
    }

  }

}

//represents examples and tests of FloodItWorld
class ExamplesMaze {

  // 2x2 nodes
  Cell node4 = new Cell(4, 10, 10);
  Cell node5 = new Cell(5, 30, 10);
  Cell node6 = new Cell(6, 30, 30);
  Cell node7 = new Cell(7, 10, 30);

  // 3x3 nodes
  Cell nodeA = new Cell(1, 10, 10);
  Cell nodeB = new Cell(2, 10, 30);
  Cell nodeC = new Cell(3, 10, 50);

  Cell nodeD = new Cell(4, 30, 10);
  Cell nodeE = new Cell(5, 30, 30);
  Cell nodeF = new Cell(6, 30, 50);

  Cell nodeG = new Cell(7, 50, 10);
  Cell nodeH = new Cell(8, 50, 30);
  Cell nodeI = new Cell(9, 50, 50);

  ArrayList<Edge> edges;
  ArrayList<Edge> edges2;
  ArrayList<Edge> edges3;
  ArrayList<Edge> edges4;

  ArrayList<Edge> workList;
  ArrayList<Edge> workList2;
  ArrayList<Edge> workList3;
  ArrayList<Edge> workList4;

  ArrayList<Integer> reconstructedList;

  // HashMap<Node, Node> map = new HashMap<Node, Node>();
  Graph g;
  Graph g2; // 2x2 easy
  Graph g3; // 2x2 complex
  Graph g4; // 3x3

  Edge edge45;
  Edge edge56;
  Edge edge64;

  Edge g3node45;
  Edge g3node56;
  Edge g3node67;
  Edge g3node47;

  // ===============================

  int sceneSize;

  MazeWorld mazeWorld1;
  MazeWorld mazeWorld2;

  ArrayList<ArrayList<Cell>> exampleBoard;
  ArrayList<ArrayList<Cell>> exampleBoard2;

  HashMap<Integer, Integer> representatives;

  Cell node0;
  Cell node1;
  Cell node2;
  Cell node3;
  ArrayList<ArrayList<Cell>> cellList1;
  ArrayList<ArrayList<Cell>> cellListModified1;

  Cell node8;
  Cell node9;
  Cell node10;
  Cell node11;
  Cell node12;
  Cell node13;
  Cell node14;
  Cell node15;

  ArrayList<ArrayList<Cell>> cellList2;
  ArrayList<ArrayList<Cell>> cellListModified2;

  Edge edge1;
  Edge edge2;
  Edge edge3;
  Edge edge4;

  Edge edge5;
  Edge edge6;
  Edge edge7;
  Edge edge8;

  WorldScene image1;
  WorldScene image2;

  MazeWorld maze3;

  void init() {
    // testing maze
    maze3 = new MazeWorld(3, new Random(1));

    this.sceneSize = MazeWorld.SCENE_SIZE;

    this.mazeWorld1 = new MazeWorld(2, new Random(1));
    this.mazeWorld2 = new MazeWorld(3, new Random(1));

    this.exampleBoard = mazeWorld1.board;
    this.exampleBoard2 = mazeWorld2.board;

    this.image1 = new WorldScene(100, 100);
    this.image2 = new WorldScene(100, 100);

    this.representatives = new HashMap<Integer, Integer>();
    this.reconstructedList = new ArrayList<Integer>();

    ////////////////

    this.node4 = new Cell(4, 10, 10);
    this.node5 = new Cell(5, 30, 10);
    this.node6 = new Cell(6, 30, 30);
    this.node7 = new Cell(7, 10, 30);

    // another set used for testing
    this.node0 = new Cell(0, 10, 10);
    this.node1 = new Cell(1, 30, 10);
    this.node2 = new Cell(2, 10, 30);
    this.node3 = new Cell(3, 30, 30);

    // mainly for testing createCells purposes
    this.node8 = new Cell(1, 30, 10);
    this.node9 = new Cell(2, 50, 10);
    this.node10 = new Cell(3, 10, 30);
    this.node11 = new Cell(4, 30, 30);
    this.node12 = new Cell(5, 50, 30);
    this.node13 = new Cell(6, 10, 50);
    this.node14 = new Cell(7, 30, 50);
    this.node15 = new Cell(8, 50, 50);

    this.edge1 = new Edge(node4, node5, 1);
    this.edge2 = new Edge(node5, node6, 2);
    this.edge3 = new Edge(node6, node4, 3);
    this.edge4 = new Edge(node4, node5, 4);

    this.edge5 = new Edge(node0, node1, 5);
    this.edge6 = new Edge(node1, node2, 6);
    this.edge7 = new Edge(node2, node0, 7);
    this.edge8 = new Edge(node0, node1, 8);

    this.cellList1 = new ArrayList<ArrayList<Cell>>(
        Arrays.asList(new ArrayList<Cell>(Arrays.asList(this.node0, this.node1)),
            new ArrayList<Cell>(Arrays.asList(this.node2, this.node3))));

    this.cellListModified1 = new ArrayList<ArrayList<Cell>>(
        Arrays.asList(new ArrayList<Cell>(Arrays.asList(this.node0, this.node1)),
            new ArrayList<Cell>(Arrays.asList(this.node2, this.node3)),
            new ArrayList<Cell>(Arrays.asList(this.node0))));

    this.cellList2 = new ArrayList<ArrayList<Cell>>(
        Arrays.asList(new ArrayList<Cell>(Arrays.asList(this.node0, this.node8, this.node9)),
            new ArrayList<Cell>(Arrays.asList(this.node10, this.node11, this.node12)),
            new ArrayList<Cell>(Arrays.asList(this.node13, this.node14, this.node15))));

    this.cellListModified2 = new ArrayList<ArrayList<Cell>>(
        Arrays.asList(new ArrayList<Cell>(Arrays.asList(this.node0, this.node8, this.node9)),
            new ArrayList<Cell>(Arrays.asList(this.node10, this.node11, this.node12)),
            new ArrayList<Cell>(Arrays.asList(this.node13, this.node14, this.node15)),
            new ArrayList<Cell>(Arrays.asList(this.node0))));

    // ====================================
    this.node0 = new Cell(0, 10, 10);
    this.node1 = new Cell(1, 30, 10);
    this.node2 = new Cell(2, 10, 30);
    this.node3 = new Cell(3, 30, 30);

    // ====================================

    edges = new ArrayList<Edge>();
    workList = new ArrayList<Edge>();
    g = new Graph(edges, workList);

    edge45 = new Edge(node4, node5, 1);
    edge56 = new Edge(node5, node6, 2);
    edge64 = new Edge(node6, node4, 3);

    workList.add(new Edge(node4, node5, 1));
    workList.add(new Edge(node5, node6, 2));
    workList.add(new Edge(node6, node4, 3));

    edges2 = new ArrayList<Edge>();
    workList2 = new ArrayList<Edge>();
    g2 = new Graph(edges2, workList2);

    workList2.add(new Edge(node4, node5, 4));
    workList2.add(new Edge(node5, node6, 3));
    workList2.add(new Edge(node7, node6, 2));
    workList2.add(new Edge(node4, node7, 1));

    edges3 = new ArrayList<Edge>();
    workList3 = new ArrayList<Edge>();
    g3 = new Graph(edges3, workList3);

    g3node45 = new Edge(node4, node5, 1);
    g3node56 = new Edge(node5, node6, 3);
    g3node67 = new Edge(node7, node6, 2);
    g3node47 = new Edge(node4, node7, 4);

    workList3.add(g3node45);
    workList3.add(g3node56);
    workList3.add(g3node67);
    workList3.add(g3node47);

    // 3x3
    edges4 = new ArrayList<Edge>();
    workList4 = new ArrayList<Edge>();
    g4 = new Graph(edges4, workList4);

    workList4.add(new Edge(nodeA, nodeB, 10));
    workList4.add(new Edge(nodeB, nodeC, 20));

    workList4.add(new Edge(nodeA, nodeD, 30));
    workList4.add(new Edge(nodeB, nodeE, 40));
    workList4.add(new Edge(nodeC, nodeF, 50));

    workList4.add(new Edge(nodeD, nodeE, 60));
    workList4.add(new Edge(nodeE, nodeF, 70));

    workList4.add(new Edge(nodeD, nodeG, 80));
    workList4.add(new Edge(nodeE, nodeH, 90));
    workList4.add(new Edge(nodeF, nodeI, 100));

    workList4.add(new Edge(nodeG, nodeH, 110));
    workList4.add(new Edge(nodeH, nodeI, 120));

  }

  // =================================

  // test for drawCell
  boolean testDrawCell(Tester t) {
    init();

    return t.checkExpect(node4.drawCell(),
        new RectangleImage(20, 20, OutlineMode.SOLID, Color.white))
        && t.checkExpect(node5.drawCell(),
            new RectangleImage(20, 20, OutlineMode.SOLID, Color.white))
        && t.checkExpect(node6.drawCell(),
            new RectangleImage(20, 20, OutlineMode.SOLID, Color.white))
        && t.checkExpect(node7.drawCell(),
            new RectangleImage(20, 20, OutlineMode.SOLID, Color.white))

        // more tests
        && t.checkExpect(node8.drawCell(),
            new RectangleImage(20, 20, OutlineMode.SOLID, Color.white))
        && t.checkExpect(node9.drawCell(),
            new RectangleImage(20, 20, OutlineMode.SOLID, Color.white))
        && t.checkExpect(node10.drawCell(),
            new RectangleImage(20, 20, OutlineMode.SOLID, Color.white))
        && t.checkExpect(node11.drawCell(),
            new RectangleImage(20, 20, OutlineMode.SOLID, Color.white));

  }

  // tests if cell has adjacent (neighbors)
  // tests for hasBottom
  // hasRight
  // hasLeft
  // hasTop
  void testHasAdjacent(Tester t) {
    init();

    t.checkExpect(exampleBoard.get(0).get(0).hasBottom(2), true);
    t.checkExpect(exampleBoard.get(0).get(0).hasRight(2), true);
    t.checkExpect(exampleBoard.get(0).get(0).hasLeft(), false);
    t.checkExpect(exampleBoard.get(0).get(0).hasTop(), false);

    t.checkExpect(exampleBoard.get(0).get(1).hasBottom(2), true);
    t.checkExpect(exampleBoard.get(0).get(1).hasRight(2), false);
    t.checkExpect(exampleBoard.get(0).get(1).hasLeft(), true);
    t.checkExpect(exampleBoard.get(0).get(1).hasTop(), false);

    t.checkExpect(exampleBoard.get(1).get(0).hasBottom(2), false);
    t.checkExpect(exampleBoard.get(1).get(0).hasRight(2), true);
    t.checkExpect(exampleBoard.get(1).get(0).hasLeft(), false);
    t.checkExpect(exampleBoard.get(1).get(0).hasTop(), true);

    t.checkExpect(exampleBoard.get(1).get(1).hasBottom(2), false);
    t.checkExpect(exampleBoard.get(1).get(1).hasRight(2), false);
    t.checkExpect(exampleBoard.get(1).get(1).hasLeft(), true);
    t.checkExpect(exampleBoard.get(1).get(1).hasTop(), true);

    // tests for another size grid
    t.checkExpect(exampleBoard2.get(0).get(0).hasBottom(3), true);
    t.checkExpect(exampleBoard2.get(0).get(0).hasRight(3), true);
    t.checkExpect(exampleBoard2.get(0).get(0).hasLeft(), false);
    t.checkExpect(exampleBoard2.get(0).get(0).hasTop(), false);

    t.checkExpect(exampleBoard2.get(0).get(2).hasBottom(3), true);
    t.checkExpect(exampleBoard2.get(0).get(2).hasRight(3), false);
    t.checkExpect(exampleBoard2.get(0).get(2).hasLeft(), true);
    t.checkExpect(exampleBoard2.get(0).get(2).hasTop(), false);

    t.checkExpect(exampleBoard2.get(2).get(0).hasBottom(3), false);
    t.checkExpect(exampleBoard2.get(2).get(0).hasRight(3), true);
    t.checkExpect(exampleBoard2.get(2).get(0).hasLeft(), false);
    t.checkExpect(exampleBoard2.get(2).get(0).hasTop(), true);

    t.checkExpect(exampleBoard2.get(2).get(2).hasBottom(3), false);
    t.checkExpect(exampleBoard2.get(2).get(2).hasRight(3), false);
    t.checkExpect(exampleBoard2.get(2).get(2).hasLeft(), true);
    t.checkExpect(exampleBoard2.get(2).get(2).hasTop(), true);
  }

  // =================================

  // tests for placeCell
  void testPlaceCell(Tester t) {
    init();
    image2.placeImageXY(exampleBoard.get(0).get(0).drawCell(), 10, 10);
    t.checkExpect(exampleBoard.get(0).get(0).placeCell(image1), image2);

    image2.placeImageXY(exampleBoard.get(0).get(1).drawCell(), 30, 10);
    t.checkExpect(
        exampleBoard.get(0).get(1).placeCell(exampleBoard.get(0).get(1).placeCell(image1)), image2);

    init();
    // test for another grid board size
    image2.placeImageXY(exampleBoard2.get(0).get(0).drawCell(), 10, 10);
    t.checkExpect(exampleBoard2.get(0).get(0).placeCell(image1), image2);

    image2.placeImageXY(exampleBoard2.get(1).get(1).drawCell(), 30, 30);
    image2.placeImageXY(exampleBoard2.get(0).get(1).drawCell(), 30, 30);
    t.checkExpect(
        exampleBoard2.get(1).get(1).placeCell(exampleBoard2.get(1).get(1).placeCell(image1)),
        image2);
  }

  // tests for setLeft, setRight, setTop, setBottom
  void testSetEdges(Tester t) {
    init();

    // examples for setEdges
    ArrayList<Edge> edges4SetList = new ArrayList<Edge>();
    edges4SetList.add(edge5);
    edges4SetList.add(edge7);

    ArrayList<Edge> edges5SetList = new ArrayList<Edge>();
    edges5SetList.add(edge5);
    edges5SetList.add(edge8);

    ArrayList<Edge> edges6SetList = new ArrayList<Edge>();
    edges6SetList.add(edge1);
    edges6SetList.add(edge3);

    ArrayList<Edge> edges7SetList = new ArrayList<Edge>();
    edges7SetList.add(edge1);
    edges7SetList.add(edge4);

    // initial: suppose this is the initial placement of edges
    exampleBoard2.get(0).get(0).setRight(edge5);
    exampleBoard2.get(0).get(0).setBottom(edge7);
    exampleBoard2.get(0).get(1).setLeft(edge5);
    exampleBoard2.get(0).get(1).setBottom(edge8);
    exampleBoard2.get(1).get(0).setTop(edge7);
    exampleBoard2.get(1).get(0).setRight(edge8);
    exampleBoard2.get(1).get(1).setTop(edge6);
    exampleBoard2.get(1).get(1).setLeft(edge7);

    // initial test
    t.checkExpect(exampleBoard2.get(0).get(0).getEdges(0), edges4SetList);
    t.checkExpect(exampleBoard2.get(0).get(1).getEdges(0), edges5SetList);

    // modify:
    init();
    exampleBoard2.get(0).get(0).setRight(edge1);
    exampleBoard2.get(0).get(0).setBottom(edge3);
    exampleBoard2.get(0).get(1).setLeft(edge1);
    exampleBoard2.get(0).get(1).setBottom(edge4);
    exampleBoard2.get(1).get(0).setTop(edge3);
    exampleBoard2.get(1).get(0).setRight(edge4);
    exampleBoard2.get(1).get(1).setTop(edge2);
    exampleBoard2.get(1).get(1).setLeft(edge3);

    // check again
    t.checkExpect(exampleBoard2.get(0).get(0).getEdges(0), edges6SetList);
    t.checkExpect(exampleBoard2.get(0).get(1).getEdges(0), edges7SetList);
  }

  // test get edges
  void testGetEdges(Tester t) {
    init();
    // we chose to test most by looking at the sizes
    // because the actual array list will be too long.
    // So we thought this way may be more efficient
    t.checkExpect(maze3.board.get(0).get(0).getEdges(60).size(), 1);
    t.checkExpect(maze3.board.get(0).get(1).getEdges(60).size(), 1);
    t.checkExpect(maze3.board.get(1).get(0).getEdges(60).size(), 2);
    t.checkExpect(maze3.board.get(1).get(1).getEdges(60).size(), 2);

    t.checkExpect(maze3.board.get(0).get(0).getEdges(1), new ArrayList<Edge>());
    t.checkExpect(maze3.board.get(0).get(1).getEdges(1), new ArrayList<Edge>());
  }

  // tests for highlight
  void testHighlight(Tester t) {
    init();

    // initial
    t.checkExpect(node0.color, Color.white);
    t.checkExpect(node1.color, Color.white);
    t.checkExpect(node2.color, Color.white);
    t.checkExpect(node3.color, Color.white);

    // modify
    node0.highlight(Color.pink);
    node1.highlight(Color.yellow);
    node2.highlight(Color.blue);
    node3.highlight(Color.green);

    // check again
    t.checkExpect(node0.color, Color.pink);
    t.checkExpect(node1.color, Color.yellow);
    t.checkExpect(node2.color, Color.blue);
    t.checkExpect(node3.color, Color.green);

  }

  // tests for createCells
  // again, it's a little inefficient to create an example cells
  // so I tested them by seeing if there is a change in sizes when we create cells
  //// the size should increase for every cell we create
  void testCreateCells(Tester t) {
    init();

    // initial
    t.checkExpect(this.exampleBoard.size(), 2);

    // modify
    this.mazeWorld1.createCells(1);

    // check again after modify
    t.checkExpect(this.exampleBoard.size(), 3);

    // test cell fields
    t.checkExpect(exampleBoard.get(0).get(0).drawCell(), node0.drawCell());
    t.checkExpect(exampleBoard.get(0).get(1).drawCell(), node1.drawCell());
    t.checkExpect(exampleBoard.get(1).get(0).drawCell(), node2.drawCell());
    t.checkExpect(exampleBoard.get(1).get(1).drawCell(), node3.drawCell());

    t.checkExpect(exampleBoard.get(0).get(0).pos, new Posn(10, 10));
    t.checkExpect(exampleBoard.get(0).get(1).pos, new Posn(30, 10));
    t.checkExpect(exampleBoard.get(1).get(0).pos, new Posn(10, 30));
    t.checkExpect(exampleBoard.get(1).get(1).pos, new Posn(30, 30));

    t.checkExpect(exampleBoard.get(0).get(0).id, 0);
    t.checkExpect(exampleBoard.get(0).get(1).id, 1);
    t.checkExpect(exampleBoard.get(1).get(0).id, 2);
    t.checkExpect(exampleBoard.get(1).get(1).id, 3);

    // initial number of cells in the ArrayList
    Cell nodecell1 = new Cell(8, 50, 50);

    // different grid board
    t.checkExpect(exampleBoard2.size(), 3);

    // modify
    this.mazeWorld2.createCells(2);

    // check again after modify
    t.checkExpect(exampleBoard2.size(), 5);

    // test cell fields
    t.checkExpect(exampleBoard2.get(0).get(0).drawCell(), node0.drawCell());
    t.checkExpect(exampleBoard2.get(0).get(1).drawCell(), node1.drawCell());
    t.checkExpect(exampleBoard2.get(2).get(2).drawCell(), nodecell1.drawCell());

    t.checkExpect(exampleBoard2.get(0).get(0).pos, new Posn(10, 10));
    t.checkExpect(exampleBoard2.get(0).get(1).pos, new Posn(30, 10));
    t.checkExpect(exampleBoard2.get(1).get(0).pos, new Posn(10, 30));
    t.checkExpect(exampleBoard2.get(1).get(1).pos, new Posn(30, 30));

  }

  // test for createEdges
  // since the edges list are too long, I thought it would be more efficient
  // to test the size of the list instead.
  void testCreateEdges(Tester t) {
    init();

    // initial
    t.checkExpect(this.maze3.edgesWorkList, new ArrayList<Edge>());
    t.checkExpect(maze3.board.get(0).get(0).getEdges(60).size(), 1);
    t.checkExpect(maze3.board.get(0).get(1).getEdges(60).size(), 1);
    t.checkExpect(maze3.board.get(1).get(0).getEdges(60).size(), 2);
    t.checkExpect(maze3.board.get(1).get(1).getEdges(60).size(), 2);

    t.checkExpect(maze3.board.get(0).get(0).hasLeft(), false);
    t.checkExpect(maze3.board.get(0).get(1).hasRight(3), true);
    t.checkExpect(maze3.board.get(1).get(0).hasTop(), true);
    t.checkExpect(maze3.board.get(1).get(1).hasBottom(3), true);

    // // null cases:
    // t.checkExpect(this.mazeWorld1.board.get(0).get(1).getEdges(1).size(), 1);
    // t.checkExpect(this.mazeWorld1.board.get(1).get(0).getEdges(0).size(), 1);
    // t.checkExpect(this.mazeWorld1.board.get(1).get(1).getEdges(0).size(), 1);

    // modify
    this.maze3.createEdges(2, new Random(1));

    t.checkExpect(maze3.board.get(0).get(0).hasLeft(), false);
    t.checkExpect(maze3.board.get(0).get(1).hasRight(3), true);
    t.checkExpect(maze3.board.get(1).get(0).hasTop(), true);
    t.checkExpect(maze3.board.get(1).get(1).hasBottom(3), true);

    // check again
    t.checkExpect(maze3.board.get(0).get(0).getEdges(60).size(), 2);
    t.checkExpect(maze3.board.get(0).get(1).getEdges(60).size(), 2);
    t.checkExpect(maze3.board.get(1).get(0).getEdges(60).size(), 3);
    t.checkExpect(maze3.board.get(1).get(1).getEdges(60).size(), 3);

    // modify again
    this.maze3.createEdges(3, new Random(1));

    // check again
    t.checkExpect(maze3.board.get(0).get(0).getEdges(60).size(), 2);
    t.checkExpect(maze3.board.get(0).get(1).getEdges(60).size(), 3);
    t.checkExpect(maze3.board.get(1).get(0).getEdges(60).size(), 3);
    t.checkExpect(maze3.board.get(1).get(1).getEdges(60).size(), 4);
  }

  // test changeVisibility
  void testChangeVisibility(Tester t) {
    init();
    t.checkExpect(node1.color, Color.white);
    t.checkExpect(node2.color, Color.white);
    t.checkExpect(node3.color, Color.white);
    t.checkExpect(node4.color, Color.white);
    node5.highlight(Color.BLUE);

    // modify
    node1.changeVisibility();
    node2.changeVisibility();
    node3.changeVisibility();
    node4.changeVisibility();
    node5.changeVisibility();

    // check again
    t.checkExpect(node1.color, Color.gray);
    t.checkExpect(node2.color, Color.gray);
    t.checkExpect(node3.color, Color.gray);
    t.checkExpect(node4.color, Color.gray);
    t.checkExpect(node5.color, Color.white);
  }

  // test for makeScene
  // again with the random weight issue. Not sure how to test
  void testMakeScene(Tester t) {
    init();

    // initial
    MazeWorld mazeWorld = new MazeWorld(2, new Random(1));
    WorldScene background = new WorldScene(sceneSize, sceneSize);
    t.checkExpect(background, new WorldScene(520, 520));

    background.placeImageXY(node0.drawCell(), 10, 10);
    background.placeImageXY(node1.drawCell(), 30, 10);
    background.placeImageXY(node2.drawCell(), 10, 30);
    background.placeImageXY(node3.drawCell(), 30, 30);
    background.placeImageXY(new RectangleImage(1, 20, OutlineMode.SOLID, Color.blue), 20, 10);
    background.placeImageXY(new RectangleImage(80, 80, OutlineMode.OUTLINE, Color.black), 0, 0);
    background.placeImageXY(new RectangleImage(200, 50, OutlineMode.SOLID, Color.white), 260, 500);
    background.placeImageXY(new TextImage("Wrong moves: 0", 20, FontStyle.REGULAR, Color.black),
        260, 510);

    t.checkExpect(mazeWorld.background, background);

    // another grid board size
    init();
    MazeWorld mazeWorld2 = new MazeWorld(3, new Random(1));
    WorldScene background2 = new WorldScene(sceneSize, sceneSize);
    t.checkExpect(background2, new WorldScene(520, 520));

    background2.placeImageXY(nodeA.drawCell(), 10, 10);
    background2.placeImageXY(nodeD.drawCell(), 30, 10);
    background2.placeImageXY(nodeG.drawCell(), 50, 10);
    background2.placeImageXY(nodeB.drawCell(), 10, 30);
    background2.placeImageXY(nodeE.drawCell(), 30, 30);
    background2.placeImageXY(nodeH.drawCell(), 50, 30);
    background2.placeImageXY(nodeC.drawCell(), 10, 50);
    background2.placeImageXY(nodeF.drawCell(), 30, 50);
    background2.placeImageXY(nodeI.drawCell(), 50, 50);
    background2.placeImageXY(new RectangleImage(1, 20, OutlineMode.SOLID, Color.blue), 20, 10);
    background2.placeImageXY(new RectangleImage(1, 20, OutlineMode.SOLID, Color.blue), 40, 10);
    background2.placeImageXY(new RectangleImage(1, 20, OutlineMode.SOLID, Color.blue), 20, 30);
    background2.placeImageXY(new RectangleImage(20, 1, OutlineMode.SOLID, Color.blue), 30, 40);
    background2.placeImageXY(new RectangleImage(120, 120, OutlineMode.OUTLINE, Color.black), 0, 0);
    background2.placeImageXY(new RectangleImage(200, 50, OutlineMode.SOLID, Color.white), 260, 500);
    background2.placeImageXY(new TextImage("Wrong moves: 0", 20, FontStyle.REGULAR, Color.black),
        260, 510);
    t.checkExpect(mazeWorld2.background, background2);
  }

  // tests for darkHighLightAll
  void testDarkHighLightAll(Tester t) {
    init();

    reconstructedList.add(1);
    reconstructedList.add(2);
    reconstructedList.add(3);
    // initial
    t.checkExpect(mazeWorld1.getCellFromId(1).color, Color.white);
    t.checkExpect(mazeWorld1.getCellFromId(2).color, Color.white);

    // modify
    mazeWorld1.darkHighLightAll(reconstructedList);

    // check again
    t.checkExpect(mazeWorld1.getCellFromId(1).color, Color.black);
    t.checkExpect(mazeWorld1.getCellFromId(2).color, Color.black);
  }

  // test topConnected, bottomConnected, leftConnected, rightConnected
  // made the test method void to see more visually how this method works.
  void testConnected(Tester t) {
    init();

    exampleBoard2.get(0).get(0).setRight(edge5);
    exampleBoard2.get(0).get(0).setBottom(edge7);

    exampleBoard2.get(0).get(1).setLeft(edge5);
    exampleBoard2.get(0).get(1).setBottom(edge8);

    exampleBoard2.get(1).get(0).setTop(edge7);
    exampleBoard2.get(1).get(0).setRight(edge8);

    exampleBoard2.get(1).get(1).setTop(edge6);
    exampleBoard2.get(1).get(1).setLeft(edge7);

    t.checkExpect(exampleBoard2.get(0).get(0).rightConnected(), true);
    t.checkExpect(exampleBoard2.get(0).get(0).bottomConnected(), true);

    t.checkExpect(exampleBoard2.get(0).get(1).leftConnected(), true);
    t.checkExpect(exampleBoard2.get(0).get(1).bottomConnected(), true);

    t.checkExpect(exampleBoard2.get(1).get(0).topConnected(), true);
    t.checkExpect(exampleBoard2.get(1).get(0).rightConnected(), true);

    t.checkExpect(exampleBoard2.get(1).get(1).topConnected(), true);
    t.checkExpect(exampleBoard2.get(1).get(1).leftConnected(), true);

    // modify
    exampleBoard2.get(0).get(0).right.notConnected();
    exampleBoard2.get(0).get(0).bottom.notConnected();

    exampleBoard2.get(0).get(1).left.notConnected();
    exampleBoard2.get(0).get(1).bottom.notConnected();

    exampleBoard2.get(1).get(0).top.notConnected();
    exampleBoard2.get(1).get(0).right.notConnected();

    exampleBoard2.get(1).get(1).top.notConnected();
    exampleBoard2.get(1).get(1).left.notConnected();

    // check again
    t.checkExpect(exampleBoard2.get(0).get(0).rightConnected(), false);
    t.checkExpect(exampleBoard2.get(0).get(0).bottomConnected(), false);

    t.checkExpect(exampleBoard2.get(0).get(1).leftConnected(), false);
    t.checkExpect(exampleBoard2.get(0).get(1).bottomConnected(), false);

    t.checkExpect(exampleBoard2.get(1).get(0).topConnected(), false);
    t.checkExpect(exampleBoard2.get(1).get(0).rightConnected(), false);

    t.checkExpect(exampleBoard2.get(1).get(1).topConnected(), false);
    t.checkExpect(exampleBoard2.get(1).get(1).leftConnected(), false);
  }

  // test get cell from id
  boolean testGetCellFromId(Tester t) {
    init();

    return t.checkExpect(maze3.getCellFromId(0), maze3.board.get(0).get(0))
        && t.checkExpect(maze3.getCellFromId(3), maze3.board.get(1).get(0))
        && t.checkExpect(maze3.getCellFromId(7), maze3.board.get(2).get(1))

        && t.checkExpect(mazeWorld1.getCellFromId(0), exampleBoard.get(0).get(0))
        && t.checkExpect(mazeWorld1.getCellFromId(1), exampleBoard.get(0).get(1))
        && t.checkExpect(mazeWorld1.getCellFromId(2), exampleBoard.get(1).get(0));
  }

  // test find
  void testFind(Tester t) {
    init();
    // test find
    g.representatives.put(4, 4);
    g.representatives.put(5, 5);
    t.checkExpect(workList.get(0), new Edge(node4, node5, 1));
    t.checkExpect(g.representatives.get(4), 4);

    t.checkExpect(g.find(g.representatives, 4), 4);
    t.checkExpect(g.find(g.representatives, 5), 5);

    g.union(g.representatives, 4, 5);
    t.checkExpect(g.find(g.representatives, 5), 4);

    // test find with kruskal
    init();
    g3.workList.sort((e1, e2) -> e1.compareTo(e2));
    g3.kruskal();

    t.checkExpect(g3.find(g3.representatives, 4), 4);
    t.checkExpect(g3.find(g3.representatives, 5), 4);
    t.checkExpect(g3.find(g3.representatives, 6), 4);
    t.checkExpect(g3.find(g3.representatives, 7), 4);

  }

  // test union
  void testUnion(Tester t) {
    init();

    g.representatives.put(4, 4);
    g.representatives.put(5, 5);
    g.representatives.put(6, 6);
    t.checkExpect(g.representatives.get(4), 4);
    t.checkExpect(g.representatives.get(5), 5);

    g.union(g.representatives, 4, 5);

    t.checkExpect(g.representatives.get(4), 4);
    t.checkExpect(g.representatives.get(5), 4);
    t.checkExpect(g.representatives.get(6), 6);

    init();
    g.representatives.put(1, 1);
    g.representatives.put(2, 2);
    g.representatives.put(3, 3);
    t.checkExpect(g.representatives.get(1), 1);
    t.checkExpect(g.representatives.get(2), 2);

    g.union(g.representatives, 1, 2);

    t.checkExpect(g.representatives.get(1), 1);
    t.checkExpect(g.representatives.get(2), 1);
    t.checkExpect(g.representatives.get(3), 3);

  }

  // test for kruskal
  void testKruskal(Tester t) {
    init();
    ArrayList<Edge> gKruskal = new ArrayList<Edge>(Arrays.asList(workList.get(0), workList.get(1)));
    t.checkExpect(g.kruskal(), gKruskal);

    // test mapping?
    // t.checkExpect(g.representatives, null);

    t.checkExpect(workList2.size(), 4);
    workList2.sort((e1, e2) -> e1.compareTo(e2));
    t.checkExpect(g2.kruskal(), new ArrayList<Edge>(Arrays.asList(new Edge(node4, node7, 1),
        new Edge(node7, node6, 2), new Edge(node5, node6, 3))));

    ArrayList<Edge> g3Kruskal = new ArrayList<Edge>();
    g3Kruskal.add(workList3.get(0));
    g3Kruskal.add(workList3.get(2));
    g3Kruskal.add(workList3.get(1));

    workList3.sort((e1, e2) -> e1.compareTo(e2));
    t.checkExpect(g3.kruskal(), g3Kruskal);

    workList4.sort((e1, e2) -> e1.compareTo(e2));
    t.checkExpect(g4.kruskal().size(), 8);
  }

  // test MazeWorld
  void testMazeWorld(Tester t) {
    init();
    MazeWorld maze1 = new MazeWorld(3, new Random());
    maze1.bigBang(maze1.SCENE_SIZE, maze1.SCENE_SIZE, .01);

    t.checkExpect(maze1.edgesWorkList.size(), 0);
    t.checkExpect(maze1.edgesWorkList, new ArrayList<Edge>());
    t.checkExpect(maze1.edgesAll.size(), 12);
    t.checkExpect(maze1.edgesSpanningTree.size(), 8);

    MazeWorld maze2 = new MazeWorld(10, new Random(0));
    maze2.bigBang(maze2.SCENE_SIZE, maze2.SCENE_SIZE, .01);
    t.checkExpect(maze2.edgesSpanningTree.size(), 99);

    // testing maze
    MazeWorld maze3 = new MazeWorld(3, new Random(1));
    maze3.bigBang(maze3.SCENE_SIZE, maze3.SCENE_SIZE, .01);
    t.checkExpect(maze3.edgesSpanningTree.size(), 8);

    MazeWorld maze4 = new MazeWorld(15, new Random());
    maze4.bigBang(maze4.SCENE_SIZE, maze4.SCENE_SIZE, .01);

  }

  // test for compareTo
  boolean testCompareTo(Tester t) {
    init();
    return t.checkExpect(workList.get(0).compareTo(workList.get(1)), -1)
        && t.checkExpect(workList.get(0).compareTo(workList.get(2)), -2)
        && t.checkExpect(workList.get(0).compareTo(workList.get(0)), 0)

        && t.checkExpect(workList2.get(0).compareTo(workList2.get(3)), 3)
        && t.checkExpect(workList2.get(1).compareTo(workList2.get(3)), 2)
        && t.checkExpect(workList2.get(2).compareTo(workList2.get(3)), 1)
        && t.checkExpect(workList2.get(3).compareTo(workList2.get(3)), 0);

  }

  // test for sort
  void testSort(Tester t) {
    init();
    workList.sort((e1, e2) -> e1.compareTo(e2));
    t.checkExpect(workList, new ArrayList<Edge>(Arrays.asList(edge45, edge56, edge64)));

    workList2.sort((e1, e2) -> e1.compareTo(e2));
    t.checkExpect(workList2, new ArrayList<Edge>(Arrays.asList(new Edge(node4, node7, 1),
        new Edge(node7, node6, 2), new Edge(node5, node6, 3), new Edge(node4, node5, 4))));

  }

  // tests for drawEdge
  boolean testDrawEdge(Tester t) {
    init();

    return t.checkExpect(this.edge1.drawEdgeVertical(),
        new RectangleImage(1, 20, OutlineMode.SOLID, Color.blue))
        && t.checkExpect(this.edge2.drawEdgeVertical(),
            new RectangleImage(1, 20, OutlineMode.SOLID, Color.blue))
        && t.checkExpect(this.edge3.drawEdgeVertical(),
            new RectangleImage(1, 20, OutlineMode.SOLID, Color.blue))
        && t.checkExpect(this.edge4.drawEdgeVertical(),
            new RectangleImage(1, 20, OutlineMode.SOLID, Color.blue))

        && t.checkExpect(this.edge1.drawEdgeHorizontal(),
            new RectangleImage(20, 1, OutlineMode.SOLID, Color.blue))
        && t.checkExpect(this.edge2.drawEdgeHorizontal(),
            new RectangleImage(20, 1, OutlineMode.SOLID, Color.blue))
        && t.checkExpect(this.edge3.drawEdgeHorizontal(),
            new RectangleImage(20, 1, OutlineMode.SOLID, Color.blue))
        && t.checkExpect(this.edge4.drawEdgeHorizontal(),
            new RectangleImage(20, 1, OutlineMode.SOLID, Color.blue));

  }

  // test for isConnected
  // made it void to test for false cases.
  // by default, edge will always be connected (true),
  // unless disconnected(false) with notConnected
  void testIsConnected(Tester t) {
    init();
    t.checkExpect(edge1.isConnected(), true);
    t.checkExpect(edge2.isConnected(), true);
    t.checkExpect(edge3.isConnected(), true);
    t.checkExpect(edge4.isConnected(), true);

    edge1.notConnected();
    edge2.notConnected();
    edge3.notConnected();
    edge4.notConnected();

    t.checkExpect(edge1.isConnected(), false);
    t.checkExpect(edge2.isConnected(), false);
    t.checkExpect(edge3.isConnected(), false);
    t.checkExpect(edge4.isConnected(), false);
  }

  // test for setConnected
  void testSetConnected(Tester t) {
    init();
    // initial
    t.checkExpect(g.kruskal().get(0).isConnected(), true);
    t.checkExpect(g.kruskal().get(1).isConnected(), true);

    // modify
    g.kruskal().get(0).setConnected(false);
    g.kruskal().get(1).setConnected(false);

    // check
    t.checkExpect(g.kruskal().get(0).isConnected(), false);
    t.checkExpect(g.kruskal().get(1).isConnected(), false);

    // modify again
    g.kruskal().get(0).setConnected(true);
    g.kruskal().get(1).setConnected(true);

    // check again
    t.checkExpect(g.kruskal().get(0).isConnected(), true);
    t.checkExpect(g.kruskal().get(1).isConnected(), true);

  }

  // test for notConnected
  void testNotConnected(Tester t) {
    init();

    // initial
    t.checkExpect(g.kruskal().get(0).isConnected(), true);
    t.checkExpect(g.kruskal().get(1).isConnected(), true);

    // modify
    g.kruskal().get(0).notConnected();
    g.kruskal().get(1).notConnected();

    // check again
    t.checkExpect(g.kruskal().get(0).isConnected(), false);
    t.checkExpect(g.kruskal().get(1).isConnected(), false);

  }

  // test for getOtherNode
  boolean testGetOtherNode(Tester t) {
    init();
    return t.checkExpect(edge1.getOtherNode(node0), node4)
        && t.checkExpect(edge2.getOtherNode(node0), node5)
        && t.checkExpect(edge3.getOtherNode(node0), node6)
        && t.checkExpect(edge1.getOtherNode(node1), node4)
        && t.checkExpect(edge2.getOtherNode(node1), node5)
        && t.checkExpect(edge3.getOtherNode(node1), node6);
  }

  // test addEdge
  void testAddEdge(Tester t) {
    init();
    // initial
    t.checkExpect(g3.edgesInTree, new ArrayList<Edge>());
    t.checkExpect(g3.edgesInTree.size(), 0);
    t.checkExpect(g4.edgesInTree, new ArrayList<Edge>());
    t.checkExpect(g4.edgesInTree.size(), 0);

    Random r = new Random(1);
    // modify
    // adding edges to edgesInTree
    g3.addEdge(node0, node0, r);
    g3.addEdge(node1, node1, r);
    g3.addEdge(node2, node2, r);
    g3.addEdge(node3, node3, r);

    g4.addEdge(node4, node4, r);
    g4.addEdge(node5, node5, r);
    g4.addEdge(node6, node6, r);
    g4.addEdge(node7, node7, r);
    g4.addEdge(node4, node4, r);

    // check again
    t.checkExpect(g3.edgesInTree.size(), 4);
    t.checkExpect(g4.edgesInTree.size(), 5);

  }

  // test path
  void testUpdatePath(Tester t) {
    init();

    // hasPath(Cell source, Cell destination, ICollection<Cell> list, int length)

    ArrayList<Cell> ws = new ArrayList<Cell>();
    ws.add(node4);
    ArrayList<Cell> seen = new ArrayList<Cell>();
    HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();

    node4.setRight(g3node45);
    node5.setLeft(g3node45);

    node5.setBottom(g3node56);
    node6.setTop(g3node56);

    node6.setLeft(g3node67);
    node7.setRight(g3node67);

    node7.setTop(g3node47);
    node4.setBottom(g3node47);

    g3.kruskal();

    t.checkExpect(ws.size(), 1);
    // 2x2 complex
    t.checkExpect(g3.search(node4, node7, ws, seen, map, 2, ws.size() - 1), true);
    t.checkExpect(g3.search(node4, node7, ws, seen, map, 2, ws.size() - 1), true);
    t.checkExpect(g3.search(node4, node7, ws, seen, map, 2, ws.size() - 1), true);
    t.checkExpect(g3.search(node4, node7, ws, seen, map, 2, ws.size() - 1), false);

    // t.checkExpect(map, null);
    t.checkExpect(g3.reconstruct(map, node7.id), new ArrayList<Integer>(Arrays.asList(4, 5, 6, 7)));
  }

  // test reconstruct and update path
  void testReconstruct(Tester t) {
    init();
    Cell maze3Cell1 = maze3.board.get(0).get(0);
    Cell maze3CellLast = maze3.board.get(2).get(2);

    ArrayList<Cell> ws = new ArrayList<Cell>();
    ws.add(maze3Cell1);
    ArrayList<Cell> seen = new ArrayList<Cell>();
    HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();

    t.checkExpect(
        maze3.spanningTree.search(maze3Cell1, maze3CellLast, ws, seen, map, 3, ws.size() - 1),
        true);
    t.checkExpect(
        maze3.spanningTree.search(maze3Cell1, maze3CellLast, ws, seen, map, 3, ws.size() - 1),
        true);
    t.checkExpect(
        maze3.spanningTree.search(maze3Cell1, maze3CellLast, ws, seen, map, 3, ws.size() - 1),
        true);
    t.checkExpect(
        maze3.spanningTree.search(maze3Cell1, maze3CellLast, ws, seen, map, 3, ws.size() - 1),
        true);
    t.checkExpect(
        maze3.spanningTree.search(maze3Cell1, maze3CellLast, ws, seen, map, 3, ws.size() - 1),
        false);

    t.checkExpect(maze3.spanningTree.reconstruct(map, maze3CellLast.id),
        new ArrayList<Integer>(Arrays.asList(0, 3, 6, 7, 8)));
  }

  // test search
  void testSearch(Tester t) {
    init();

    // hasPath(Cell source, Cell destination, ICollection<Cell> list, int length)

    ArrayList<Cell> ws = new ArrayList<Cell>();
    ws.add(node4);
    ArrayList<Cell> seen = new ArrayList<Cell>();
    HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();

    node4.setRight(g3node45);
    node5.setLeft(g3node45);

    node5.setBottom(g3node56);
    node6.setTop(g3node56);

    node6.setLeft(g3node67);
    node7.setRight(g3node67);

    node7.setTop(g3node47);
    node4.setBottom(g3node47);

    g3.kruskal();

    t.checkExpect(ws.size(), 1);
    // 2x2 complex
    t.checkExpect(g3.search(node4, node7, ws, seen, map, 2, ws.size() - 1), true);
    t.checkExpect(g3.search(node4, node7, ws, seen, map, 2, ws.size() - 1), true);
    t.checkExpect(g3.search(node4, node7, ws, seen, map, 2, ws.size() - 1), true);
    t.checkExpect(g3.search(node4, node7, ws, seen, map, 2, ws.size() - 1), false);

    // t.checkExpect(map, null);
    t.checkExpect(g3.reconstruct(map, node7.id), new ArrayList<Integer>(Arrays.asList(4, 5, 6, 7)));
  }

  // test moveUp, moveDown, moveLeft, moveRight
  void testMove(Tester t) {
    init();
    // initial
    t.checkExpect(maze3.currentCell.color, Color.white);
    // modify
    maze3.currentCell = maze3.spanningTree.moveDown(maze3.currentCell, 3);
    maze3.currentCell = maze3.spanningTree.moveDown(maze3.currentCell, 3);
    maze3.currentCell = maze3.spanningTree.moveDown(maze3.currentCell, 3);
    maze3.currentCell = maze3.spanningTree.moveRight(maze3.currentCell, 3);
    maze3.currentCell = maze3.spanningTree.moveRight(maze3.currentCell, 3);
    maze3.currentCell = maze3.spanningTree.moveRight(maze3.currentCell, 3);
    maze3.currentCell = maze3.spanningTree.moveUp(maze3.currentCell);
    maze3.currentCell = maze3.spanningTree.moveUp(maze3.currentCell);
    maze3.currentCell = maze3.spanningTree.moveLeft(maze3.currentCell);
    maze3.currentCell = maze3.spanningTree.moveUp(maze3.currentCell);
    maze3.currentCell = maze3.spanningTree.moveUp(maze3.currentCell);
    maze3.currentCell = maze3.spanningTree.moveDown(maze3.currentCell, 3);
    maze3.currentCell = maze3.spanningTree.moveDown(maze3.currentCell, 3);
    maze3.currentCell = maze3.spanningTree.moveRight(maze3.currentCell, 3);
    maze3.currentCell = maze3.spanningTree.moveUp(maze3.currentCell);
    maze3.currentCell = maze3.spanningTree.moveUp(maze3.currentCell);
    // check again
    t.checkExpect(maze3.board.get(0).get(0).color, Color.gray);
    t.checkExpect(maze3.currentCell.color, Color.red);

  }

  // test onTick
  void testOnTick(Tester t) {
    init();

    // initial
    maze3.onTick();
    t.checkExpect(maze3.animate, false);

    // modify
    maze3.onKeyEvent("b"); // sets animate to true
    maze3.onTick();
    t.checkExpect(maze3.animate, true);

    maze3.animate = false;

    // check again
    maze3.onTick();
    t.checkExpect(maze3.animate, false);
    maze3.onTick();
    t.checkExpect(maze3.animate, false);

    init();
    maze3.onKeyEvent("b");
    maze3.onTick();
    t.checkExpect(maze3.cellFirst.color, Color.gray);
    maze3.onTick();
    t.checkExpect(maze3.cellFirst.color, Color.gray);

    maze3.onTick();
    t.checkExpect(maze3.cellFirst.color, Color.gray);
    maze3.onTick();
    maze3.onTick();
    maze3.onTick();
    t.checkExpect(maze3.cellFirst.color, Color.black);
    t.checkExpect(maze3.cellLast.color, Color.black);

  }

  // test for initialize
  void testInitialize(Tester t) {
    init();
    MazeWorld maze4 = new MazeWorld(4, new Random(1));

    t.checkExpect(maze4.board.size(), 4);
    t.checkExpect(maze4.edgesWorkList, new ArrayList<Edge>());
    t.checkExpect(maze4.edgesAll.size(), 24);
    t.checkExpect(maze4.edgesSpanningTree.size(), 15);
    // t.checkExpect(maze4.spanningTree.size(), new Graph(new ArrayList<Edge>(),
    // maze4.edgesWorkList));
    t.checkExpect(maze4.searchWorkList.size(), 1);
    t.checkExpect(maze4.seenList, new ArrayList<Cell>());
    t.checkExpect(maze4.searchMap, new HashMap<Integer, Integer>());
    t.checkExpect(maze4.animate, false);
    t.checkExpect(maze4.r, new Random());
    t.checkExpect(maze4.length, 4);
    t.checkExpect(maze4.cellFirst, maze4.board.get(0).get(0));
    t.checkExpect(maze4.currentCell, maze4.board.get(0).get(0));
    t.checkExpect(maze4.cellLast, maze4.board.get(maze4.length - 1).get(maze4.length - 1));
    maze4.createCells(maze4.length);
    maze4.createEdges(maze4.length, maze4.r);
    t.checkExpect(maze4.wrongMoves, 0);
    t.checkExpect(maze4.win, false);

  }

  // test placeEdge
  void testPlaceEdge(Tester t) {
    init();
    WorldScene background = new WorldScene(500, 500);
    WorldScene backgroundTest = new WorldScene(500, 500);
    Edge e200 = new Edge(node4, node5, new Random(1));
    Edge e300 = new Edge(node5, node6, new Random(1));

    e200.placeEdgeHorizontal(background);
    backgroundTest.placeImageXY(e200.drawEdgeHorizontal(), 10, 20);
    t.checkExpect(background, backgroundTest);

    background = new WorldScene(500, 500);
    backgroundTest = new WorldScene(500, 500);

    e200.placeEdgeVertical(background);
    backgroundTest.placeImageXY(e200.drawEdgeVertical(), 20, 10);
    t.checkExpect(background, backgroundTest);

    background = new WorldScene(500, 500);
    backgroundTest = new WorldScene(500, 500);
    e300.placeEdgeVertical(background);
    backgroundTest.placeImageXY(e300.drawEdgeVertical(), 40, 10);

    t.checkExpect(background, backgroundTest);

    background = new WorldScene(500, 500);
    backgroundTest = new WorldScene(500, 500);
    e300.placeEdgeHorizontal(background);
    backgroundTest.placeImageXY(e300.drawEdgeHorizontal(), 30, 20);
    t.checkExpect(background, backgroundTest);
  }

  // test onKeyEvent
  void testOnKeyEvent(Tester t) {
    init();
    maze3.onKeyEvent("b");
    t.checkExpect(maze3.animate, true);

    init();
    maze3.onKeyEvent("d");
    t.checkExpect(maze3.animate, true);

    init();
    maze3.onKeyEvent("r");
    t.checkExpect(maze3.animate, false);
    t.checkExpect(maze3.board.size(), 3);
    t.checkExpect(maze3.edgesWorkList, new ArrayList<Edge>());
    t.checkExpect(maze3.edgesAll.size(), 12);
    t.checkExpect(maze3.edgesSpanningTree.size(), 8);
    t.checkExpect(maze3.searchWorkList.size(), 1);
    t.checkExpect(maze3.seenList, new ArrayList<Cell>());

    init();
    maze3.onKeyEvent("b");
    maze3.onTick();
    maze3.onTick();
    maze3.onTick();
    maze3.onTick();
    t.checkExpect(maze3.cellFirst.color, Color.gray);

    maze3.onKeyEvent("v");
    t.checkExpect(maze3.cellFirst.color, Color.gray);

    init();
    maze3.onKeyEvent("down");
    t.checkExpect(maze3.currentCell, maze3.board.get(1).get(0));

  }

}