package net.lecousin.framework.tree;

public class TreeAccessor
{
  private TreeAccessor() { /* instantiation not allowed */ }
  
  public static interface NodeProvider<NodeType>
  {
    public String get_node_fullname_separator();
    public NodeType get_node(NodeType parent, String name);
  }
  
  public static interface LeafProvider<NodeType, LeafType>
  {
    public String get_node_leaf_fullname_separator();
    public LeafType get_leaf(NodeType parent, String name);
  }
  
  public static <NodeType> NodeType get_node_by_fullname(NodeType root, String fullname, NodeProvider<NodeType> node_provider) 
  {
    String sep = node_provider.get_node_fullname_separator();
    int i = fullname.indexOf(sep);
    if (i < 0)
      return node_provider.get_node(root, fullname);
    NodeType node = node_provider.get_node(root, fullname.substring(0, i));
    if (node == null)
      return null;
    return get_node_by_fullname(node, fullname.substring(i + sep.length()), node_provider);
  }
  
  public static <NodeType, LeafType> LeafType get_leaf_by_fullname(NodeType root, String fullname, NodeProvider<NodeType> node_provider, LeafProvider<NodeType,LeafType> leaf_provider) {
    String sep = leaf_provider.get_node_leaf_fullname_separator();
    int i = fullname.lastIndexOf(sep);
    if (i < 0)
      return leaf_provider.get_leaf(root, fullname);
    NodeType parent =get_node_by_fullname(root, fullname.substring(0, i), node_provider);
    if (parent == null)
      return null;
    return leaf_provider.get_leaf(parent, fullname.substring(i + sep.length()));
  }
}
