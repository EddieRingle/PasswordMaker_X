package io.github.eddieringle.android.apps.passwordmaker.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.common.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.InjectView;
import butterknife.Views;
import io.github.eddieringle.android.apps.passwordmaker.R;
import io.github.eddieringle.android.apps.passwordmaker.core.PMConstants;
import io.github.eddieringle.android.apps.passwordmaker.core.PMProfile;
import io.github.eddieringle.android.apps.passwordmaker.ui.activity.BaseActivity;
import io.github.eddieringle.android.apps.passwordmaker.util.GsonUtils;

public class ProfileEditFragment extends Fragment {

    private ArrayAdapter<String> mCharacterSetAdapter;

    private ArrayAdapter<String> mHashesAdapter;

    private ArrayAdapter<String> mL33tLevelAdapter;

    private ArrayAdapter<String> mL33tOrderAdapter;

    private String mOldName;

    private String[] mCharsets;

    private String[] mHashes;

    private String[] mL33tLevels;

    private String[] mL33tOrders;

    private PMProfile mProfile;

    private String mCustomCharsetCache;

    @InjectView(R.id.use_domain)
    CheckBox mUseDomain;

    @InjectView(R.id.use_other)
    CheckBox mUseOther;

    @InjectView(R.id.use_protocol)
    CheckBox mUseProtocol;

    @InjectView(R.id.use_subdomain)
    CheckBox mUseSubdomain;

    @InjectView(R.id.custom_character_set)
    EditText mCustomCharacterSet;

    @InjectView(R.id.modifier)
    EditText mModifier;

    @InjectView(R.id.password_length)
    EditText mPasswordLength;

    @InjectView(R.id.password_prefix)
    EditText mPasswordPrefix;

    @InjectView(R.id.password_suffix)
    EditText mPasswordSuffix;

    @InjectView(R.id.profile_name)
    EditText mProfileName;

    @InjectView(R.id.username)
    EditText mUsername;

    @InjectView(R.id.character_set)
    Spinner mCharacterSet;

    @InjectView(R.id.hashing_algorithm)
    Spinner mHashingAlgorithm;

    @InjectView(R.id.l33t_level)
    Spinner mL33tLevel;

    @InjectView(R.id.l33t_order)
    Spinner mL33tOrder;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle args = getArguments();
        mProfile = GsonUtils.fromJson(args.getString("profile", ""), PMProfile.class);

        getBaseActivity().getSupportActionBar().setSubtitle(mProfile.getProfileName());

        if (mProfile.getProfileName() == null || mProfile.getProfileName().isEmpty()) {
            getBaseActivity().getSupportActionBar().setTitle("New Profile");
            getBaseActivity().getSupportActionBar().setSubtitle(null);
            mProfile.setProfileName("");
        }

        mOldName = mProfile.getProfileName();

