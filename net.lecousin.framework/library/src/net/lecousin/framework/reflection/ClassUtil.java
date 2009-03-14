/**
 * Project:  GFramework Basics
 * Package:  net.lecousin.framework.basics.reflection
 * Filename: ClassUtils.java
 * Created on 3 janv. 2006 16:26:33
 * Author: Guillaume LE COUSIN
 */
package net.lecousin.framework.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author Guillaume LE COUSIN
 *
 */
public class ClassUtil
{
  private ClassUtil() { /* instantiation not allowed */ }
  
  @SuppressWarnings("unchecked")
  public static <T> Class<? extends T> load(Collection<ClassLoader> loaders, String class_name, Class<T> base_class)
  {
    for (Iterator<ClassLoader> it = loaders.iterator(); it.hasNext(); )
    {
      try
      {
        Class<?> cl = it.next().loadClass(class_name);
        if (!base_class.isAssignableFrom(cl))
          return null;
        return (Class<? extends T>)cl;
      } catch (ClassNotFoundException e) { continue; }
    }
    return null;
  }
  
  @SuppressWarnings("unchecked")
  public static Constructor get_valid_constructor(Class cl, Class[] types)
  {
    Constructor[] ctors = cl.getConstructors();
    for (int i = 0; i < ctors.length; ++i)
    {
      Class<?>[] classes = ctors[i].getParameterTypes();
      if (classes.length != types.length)
        continue;
      boolean ok = true;
      for (int j = 0; ok && j < classes.length; ++j)
        if (!classes[j].isAssignableFrom(types[j]))
          ok = false;
      if (ok)
        return ctors[i];
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public static Constructor get_valid_constructor_inverse(Class cl, Class<?>[] types)
  {
    Constructor[] ctors = cl.getConstructors();
    for (int i = 0; i < ctors.length; ++i)
    {
      Class<?>[] classes = ctors[i].getParameterTypes();
      if (classes.length != types.length)
        continue;
      boolean ok = true;
      for (int j = 0; ok && j < classes.length; ++j)
        if (!types[j].isAssignableFrom(classes[j]))
          ok = false;
      if (ok)
        return ctors[i];
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public static Constructor get_valid_constructor(Class cl, Object[] params)
  {
    Constructor[] ctors = cl.getConstructors();
    for (int i = 0; i < ctors.length; ++i)
    {
      Class<?>[] classes = ctors[i].getParameterTypes();
      if (classes.length != params.length)
        continue;
      boolean ok = true;
      for (int j = 0; ok && j < classes.length; ++j)
        if (!classes[j].isAssignableFrom(params[j].getClass()))
          ok = false;
      if (ok)
        return ctors[i];
    }
    return null;
  }
  
  @SuppressWarnings("unchecked")
  public static String get_no_valid_constructor_error_message(String object_type_name, Class type, Object[] params)
  {
    String msg = "Cannot find appropriate constructor for " + object_type_name + " type '" + type.getName() + "':\r\nRequires constructor accepting the following parameters:\r\n(";
    for (int i = 0; i < params.length; ++i)
    {
      if (i > 0) msg += ", ";
      msg += params[i].getClass().getName();
    }
    msg += ")";
    return msg;
  }
  
  @SuppressWarnings("unchecked")
  public static <T> T new_instance(T source, Object[] ctor_params)
  throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException
  {
    return new_instance((Class<T>)source.getClass(), ctor_params);
  }
  
  public static <T> T new_instance_no_exception(T source, Object[] ctor_params)
  {
    try
    {
      return new_instance(source, ctor_params);
    } catch (Exception e)
    { return null; }
  }
  
  @SuppressWarnings("unchecked")
  public static <T> T new_instance(Class<T> clazz, Object[] ctor_params)
  throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException
  {
    Constructor ctor = get_valid_constructor(clazz, ctor_params);
    if (ctor == null)
    {
      String params = "";
      for (int i = 0; i < ctor_params.length; ++i)
      {
        if (i > 0) params += ", ";
        params += ctor_params[i].getClass().getName();
      }
      throw new NoSuchMethodException("No valid constructor for parameters (" + params + ") in class " + clazz.getName());
    }
    return (T)ctor.newInstance(ctor_params);
  }
  public static <T> T new_instance_no_exception(Class<T> clazz, Object[] ctor_params)
  {
    try
    {
      return new_instance(clazz, ctor_params);
    } catch (Exception e)
    { return null; }
  }
}
