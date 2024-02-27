import java.awt.Color;
import java.util.Random;

import javalib.impworld.WorldScene;
import javalib.worldimages.OutlineMode;
import javalib.worldimages.RectangleImage;
import javalib.worldimages.WorldImage;

// represent an Edge class
// Edge object that has two nodes,
// and a weight
class Edge {
  Cell n1;
  Cell n2;
  boolean connected;

  int weight;
  // Random r = new Random();

  // actual constructor
  Edge(Cell n1, Cell n2, Random r) {
    this.n1 = n1;
    this.n2 = n2;
    this.weight = r.nextInt(10000);
    this.connected = true;
  }

  // this is for testing
  Edge(Cell n1, Cell n2, int weight) {
    this.n1 = n1;
    this.n2 = n2;
    this.weight = weight;
    this.connected = true;
  }

  // sets connection of edge to false
  void notConnected() {
    this.connected = false;
  }

  // compares to edges based on weight
  public int compareTo(Edge other) {
    return this.weight - other.weight;
  }

  // EFFECT: sets the edge to connected
  public void setConnected(boolean b) {
    this.connected = b;
  }

  // draws the edge
  public WorldImage drawEdgeVertical() {
    // if not connected, draw wall
    return new RectangleImage(1, 20, OutlineMode.SOLID, Color.blue);
  }

  // draws the edge horizontal
  public WorldImage drawEdgeHorizontal() {
    // if not connected, draw wall
    return new RectangleImage(20, 1, OutlineMode.SOLID, Color.blue);
  }

  // is the edge connected?
  public boolean isConnected() {
    return this.connected;
  }

  // given a node, returns the other node
  public Cell getOtherNode(Cell next) {
    if (this.n1.equals(next)) {
      return n2;
    }
    else {
      return n1;
    }
  }

  // place edge on given background horizontal
  // EFFECT: background gets this edge placed onto it
  public void placeEdgeHorizontal(WorldScene background) {
    background.placeImageXY(this.drawEdgeHorizontal(), n1.pos.x, n1.pos.y + 10);
  }

  // place edge on given background horizontal
  // EFFECT: background gets this edge placed onto it
  public void placeEdgeVertical(WorldScene background) {
    background.placeImageXY(this.drawEdgeVertical(), n1.pos.x + 10, n1.pos.y);
  }

}
