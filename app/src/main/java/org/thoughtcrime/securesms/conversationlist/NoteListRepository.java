package org.thoughtcrime.securesms.conversationlist;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.paging.DataSource;

import org.thoughtcrime.securesms.conversationlist.model.Conversation;
import org.thoughtcrime.securesms.util.paging.Invalidator;

class NoteListRepository implements ConversationListViewModel.Repository {

  private final Context context;

  public NoteListRepository(@NonNull Context context) {
    this.context = context;
  }

  @Override
  public DataSource.Factory<Integer, Conversation> getDataSourceFactory(@NonNull Invalidator invalidator) {
    return new DataSource.Factory<Integer, Conversation>() {
      @NonNull
      @Override
      public DataSource<Integer, Conversation> create() {
        return new ConversationListDataSource.NoteListDataSource(context, invalidator);
      }
    };
  }

  @Override
  public boolean isArchived() {
    return false;
  }
}
