package io.github.eddieringle.android.apps.passwordmaker.ui.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import io.github.eddieringle.android.apps.passwordmaker.R;
import io.github.eddieringle.android.apps.passwordmaker.ui.fragment.ProfileEditFragment;

public class ProfileEditActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_profile_edit);

        FragmentManager fm = getSupportFragmentManager();
        Fragment f = fm.findFragmentByTag(ProfileEditFragment.class.getName());
        if (f == null) {
            f = new ProfileEditFragment();
            f.setArguments(getIntent().getExtras());
            fm.beginTransaction()
                    .add(R.id.container, f, ProfileEditFragment.class.getName())
                    .commit();
        }

        setResult(RESULT_CANCELED);
    }
}
