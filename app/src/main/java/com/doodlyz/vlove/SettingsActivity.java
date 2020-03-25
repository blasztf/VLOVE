package com.doodlyz.vlove;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.List;

public class SettingsActivity extends AppCompatPreferenceActivity {
    public static final String EXTRA_SHOW_INIT_FRAGMENT = SettingsActivity.class.getName() + ".EXTRA_SHOW_INIT_FRAGMENT";

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list pref_general, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else {
                // For all other pref_general, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        if (preference != null) {
            // Set the listener to watch for value changes.
            preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

            // Trigger the listener immediately with the preference's
            // current value.
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getString(preference.getKey(), ""));
        }
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getIntent().getBooleanExtra(EXTRA_SHOW_INIT_FRAGMENT, false)) {
            getIntent().putExtra(PreferenceActivity.EXTRA_NO_HEADERS, true);
            getIntent().putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, isXLargeTablet(this) ? AboutFragment.class.getName() : GeneralFragment.class.getName());
        }

        super.onCreate(savedInstanceState);
        setSupportToolbar();
        fixContentView();

    }

    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(isXLargeTablet(this) ? R.xml.pref_headers_landscape : R.xml.pref_headers_potrait, target);
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return GeneralFragment.class.getName().equals(fragmentName) ||
                PopupFragment.class.getName().equals(fragmentName) ||
                BoardFragment.class.getName().equals(fragmentName) ||
                LaterFragment.class.getName().equals(fragmentName) ||
                SaverFragment.class.getName().equals(fragmentName) ||
                LoginFragment.class.getName().equals(fragmentName) ||
                AboutFragment.class.getName().equals(fragmentName);
    }

    private void fixContentView() {
        int horizontalPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, getResources().getDimension(R.dimen.pref_content_pad), getResources().getDisplayMetrics());
        int verticalPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, getResources().getDimension(R.dimen.pref_content_pad), getResources().getDisplayMetrics());
        int topPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, getActionBarHeight(), getResources().getDisplayMetrics());

        ((View) getListView().getParent().getParent()).setPadding(horizontalPadding, topPadding, horizontalPadding, verticalPadding);
    }

    private int getActionBarHeight() {
        TypedArray styledAttributes = getTheme().obtainStyledAttributes(new int[] { android.R.attr.actionBarSize });
        int actionBarHeight = (int) styledAttributes.getDimension(0, 48+30);
        styledAttributes.recycle();
        return actionBarHeight;
    }

    private void setSupportToolbar() {
        ViewGroup root = findViewById(android.R.id.content);
        ViewGroup layout = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.logo_title, root, false);
        root.addView(layout, 0);

        Toolbar toolbar = layout.findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        toolbar.setTitle(R.string.action_settings);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
    }

    public static class GeneralFragment extends AbstractFragment {
        @Override
        protected void onPreferencesAdd(@Nullable Bundle savedInstanceState) {
            addPreferencesFromResource(R.xml.pref_group_popup);
            addPreferencesFromResource(R.xml.pref_group_board);
            addPreferencesFromResource(R.xml.pref_group_later);
            addPreferencesFromResource(R.xml.pref_group_saver);
            addPreferencesFromResource(R.xml.pref_group_login);
            addPreferencesFromResource(R.xml.pref_group_about);
        }
    }

    public static class PopupFragment extends AbstractFragment {
        @Override
        protected void onPreferencesAdd(@Nullable Bundle savedInstanceState) {
            addPreferencesFromResource(R.xml.pref_group_popup);
        }
    }

    public static class BoardFragment extends AbstractFragment {
        @Override
        protected void onPreferencesAdd(@Nullable Bundle savedInstanceState) {
            addPreferencesFromResource(R.xml.pref_group_board);
        }
    }

    public static class LaterFragment extends AbstractFragment {
        @Override
        protected void onPreferencesAdd(@Nullable Bundle savedInstanceState) {
            addPreferencesFromResource(R.xml.pref_group_later);
        }
    }

    public static class SaverFragment extends AbstractFragment {
        @Override
        protected void onPreferencesAdd(@Nullable Bundle savedInstanceState) {
            addPreferencesFromResource(R.xml.pref_group_saver);
        }
    }

    public static class LoginFragment extends AbstractFragment {
        @Override
        protected void onPreferencesAdd(@Nullable Bundle savedInstanceState) {
            addPreferencesFromResource(R.xml.pref_group_login);
        }
    }

    public static class AboutFragment extends AbstractFragment {
        @Override
        protected void onPreferencesAdd(@Nullable Bundle savedInstanceState) {
            addPreferencesFromResource(R.xml.pref_group_about);
        }
    }

    public static abstract class AbstractFragment extends PreferenceFragmentCompat {

        protected abstract void onPreferencesAdd(@Nullable Bundle savedInstanceState);

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            onPreferencesAdd(savedInstanceState);
            setHasOptionsMenu(false);
            bindPreferencesSummaryToValue();
            bindPreferenceBoardSyncIntervalOnChange();
        }

        private void bindPreferencesSummaryToValue() {
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_sync_post_interval)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_user)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_download_path)));
        }

        private void bindPreferenceBoardSyncIntervalOnChange()  {
            Preference preference = findPreference(getString(R.string.pref_key_sync_post_interval));
            if (preference != null) {
                final Preference.OnPreferenceChangeListener oldListener = preference.getOnPreferenceChangeListener();
                preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object value) {
                        ListPreference listPreference = (ListPreference) preference;
                        int index = listPreference.findIndexOfValue(value.toString());
                        long interval = Long.parseLong(listPreference.getEntryValues()[index].toString());
                        if (interval == -1L) {
                            getActivity().sendBroadcast(new Intent(Action.BOARD_SYNC_CANCEL));
                        } else {
                            getActivity().sendBroadcast(new Intent(Action.BOARD_SYNC));
                        }
                        return oldListener.onPreferenceChange(preference, value);
                    }
                });
            }
        }
    }

    @SuppressLint("ValidFragment")
    private static class PreferenceFragmentCompat extends PreferenceFragment {
        private ListView mListView;

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            if (getListView() != null) {
                getListView().setDivider(null);
            }
        }

        private ListView getListView() {
            if (mListView == null) {
                if (getView() != null) {
                    mListView = getView().findViewById(android.R.id.list);
                }
            }

            return mListView;
        }
    }
}
