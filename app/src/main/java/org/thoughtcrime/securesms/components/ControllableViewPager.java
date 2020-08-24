package org.thoughtcrime.securesms.components;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import org.thoughtcrime.securesms.components.viewpager.HackyViewPager;

/**
 * An implementation of {@link ViewPager} that disables swiping when the view is disabled.
 */
public class ControllableViewPager extends HackyViewPager {

  private boolean swipingLocked;

  public ControllableViewPager(@NonNull Context context) {
    super(context);
  }

  public ControllableViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  /** Disable swiping between pages. */
  public void setSwipingLocked(boolean swipingLocked) {
    this.swipingLocked = swipingLocked;
  }

  @Override
  public boolean onTouchEvent(MotionEvent ev) {
    return !swipingLocked && isEnabled() && super.onTouchEvent(ev);
  }

  @Override
  public boolean onInterceptTouchEvent(MotionEvent ev) {
    return !swipingLocked && isEnabled() && super.onInterceptTouchEvent(ev);
  }
}