        mCharsets = getResources().getStringArray(R.array.character_sets);
        mCharacterSetAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, mCharsets);
        mCharacterSet.setAdapter(mCharacterSetAdapter);

        mHashes = getResources().getStringArray(R.array.hash_algorithms);
        mHashesAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, mHashes);
        mHashingAlgorithm.setAdapter(mHashesAdapter);

        mL33tLevels = getResources().getStringArray(R.array.l33t_levels);
        mL33tLevelAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, mL33tLevels);
        mL33tLevel.setAdapter(mL33tLevelAdapter);

        mL33tOrders = getResources().getStringArray(R.array.l33t_orders);
        mL33tOrderAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, mL33tOrders);
        mL33tOrder.setAdapter(mL33tOrderAdapter);

        setFields(mProfile);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile_edit, container, false);
        Views.inject(this, v);
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.edit_profile, menu);

        ActionBar bar = getBaseActivity().getSupportActionBar();
        bar.setHomeButtonEnabled(true);
        bar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save && validateInputs()) {
            boolean addedEarly = false;
            ArrayList<PMProfile> profiles = new ArrayList<PMProfile>();
            TypeToken<List<PMProfile>> listToken = new TypeToken<List<PMProfile>>(){};
            String listJson = getBaseActivity().getPrefs().getString("profiles", "");
            List<PMProfile> profileList = GsonUtils.fromJson(listJson, listToken.getType());
            for (PMProfile p : profileList) {
                if (!addedEarly) {
                  if (p.getProfileName().equals(mProfile.getProfileName())) {
                    profiles.add(mProfile);
                    addedEarly = true;
                    continue;
                  }
                }
                if (p.getProfileName().equals(mOldName)) {
                    continue;
                }
                profiles.add(p);
            }
            if (!addedEarly) {
                profiles.add(mProfile);
            }
            getBaseActivity().getPrefsEditor()
                    .putString("profiles", GsonUtils.toJson(profiles))
                    .commit();
            if (getBaseActivity().getPrefs().getString("current_profile", "").equals(mOldName)) {
                getBaseActivity().getPrefsEditor()
                        .putString("current_profile", mProfile.getProfileName())
                        .commit();
            }
            Toast.makeText(getBaseActivity(), R.string.toast_profile_save_success, Toast.LENGTH_SHORT).show();
            getBaseActivity().setResult(Activity.RESULT_OK);
            getBaseActivity().finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public BaseActivity getBaseActivity() {
        return (BaseActivity) getActivity();
    }

    public void setFields(final PMProfile profile) {
        mUseDomain.setChecked(profile.getUseUrlDomain());
        mUseOther.setChecked(profile.getUseUrlOther());
        mUseProtocol.setChecked(profile.getUseUrlProtocol());
        mUseSubdomain.setChecked(profile.getUseUrlSubdomain());

        mModifier.setText((profile.getModifier()!=null)?profile.getModifier():"");
        mPasswordLength.setText((profile.getPasswordLength()!=null)?Integer.toString(profile.getPasswordLength()):"");
        mPasswordPrefix.setText((profile.getPasswordPrefix()!=null)?profile.getPasswordPrefix():"");
        mPasswordSuffix.setText((profile.getPasswordSuffix()!=null)?profile.getPasswordSuffix():"");
        mProfileName.setText((profile.getProfileName()!=null)?profile.getProfileName():"");
        mUsername.setText((profile.getUsername()!=null)?profile.getUsername():"");

        final String charsetAlphaName = getString(R.string.charset_alpha_name);
        final String charsetAlphaNumName = getString(R.string.charset_alphanum_name);
        final String charsetAlphaNumSymName = getString(R.string.charset_alphanumsym_name);
        final String charsetHexName = getString(R.string.charset_hex_name);
        final String charsetNumbersName = getString(R.string.charset_numbers_name);
        final String charsetSymbolsName = getString(R.string.charset_symbols_name);
        final String charsetCustomName = getString(R.string.charset_custom_name);

        final List<String> charsetList = Arrays.asList(mCharsets);
        final int charsetAlphaIndex = charsetList.indexOf(charsetAlphaName);
        final int charsetAlphaNumIndex = charsetList.indexOf(charsetAlphaNumName);
        final int charsetAlphaNumSymIndex = charsetList.indexOf(charsetAlphaNumSymName);
        final int charsetHexadecimalIndex = charsetList.indexOf(charsetHexName);
        final int charsetNumbersIndex = charsetList.indexOf(charsetNumbersName);
        final int charsetSymbolsIndex = charsetList.indexOf(charsetSymbolsName);
        final int charsetCustomIndex = charsetList.indexOf(charsetCustomName);

        final String charset = profile.getCharacterSet();
        mCustomCharsetCache = null;
        mCustomCharacterSet.setEnabled(false);
        if (PMConstants.CHARSET_ALPHA.equals(charset)) {
            mCharacterSet.setSelection(charsetAlphaIndex);
            mCharacterSet.setTag(PMConstants.CHARSET_ALPHA);
        } else if (PMConstants.CHARSET_ALPHANUM.equals(charset)) {
            mCharacterSet.setSelection(charsetAlphaNumIndex);
            mCharacterSet.setTag(PMConstants.CHARSET_ALPHANUM);
        } else if (PMConstants.CHARSET_ALPHANUMSYM.equals(charset)) {
            mCharacterSet.setSelection(charsetAlphaNumSymIndex);
            mCharacterSet.setTag(PMConstants.CHARSET_ALPHANUMSYM);
        } else if (PMConstants.CHARSET_HEX.equals(charset)) {
            mCharacterSet.setSelection(charsetHexadecimalIndex);
            mCharacterSet.setTag(PMConstants.CHARSET_HEX);
        } else if (PMConstants.CHARSET_NUMBERS.equals(charset)) {
            mCharacterSet.setSelection(charsetNumbersIndex);
            mCharacterSet.setTag(PMConstants.CHARSET_NUMBERS);
        } else if (PMConstants.CHARSET_SYMBOLS.equals(charset)) {
            mCharacterSet.setSelection(charsetSymbolsIndex);
            mCharacterSet.setTag(PMConstants.CHARSET_SYMBOLS);
        } else {
            mCustomCharsetCache = charset;
            mCharacterSet.setSelection(charsetCustomIndex);
            mCharacterSet.setTag(PMConstants.CHARSET_CUSTOM);
            mCustomCharacterSet.setText(charset);
            mCustomCharacterSet.setEnabled(true);
        }

        mCharacterSet.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mCustomCharacterSet.setEnabled(false);
                if (position == charsetAlphaIndex) {
                    mCharacterSet.setTag(PMConstants.CHARSET_ALPHA);
                    mCustomCharacterSet.setText(PMConstants.CHARSET_ALPHA);
                } else if (position == charsetAlphaNumIndex) {
                    mCharacterSet.setTag(PMConstants.CHARSET_ALPHANUM);
                    mCustomCharacterSet.setText(PMConstants.CHARSET_ALPHANUM);
                } else if (position == charsetAlphaNumSymIndex) {
                    mCharacterSet.setTag(PMConstants.CHARSET_ALPHANUMSYM);
                    mCustomCharacterSet.setText(PMConstants.CHARSET_ALPHANUMSYM);
                } else if (position == charsetHexadecimalIndex) {
                    mCharacterSet.setTag(PMConstants.CHARSET_HEX);
                    mCustomCharacterSet.setText(PMConstants.CHARSET_HEX);
                } else if (position == charsetNumbersIndex) {
                    mCharacterSet.setTag(PMConstants.CHARSET_NUMBERS);
                    mCustomCharacterSet.setText(PMConstants.CHARSET_NUMBERS);
                } else if (position == charsetSymbolsIndex) {
                    mCharacterSet.setTag(PMConstants.CHARSET_SYMBOLS);
                    mCustomCharacterSet.setText(PMConstants.CHARSET_SYMBOLS);
                } else if (position == charsetCustomIndex) {
                    mCharacterSet.setTag(PMConstants.CHARSET_CUSTOM);
                    if (mCustomCharsetCache != null && !mCustomCharsetCache.isEmpty()) {
                        mCustomCharacterSet.setText(mCustomCharsetCache);
                    }
                    mCustomCharacterSet.setEnabled(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        mCustomCharacterSet.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mCharacterSet.getSelectedItemPosition() == charsetCustomIndex) {
                    mCustomCharsetCache = s.toString();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        final String hashMd4Name = getString(R.string.hash_md4);
        final String hashHmacMd4Name = getString(R.string.hash_hmac_md4);
        final String hashMd5Name = getString(R.string.hash_md5);
        final String hashMd5v6Name = getString(R.string.hash_md5_v6);
        final String hashHmacMd5Name = getString(R.string.hash_hmac_md5);
        final String hashHmacMd5v6Name = getString(R.string.hash_hmac_md5_v6);
        final String hashSha1Name = getString(R.string.hash_sha1);
        final String hashHmacSha1Name = getString(R.string.hash_hmac_sha1);
        final String hashSha256Name = getString(R.string.hash_sha256);
        final String hashHmacSha256FixName = getString(R.string.hash_hmac_sha256_fix);
        final String hashHmacSha256Name = getString(R.string.hash_hmac_sha256);
        final String hashRmd160Name = getString(R.string.hash_rmd160);
        final String hashHmacRmd160Name = getString(R.string.hash_hmac_rmd160);

        final List<String> hashList = Arrays.asList(mHashes);
        final int hashMd4Index = hashList.indexOf(hashMd4Name);
        final int hashHmacMd4Index = hashList.indexOf(hashHmacMd4Name);
        final int hashMd5Index = hashList.indexOf(hashMd5Name);
        final int hashMd5v6Index = hashList.indexOf(hashMd5v6Name);
        final int hashHmacMd5Index = hashList.indexOf(hashHmacMd5Name);
        final int hashHmacMd5v6Index = hashList.indexOf(hashHmacMd5v6Name);
        final int hashSha1Index = hashList.indexOf(hashSha1Name);
        final int hashHmacSha1Index = hashList.indexOf(hashHmacSha1Name);
        final int hashSha256Index = hashList.indexOf(hashSha256Name);
        final int hashHmacSha256FixIndex = hashList.indexOf(hashHmacSha256FixName);
        final int hashHmacSha256Index = hashList.indexOf(hashHmacSha256Name);
        final int hashRmd160Index = hashList.indexOf(hashRmd160Name);
        final int hashHmacRmd160Index = hashList.indexOf(hashHmacRmd160Name);

        final String hash = profile.getHashAlgorithm();
        if (PMConstants.HASH_MD4.equals(hash)) {
            mHashingAlgorithm.setSelection(hashMd4Index);
            mHashingAlgorithm.setTag(PMConstants.HASH_MD4);
        } else if (PMConstants.HASH_HMAC_MD4.equals(hash)) {
            mHashingAlgorithm.setSelection(hashHmacMd4Index);
            mHashingAlgorithm.setTag(PMConstants.HASH_HMAC_MD4);
        } else if (PMConstants.HASH_MD5.equals(hash)) {
            mHashingAlgorithm.setSelection(hashMd5Index);
            mHashingAlgorithm.setTag(PMConstants.HASH_MD5);
        } else if (PMConstants.HASH_MD5_V6.equals(hash)) {
            mHashingAlgorithm.setSelection(hashMd5v6Index);
            mHashingAlgorithm.setTag(PMConstants.HASH_MD5_V6);
        } else if (PMConstants.HASH_HMAC_MD5.equals(hash)) {
            mHashingAlgorithm.setSelection(hashHmacMd5Index);
            mHashingAlgorithm.setTag(PMConstants.HASH_HMAC_MD5);
        } else if (PMConstants.HASH_HMAC_MD5_V6.equals(hash)) {
            mHashingAlgorithm.setSelection(hashHmacMd5v6Index);
            mHashingAlgorithm.setTag(PMConstants.HASH_HMAC_MD5_V6);
        } else if (PMConstants.HASH_SHA1.equals(hash)) {
            mHashingAlgorithm.setSelection(hashSha1Index);
            mHashingAlgorithm.setTag(PMConstants.HASH_SHA1);
        } else if (PMConstants.HASH_HMAC_SHA1.equals(hash)) {
            mHashingAlgorithm.setSelection(hashHmacSha1Index);
            mHashingAlgorithm.setTag(PMConstants.HASH_HMAC_SHA1);
        } else if (PMConstants.HASH_SHA256.equals(hash)) {
            mHashingAlgorithm.setSelection(hashSha256Index);
            mHashingAlgorithm.setTag(PMConstants.HASH_SHA256);
        } else if (PMConstants.HASH_HMAC_SHA256_FIX.equals(hash)) {
            mHashingAlgorithm.setSelection(hashHmacSha256FixIndex);
            mHashingAlgorithm.setTag(PMConstants.HASH_HMAC_SHA256_FIX);
        } else if (PMConstants.HASH_HMAC_SHA256.equals(hash)) {
            mHashingAlgorithm.setSelection(hashHmacSha256Index);
            mHashingAlgorithm.setTag(PMConstants.HASH_HMAC_SHA256);
        } else if (PMConstants.HASH_RMD160.equals(hash)) {
            mHashingAlgorithm.setSelection(hashRmd160Index);
            mHashingAlgorithm.setTag(PMConstants.HASH_RMD160);
        } else if (PMConstants.HASH_HMAC_RMD160.equals(hash)) {
            mHashingAlgorithm.setSelection(hashHmacRmd160Index);
            mHashingAlgorithm.setTag(PMConstants.HASH_HMAC_RMD160);
        }

        mHashingAlgorithm.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == hashMd4Index) {
                    mHashingAlgorithm.setTag(PMConstants.HASH_MD4);
                } else if (position == hashHmacMd4Index) {
                    mHashingAlgorithm.setTag(PMConstants.HASH_HMAC_MD4);
                } else if (position == hashMd5Index) {
                    mHashingAlgorithm.setTag(PMConstants.HASH_MD5);
                } else if (position == hashMd5v6Index) {
                    mHashingAlgorithm.setTag(PMConstants.HASH_MD5_V6);
                } else if (position == hashHmacMd5Index) {
                    mHashingAlgorithm.setTag(PMConstants.HASH_HMAC_MD5);
                } else if (position == hashHmacMd5v6Index) {
                    mHashingAlgorithm.setTag(PMConstants.HASH_HMAC_MD5_V6);
                } else if (position == hashSha1Index) {
                    mHashingAlgorithm.setTag(PMConstants.HASH_SHA1);
                } else if (position == hashHmacSha1Index) {
                    mHashingAlgorithm.setTag(PMConstants.HASH_HMAC_SHA1);
                } else if (position == hashSha256Index) {
                    mHashingAlgorithm.setTag(PMConstants.HASH_SHA256);
                } else if (position == hashHmacSha256FixIndex) {
                    mHashingAlgorithm.setTag(PMConstants.HASH_HMAC_SHA256_FIX);
                } else if (position == hashHmacSha256Index) {
                    mHashingAlgorithm.setTag(PMConstants.HASH_HMAC_SHA256);
                } else if (position == hashRmd160Index) {
                    mHashingAlgorithm.setTag(PMConstants.HASH_RMD160);
                } else if (position == hashHmacRmd160Index) {
                    mHashingAlgorithm.setTag(PMConstants.HASH_HMAC_RMD160);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        Integer l33tLevel = profile.getL33tLevel();
        if (l33tLevel == null || l33tLevel < 0) {
            l33tLevel = 0;
        } else if (l33tLevel > 8) {
            l33tLevel = 8;
        }
        mL33tLevel.setSelection(l33tLevel);
        mL33tLevel.setEnabled(true);

        final String l33tNeverName = getString(R.string.l33t_use_never);
        final String l33tBeforeName = getString(R.string.l33t_use_before);
        final String l33tAfterName = getString(R.string.l33t_use_after);
        final String l33tBothName = getString(R.string.l33t_use_before_and_after);

        List<String> l33tOrderList = Arrays.asList(mL33tOrders);
        final int l33tNeverIndex = l33tOrderList.indexOf(l33tNeverName);
        final int l33tBeforeIndex = l33tOrderList.indexOf(l33tBeforeName);
        final int l33tAfterIndex = l33tOrderList.indexOf(l33tAfterName);
        final int l33tBothIndex = l33tOrderList.indexOf(l33tBothName);

        final String l33tOrder = profile.getL33tOrder();
        if (PMConstants.L33T_NEVER.equals(l33tOrder)) {
            mL33tOrder.setSelection(l33tNeverIndex);
            mL33tLevel.setEnabled(false);
            mL33tOrder.setTag(PMConstants.L33T_NEVER);
        } else if (PMConstants.L33T_BEFORE.equals(l33tOrder)) {
            mL33tOrder.setSelection(l33tBeforeIndex);
            mL33tOrder.setTag(PMConstants.L33T_BEFORE);
        } else if (PMConstants.L33T_AFTER.equals(l33tOrder)) {
            mL33tOrder.setSelection(l33tAfterIndex);
            mL33tOrder.setTag(PMConstants.L33T_AFTER);
        } else if (PMConstants.L33T_BEFORE_AND_AFTER.equals(l33tOrder)) {
            mL33tOrder.setSelection(l33tBothIndex);
            mL33tOrder.setTag(PMConstants.L33T_BEFORE_AND_AFTER);
        }

        mL33tOrder.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d("ProfileEditFragment", "Enabled? " + position + " != " + l33tNeverIndex);
                mL33tLevel.setEnabled(position != l33tNeverIndex);
                if (position == l33tNeverIndex) {
                    mL33tOrder.setTag(PMConstants.L33T_NEVER);
                } else if (position == l33tBeforeIndex) {
                    mL33tOrder.setTag(PMConstants.L33T_BEFORE);
                } else if (position == l33tAfterIndex) {
                    mL33tOrder.setTag(PMConstants.L33T_AFTER);
                } else if (position == l33tBothIndex) {
                    mL33tOrder.setTag(PMConstants.L33T_BEFORE_AND_AFTER);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    protected boolean validateInputs() {
        final String profileName = mProfileName.getText().toString();
        if (profileName.trim().isEmpty()) {
            mProfileName.setText("");
            mProfileName.setError("Profile name cannot be blank");
            return false;
        }
        if (mPasswordLength.getText().toString().trim().isEmpty()) {
            mPasswordLength.setText("");
            mPasswordLength.setError("Password length cannot be blank");
            return false;
        }
        if (PMConstants.CHARSET_CUSTOM.equals((String)mCharacterSet.getTag())) {
            if (mCustomCharacterSet.getText().toString().length() < 2) {
                mCustomCharacterSet.setError("Custom character set must contain at least 2 characters");
                return false;
            }
        }

        mProfile.setProfileName(mProfileName.getText().toString());
        mProfile.setUseUrlProtocol(mUseProtocol.isChecked());
        mProfile.setUseUrlSubdomain(mUseSubdomain.isChecked());
        mProfile.setUseUrlDomain(mUseDomain.isChecked());
        mProfile.setUseUrlOther(mUseOther.isChecked());
        mProfile.setUsername(mUsername.getText().toString());
        mProfile.setPasswordSuffix(mPasswordSuffix.getText().toString());
        mProfile.setPasswordPrefix(mPasswordPrefix.getText().toString());
        if (PMConstants.CHARSET_CUSTOM.equals((String)mCharacterSet.getTag())) {
            mProfile.setCharacterSet(mCustomCharacterSet.getText().toString());
        } else {
            mProfile.setCharacterSet((String)mCharacterSet.getTag());
        }
        mProfile.setHashAlgorithm((String)mHashingAlgorithm.getTag());
        mProfile.setL33tLevel(mL33tLevel.getSelectedItemPosition());
        mProfile.setL33tOrder((String)mL33tOrder.getTag());
        mProfile.setModifier(mModifier.getText().toString());
        mProfile.setPasswordLength(Integer.parseInt(mPasswordLength.getText().toString()));

        return true;
    }
}
