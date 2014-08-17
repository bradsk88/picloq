package ca.bradj.picloq.app.zone;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
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

public class SelectTimeZoneRegionActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setContentView(R.layout.activity_select_zone);

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        ListView zoneList = (ListView) findViewById(R.id.zoneList);
        final List<String> listContents = Lists.newArrayList(getRegions());
        zoneList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listContents));
        zoneList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String selected = listContents.get(i);
                Intent intent = new Intent(SelectTimeZoneRegionActivity.this, SelectTimeZoneActivity.class);
                intent.putExtra(AVKey.ZONE_REGION, selected);
                startActivity(intent);
                finish();
            }
        });
        getWindow().setBackgroundDrawableResource(R.drawable.abc_ab_solid_light_holo);
    }

    public Set<String> getRegions() {
        String[] availableIDs = TimeZone.getAvailableIDs();
        Set<String> regions = Sets.newTreeSet();
        for (String i : availableIDs) {
            String[] split = i.split("/");
            if (split.length > 0) {
                regions.add(split[0]);
            }
        }
        return regions;
    }
}
