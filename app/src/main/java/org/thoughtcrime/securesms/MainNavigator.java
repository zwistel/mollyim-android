package org.thoughtcrime.securesms;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import org.thoughtcrime.securesms.conversation.ConversationActivity;
import org.thoughtcrime.securesms.conversationlist.ConversationListArchiveFragment;
import org.thoughtcrime.securesms.conversationlist.ConversationListFragment;
import org.thoughtcrime.securesms.groups.ui.creategroup.CreateGroupActivity;
import org.thoughtcrime.securesms.recipients.RecipientId;

public class MainNavigator {

  private final Activity activity;

  MainNavigator(@NonNull Activity activity) {
    this.activity = activity;
  }

  public static MainNavigator get(@NonNull Activity activity) {
    if (activity instanceof MainActivity) {
      return ((MainActivity) activity).getNavigator();
    }
    if (activity instanceof ArchiveActivity) {
      return ((ArchiveActivity) activity).getNavigator();
    }
    throw new IllegalArgumentException("Activity must be an instance of MainActivity or ArchiveActivity!");
  }

  public void goToConversation(@NonNull RecipientId recipientId, long threadId, int distributionType, int startingPosition) {
    Intent intent = ConversationActivity.buildIntent(activity, recipientId, threadId, distributionType, startingPosition);

    activity.startActivity(intent);
    activity.overridePendingTransition(R.anim.slide_from_end, R.anim.fade_scale_out);
  }

  public void goToAppSettings() {
    Intent intent = new Intent(activity, ApplicationPreferencesActivity.class);
    activity.startActivity(intent);
  }

  public void goToArchiveList() {
    Intent intent = new Intent(activity, ArchiveActivity.class);
    activity.startActivity(intent);
  }

  public void goToGroupCreation() {
    activity.startActivity(CreateGroupActivity.newIntent(activity));
  }

  public void goToInvite() {
    Intent intent = new Intent(activity, InviteActivity.class);
    activity.startActivity(intent);
  }
}
