package io.github.eddieringle.android.apps.passwordmaker.ui.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.ArrayList;

import butterknife.InjectView;
import butterknife.Views;
import io.github.eddieringle.android.apps.passwordmaker.BuildConfig;
import io.github.eddieringle.android.apps.passwordmaker.R;
import io.github.eddieringle.android.apps.passwordmaker.core.PMProfile;
import io.github.eddieringle.android.apps.passwordmaker.ui.activity.BaseActivity;
import io.github.eddieringle.android.apps.passwordmaker.ui.activity.MainActivity;
import io.github.eddieringle.android.apps.passwordmaker.ui.activity.ProfileEditActivity;
import io.github.eddieringle.android.apps.passwordmaker.ui.adapter.BaseListAdapter;
import io.github.eddieringle.android.apps.passwordmaker.util.GsonUtils;

public class GenerateFragment extends Fragment {

    private ArrayList<PMProfile> mProfiles = new ArrayList<PMProfile>();

    private PMProfile mSelectedProfile;

    private WebView mWebView;

    @InjectView(R.id.profile_selection)
    Spinner mProfileSelection;

    @InjectView(R.id.edit_profile)
    ImageButton mEditProfile;

    @InjectView(R.id.input_text)
    EditText mInputText;

    @InjectView(R.id.master_password)
    EditText mMasterPassword;

    @InjectView(R.id.generated_password)
    EditText mGeneratedPassword;

    @InjectView(R.id.copy_password)
    ImageButton mCopyPassword;

    @InjectView(R.id.using_text)
    TextView mUsingText;

    private static String processInputText(PMProfile profile, String input) {
        if (input.isEmpty()) {
            return "";
        }
        Uri uri;
        boolean emptyProtocol;
        if (input.contains("://")) {
            emptyProtocol = false;
            uri = Uri.parse(input);
        } else {
            emptyProtocol = true;
            uri = Uri.parse("://" + input);
        }
        StringBuilder result = new StringBuilder(input.length());
        if (profile.getUseUrlProtocol() && uri.getScheme() != null && !emptyProtocol) {
            result.append(uri.getScheme());
            result.append(':');
            if (input.contains("://")) {
                result.append("//");
            }
        }
        if (uri.getUserInfo() != null) {
            result.append(uri.getUserInfo()).append('@');
        }
        if (uri.getHost() != null) {
            String[] host = uri.getHost().split("\\.");
            if (profile.getUseUrlSubdomain() && host.length > 2) {
                for (int i = 0; i < host.length - 2; i++) {
                    if (i != 0) {
                        result.append('.');
                    }
                    result.append(host[i]);
                }
                if (profile.getUseUrlDomain()) {
                    result.append('.');
                }
            }
            if (profile.getUseUrlDomain()) {
                if (host.length > 1) {
                    result.append(host[host.length - 2]);
                }
                if (host.length > 0) {
                    result.append('.').append(host[host.length - 1]);
                }
            }
        }
        if (profile.getUseUrlOther()) {
            if (uri.getPort() >= 0) {
                result.append(':').append(uri.getPort());
            }
            boolean hasPath = false;
            if (uri.getPath() != null) {
                hasPath = true;
                result.append(uri.getPath());
            }
            if (uri.getQuery() != null) {
                if (!hasPath) {
                    result.append('/');
                }
                result.append('?').append(uri.getQuery());
            }
        }
        return result.toString();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_generate, container, false);
        Views.inject(this, v);

