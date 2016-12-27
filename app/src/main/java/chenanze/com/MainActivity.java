package chenanze.com;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.orhanobut.logger.Logger;
import com.parrot.arsdk.ARSDK;
import com.parrot.sdksample.R;
import com.parrot.sdksample.activity.DeviceListActivity;
import com.thalmic.myo.Hub;
import com.thalmic.myo.scanner.ScanActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();

    private Context mContext;

    // this block loads the native libraries
    // it is mandatory
    static {
        ARSDK.loadSDKLibs();
    }

    @BindView(R.id.connect_parrot_bt)
    Button mConnectParrotBt;
    @BindView(R.id.connect_myo_bt)
    Button mConnectMyoBt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // First, we initialize the Hub singleton with an application identifier.
        Hub hub = Hub.getInstance();
        if (!hub.init(this, getPackageName())) {
            // We can't do anything with the Myo device if the Hub can't be initialized, so exit.
            Toast.makeText(this, "Couldn't initialize Hub", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

    }

    @OnClick({R.id.connect_myo_bt, R.id.connect_parrot_bt})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.connect_myo_bt:
                Logger.t(TAG).d("connect_myo_bt");
                startActivity(new Intent(mContext, ScanActivity.class));
                break;
            case R.id.connect_parrot_bt:
                startActivity(new Intent(mContext, DeviceListActivity.class));
                Logger.t(TAG).d("connect_parrot_bt");
                break;
        }
    }
}
