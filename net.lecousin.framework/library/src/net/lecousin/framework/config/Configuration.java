package net.lecousin.framework.config;

public abstract class Configuration
{
  public abstract ConfigSection get_root(String namespace);
  
  public final ConfigSection get_section(String namespace, String section_fullname) {
    ConfigSection cs = get_root(namespace);
    if (cs == null)
      return null;
    return cs.get_subsection_by_fullname(section_fullname);
  }
  
  public final String get_parameter(String namespace, String parameter_fullname) {
    ConfigSection cs = get_root(namespace);
    if (cs == null)
      return null;
    return cs.get_parameter_by_fullname(parameter_fullname);
  }
  
  private static Configuration instance = null;
  public static void initialize(Configuration instance) { Configuration.instance = instance; }
  public static Configuration get_instance() { return instance; }
}
