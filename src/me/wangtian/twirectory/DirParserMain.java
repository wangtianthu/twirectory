package me.wangtian.twirectory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.xml.sax.SAXException;

/**
 * A test
 */
public class DirParserMain {
  public static void main(String[] args) {
    // Load a normal page
    try {
      InputStream in = DirParser.class.getResourceAsStream("list-t.html");
      DirParser parser = new DirParser();
      List<DirEntry> entries = parser.parse(in);
      for (DirEntry de : entries) {
        System.out.println(de);
      }
    } catch (SAXException e) {
      System.out.println("Error: " + e);
    } catch (IOException e) {
      System.out.println("Error: " + e);
    }

    // Load a terminal page
    try {
      InputStream in = DirParser.class.getResourceAsStream("list-t3-final.html");
      DirParser parser = new DirParser();
      List<DirEntry> entries = parser.parse(in);
      for (DirEntry de : entries) {
        System.out.println(de);
      }
    } catch (SAXException e) {
      System.out.println("Error: " + e);
    } catch (IOException e) {
      System.out.println("Error: " + e);
    }
  }
}
