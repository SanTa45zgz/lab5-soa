package soa.eip;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class Router extends RouteBuilder {

  public static final String DIRECT_URI = "direct:twitter";

  @Override
  public void configure() {
    from(DIRECT_URI)
        .log("Body contains \"${body}\"")
        .log("Searching twitter for \"${body}\"!")
        .process(new MyProcessor())
        .log("Body format \"${body}\"")
        .log("Header count \"${header.count}\"")
        .toD("twitter-search:${body}?count=${header.count}")
        .log("Body now contains the response from twitter:\n${body}");
  }

  // https://camel.apache.org/components/latest/eips/process-eip.html

  public class MyProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
      Message msg = exchange.getIn();
      String body = exchange.getIn().getBody(String.class);
      if (body.matches("^.+ max:[0-9]+$")) {
        // get the value of count
        String splits[] = body.split(":");
        int count = Integer.parseInt(splits[1]);
        // delete max: from body string
        String newBody = splits[0].replace(" max", "");
        msg.setBody(newBody);
        msg.setHeader("count", count);

      } else {
        // set default header count (5)
        // https://camel.apache.org/components/latest/twitter-search-component.html
        msg.setHeader("count", 5);
      }

    }
  }
}

