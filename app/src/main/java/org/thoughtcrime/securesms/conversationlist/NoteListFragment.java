/*
 * Copyright (C) 2015 Open Whisper Systems
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.thoughtcrime.securesms.conversationlist;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ActionMode;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.color.MaterialColor;
import org.thoughtcrime.securesms.color.MaterialColors;
import org.thoughtcrime.securesms.database.DatabaseFactory;
import org.thoughtcrime.securesms.database.ThreadDatabase;
import org.thoughtcrime.securesms.dependencies.ApplicationDependencies;
import org.thoughtcrime.securesms.profiles.ProfileName;
import org.thoughtcrime.securesms.recipients.Recipient;
import org.thoughtcrime.securesms.recipients.RecipientDetails;
import org.thoughtcrime.securesms.recipients.RecipientId;
import org.thoughtcrime.securesms.util.ThemeUtil;

import java.util.List;


public class NoteListFragment extends ConversationListFragment implements ActionMode.Callback
{
  private RecyclerView list;
  private View         chipLayout;
  private ChipGroup    chipGroup;

  private NoteListRepository repository;

  public static NoteListFragment newInstance() {
    return new NoteListFragment();
  }

  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    repository = new NoteListRepository(ApplicationDependencies.getApplication());
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle bundle) {
    return inflater.inflate(R.layout.note_list_fragment, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    list       = view.findViewById(R.id.list);
    chipLayout = view.findViewById(R.id.chip_layout);
    chipGroup  = view.findViewById(R.id.chip_group);

    chipGroup.setOnCheckedChangeListener(onCheckedChangeListener);

    setChipGroupCheckedColor();
  }

  @Override
  public void onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);
    menu.findItem(R.id.menu_new_group).setVisible(false);
    menu.findItem(R.id.menu_mark_all_read).setVisible(false);
    menu.findItem(R.id.menu_invite).setVisible(false);
    menu.findItem(R.id.menu_search).setVisible(false);
  }

  @Override
  protected void updateEmptyState(boolean isConversationEmpty) {}

  @Override
  protected void updateReminders() {}

  @Override
  public void onUpdateFab(ImageView fab) {
    fab.setOnClickListener(this::onFabClick);
    fab.setImageResource(R.drawable.ic_add_white_original_24dp);
  }

  @Override
  protected void onPostSubmitList(@NonNull ConversationListViewModel.ConversationList conversationList) {
    updateChipGroup(conversationList.getPalette());
  }

  private void updateChipGroup(List<MaterialColor> palette) {
    for (int i = 0; i < chipGroup.getChildCount(); i++) {
      Chip chip = (Chip) chipGroup.getChildAt(i);

      MaterialColor chipColor = getMaterialColorFromChip(chip);

      if (palette.contains(chipColor)) {
        chip.setVisibility(View.VISIBLE);
      } else {
        chip.setVisibility(View.GONE);
        chip.setChecked(false);
      }
    }

    chipLayout.setVisibility(palette.isEmpty() ? View.GONE : View.VISIBLE);
  }

  private void setChipGroupCheckedColor() {
    final int checkedTextColor         = getResources().getColor(ThemeUtil.isDarkTheme(requireContext()) ? R.color.core_grey_05 : R.color.core_white);
    final int uncheckedTextColor       = getResources().getColor(ThemeUtil.isDarkTheme(requireContext()) ? R.color.core_grey_05 : R.color.core_grey_90);
    final int uncheckedBackgroundColor = getResources().getColor(ThemeUtil.isDarkTheme(requireContext()) ? R.color.core_grey_75 : R.color.core_grey_10);

    for (int i = 0; i < chipGroup.getChildCount(); i++) {
      Chip chip = (Chip) chipGroup.getChildAt(i);
      MaterialColor chipColor = getMaterialColorFromChip(chip);

      if (chip.isChecked()) {
        chip.setTextColor(checkedTextColor);
        chip.setChipBackgroundColor(ColorStateList.valueOf(chipColor.toAvatarColor(requireContext())));
        chip.setChipIconTint(ColorStateList.valueOf(uncheckedBackgroundColor));
      } else {
        chip.setTextColor(uncheckedTextColor);
        chip.setChipBackgroundColor(ColorStateList.valueOf(uncheckedBackgroundColor));
        chip.setChipIconTint(ColorStateList.valueOf(chipColor.toAvatarColor(requireContext())));
      }
    }
  }

  private void onFabClick(View v) {
    handleCreateNote();
  }

  private void handleCreateNote() {
    final Context context = requireContext();

    final Recipient   recipient   = Recipient.external(context, RecipientDetails.NOTE_PREFIX + System.currentTimeMillis());
    final RecipientId recipientId = recipient.getId();

    DatabaseFactory.getRecipientDatabase(context).setProfileName(recipientId,
        ProfileName.fromParts(getString(R.string.NoteListFragment_untitled_note), ""));

    long threadId = DatabaseFactory.getThreadDatabase(context).createThreadForRecipient(recipientId, false, ThreadDatabase.DistributionTypes.DEFAULT);

    getNavigator().goToConversation(recipientId, threadId, ThreadDatabase.DistributionTypes.DEFAULT, -1);
  }

  @Override
  ConversationListViewModel.Repository getRepository() {
    return repository;
  }

  @Override
  public boolean onCreateActionMode(ActionMode mode, Menu menu) {
    MenuInflater inflater = getActivity().getMenuInflater();

    inflater.inflate(getActionModeMenuRes(), menu);
    inflater.inflate(R.menu.note_list_batch, menu);

    mode.setTitle("1");

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.action_mode_status_bar));
    }

    if (Build.VERSION.SDK_INT >= 23) {
      int current = getActivity().getWindow().getDecorView().getSystemUiVisibility();
      getActivity().getWindow().getDecorView().setSystemUiVisibility(current & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    return true;
  }

  @Override
  protected void setCorrectMenuVisibility(@NonNull Menu menu) {}

  private final ChipGroup.OnCheckedChangeListener onCheckedChangeListener =
      new ChipGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(ChipGroup group, int checkedId) {
          Chip chip = chipGroup.findViewById(checkedId);

          if (chip != null) {
            repository.setColorFilter(getMaterialColorFromChip(chip).serialize());
          } else {
            repository.setColorFilter(null);
          }

          setChipGroupCheckedColor();
        }
      };

  private MaterialColor getMaterialColorFromChip(final @NonNull Chip chip) {
    ColorStateList strokeColor = chip.getChipStrokeColor();

    if (strokeColor == null) {
      throw new AssertionError("Chip must have color set");
    }
    return MaterialColors.CONVERSATION_PALETTE.getByColor(requireContext(), strokeColor.getDefaultColor());
  }
}
