package io.github.eddieringle.android.apps.passwordmaker.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.InjectView;
import butterknife.Views;
import io.github.eddieringle.android.apps.passwordmaker.R;
import io.github.eddieringle.android.apps.passwordmaker.core.PMProfile;
import io.github.eddieringle.android.apps.passwordmaker.ui.activity.BaseActivity;
import io.github.eddieringle.android.apps.passwordmaker.ui.activity.MainActivity;
import io.github.eddieringle.android.apps.passwordmaker.ui.activity.ProfileEditActivity;
import io.github.eddieringle.android.apps.passwordmaker.ui.adapter.BaseListAdapter;
import io.github.eddieringle.android.apps.passwordmaker.util.GsonUtils;

public class ProfilesFragment extends ListFragment {

    private ActionMode mActionMode;

    private ArrayList<PMProfile> mProfiles = new ArrayList<PMProfile>();

    private ProfilesListAdapter mAdapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getBaseActivity().getBus().register(this);
        getListView().setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        getListView().setMultiChoiceModeListener(new MultiChoiceModeImpl());
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.profiles, menu);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        PMProfile profile = mProfiles.get(position);
        final Intent editProfile = new Intent(getBaseActivity(), ProfileEditActivity.class);
        editProfile.putExtra("profile", GsonUtils.toJson(profile));
        getBaseActivity().startActivityForResult(editProfile, 1);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add_profile) {
            final Intent newProfile = new Intent(getBaseActivity(), ProfileEditActivity.class);
            PMProfile emptyProfile = PMProfile.createDefault();
            emptyProfile.setProfileName("");
            newProfile.putExtra("profile", GsonUtils.toJson(emptyProfile));
            getBaseActivity().startActivityForResult(newProfile, 1);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public BaseActivity getBaseActivity() {
        return (BaseActivity) getActivity();
    }

    public void loadProfiles(List<PMProfile> profileList) {
        mProfiles.clear();
        mProfiles.addAll(profileList);
        mAdapter = new ProfilesListAdapter(getBaseActivity());
        mAdapter.addAll(mProfiles);
        setListAdapter(mAdapter);
    }

    @Subscribe
    public void receiveUpdatedProfiles(MainActivity.UpdatedProfilesEvent event) {
        if (event != null && event.profileList != null) {
            loadProfiles(event.profileList);
        }
    }

    class MultiChoiceModeImpl implements AbsListView.MultiChoiceModeListener {

        int checkedCount = 0;

        HashMap<Integer, Boolean> checkMap = new HashMap<Integer, Boolean>();

        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            if (checked) {
                checkedCount++;
            } else {
                checkedCount--;
            }
            checkMap.put(position, checked);
            mAdapter.setNewSelection(position, checked);
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.profiles_cab, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.action_delete) {
                final ArrayList<PMProfile> trimList = new ArrayList<PMProfile>();
                int i = 0;
                for (PMProfile p : mProfiles) {
                    if (!(checkMap.get(i) != null && checkMap.get(i))) {
                        trimList.add(p);
                    }
                    i++;
                }
                if (trimList.isEmpty()) {
                    trimList.add(PMProfile.createDefault());
                    Toast.makeText(getBaseActivity(), R.string.toast_profile_list_default, Toast.LENGTH_SHORT).show();
                }
                getBaseActivity().getPrefsEditor()
                        .putString("profiles", GsonUtils.toJson(trimList))
                        .commit();
                getBaseActivity().getBus().post(new NeedProfileListRefreshEvent());
                mAdapter.clearSelection();
                mode.finish();
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
        }
    }

    class ProfilesListAdapter extends BaseListAdapter<PMProfile> {

        public ProfilesListAdapter(Context context) {
            super(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(android.R.layout.simple_list_item_1, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            PMProfile item = getItem(position);
            holder.profileName.setText(item.getProfileName());

            convertView.setBackgroundColor(Color.TRANSPARENT);
            if (isPositionChecked(position)) {
                convertView.setBackgroundResource(R.color.checked_passwordmaker);
            }

            return convertView;
        }

        class ViewHolder {

            @InjectView(android.R.id.text1)
            TextView profileName;

            public ViewHolder(View v) {
                Views.inject(this, v);
            }
        }
    }

    public static class NeedProfileListRefreshEvent {
    }
}
