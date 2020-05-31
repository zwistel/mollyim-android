package org.thoughtcrime.securesms.conversationlist;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.paging.DataSource;

import org.thoughtcrime.securesms.conversationlist.model.Conversation;
import org.thoughtcrime.securesms.util.paging.Invalidator;

class ConversationListRepository implements ConversationListViewModel.Repository {

  private final Context context;
  private final boolean archived;

  public ConversationListRepository(@NonNull Context context, boolean archived) {
    this.context = context;
    this.archived = archived;
  }

  @Override
  public DataSource.Factory<Integer, Conversation> getDataSourceFactory(@NonNull Invalidator invalidator) {
    return new DataSource.Factory<Integer, Conversation>() {
      @NonNull
      @Override
      public DataSource<Integer, Conversation> create() {
        if (archived) {
          return new ConversationListDataSource.ArchivedConversationListDataSource(context, invalidator);
        } else {
          return new ConversationListDataSource.UnarchivedConversationListDataSource(context, invalidator);
        }
      }
    };
  }

  @Override
  public boolean isArchived() {
    return archived;
  }
}
