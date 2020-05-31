package org.thoughtcrime.securesms;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public abstract class MainFragment extends LoggingFragment {

  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);

    if (!(requireActivity() instanceof MainActivity)) {
      throw new IllegalStateException("Can only be used inside of MainActivity!");
    }
  }

  public abstract void onUpdateFab(ImageView fab);

  protected Toolbar getToolbar() {
    return ((MainActivity) requireActivity()).getToolbar();
  }

  protected View getSnackbarAnchor() {
    return ((MainActivity) requireActivity()).getSnackbarAnchor();
  }

  protected @NonNull MainNavigator getNavigator() {
    return MainNavigator.get(requireActivity());
  }
}
