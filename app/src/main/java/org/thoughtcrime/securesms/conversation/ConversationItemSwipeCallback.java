package org.thoughtcrime.securesms.conversation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import org.thoughtcrime.securesms.database.model.MessageRecord;
import org.thoughtcrime.securesms.util.AccessibilityUtil;
import org.thoughtcrime.securesms.util.ServiceUtil;

class ConversationItemSwipeCallback extends ItemTouchHelper.SimpleCallback {

  private static float SWIPE_SUCCESS_DX           = ConversationSwipeAnimationHelper.TRIGGER_DX;
  private static long  SWIPE_SUCCESS_VIBE_TIME_MS = 10;

  private boolean swipeBack;
  private boolean shouldTriggerSwipeFeedback;
  private boolean canTriggerSwipe;
  private float   latestDownX;
  private float   latestDownY;
  private int     latestDirection;

  private final SwipeAvailabilityProvider     swipeAvailabilityProvider;
  private final ConversationItemTouchListener itemTouchListener;
  private final OnSwipeListener               onSwipeListener;

  ConversationItemSwipeCallback(@NonNull SwipeAvailabilityProvider swipeAvailabilityProvider,
                                @NonNull OnSwipeListener onSwipeListener)
  {
    super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
    this.itemTouchListener          = new ConversationItemTouchListener(this::updateLatestDownCoordinate);
    this.swipeAvailabilityProvider  = swipeAvailabilityProvider;
    this.onSwipeListener            = onSwipeListener;
    this.shouldTriggerSwipeFeedback = true;
    this.canTriggerSwipe            = true;
  }

  void attachToRecyclerView(@NonNull RecyclerView recyclerView) {
    recyclerView.addOnItemTouchListener(itemTouchListener);
    new ItemTouchHelper(this).attachToRecyclerView(recyclerView);
  }

  @Override
  public boolean onMove(@NonNull RecyclerView recyclerView,
                        @NonNull RecyclerView.ViewHolder viewHolder,
                        @NonNull RecyclerView.ViewHolder target)
  {
    return false;
  }

  @Override
  public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
    shouldTriggerSwipeFeedback = true;

    if ((getSwipeMessageDirs(viewHolder) & direction) != direction) {
      return;
    }

    ConversationItem    item          = ((ConversationItem) viewHolder.itemView);
    ConversationMessage messageRecord = item.getConversationMessage();
    onSwipeListener.onSwipedMessage(messageRecord, direction);
  }

  @Override
  public int getSwipeDirs(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder)
  {
    int defaultSwipeDirs = super.getSwipeDirs(recyclerView, viewHolder);
    int availSwipeDirs   = getSwipeMessageDirs(viewHolder);
    return defaultSwipeDirs & availSwipeDirs;
  }

  @Override
  public int convertToAbsoluteDirection(int flags, int layoutDirection) {
    if (swipeBack) {
      swipeBack = false;
      return 0;
    }
    return super.convertToAbsoluteDirection(flags, layoutDirection);
  }

  @Override
  public void onChildDraw(
          @NonNull Canvas c,
          @NonNull RecyclerView recyclerView,
          @NonNull RecyclerView.ViewHolder viewHolder,
          float dx, float dy, int actionState, boolean isCurrentlyActive)
  {
    final int dirFlag = getHorizontalDirFlag(dx);

    if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && (getSwipeMessageDirs(viewHolder) & dirFlag) == dirFlag) {
      float sign = (dirFlag == ItemTouchHelper.LEFT) ? -1f : 1f;
      ConversationSwipeAnimationHelper.update((ConversationItem) viewHolder.itemView, Math.abs(dx), sign);
      handleSwipeFeedback((ConversationItem) viewHolder.itemView, Math.abs(dx));
      if (canTriggerSwipe) {
        setTouchListener(recyclerView, viewHolder, dx);
      }
    } else {
      ConversationSwipeAnimationHelper.update((ConversationItem) viewHolder.itemView, 0, 1);
    }

    if (dx == 0 || dirFlag != latestDirection) {
      shouldTriggerSwipeFeedback = true;
      canTriggerSwipe            = true;
      latestDirection            = dirFlag;
    }
  }

  private void handleSwipeFeedback(@NonNull ConversationItem item, float dx) {
    if (dx > SWIPE_SUCCESS_DX && shouldTriggerSwipeFeedback) {
      vibrate(item.getContext());
      ConversationSwipeAnimationHelper.trigger(item);
      shouldTriggerSwipeFeedback = false;
    }
  }

  @SuppressLint("ClickableViewAccessibility")
  private void setTouchListener(@NonNull RecyclerView recyclerView,
                                @NonNull RecyclerView.ViewHolder viewHolder,
                                float dx)
  {
    recyclerView.setOnTouchListener((v, event) -> {
      switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
          shouldTriggerSwipeFeedback = true;
          break;
        case MotionEvent.ACTION_UP:
          handleTouchActionUp(recyclerView, viewHolder, dx);
        case MotionEvent.ACTION_CANCEL:
          swipeBack = true;
          shouldTriggerSwipeFeedback = false;
          resetProgressIfAnimationsDisabled(viewHolder);
          break;
      }
      return false;
    });
  }

  private void handleTouchActionUp(@NonNull RecyclerView recyclerView,
                                   @NonNull RecyclerView.ViewHolder viewHolder,
                                   float dx)
  {
    if (Math.abs(dx) > SWIPE_SUCCESS_DX) {
      canTriggerSwipe = false;
      onSwiped(viewHolder, getHorizontalDirFlag(dx));
      if (shouldTriggerSwipeFeedback) {
        vibrate(viewHolder.itemView.getContext());
      }
      recyclerView.setOnTouchListener(null);
    }
    recyclerView.cancelPendingInputEvents();
  }

  private static void resetProgressIfAnimationsDisabled(RecyclerView.ViewHolder viewHolder) {
    if (AccessibilityUtil.areAnimationsDisabled(viewHolder.itemView.getContext())) {
      ConversationSwipeAnimationHelper.update((ConversationItem) viewHolder.itemView,
                                              0f,
                                              1);
    }
  }

  private int getSwipeMessageDirs(@NonNull RecyclerView.ViewHolder viewHolder) {
    if (!(viewHolder.itemView instanceof ConversationItem)) return 0;

    ConversationItem item = ((ConversationItem) viewHolder.itemView);

    if (item.disallowSwipe(latestDownX, latestDownY)) return 0;

    return swipeAvailabilityProvider.getSwipeMessageDirs(item.getConversationMessage());
  }

  private void updateLatestDownCoordinate(float x, float y) {
    latestDownX = x;
    latestDownY = y;
  }

  private static int getHorizontalDirFlag(float dX) {
    return dX > 0 ? ItemTouchHelper.RIGHT : ItemTouchHelper.LEFT;
  }

  private static void vibrate(@NonNull Context context) {
    Vibrator vibrator = ServiceUtil.getVibrator(context);
    if (vibrator != null) vibrator.vibrate(SWIPE_SUCCESS_VIBE_TIME_MS);
  }

  interface SwipeAvailabilityProvider {
    int getSwipeMessageDirs(ConversationMessage conversationMessage);
  }

  interface OnSwipeListener {
    void onSwipedMessage(ConversationMessage conversationMessage, int direction);
  }
}
