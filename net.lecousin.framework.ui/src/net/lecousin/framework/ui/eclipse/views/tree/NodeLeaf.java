package net.lecousin.framework.ui.eclipse.views.tree;

public abstract class NodeLeaf extends Node
{

  public NodeLeaf(Node parent) {
    super(parent);
  }
  
  @Override
  public Node[] getChildren()
  {
    return null;
  }
  
  @Override
  public boolean hasChildren()
  {
    return false;
  }
  
}
