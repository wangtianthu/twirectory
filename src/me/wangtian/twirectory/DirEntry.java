package me.wangtian.twirectory;

import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import android.text.Html;

/**
 * An entry for the twitter directory
 */
public class DirEntry {
  private static final String DIR_URL = "https://twitter.com/i/directory/profiles/";
  private static final String PROFILE_URL = "https://twitter.com/";

  public enum Type {
    RANGE,
    TERMINAL
  }
  private Type type;
  private String nameStart;
  private String nameEnd;  // only for RANGE types, optional
  private String urlSuffix;  // url for drilling down

  private String screenName = null;  // a single screen name
  private List<String> screenNameList = null;  // only for TERMINAL types

  public DirEntry(Type type, String nameStart, String nameEnd, String urlSuffix) {
    Preconditions.checkArgument(type == Type.RANGE);
    this.type = type;
    this.nameStart = nameStart;
    this.nameEnd = nameEnd;
    this.urlSuffix = urlSuffix;
  }

  public DirEntry(Type type, String name) {
    Preconditions.checkArgument(type == Type.TERMINAL);
    this.type = type;
    this.nameStart = name;
    this.nameEnd = null;
  }

  public DirEntry(Type type, String name, String screenName) {
    Preconditions.checkArgument(type == Type.TERMINAL);
    this.type = type;
    this.nameStart = name;
    this.nameEnd = null;
    this.screenName = screenName;
  }

  public String getNameStart() {
    return nameStart;
  }

  public String getNameEnd() {
    return nameEnd;
  }

  public String getScreenName() {
    return screenName;
  }

  public List<String> getScreenNameList() {
    return screenNameList;
  }

  public Type getType() {
    return type;
  }

  public int getCount() {
    return screenNameList != null ? screenNameList.size() : 1;
  }

  public void addScreenName(String screenName) {
    Preconditions.checkArgument(type == Type.TERMINAL);
    if (screenNameList == null) {
      screenNameList = Lists.newArrayList();
      if (screenName != null) {
        screenNameList.add(this.screenName);  // add its own name first
        this.screenName = null;  // clean up my own screen name
      }
    }
    screenNameList.add(screenName);
  }

  public String getUrl() {
    if (type == Type.RANGE) {
      return DIR_URL + urlSuffix;
    } else if (screenName != null) {
      return PROFILE_URL + screenName.substring(1);  // skip the initial "@"
    }
    return null;
  }

  private static List<String> START_LETTERS = Lists.newArrayList(
      "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R",
      "S", "T", "U", "V", "W", "X", "Y", "Z"
  );
  private static List<DirEntry> TOP_LEVEL_LIST = Lists.newArrayList(Iterables.transform(
      START_LETTERS,
      new Function<String, DirEntry>() {
        public DirEntry apply(String s) {
          return new DirEntry(Type.RANGE, s, null, s.toLowerCase());
        }
      }));

  public static List<DirEntry> getInitialList() {
    return TOP_LEVEL_LIST;
  }

  @Override
  public String toString() {
    if (type == Type.RANGE) {
      if (nameEnd == null) {
        return String.format("%s ~", nameStart);
      } else {
        return String.format("%s ~ %s", nameStart, nameEnd);
      }
    } else {
      if (getCount() > 1) {
        return String.format("%s - %d duplicates", nameStart, getCount());
      } else {
        return String.format("%s - %s", nameStart, screenName);
      }
    }
  }

  public String debugString() {
    if (type == Type.RANGE) {
      return String.format("[%s] (%s,%s), url=%s",
          type, nameStart, nameEnd, urlSuffix);
    } else {
      return String.format("[%s] %s, %s, %s",
          type, nameStart, screenName, screenNameList);
    }
  }

  /**
   * Expand a composite TERMINAL entries into a list of simple entries.
   */
  public List<DirEntry> expand() {
    Preconditions.checkArgument(type == Type.TERMINAL);
    if (this.screenNameList == null) {
      return Lists.newArrayList(this);
    } else {
      List<DirEntry> entries = Lists.newArrayList();
      for (String scrName : this.screenNameList) {
        entries.add(new DirEntry(Type.TERMINAL, this.nameStart, scrName));
      }
      return entries;
    }
  }
}
