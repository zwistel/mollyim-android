package org.thoughtcrime.securesms;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import org.thoughtcrime.securesms.util.DynamicNoActionBarTheme;
import org.thoughtcrime.securesms.util.DynamicTheme;

public class ArchiveActivity extends PassphraseRequiredActivity {

  private final DynamicTheme  dynamicTheme = new DynamicNoActionBarTheme();
  private final MainNavigator navigator    = new MainNavigator(this);

  @Override
  protected void onCreate(Bundle savedInstanceState, boolean ready) {
    super.onCreate(savedInstanceState, ready);
    setContentView(R.layout.archive_activity);

    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
  }

  @Override
  protected void onPreCreate() {
    super.onPreCreate();
    dynamicTheme.onCreate(this);
  }

  @Override
  protected void onResume() {
    super.onResume();
    dynamicTheme.onResume(this);
  }

  public @NonNull MainNavigator getNavigator() {
    return navigator;
  }
}
