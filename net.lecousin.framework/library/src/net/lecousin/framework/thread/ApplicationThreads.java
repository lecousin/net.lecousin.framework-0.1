package net.lecousin.framework.thread;

import java.util.Timer;

public class ApplicationThreads
{
  private ApplicationThreads() { /* instantiation not allowed */ }
  
  private static Timer timer = null;
  public static Timer get_timer() {
    if (timer == null)
      timer = new Timer();
    return timer;
  }
}
