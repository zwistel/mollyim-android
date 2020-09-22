package org.thoughtcrime.securesms;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.thoughtcrime.securesms.components.ControllableViewPager;
import org.thoughtcrime.securesms.conversationlist.ConversationListFragment;
import org.thoughtcrime.securesms.conversationlist.NoteListFragment;
import org.thoughtcrime.securesms.recipients.Recipient;
import org.thoughtcrime.securesms.util.AvatarUtil;
import org.thoughtcrime.securesms.util.CommunicationActions;
import org.thoughtcrime.securesms.util.DynamicNoActionBarTheme;
import org.thoughtcrime.securesms.util.DynamicTheme;
import org.thoughtcrime.securesms.util.concurrent.SimpleTask;

public class MainActivity extends PassphraseRequiredActivity {

  private final DynamicTheme  dynamicTheme = new DynamicNoActionBarTheme();
  private final MainNavigator navigator    = new MainNavigator(this);

  private ImageView                 icon;
  private MainPagerAdapter          pagerAdapter;
  private ControllableViewPager     fragmentPager;
  private DrawerLayout              drawerLayout;
  private MainActionBarDrawerToggle toggle;
  private FloatingActionButton      fab;

  @Override
  protected void onCreate(Bundle savedInstanceState, boolean ready) {
    super.onCreate(savedInstanceState, ready);
    setContentView(R.layout.main_activity);

    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    icon = findViewById(R.id.toolbar_icon);
//    icon.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
    icon.setOnClickListener(v -> getNavigator().goToAppSettings());

    pagerAdapter = new MainPagerAdapter(getSupportFragmentManager());

    fragmentPager = findViewById(R.id.view_pager);
    fragmentPager.setAdapter(pagerAdapter);
    fragmentPager.setSwipingLocked(true);
    fragmentPager.addOnPageChangeListener(onPageChangeListener);
    fragmentPager.post(() -> onPageChangeListener.onPageSelected(fragmentPager.getCurrentItem()));

//    drawerLayout = findViewById(R.id.drawer_layout);
//    toggle = new MainActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer__open, R.string.navigation_drawer__close);
//    toggle.setDrawerIndicatorEnabled(false);
//    drawerLayout.addDrawerListener(toggle);

    fab = findViewById(R.id.fab);

    setUpBottomNavView();

    handleGroupLinkInIntent(getIntent());
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    handleGroupLinkInIntent(intent);
  }

  @Override
  protected void onPreCreate() {
    super.onPreCreate();
    dynamicTheme.onCreate(this);
  }

  @Override
  protected void onResume() {
    super.onResume();
    dynamicTheme.onResume(this);

    SimpleTask.run(getLifecycle(), Recipient::self, this::initializeProfileIcon);
  }

  @Override
  protected void onPostCreate(@Nullable Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
//    toggle.syncState();
  }

  @Override
  public void onConfigurationChanged(@NonNull Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
//    toggle.onConfigurationChanged(newConfig);
  }

  public @NonNull MainNavigator getNavigator() {
    return navigator;
  }

  private void setUpBottomNavView() {
    BottomNavigationView bottomNavView = findViewById(R.id.bottom_nav);
    bottomNavView.setOnNavigationItemSelectedListener(item -> {
      switch (item.getItemId()) {
        case R.id.bottom_nav_home:
          fragmentPager.setCurrentItem(0);
          return true;
        case R.id.bottom_nav_notebook:
          fragmentPager.setCurrentItem(1);
          return true;
        default:
          return false;
      }
    });
  }

  private ConversationListFragment getFragment(int position) {
    return (ConversationListFragment) pagerAdapter.instantiateItem(fragmentPager, position);
  }

  private void initializeProfileIcon(@NonNull Recipient recipient) {
    AvatarUtil.loadIconIntoImageView(recipient, icon);
  }

  private class MainActionBarDrawerToggle extends ActionBarDrawerToggle {

    MainActionBarDrawerToggle(Activity activity, DrawerLayout drawerLayout, Toolbar toolbar,
                              int openDrawerContentDescRes, int closeDrawerContentDescRes) {
      super(activity, drawerLayout, toolbar, openDrawerContentDescRes, closeDrawerContentDescRes);
    }

    @Override
    public void onDrawerOpened(View drawerView) {
      super.onDrawerOpened(drawerView);
      invalidateOptionsMenu();
      stopSearchAndSelection();
    }

    private void stopSearchAndSelection() {
      // TODO
    }
  }

  private final ViewPager.SimpleOnPageChangeListener onPageChangeListener =
      new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
          getFragment(position).onUpdateFab(fab);
        }
      };

  private static class MainPagerAdapter extends FragmentStatePagerAdapter {

    MainPagerAdapter(@NonNull FragmentManager fm) {
      super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
      switch (position) {
        case 0:
          return ConversationListFragment.newInstance();
        case 1:
          return NoteListFragment.newInstance();
        default:
          throw new RuntimeException();
      }
    }

    @Override
    public int getCount() {
      return 2;
    }
  }

  private void handleGroupLinkInIntent(Intent intent) {
    Uri data = intent.getData();
    if (data != null) {
      CommunicationActions.handlePotentialGroupLinkUrl(this, data.toString());
    }
  }
}
