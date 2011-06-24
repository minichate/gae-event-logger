package ca.sheepdoginc.eventlogger;

import com.google.appengine.api.channel.ChannelFailureException;
import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class EventLogger extends Handler {

  public static final Logger log = Logger.getLogger(EventLogger.class.getName());

  @SuppressWarnings("deprecation")
  @Override
  public synchronized void publish(LogRecord record) {
    if (!EventLevel.EVENT.equals(record.getLevel())) {
      return;
    }

    Date now = new Date(1000 * (System.currentTimeMillis() / 1000));
    now.setSeconds(0);

    String cacheKey = String.format("%s__%d", record.getMessage(),
        now.getTime());

    MemcacheService service = MemcacheServiceFactory.getMemcacheService("ca.sheepdoginc.eventlog");
    service.increment(cacheKey, 1L, 0L);

    try {
      ChannelService channelService = ChannelServiceFactory.getChannelService();
      String msg = String.format("{\"timestamp\":\"%s\", \"msg\":\"%s\"}",
          new Date().toLocaleString(), record.getMessage());
      channelService.sendMessage(new ChannelMessage("events", msg));
    } catch (ChannelFailureException e) {
      log.log(Level.WARNING, "Could not notify Channel service", e);
    }

  }

  @Override
  public void flush() {
    log.warning("In EventLogger.flush()");
  }

  @Override
  public void close() throws SecurityException {
    log.warning("In EventLogger.close()");
  }

}
