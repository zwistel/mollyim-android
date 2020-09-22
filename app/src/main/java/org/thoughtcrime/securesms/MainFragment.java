package org.thoughtcrime.securesms;

import android.content.Context;
import android.widget.ImageView;

import androidx.annotation.NonNull;

public class MainFragment extends LoggingFragment {

  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);
  }

  public void onUpdateFab(ImageView fab) {}

  protected @NonNull MainNavigator getNavigator() {
    return MainNavigator.get(requireActivity());
  }
}
