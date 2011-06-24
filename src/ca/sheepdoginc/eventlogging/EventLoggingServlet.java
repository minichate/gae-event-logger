package ca.sheepdoginc.eventlogging;

import ca.sheepdoginc.eventlogger.EventLevel;
import ca.sheepdoginc.eventlogger.EventLogger;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class EventLoggingServlet extends HttpServlet {

  public static final Logger log = Logger.getLogger(EventLoggingServlet.class.getName());

  static {
    EventLoggingServlet.log.addHandler(new EventLogger());
  }

  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {

    String series = req.getParameter("series");
    String[] vars = null;

    resp.setContentType("text/html");

    if (series != null && !series.isEmpty()) {
      vars = series.split(":");
    }

    if (vars == null) {
      return;
    }

    for (String x : vars) {
      log.log(EventLevel.EVENT, x);
    }

    String url = "/eventimager?series=" + series + "&date="
        + new Date().getTime();

    resp.getWriter().println("<img src=\"" + url + "\" />");
  }
}
