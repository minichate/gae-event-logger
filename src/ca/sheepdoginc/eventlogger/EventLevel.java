package ca.sheepdoginc.eventlogger;

import java.util.logging.Level;

public class EventLevel extends Level {

  public static final Level EVENT = new EventLevel("EVENT",
      Level.WARNING.intValue() + 1);

  private static final long serialVersionUID = 6549033340395199214L;

  protected EventLevel(String name, int value) {
    super(name, value);
  }

}
