package com.scalesec.vulnado;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.net.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinkLister {
  
  private static final Logger logger = LoggerFactory.getLogger(LinkLister.class);
  
  private LinkLister() {
    // private constructor 
  }

  public static List<String> getLinks(String url) throws IOException {
    List<String> result = new ArrayList<String>();
    Document doc = Jsoup.connect(url).get();
    Elements links = doc.select("a");
    for (Element link : links) {
      result.add(link.absUrl("href"));
    }
    return result;
  }

  public static List<String> getLinksV2(String url) throws BadRequest {
    try {
      URL aUrl= new URL(url);
      String host = aUrl.getHost();
      logger.info(host);
      if (host.startsWith("172.") || host.startsWith("192.168") || host.startsWith("10.")){
        throw new BadRequest("Use of Private IP");
      } else {
        return getLinks(url);
      }
    } catch(Exception e) {
      throw new BadRequest(e.getMessage());
    }
  }
  
  // test methods
  public static void main(String[] args) throws Exception {
    testPrivateIP();
    testPublicIP();
  }

  public static void testPrivateIP() throws Exception {
    try {
      getLinksV2("http://192.168.0.1");
    } catch (BadRequest e) {
      logger.info("Private IP blocked"); 
    }
  }
  
  public static void testPublicIP() throws Exception {
    List<String> links = getLinksV2("http://example.com");
    logger.info("Public IP allowed, links: " + links);
  }

}

class BadRequest extends Exception {
  public BadRequest(String message) {
    super(message);
  }
}