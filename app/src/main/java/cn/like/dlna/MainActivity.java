package cn.like.dlna;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.imooc.dlna.DLNA;
import com.imooc.dlna.util.DLNAUtil;

import org.cybergarage.upnp.Device;
import org.cybergarage.upnp.device.DeviceChangeListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    static final int PLAY_TYPE_LOCAL = 1;
    static final int PLAY_TYPE_ONLINE = 2;
    private static final String TRANS_URI = "uriData";
    private static final String TRANS_TYPE = "typeData";

    RecyclerView recyclerView;
    ProgressBar progressBar;
    List<Device> devices = new ArrayList<>();
    EquipmentAdapter adapter;
    private String uri;
    private int urlType = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        urlType = getIntent().getIntExtra(TRANS_TYPE, 0);
        uri = getIntent().getStringExtra(TRANS_URI);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        adapter = new EquipmentAdapter(devices);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnItemTouchListener(new OnRecyclerItemClickListener(recyclerView) {

            @Override
            public void onItemClick(RecyclerView.ViewHolder vh, int position, int type) {

                if (((EquipmentAdapter.ViewHolder)(vh)).imageView.getVisibility() == View.VISIBLE) {
                    DLNA.getInstance().stop();
                    adapter.notifyDataSetChanged();
                    Toast.makeText(MainActivity.this, "dlna connect closed", Toast.LENGTH_SHORT).show();
                } else {
                    adapter.notifyDataSetChanged();
                    ((EquipmentAdapter.ViewHolder)(vh)).imageView.setVisibility(View.VISIBLE);
                    DLNA.getInstance().setSelectedDevice(adapter.getItem(position));

                    if (urlType == PLAY_TYPE_LOCAL) {
                        DLNA.getInstance().playSDCardMedia(uri);
                    }
                    if (urlType == PLAY_TYPE_ONLINE) {
                        DLNA.getInstance().playMedia(uri);
                    }

                    Toast.makeText(MainActivity.this, "dlna connect opened", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onItemLongClick(RecyclerView.ViewHolder vh, int position, int type) {

            }
        });
        initDevice();
    }

    public void initDevice() {
        DLNA.getInstance().init(getApplicationContext(), new DeviceChangeListener() {

            @Override
            public void deviceAdded(Device d) {

                if (!DLNAUtil.isMediaRenderDevice(d))
                    return;
                int size = devices.size();
                for (int i = 0; i < size; i++) {
                    String udnString = devices.get(i).getUDN();
                    if (d.getUDN().equalsIgnoreCase(udnString)) {
                        devices.remove(i);
                        devices.add(i, d);
                        return;
                    }
                }

                devices.add(d);
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        adapter.clear();
                        adapter.addAll(devices);
                        adapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });
            }

            @Override
            public void deviceRemoved(Device d) {
                if (!DLNAUtil.isMediaRenderDevice(d)) {
                    return;
                }
                int size = devices.size();
                for (int i = 0; i < size; i++) {
                    String udnString = devices.get(i).getUDN();
                    if (d.getUDN().equalsIgnoreCase(udnString)) {
                        Device device = devices.remove(i);

                        if (DLNA.getInstance().getSelectedDevice() != null
                                && DLNA.getInstance().getSelectedDevice().getUDN().equalsIgnoreCase(device.getUDN())) {
                            Toast.makeText(getApplicationContext(), getString(R.string.dlna_device_delete_tip),
                                    Toast.LENGTH_SHORT).show();
                        }
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                adapter.clear();
                                adapter.addAll(devices);
                                adapter.notifyDataSetChanged();
                            }
                        });

                        break;
                    }
                }

            }
        });
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static void playOnLineUrl(Context context, String uri) {

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(TRANS_URI , uri);
        intent.putExtra(TRANS_TYPE, PLAY_TYPE_ONLINE);
        context.startActivity(intent);
    }

    public static void playLocalData(Context context, String uri) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(TRANS_URI , uri);
        intent.putExtra(TRANS_TYPE, PLAY_TYPE_LOCAL);
        context.startActivity(intent);
    }

    static class EquipmentAdapter extends RecyclerView.Adapter<EquipmentAdapter.ViewHolder> {
        List<Device> devices;

        public EquipmentAdapter(List<Device> devices) {
            if (devices == null)
                devices = new ArrayList<>();
            this.devices = devices;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.equipment_item_layout, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.textView.setText(devices.get(position).getFriendlyName());
            holder.imageView.setVisibility(View.INVISIBLE);
        }

        @Override
        public int getItemCount() {
            return devices.size();
        }

        public void clear() {
            devices.clear();
        }

        public void addAll(List<Device> devices) {
            devices.addAll(devices);
        }

        public Device getItem(int position) {
            return devices.get(position);
        }

        static class ViewHolder extends RecyclerView.ViewHolder {

            ImageView imageView;
            TextView textView;

            public ViewHolder(View itemView) {
                super(itemView);
                imageView = (ImageView) itemView.findViewById(R.id.img_dlan_checked);
                textView = (TextView) itemView.findViewById(R.id.dlna_device_name);
            }
        }
    }
}
