package ca.sheepdoginc.eventlogging;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

import com.googlecode.charts4j.AxisLabels;
import com.googlecode.charts4j.AxisLabelsFactory;
import com.googlecode.charts4j.AxisStyle;
import com.googlecode.charts4j.AxisTextAlignment;
import com.googlecode.charts4j.Color;
import com.googlecode.charts4j.DataUtil;
import com.googlecode.charts4j.GCharts;
import com.googlecode.charts4j.LegendPosition;
import com.googlecode.charts4j.Line;
import com.googlecode.charts4j.LineChart;
import com.googlecode.charts4j.Plots;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class EventImager extends HttpServlet {
  
  public static int HOURS = 2;

  @SuppressWarnings("deprecation")
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {

    MemcacheService service = MemcacheServiceFactory.getMemcacheService("ca.sheepdoginc.eventlog");

    Date now = new Date(1000 * (System.currentTimeMillis() / 1000));
    now.setSeconds(0);

    String series = req.getParameter("series");
    String[] vars = null;

    resp.setContentType("text/html");

    if (series != null && !series.isEmpty()) {
      vars = series.split(":");
    }

    if (vars == null) {
      return;
    }

    Map<String, List<Long>> points = new HashMap<String, List<Long>>();
    Map<String, List<String>> keys = new HashMap<String, List<String>>();
    for (String x : vars) {
      points.put(x, new ArrayList<Long>());
      keys.put(x, new ArrayList<String>());
    }

    List<String> labels = new ArrayList<String>();

    Long maxVal = 0L;
    
    SimpleDateFormat formatter = new SimpleDateFormat("hh:mm a", req.getLocale());

    int count = 0;
    for (long t = now.getTime() - (HOURS * 60 * 60 * 1000); t <= now.getTime(); t += 60 * 1000) {
      for (String x : vars) {
        keys.get(x).add(x + "__" + t);
      }

      if (count % 10 == 0) {
        Date d = new Date(t);
        labels.add(formatter.format(d));
      }

      count++;
    }

    for (String k : vars) {
      Map<String, Object> ds = service.getAll(keys.get(k));
      for (long t = now.getTime() - (HOURS * 60 * 60 * 1000); t <= now.getTime(); t += 60 * 1000) {
        Long data = (Long) ds.get(k + "__" + t);
        if (data == null) {
          data = 0L;
        }
        if (data > maxVal) {
          maxVal = data;
        }

        points.get(k).add(data);
      }
    }

    if (maxVal == 0L) {
      maxVal = 1L;
    }

    List<Line> lines = new ArrayList<Line>();
    Color[] cs = new Color[] {Color.RED, Color.BLUE, Color.GREEN};
    count = 0;
    for (String x : vars) {
      Line line = Plots.newLine(DataUtil.scaleWithinRange(0, maxVal,
          points.get(x)));
      line.setColor(cs[count % 3]);
      line.setLegend(x);
      lines.add(line);
      count++;
    }

    AxisLabels xAxis = AxisLabelsFactory.newAxisLabels(labels);
    xAxis.setAxisStyle(AxisStyle.newAxisStyle(Color.BLACK, 9,
        AxisTextAlignment.CENTER));

    AxisLabels yAxis = AxisLabelsFactory.newNumericRangeAxisLabels(0, maxVal);
    yAxis.setAxisStyle(AxisStyle.newAxisStyle(Color.BLACK, 9,
        AxisTextAlignment.CENTER));

    LineChart chart = GCharts.newLineChart(lines);
    chart.setSize(600, 200);
    chart.addXAxisLabels(xAxis);
    chart.addYAxisLabels(yAxis);
    chart.setGrid((double) 100 / 12, 14, 1, 3);
    chart.setLegendPosition(LegendPosition.BOTTOM);

    String url = chart.toURLString();

    resp.sendRedirect(url);
  }
}
