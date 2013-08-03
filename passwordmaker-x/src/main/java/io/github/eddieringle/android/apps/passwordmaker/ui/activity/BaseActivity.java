package io.github.eddieringle.android.apps.passwordmaker.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;

import io.github.eddieringle.android.apps.passwordmaker.R;
import io.github.eddieringle.android.apps.passwordmaker.ext.ScopedBus;

public class BaseActivity extends FragmentActivity {

    public static final int NO_LAYOUT = -1;

    private SharedPreferences mPrefs;

    private SharedPreferences.Editor mPrefsEditor;

    private ScopedBus mBus = new ScopedBus();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        onCreate(savedInstanceState, NO_LAYOUT);
    }

    protected final void onCreate(Bundle savedInstanceState, int layout) {
        super.onCreate(savedInstanceState);
        if (layout != NO_LAYOUT) {
            setContentView(layout);
        }

        mPrefs = PreferenceManager.getDefaultSharedPreferences(BaseActivity.this);
        mPrefsEditor = mPrefs.edit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_report_issue:
                final Intent githubIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.report_issue_url)));
                startActivity(githubIntent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public ScopedBus getBus() {
        return mBus;
    }

    public SharedPreferences getPrefs() {
        return mPrefs;
    }

    public SharedPreferences.Editor getPrefsEditor() {
        return mPrefsEditor;
    }
}
