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
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.view.ActionMode;

import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.database.DatabaseFactory;
import org.thoughtcrime.securesms.database.ThreadDatabase;
import org.thoughtcrime.securesms.dependencies.ApplicationDependencies;
import org.thoughtcrime.securesms.profiles.ProfileName;
import org.thoughtcrime.securesms.recipients.Recipient;
import org.thoughtcrime.securesms.recipients.RecipientDetails;
import org.thoughtcrime.securesms.recipients.RecipientId;


public class NoteListFragment extends ConversationListFragment implements ActionMode.Callback
{
  public static NoteListFragment newInstance() {
    return new NoteListFragment();
  }

  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);
//    setHasOptionsMenu(false);
  }

  @Override
  public void onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);
    menu.findItem(R.id.menu_new_group).setVisible(false);
    menu.findItem(R.id.menu_mark_all_read).setVisible(false);
    menu.findItem(R.id.menu_invite).setVisible(false);
  }

  @Override
  protected void updateReminders() {}

  @Override
  public void onUpdateFab(ImageView fab) {
    fab.setOnClickListener(this::onFabClick);
    fab.setImageResource(R.drawable.ic_add_white_original_24dp);
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
    return new NoteListRepository(ApplicationDependencies.getApplication());
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
}
