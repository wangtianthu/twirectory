package me.wangtian.twirectory;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends FragmentActivity {
  DirEntryFragment dirEntryFragment;
  LinearLayout stackArea;

  /**
   * Called when the activity is first created.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    stackArea = (LinearLayout) findViewById(R.id.dir_stack);
    dirEntryFragment = (DirEntryFragment) getSupportFragmentManager().findFragmentById(
        R.id.dir_entry_frag);
    dirEntryFragment.setClickListener(new DirEntryClickListenerImpl());
  }

  @Override
  protected void onStart() {
    super.onStart();
    dirEntryFragment.loadDirEntries(null);
  }

  public interface DirEntryClickListener {
    public void onClick(DirEntry entry);
  }

  public class DirEntryClickListenerImpl implements DirEntryClickListener {
    public void onClick(DirEntry entry) {
      if (entry.getType() == DirEntry.Type.TERMINAL) {
        if (entry.getCount() == 1) {
          // We have reached the end, create an intern to open Twitter
          Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(entry.getUrl()));
          Intent chooser = Intent.createChooser(intent, "View this user with");
          if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(chooser);
          }
        } else {
          // Create another list of basic terminal entries from current one
          addStackEntry(entry);
          dirEntryFragment.updateListView(entry.expand());
        }
      } else {
        // There are more to explore
        addStackEntry(entry);
        dirEntryFragment.loadDirEntries(entry.getUrl());
      }
    }
  }

  public void addStackEntry(DirEntry parent) {
    TextView tv = (TextView) getLayoutInflater().inflate(R.layout.dir_stack_bar, null);
    tv.setText(parent.toString());
  }

  @Override
  public void onBackPressed() {
    if (!dirEntryFragment.goBack()) {
      super.onBackPressed();
    }
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event)  {
    if ( keyCode == KeyEvent.KEYCODE_BACK
        && event.getRepeatCount() == 0) {
      onBackPressed();
      return true;
    }
    return super.onKeyDown(keyCode, event);
  }
}
