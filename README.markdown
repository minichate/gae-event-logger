Event Logging for Google AppEngine
==================================

In production its often useful to gather statistics on the events that happen
in your system. For example, how many successes were there vs. login failures,
or how many AJAX calls in the last hour vs. an hour ago?

This project install a Logger that can be used to help collect that data, and
display results back in chart form.

The logging is:

* Fast - Logs are stored in memcache, so its safe to make many calls
* Comparable - Easy to compare success vs. failure.
* Non-Persistant - Never stored in BigTable, so history is limited to 2 hours.

Installation
------------

Copy the eventlogging .jar file into you lib directory (on AppEngine, in
war/WEB-INF/lib). Edit your web.xml file to include the following:

```xml
<servlet>
	<servlet-name>EventImager</servlet-name>
	<servlet-class>ca.sheepdoginc.eventlogging.EventImager</servlet-class>
</servlet>
<servlet-mapping>
	<servlet-name>EventImager</servlet-name>
	<url-pattern>/eventimager</url-pattern>
</servlet-mapping>
```

This will add the chart handler to your project. Feel free to edit the 
actual endpoint for the chart (currently set to /eventimager)

There are a few bugs in AppEngines logging facilities, so to work around them
we need to initialize the logger statically:

```java
class MyFancyClass {
	private static final Logger log = Logger.getLogger(MyFancyClass.class.getName());
		
	static {
		MyFancyClass.log.addHandler(new EventLogger());
	}
	
	// ....
}
```

Usage
-----

To actually use the logger, you'll just need to do something like:

```java
log.log(EventLevel.EVENT, "rpc.request.success");
```

Notice that we're using the EVENT log level -- EventLogging will ignore log
levels that don't match the EventLevel.EVENT log level. The above captures a
successful RPC request -- you'd probably also want to catch failures for
comparison:

```java
log.log(EventLevel.EVENT, "rpc.request.failure");
```