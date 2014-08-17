package ca.bradj.picloq.app.zone;

import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import ca.bradj.picloq.app.AVKey;
import ca.bradj.picloq.app.R;

public class SelectTimeZoneActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String region = getIntent().getStringExtra(AVKey.ZONE_REGION);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setContentView(R.layout.activity_select_zone);

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        ListView zoneList = (ListView) findViewById(R.id.zoneList);
        final List<String> listContents = Lists.newArrayList(getAvailableIDs(region));
        zoneList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listContents));
        zoneList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String selected = listContents.get(i);
                preferences.edit().putString(AVKey.TIMEZONE, selected).apply();
                finish();
            }
        });
        getWindow().setBackgroundDrawableResource(R.drawable.abc_ab_solid_light_holo);
    }

    private Iterable<? extends String> getAvailableIDs(String region) {
        if (region == null || region.isEmpty()) {
            return Lists.newArrayList(TimeZone.getAvailableIDs());
        }
        Set<String> zones = Sets.newTreeSet();
        for (String i : TimeZone.getAvailableIDs()) {
            if (i.startsWith(region)) {
                zones.add(i);
            }
        }
        return zones;
    }

}
