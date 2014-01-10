package me.wangtian.twirectory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.google.common.collect.Lists;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import nu.validator.htmlparser.common.XmlViolationPolicy;
import nu.validator.htmlparser.sax.HtmlParser;

/**
 * Parser for the directory
 */
public class DirParser {
  public List<DirEntry> parse(InputStream in) throws SAXException, IOException {
    Handler handler = new Handler();
    try {
      InputSource inputSource = new InputSource(in);
      inputSource.setEncoding("UTF-8");
      HtmlParser parser = new HtmlParser(XmlViolationPolicy.ALLOW);
      parser.setContentHandler(handler);
      parser.parse(inputSource);
      return handler.getResults();
    } finally {
      in.close();
    }
  }

  private class Handler extends DefaultHandler {
    private List<DirEntry> resultEntries = Lists.newArrayList();
    private boolean inDir = false;
    private boolean inDirEntry = false;
    private String url = null, value1 = null, value2 = null;
    private boolean inName = false;
    private boolean inScreenName = false;
    private boolean isTerminal = false;
    private StringBuilder sb = new StringBuilder();
    private int divDepth = 0;

    public void startElement(String uri, String localName, String qName,
                             Attributes atts) throws SAXException {
      String tag = qName.toLowerCase();
      String classValue = atts.getValue("class");  // in <style>
      String hrefValue = atts.getValue("href");    // in <a>
      String titleValue = atts.getValue("title");  // in <span>

      if ("div".equals(tag)) {
        if ("directory-page".equals(classValue)) {
          inDir = true;  // enter directory section
        } else {
          ++divDepth;
        }
      }

      if (inDir) {
        if ("li".equals(tag)) {
          inDirEntry = true;
        }
        if (inDirEntry) {
          if ("a".equals(tag)) {
            // link
            url = hrefValue;
          } else if ("span".equals(tag)) {
            if ("name".equals(classValue)) {
              inName = true;
              isTerminal = true;
            } else if ("screenname".equals(classValue)) {
              inScreenName = true;
              isTerminal = true;
            } else {
              // Range case
              if (value1 == null) {
                value1 = titleValue;  // from name
              } else {
                value2 = titleValue;  // to name
              }
            }
          }
        }
      }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
      String tag = qName.toLowerCase();
      if (inDirEntry) {
        if ("div".equals(tag)) {
          --divDepth;
          if (divDepth == 0) {
            inDir = false;
            return;
          }
        }
        if ("span".equals(tag)) {
          if (inName) {
            value1 = sb.toString();
            inName = false;
          } else if (inScreenName) {
            value2 = sb.toString();
            inScreenName = false;
          }
          sb.setLength(0);
        } else if ("li".equals(tag)) {
          if (value1 != null) {
            if (isTerminal) {
              resultEntries.add(new DirEntry(DirEntry.Type.TERMINAL, value1, value2));
            } else {
              if (url != null && url.lastIndexOf("/") != -1) {
                String urlSuffix = url.substring(url.lastIndexOf("/") + 1);
                resultEntries.add(new DirEntry(DirEntry.Type.RANGE, value1, value2, urlSuffix));
              }
            }
          }
          // Clear the value
          inDirEntry = false;
          value1 = null;
          value2 = null;
        }
      }
    }

    @Override
    public void endDocument() throws SAXException {
      super.endDocument();
      // Merge all terminal entries with the same name
      List<DirEntry> newEntries = Lists.newArrayList();
      DirEntry prevEntry = null;
      for (DirEntry entry : resultEntries) {
        if (entry.getType() == DirEntry.Type.TERMINAL) {
          if (prevEntry == null) {
            prevEntry = entry;
            newEntries.add(entry);
          } else if (entry.getNameStart().equals(prevEntry.getNameStart())) {
            prevEntry.addScreenName(entry.getScreenName());
          } else {
            prevEntry = entry;
            newEntries.add(entry);
          }
        }
      }
      if (!newEntries.isEmpty()) {
        resultEntries = newEntries;
      }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
      if (inName || inScreenName) {
        sb.append(ch, start, length);
      }
    }

    public List<DirEntry> getResults() {
      return resultEntries;
    }
  }
}
