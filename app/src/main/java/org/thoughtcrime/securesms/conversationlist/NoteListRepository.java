package org.thoughtcrime.securesms.conversationlist;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.DataSource;

import org.thoughtcrime.securesms.conversationlist.model.Conversation;
import org.thoughtcrime.securesms.util.paging.Invalidator;

class NoteListRepository implements ConversationListViewModel.Repository {

  private final Context context;

  private @Nullable String color;

  private Invalidator invalidator;

  NoteListRepository(@NonNull Context context) {
    this.context = context;
  }

  @Override
  public DataSource.Factory<Integer, Conversation> getDataSourceFactory(@NonNull Invalidator invalidator) {
    this.invalidator = invalidator;
    return new DataSource.Factory<Integer, Conversation>() {
      @NonNull
      @Override
      public DataSource<Integer, Conversation> create() {
        return new ConversationListDataSource.NoteListDataSource(context, invalidator, color);
      }
    };
  }

  public void setColorFilter(@Nullable String color) {
    this.color = color;
    if (invalidator != null) {
      invalidator.invalidate();
    }
  }

  @Override
  public boolean isArchived() {
    return false;
  }
}
