package net.lecousin.framework.config;

import net.lecousin.framework.tree.TreeAccessor;

public abstract class ConfigSection
{
  public static final String CONFIG_SECTION_SEPARATOR = "/";
  public static final String PARAMETER_SEPARATOR = ":";
  
  public static class SectionProvider implements TreeAccessor.NodeProvider<ConfigSection> {
    public String get_node_fullname_separator() { return CONFIG_SECTION_SEPARATOR; }
    public ConfigSection get_node(ConfigSection parent, String name) { return parent.get_subsection(name); }
    
    private SectionProvider() { /* singleton */ }
    private static SectionProvider instance = null;
    private static SectionProvider get_instance() {
      if (instance == null) instance = new SectionProvider();
      return instance;
    }
  }
  public static class ParameterProvider implements TreeAccessor.LeafProvider<ConfigSection, String> {
    public String get_node_leaf_fullname_separator() { return PARAMETER_SEPARATOR; }
    public String get_leaf(ConfigSection parent, String name) { return parent.get_parameter(name); }
    
    private ParameterProvider() { /* singleton */ }
    private static ParameterProvider instance = null;
    private static ParameterProvider get_instance() {
      if (instance == null) instance = new ParameterProvider();
      return instance;
    }
  }

  public abstract ConfigSection get_subsection(String name);
  
  public final ConfigSection get_subsection_by_fullname(String fullname) 
  {
    return TreeAccessor.get_node_by_fullname(this, fullname, SectionProvider.get_instance());
  }
  
  public abstract String get_parameter(String name);
  
  public final String get_parameter_by_fullname(String fullname) {
    return TreeAccessor.get_leaf_by_fullname(this, fullname, SectionProvider.get_instance(), ParameterProvider.get_instance());
  }
}
