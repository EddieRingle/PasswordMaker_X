package io.github.eddieringle.android.apps.passwordmaker.core;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class PMProfile implements Serializable {

    @SerializedName("use_url_domain")
    private Boolean mUseUrlDomain;

    @SerializedName("use_url_other")
    private Boolean mUseUrlOther;

    @SerializedName("use_url_protocol")
    private Boolean mUseUrlProtocol;

    @SerializedName("use_url_subdomain")
    private Boolean mUseUrlSubdomain;

    @SerializedName("l33t_level")
    private Integer mL33tLevel;

    @SerializedName("password_length")
    private Integer mPasswordLength;

    @SerializedName("charset")
    private String mCharacterSet;

    @SerializedName("hash_algorithm")
    private String mHashAlgorithm;

    @SerializedName("l33t_order")
    private String mL33tOrder;

    @SerializedName("modifier")
    private String mModifier;

    @SerializedName("password_prefix")
    private String mPasswordPrefix;

    @SerializedName("password_suffix")
    private String mPasswordSuffix;

    @SerializedName("name")
    private String mProfileName;

    @SerializedName("username")
    private String mUsername;

    public Boolean getUseUrlDomain() {
        return mUseUrlDomain;
    }

    public void setUseUrlDomain(Boolean useUrlDomain) {
        mUseUrlDomain = useUrlDomain;
    }

    public Boolean getUseUrlOther() {
        return mUseUrlOther;
    }

    public void setUseUrlOther(Boolean useUrlOther) {
        mUseUrlOther = useUrlOther;
    }

    public Boolean getUseUrlProtocol() {
        return mUseUrlProtocol;
    }

    public void setUseUrlProtocol(Boolean useUrlProtocol) {
        mUseUrlProtocol = useUrlProtocol;
    }

    public Boolean getUseUrlSubdomain() {
        return mUseUrlSubdomain;
    }

    public void setUseUrlSubdomain(Boolean useUrlSubdomain) {
        mUseUrlSubdomain = useUrlSubdomain;
    }

    public Integer getL33tLevel() {
        return mL33tLevel;
    }

    public void setL33tLevel(Integer l33tLevel) {
        mL33tLevel = l33tLevel;
    }

    public Integer getPasswordLength() {
        return mPasswordLength;
    }

    public void setPasswordLength(Integer passwordLength) {
        mPasswordLength = passwordLength;
    }

    public String getCharacterSet() {
        return mCharacterSet;
    }

    public void setCharacterSet(String characterSet) {
        mCharacterSet = characterSet;
    }

    public String getHashAlgorithm() {
        return mHashAlgorithm;
    }

    public void setHashAlgorithm(String hashAlgorithm) {
        mHashAlgorithm = hashAlgorithm;
    }

    public String getL33tOrder() {
        return mL33tOrder;
    }

    public void setL33tOrder(String l33tOrder) {
        mL33tOrder = l33tOrder;
    }

    public String getModifier() {
        return mModifier;
    }

    public void setModifier(String modifier) {
        mModifier = modifier;
    }

    public String getPasswordPrefix() {
        return mPasswordPrefix;
    }

    public void setPasswordPrefix(String passwordPrefix) {
        mPasswordPrefix = passwordPrefix;
    }

    public String getPasswordSuffix() {
        return mPasswordSuffix;
    }

    public void setPasswordSuffix(String passwordSuffix) {
        mPasswordSuffix = passwordSuffix;
    }

    public String getProfileName() {
        return mProfileName;
    }

    public void setProfileName(String profileName) {
        mProfileName = profileName;
    }

    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String username) {
        mUsername = username;
    }

    public static PMProfile createDefault() {
        PMProfile defaultProfile = new PMProfile();
        defaultProfile.setCharacterSet(PMConstants.CHARSET_ALPHANUMSYM);
        defaultProfile.setHashAlgorithm(PMConstants.HASH_MD5);
        defaultProfile.setL33tLevel(1);
        defaultProfile.setL33tOrder(PMConstants.L33T_NEVER);
        defaultProfile.setModifier("");
        defaultProfile.setPasswordLength(8);
        defaultProfile.setPasswordPrefix("");
        defaultProfile.setPasswordSuffix("");
        defaultProfile.setProfileName("Default");
        defaultProfile.setUsername("");
        defaultProfile.setUseUrlDomain(true);
        defaultProfile.setUseUrlOther(false);
        defaultProfile.setUseUrlProtocol(false);
        defaultProfile.setUseUrlSubdomain(false);
        return defaultProfile;
    }
}
