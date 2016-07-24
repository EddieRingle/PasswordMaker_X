package io.github.eddieringle.android.apps.passwordmaker.ui.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.common.reflect.TypeToken;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

import io.github.eddieringle.android.apps.passwordmaker.R;
import io.github.eddieringle.android.apps.passwordmaker.core.PMConstants;
import io.github.eddieringle.android.apps.passwordmaker.core.PMProfile;
import io.github.eddieringle.android.apps.passwordmaker.ui.fragment.AboutFragment;
import io.github.eddieringle.android.apps.passwordmaker.ui.fragment.GenerateFragment;
import io.github.eddieringle.android.apps.passwordmaker.ui.fragment.ProfilesFragment;
import io.github.eddieringle.android.apps.passwordmaker.util.GsonUtils;

public class MainActivity extends BaseActivity implements ActionBar.TabListener {

    private ArrayList<PMProfile> mProfileList = new ArrayList<PMProfile>();

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
     * will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                publishUpdatedProfiles();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_main);

        getBus().register(this);

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        if (getPrefs().getBoolean("first_run", true)) {
            final ArrayList<PMProfile> profilesList = new ArrayList<PMProfile>();
            profilesList.add(PMProfile.createDefault());
            getPrefsEditor().putString("profiles", GsonUtils.toJson(profilesList));
            getPrefsEditor().putString("current_profile", "Default");
            getPrefsEditor().putBoolean("first_run", false);
            getPrefsEditor().commit();
        }

        publishUpdatedProfiles();

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        getBus().paused();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getBus().resumed();
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Produce
    public UpdatedProfilesEvent produceUpdatedProfiles() {
        return new UpdatedProfilesEvent(mProfileList);
    }

    @Subscribe
    public void receiveRefreshRequest(ProfilesFragment.NeedProfileListRefreshEvent event) {
        if (event != null) {
            publishUpdatedProfiles();
        }
    }

    protected void publishUpdatedProfiles() {
        mProfileList.clear();
        TypeToken<List<PMProfile>> listToken = new TypeToken<List<PMProfile>>() {};
        String profilesJson = getPrefs().getString("profiles", "");
        mProfileList.addAll((List<PMProfile>)GsonUtils.fromJson(profilesJson, listToken.getType()));
        getBus().post(new UpdatedProfilesEvent(mProfileList));
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return new GenerateFragment();
            } else if (position == 1) {
                return new ProfilesFragment();
            } else if (position == 2) {
                return new AboutFragment();
            }
            return new Fragment();
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section_generate).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section_profiles).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section_about).toUpperCase(l);
            }
            return null;
        }
    }

    public class UpdatedProfilesEvent {

        public List<PMProfile> profileList;

        public UpdatedProfilesEvent(List<PMProfile> profileList) {
            if (profileList != null) {
                this.profileList = new ArrayList<PMProfile>();
                this.profileList.addAll(profileList);
            } else {
                this.profileList = null;
            }
        }
    }
}
