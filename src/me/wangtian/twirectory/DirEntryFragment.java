package me.wangtian.twirectory;

import java.io.IOException;
import java.util.List;
import java.util.Stack;

import com.google.common.collect.Lists;

import org.xml.sax.SAXException;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Fragment for the list of directory entries
 */
public class DirEntryFragment extends ListFragment implements AdapterView.OnItemClickListener {
  private static final String TAG = "me.wangtian.twirectory";

  Stack<List<DirEntry>> navStack = new Stack<List<DirEntry>>();
  List<DirEntry> dirEntries = Lists.newArrayList();
  ArrayAdapter<DirEntry> listAdapter;
  MainActivity.DirEntryClickListener clickListener;

  public DirEntryFragment() {
  }

  public void setClickListener(MainActivity.DirEntryClickListener clickListener) {
    this.clickListener = clickListener;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    listAdapter = new ArrayAdapter<DirEntry>(getActivity(), R.layout.dir_entry, dirEntries);
  }

  @Override
  public void onStart() {
    super.onStart();
    setListAdapter(listAdapter);
    ListView v = getListView();
    if (v != null) {
      v.setOnItemClickListener(this);
    }
    loadDirEntries(null);
  }

  public void loadDirEntries(String urlSuffix) {
    if (urlSuffix == null) {
      updateListView(DirEntry.getInitialList());
    } else {
      // Download a new url suffix
      new FetchDirectoryTask().execute(urlSuffix);
    }
  }

  // For OnItemClickListener interface
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    navStack.push(Lists.newArrayList(dirEntries));  // push a copy of current list to the stack

    DirEntry entry = dirEntries.get(position);
    clickListener.onClick(entry);  // pass the control back to the upper level activity
  }

  public void updateListView(List<DirEntry> entries) {
    this.dirEntries.clear();
    this.dirEntries.addAll(entries);
    listAdapter.notifyDataSetChanged();
    getListView().scrollTo(0, 0);  // scroll back to top
  }

  public boolean goBack() {
    if (navStack.size() > 0) {
      updateListView(navStack.pop());
      return true;
    }
    return false;
  }

  /**
   * The task to download directory page
   */
  private class FetchDirectoryTask extends AsyncTask<String, Void, List<DirEntry>> {

    @Override
    protected List<DirEntry> doInBackground(String... urls) {
      try {
        return DirFetcher.fetchFrom(urls[0]);
      } catch (IOException e) {
        return null;
      } catch (SAXException e) {
        return null;
      }
    }

    @Override
    protected void onPostExecute(List<DirEntry> result) {
      updateListView(result);
    }
  }
}