        mWebView = new WebView(getActivity());
        mWebView.setVisibility(View.GONE);
        ((ViewGroup)((ViewGroup)v).getChildAt(0)).addView(mWebView);

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(new PmxAppInterface(GenerateFragment.this), "pmxApp");
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                return true;
            }
        });
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if ("file:///android_asset/passwordmaker.html".equals(url)) {
                    String useText = processInputText(mSelectedProfile, mInputText.getText().toString());
                    mUsingText.setText(getString(R.string.using_text, useText));
                    StringBuilder sb = new StringBuilder();
                    sb.append("javascript:pmxApp.submitPassword(pmxGenerate(");
                    sb.append("'" + StringEscapeUtils.escapeEcmaScript(mSelectedProfile.getCharacterSet()) + "',");
                    sb.append("'" + mSelectedProfile.getHashAlgorithm() + "',");
                    sb.append("'" + mSelectedProfile.getL33tOrder() + "',");
                    sb.append(Integer.toString(mSelectedProfile.getL33tLevel()) + ",");
                    sb.append(Integer.toString(mSelectedProfile.getPasswordLength()) + ",");
                    sb.append("'" + mMasterPassword.getText().toString() + "',");
                    sb.append("'" + useText + "',");
                    sb.append("'" + StringEscapeUtils.escapeEcmaScript(mSelectedProfile.getUsername()) + "',");
                    sb.append("'" + StringEscapeUtils.escapeEcmaScript(mSelectedProfile.getModifier()) + "',");
                    sb.append("'" + StringEscapeUtils.escapeEcmaScript(mSelectedProfile.getPasswordPrefix()) + "',");
                    sb.append("'" + StringEscapeUtils.escapeEcmaScript(mSelectedProfile.getPasswordSuffix()) + "',");
                    sb.append("pmxApp));"); /* callback */

                    mWebView.loadUrl(sb.toString());
                }
            }
        });

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final TextWatcher onInputChanged = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                mWebView.loadUrl("file:///android_asset/passwordmaker.html");
            }
        };

        mProfileSelection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSelectedProfile = mProfiles.get(position);
                getBaseActivity().getPrefsEditor()
                        .putString("current_profile", mSelectedProfile.getProfileName())
                        .commit();
                mWebView.loadUrl("file:///android_asset/passwordmaker.html");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        mEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent editProfile = new Intent(getBaseActivity(), ProfileEditActivity.class);
                editProfile.putExtra("profile", GsonUtils.toJson(mSelectedProfile));
                getBaseActivity().startActivityForResult(editProfile, 1);
            }
        });
        mInputText.addTextChangedListener(onInputChanged);
        mMasterPassword.addTextChangedListener(onInputChanged);
        mCopyPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mGeneratedPassword.getText().toString().isEmpty()) {
                    ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboard.setPrimaryClip(ClipData.newPlainText("PMX Password", mGeneratedPassword.getText().toString()));
                    Toast.makeText(getActivity(), "Password copied to clipboard", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "No password to copy", Toast.LENGTH_SHORT).show();
                }
            }
        });

        getBaseActivity().getBus().register(this);
    }

    public BaseActivity getBaseActivity() {
        return (BaseActivity) getActivity();
    }

    @Subscribe
    public void receiveUpdatedProfiles(MainActivity.UpdatedProfilesEvent event) {
        if (event != null && event.profileList != null) {
            mProfiles.clear();
            mProfiles.addAll(event.profileList);
            ProfileSpinnerAdapter spinnerAdapter = new ProfileSpinnerAdapter(getBaseActivity());
            spinnerAdapter.addAll(event.profileList);
            mProfileSelection.setAdapter(spinnerAdapter);

            String current = getBaseActivity().getPrefs().getString("current_profile", "");
            int i = 0;
            for (PMProfile p : event.profileList) {
                if (p == null) {
                    continue;
                }
                if (current.equals(p.getProfileName())) {
                    mSelectedProfile = p;
                    mProfileSelection.setSelection(i);
                    break;
                }
                i++;
            }

            mWebView.loadUrl("file:///android_asset/passwordmaker.html");
        }
    }

    class PmxAppInterface {

        GenerateFragment mFragment;

        public PmxAppInterface(GenerateFragment f) {
            mFragment = f;
        }

        @JavascriptInterface
        public void submitPassword(final String password) {
            mFragment.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mFragment.mGeneratedPassword.setText(password);
                }
            });
        }

        @JavascriptInterface
        public void logToLolcat(String msg) {
            if (BuildConfig.DEBUG) {
                Log.d("PmxAppInterface", msg);
            }
        }
    }

    class ProfileSpinnerAdapter extends BaseListAdapter<PMProfile> {

        public ProfileSpinnerAdapter(Context context) {
            super(context);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return getBaseView(position, convertView, parent, android.R.layout.simple_spinner_dropdown_item);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getBaseView(position, convertView, parent, android.R.layout.simple_spinner_item);
        }

        public View getBaseView(int position, View convertView, ViewGroup parent, int layout) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(layout, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            PMProfile item = getItem(position);
            holder.profileName.setText(item.getProfileName());

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
}
