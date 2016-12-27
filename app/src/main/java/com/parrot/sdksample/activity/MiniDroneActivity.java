package com.parrot.sdksample.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.chenanze.rxbusmanager.RxBusManager;
import com.parrot.arsdk.arcommands.ARCOMMANDS_MINIDRONE_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_MINIDRONE_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DEVICE_STATE_ENUM;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.sdksample.R;
import com.parrot.sdksample.drone.MiniDrone;
import com.thalmic.myo.AbstractDeviceListener;
import com.thalmic.myo.Arm;
import com.thalmic.myo.DeviceListener;
import com.thalmic.myo.Hub;
import com.thalmic.myo.Myo;
import com.thalmic.myo.Pose;
import com.thalmic.myo.Quaternion;
import com.thalmic.myo.XDirection;
import com.thalmic.myo.scanner.ScanActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import chenanze.com.utils.SpUtil;

public class MiniDroneActivity extends AppCompatActivity {
    private static final String TAG = "MiniDroneActivity";

    private final String MODE_NORMAL = "normal";
    private final String MODE_DEBUG = "debug";
    private final String UPDATE_PITCH_EVENT = "update_pitch_event";
    private final String UPDATE_ROLL_EVENT = "update_roll_event";
    private final String UPDATE_YAW_EVENT = "update_yaw_event";
    private final String UPDATE_RESET_PITCH_ROLL_EVENT = "update_reset_pitch_roll_event";
    private final String UPDATE_RESET_YAW_EVENT = "update_reset_yaw_event";
    private final String STATUS_FORWARD = "forward";
    private final String STATUS_RESET = "reset";
    private final String STATUS_BACK = "back";
    private final String STATUS_ROLL_RIGHT = "rightRoll";
    private final String STATUS_ROLL_LEFT = "leftRoll";

    @BindView(R.id.parrot_connect_myo_bt)
    Button parrotConnectMyoBt;
    @BindView(R.id.myo_status_tv)
    TextView myoStatusTv;
    @BindView(R.id.myo_lock_status_tv)
    TextView myoLockStatusTv;
    @BindView(R.id.pitch_tv)
    TextView pitchTv;
    @BindView(R.id.roll_tv)
    TextView rollTv;
    @BindView(R.id.yaw_tv)
    TextView yawTv;
    @BindView(R.id.debug_bt)
    Button mDebugBt;
    @BindView(R.id.mode_tv)
    TextView mModeTv;

    private MiniDrone mMiniDrone;

    private ProgressDialog mConnectionProgressDialog;
    private ProgressDialog mDownloadProgressDialog;

    private TextView mBatteryLabel;
    private Button mTakeOffLandBt;
    private Button mDownloadBt;

    private int mNbMaxDownload;
    private int mCurrentDownloadIndex;
    private Context mContext;

    private volatile boolean isRun = true;
    private volatile int mRoll;
    private volatile int mPitch;
    private volatile int mYaw;
    private volatile boolean mIsFist;
    private boolean mGeatureReset = false;
    private boolean mDebug = false;
    private String mPitchRollStatus;
    private int mIsLeftArm = -1;
    private boolean mIsWaveIn = false;
    private boolean mIsWaveOut = false;


    @OnClick(R.id.parrot_connect_myo_bt)
    void onClick(View view) {
        startActivity(new Intent(mContext, ScanActivity.class));
    }

    @OnClick(R.id.debug_bt)
    public void onClick() {
        if (mDebug == false) {
            mDebug = true;
            checkIsDebugMode();
        } else {
            mDebug = false;
            checkIsNormalMode();
        }
    }

    private void checkIsNormalMode() {
        if (!mDebug) {// mDebug 被多线程修改时的验证
            Log.d(TAG, "调试模式已关闭");
            SpUtil.setMode(MODE_NORMAL);
            mDebugBt.setText("打开调试模式");
            mModeTv.setText("正常模式");
        }
    }

    private void checkIsDebugMode() {
        if (mDebug) {// mDebug 被多线程修改时的验证
            Log.d(TAG, "调试模式已开启");
            SpUtil.setMode(MODE_DEBUG);
            mDebugBt.setText("关闭调试模式");
            mModeTv.setText("调试模式");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_minidrone);
        ButterKnife.bind(this);

        if (SpUtil.getMode().equals(MODE_NORMAL)) {
            mDebug = false;
            checkIsNormalMode();
        } else {
            mDebug = true;
            checkIsDebugMode();
        }

        // First, we initialize the Hub singleton with an application identifier.
        Hub hub = Hub.getInstance();
        if (!hub.init(this, getPackageName())) {
            // We can't do anything with the Myo device if the Hub can't be initialized, so exit.
            Toast.makeText(this, "Couldn't initialize Hub", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        // Next, register for DeviceListener callbacks.
        hub.addListener(mListener);


        initIHM();

        Intent intent = getIntent();
        ARDiscoveryDeviceService service = intent.getParcelableExtra(DeviceListActivity.EXTRA_DEVICE_SERVICE);
        mMiniDrone = new MiniDrone(this, service);
        mMiniDrone.addListener(mMiniDroneListener);

        RxBusManager.getInstance().t(TAG).on(UPDATE_PITCH_EVENT, content -> pitchTv.setText((String) content))
                .on(UPDATE_ROLL_EVENT, content -> rollTv.setText((String) content))
                .on(UPDATE_YAW_EVENT, content -> yawTv.setText((String) content));

        controlLoop();
    }

    private void controlLoop() {
        new Thread(() -> {
            try {
                boolean reset = false;

                while (isRun) {
                    Thread.sleep(10);
                    if (mPitch >= -10 && mPitch <= 20 && mRoll > -10 && mRoll < 40) {
                        if (!reset) {
                            resetPitchRoll();
                            resetYaw();
                            reset = true;
                        }
                        Log.d("pitch", "pitch: 0");
                    }
                    if (!mIsFist) {
                        if (mPitch > 10 && mPitch <= 20) {
                            forward((byte) (20 * mIsLeftArm));
                            reset = false;
                        }
                        if (mPitch > 20) {
                            forward((byte) (50 * mIsLeftArm));
                            reset = false;
                        }
                        if (mPitch < -10 && mPitch >= -20) {
                            back((byte) (-20 * mIsLeftArm));
                            reset = false;
                        }
                        if (mPitch < -20) {
                            back((byte) (-50 * mIsLeftArm));
                            reset = false;
                        }


                        if (mRoll >= 10) {
                            rollRight();
                            reset = false;
                        } else if (mRoll <= -10) {
                            rollLeft();
                            reset = false;
                        }
                    } else {
                        if (mRoll >= 7) {
                            yawRight();
                            reset = false;
                        } else if (mRoll <= -7) {
                            yawLeft();
                            reset = false;
                        }
                    }

////                        Log.d("testxxx", "roll:" + (int) mRoll + " pitch:" + (int) mPitch + " yaw:" + (int) mYaw);
                }

                Log.d("testxxx", "stop event loop");

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxBusManager.getInstance().unregister(TAG);
        // We don't want any callbacks when the Activity is gone, so unregister the listener.
        Hub.getInstance().removeListener(mListener);

        if (isFinishing()) {
            // The Activity is finishing, so shutdown the Hub. This will disconnect from the Myo.
            Hub.getInstance().shutdown();
        }

        isRun = false;
    }

    @Override
    protected void onStart() {
        super.onStart();

        // show a loading view while the minidrone is connecting
        if ((mMiniDrone != null) && !(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING.equals(mMiniDrone.getConnectionState()))) {
            mConnectionProgressDialog = new ProgressDialog(this, R.style.AppCompatAlertDialogStyle);
            mConnectionProgressDialog.setIndeterminate(true);
            mConnectionProgressDialog.setMessage("Connecting ...");
            mConnectionProgressDialog.setCancelable(false);
            mConnectionProgressDialog.show();

            // if the connection to the MiniDrone fails, finish the activity
            if (!mMiniDrone.connect()) {
                finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mMiniDrone != null) {
            mConnectionProgressDialog = new ProgressDialog(this, R.style.AppCompatAlertDialogStyle);
            mConnectionProgressDialog.setIndeterminate(true);
            mConnectionProgressDialog.setMessage("Disconnecting ...");
            mConnectionProgressDialog.setCancelable(false);
            mConnectionProgressDialog.show();

            if (!mMiniDrone.disconnect()) {
                finish();
            }
        } else {
            finish();
        }
    }

    private void initIHM() {

        findViewById(R.id.emergencyBt).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mMiniDrone.emergency();
            }
        });

        mTakeOffLandBt = (Button) findViewById(R.id.takeOffOrLandBt);
        mTakeOffLandBt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                switch (mMiniDrone.getFlyingState()) {
                    case ARCOMMANDS_MINIDRONE_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_LANDED:
                        mMiniDrone.takeOff();
                        break;
                    case ARCOMMANDS_MINIDRONE_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_FLYING:
                    case ARCOMMANDS_MINIDRONE_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_HOVERING:
                        mMiniDrone.land();
                        break;
                    default:
                }
            }
        });

        findViewById(R.id.takePictureBt).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mMiniDrone.takePicture();
            }
        });

        mDownloadBt = (Button) findViewById(R.id.downloadBt);
        mDownloadBt.setEnabled(false);
        mDownloadBt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mMiniDrone.getLastFlightMedias();

                mDownloadProgressDialog = new ProgressDialog(MiniDroneActivity.this, R.style.AppCompatAlertDialogStyle);
                mDownloadProgressDialog.setIndeterminate(true);
                mDownloadProgressDialog.setMessage("Fetching medias");
                mDownloadProgressDialog.setCancelable(false);
                mDownloadProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mMiniDrone.cancelGetLastFlightMedias();
                    }
                });
                mDownloadProgressDialog.show();
            }
        });

        findViewById(R.id.gazUpBt).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        mMiniDrone.setGaz((byte) 50);
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        mMiniDrone.setGaz((byte) 0);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        findViewById(R.id.gazDownBt).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        mMiniDrone.setGaz((byte) -50);
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        mMiniDrone.setGaz((byte) 0);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        findViewById(R.id.yawLeftBt).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        mMiniDrone.setYaw((byte) -50);
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        mMiniDrone.setYaw((byte) 0);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        findViewById(R.id.yawRightBt).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        mMiniDrone.setYaw((byte) 50);
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        mMiniDrone.setYaw((byte) 0);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        findViewById(R.id.forwardBt).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        mMiniDrone.setPitch((byte) 50);
                        mMiniDrone.setFlag((byte) 1);
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        mMiniDrone.setPitch((byte) 0);
                        mMiniDrone.setFlag((byte) 0);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        findViewById(R.id.backBt).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        mMiniDrone.setPitch((byte) -50);
                        mMiniDrone.setFlag((byte) 1);
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        mMiniDrone.setPitch((byte) 0);
                        mMiniDrone.setFlag((byte) 0);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        findViewById(R.id.rollLeftBt).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        mMiniDrone.setRoll((byte) -50);
                        mMiniDrone.setFlag((byte) 1);
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        mMiniDrone.setRoll((byte) 0);
                        mMiniDrone.setFlag((byte) 0);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        findViewById(R.id.rollRightBt).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        mMiniDrone.setRoll((byte) 50);
                        mMiniDrone.setFlag((byte) 1);
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        mMiniDrone.setRoll((byte) 0);
                        mMiniDrone.setFlag((byte) 0);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        mBatteryLabel = (TextView) findViewById(R.id.batteryLabel);
    }

    private final MiniDrone.Listener mMiniDroneListener = new MiniDrone.Listener() {
        @Override
        public void onDroneConnectionChanged(ARCONTROLLER_DEVICE_STATE_ENUM state) {
            switch (state) {
                case ARCONTROLLER_DEVICE_STATE_RUNNING:
                    mConnectionProgressDialog.dismiss();
                    break;

                case ARCONTROLLER_DEVICE_STATE_STOPPED:
                    // if the deviceController is stopped, go back to the previous activity
                    mConnectionProgressDialog.dismiss();
                    finish();
                    break;

                default:
                    break;
            }
        }

        @Override
        public void onBatteryChargeChanged(int batteryPercentage) {
            mBatteryLabel.setText(String.format("%d%%", batteryPercentage));
        }

        @Override
        public void onPilotingStateChanged(ARCOMMANDS_MINIDRONE_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM state) {
            switch (state) {
                case ARCOMMANDS_MINIDRONE_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_LANDED:
                    mTakeOffLandBt.setText("Take off");
                    mTakeOffLandBt.setEnabled(true);
                    mDownloadBt.setEnabled(true);
                    break;
                case ARCOMMANDS_MINIDRONE_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_FLYING:
                case ARCOMMANDS_MINIDRONE_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_HOVERING:
                    mTakeOffLandBt.setText("Land");
                    mTakeOffLandBt.setEnabled(true);
                    mDownloadBt.setEnabled(false);
                    break;
                default:
                    mTakeOffLandBt.setEnabled(false);
                    mDownloadBt.setEnabled(false);
            }
        }

        @Override
        public void onPictureTaken(ARCOMMANDS_MINIDRONE_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_ENUM error) {
            Log.i(TAG, "Picture has been taken");
        }

        @Override
        public void onMatchingMediasFound(int nbMedias) {
            mDownloadProgressDialog.dismiss();

            mNbMaxDownload = nbMedias;
            mCurrentDownloadIndex = 1;

            if (nbMedias > 0) {
                mDownloadProgressDialog = new ProgressDialog(MiniDroneActivity.this, R.style.AppCompatAlertDialogStyle);
                mDownloadProgressDialog.setIndeterminate(false);
                mDownloadProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mDownloadProgressDialog.setMessage("Downloading medias");
                mDownloadProgressDialog.setMax(mNbMaxDownload * 100);
                mDownloadProgressDialog.setSecondaryProgress(mCurrentDownloadIndex * 100);
                mDownloadProgressDialog.setProgress(0);
                mDownloadProgressDialog.setCancelable(false);
                mDownloadProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mMiniDrone.cancelGetLastFlightMedias();
                    }
                });
                mDownloadProgressDialog.show();
            }
        }

        @Override
        public void onDownloadProgressed(String mediaName, int progress) {
            mDownloadProgressDialog.setProgress(((mCurrentDownloadIndex - 1) * 100) + progress);
        }

        @Override
        public void onDownloadComplete(String mediaName) {
            mCurrentDownloadIndex++;
            mDownloadProgressDialog.setSecondaryProgress(mCurrentDownloadIndex * 100);

            if (mCurrentDownloadIndex > mNbMaxDownload) {
                mDownloadProgressDialog.dismiss();
                mDownloadProgressDialog = null;
            }
        }
    };


    private DeviceListener mListener = new AbstractDeviceListener() {

        // onConnect() is called whenever a Myo has been connected.
        @Override
        public void onConnect(Myo myo, long timestamp) {
            // Set the text color of the text view to cyan when a Myo connects.
//            parrotConnectMyoBt.setTextColor(Color.CYAN);
            parrotConnectMyoBt.setText("MYO已连接");
        }

        // onDisconnect() is called whenever a Myo has been disconnected.
        @Override
        public void onDisconnect(Myo myo, long timestamp) {
            // Set the text color of the text view to red when a Myo disconnects.
            parrotConnectMyoBt.setTextColor(Color.RED);
            parrotConnectMyoBt.setText("MYO已断开");
        }

        // onArmSync() is called whenever Myo has recognized a Sync Gesture after someone has put it on their
        // arm. This lets Myo know which arm it's on and which way it's facing.
        @Override
        public void onArmSync(Myo myo, long timestamp, Arm arm, XDirection xDirection) {
            if (myo.getArm() == Arm.LEFT) {
                Log.d(TAG, "onArmSync: arm_left");
                mIsLeftArm = -1;
                myoStatusTv.setText(R.string.arm_left);
            } else {
                Log.d(TAG, "onArmSync: arm_right");
                mIsLeftArm = 1;
                myoStatusTv.setText(R.string.arm_right);
            }
        }

        // onArmUnsync() is called whenever Myo has detected that it was moved from a stable position on a person's arm after
        // it recognized the arm. Typically this happens when someone takes Myo off of their arm, but it can also happen
        // when Myo is moved around on the arm.
        @Override
        public void onArmUnsync(Myo myo, long timestamp) {
            myoStatusTv.setText(R.string.unsync);
        }

        // onUnlock() is called whenever a synced Myo has been unlocked. Under the standard locking
        // policy, that means poses will now be delivered to the listener.
        @Override
        public void onUnlock(Myo myo, long timestamp) {
            myoLockStatusTv.setText(R.string.unlocked);
        }

        // onLock() is called whenever a synced Myo has been locked. Under the standard locking
        // policy, that means poses will no longer be delivered to the listener.
        @Override
        public void onLock(Myo myo, long timestamp) {
            myoLockStatusTv.setText(R.string.locked);
        }

        // onOrientationData() is called whenever a Myo provides its current orientation,
        // represented as a quaternion.
        @Override
        public void onOrientationData(Myo myo, long timestamp, Quaternion rotation) {
            // Calculate Euler angles (roll, pitch, and yaw) from the quaternion.
            float roll = (float) Math.toDegrees(Quaternion.roll(rotation));
            float pitch = (float) Math.toDegrees(Quaternion.pitch(rotation));
            float yaw = (float) Math.toDegrees(Quaternion.yaw(rotation));

            // Adjust roll and pitch for the orientation of the Myo on the arm.
            if (myo.getXDirection() == XDirection.TOWARD_ELBOW) {
                roll *= -1;
                pitch *= -1;
            }


            // Next, we apply a rotation to the text view using the roll, pitch, and yaw.
//            mTextView.setRotation(roll);
//            mTextView.setRotationX(pitch);
//            mTextView.setRotationY(yaw);
//
            mRoll = (int) roll;
            mPitch = (int) pitch;
            mYaw = (int) yaw;

//            Log.d("test", "roll:"+(int)roll+" pitch:"+(int)pitch+" yaw:"+(int)yaw);
        }

        // onPose() is called whenever a Myo provides a new pose.
        @Override
        public void onPose(Myo myo, long timestamp, Pose pose) {
            // Handle the cases of the Pose enumeration, and change the text of the text view
            // based on the pose we receive.
            switch (pose) {
                case UNKNOWN:
                    mIsFist = false;
                    myoStatusTv.setText(getString(R.string.unkown));
                    Log.d("test", "onPose: UNKNOWN");
                    resetGaz();
                    resetWaveStatus();
                    break;
                case REST:
                    mIsFist = false;
                    Log.d("test", "onPose: REST");
                    myoStatusTv.setText(getString(R.string.rest));
                    resetGaz();
                    resetWaveStatus();
                    break;
                case DOUBLE_TAP:
                    mIsFist = false;
                    Log.d("test", "onPose: DOUBLE_TAP");
                    myoStatusTv.setText(getString(R.string.double_tap));
                    resetGaz();
                    resetWaveStatus();
                    takeOffAndLand();
                    break;
                case FIST:
                    Log.d("test", "onPose: FIST");
                    myoStatusTv.setText(getString(R.string.pose_fist));
                    mIsFist = true;
                    resetGaz();
                    resetWaveStatus();
                    break;
                case WAVE_IN:
                    mIsFist = false;
                    Log.d("test", "onPose: WAVE_IN");
                    resetGaz();
                    gazDown();
                    mIsWaveIn = true;
                    mIsWaveOut = false;
                    myoStatusTv.setText(getString(R.string.pose_wavein));
                    break;
                case WAVE_OUT:
                    mIsFist = false;

                    Log.d("test", "onPose: WAVE_OUT");
                    resetGaz();
                    gazUp();
                    mIsWaveOut = true;
                    mIsWaveIn = false;
                    myoStatusTv.setText(getString(R.string.pose_waveout));
                    break;
                case FINGERS_SPREAD:
                    mIsFist = false;
                    resetGaz();
                    gazUp();
                    mIsWaveOut = true;
                    mIsWaveIn = false;
                    Log.d("test", "onPose: FINGERS_SPREAD");
                    myoStatusTv.setText(getString(R.string.pose_fingersspread));
                    break;
            }

            if (pose != Pose.UNKNOWN && pose != Pose.REST) {
//                resetGaz();
                // Tell the Myo to stay unlocked until told otherwise. We do that here so you can
                // hold the poses without the Myo becoming locked.
                myo.unlock(Myo.UnlockType.HOLD);

                // Notify the Myo that the pose has resulted in an action, in this case changing
                // the text on the screen. The Myo will vibrate.
                myo.notifyUserAction();
            } else {
                // Tell the Myo to stay unlocked only for a short period. This allows the Myo to
                // stay unlocked while poses are being performed, but lock after inactivity.
//                myo.unlock(Myo.UnlockType.TIMED);
            }
        }
    };

    private void takeOffAndLand() {
        if (mDebug) {
            Log.d("takeoffandland", "takeOffAndLand");
        } else {
            switch (mMiniDrone.getFlyingState()) {
                case ARCOMMANDS_MINIDRONE_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_LANDED:
                    mMiniDrone.takeOff();
                    break;
                case ARCOMMANDS_MINIDRONE_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_FLYING:
                case ARCOMMANDS_MINIDRONE_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_HOVERING:
                    mMiniDrone.land();
                    break;
                default:
            }
        }
    }

    private void forward(byte data) {
        if (mDebug) {
            String msg = STATUS_BACK + " " + data;
            Log.d("PitchRoll", msg);
            if (!STATUS_FORWARD.equals(mPitchRollStatus)) {
                RxBusManager.getInstance().post(UPDATE_PITCH_EVENT, msg);
                mPitchRollStatus = STATUS_FORWARD;
            }
        } else {
            mMiniDrone.setPitch(data);
            mMiniDrone.setFlag((byte) 1);
        }
    }

    private void back(byte data) {
        if (mDebug) {
            String msg = STATUS_BACK + " " + data;
            Log.d("PitchRoll", msg);
            if (!STATUS_BACK.equals(mPitchRollStatus)) {
                RxBusManager.getInstance().post(UPDATE_PITCH_EVENT, msg);
                mPitchRollStatus = STATUS_BACK;
            }
        } else {
            mMiniDrone.setPitch(data);
            mMiniDrone.setFlag((byte) 1);
        }
    }

    private void rollLeft() {
        if (mDebug) {
            Log.d("PitchRoll", STATUS_ROLL_LEFT);
            if (!STATUS_ROLL_LEFT.equals(mPitchRollStatus)) {
                RxBusManager.getInstance().post(UPDATE_ROLL_EVENT, STATUS_ROLL_LEFT);
                mPitchRollStatus = STATUS_ROLL_LEFT;
            }
        } else {
            mMiniDrone.setRoll((byte) -50);
            mMiniDrone.setFlag((byte) 1);
        }
    }

    private void rollRight() {
        if (mDebug) {
            Log.d("PitchRoll", STATUS_ROLL_RIGHT);
            if (!STATUS_ROLL_RIGHT.equals(mPitchRollStatus)) {
                RxBusManager.getInstance().post(UPDATE_ROLL_EVENT, STATUS_ROLL_RIGHT);
                mPitchRollStatus = STATUS_ROLL_RIGHT;
            }
        } else {
            mMiniDrone.setRoll((byte) 50);
            mMiniDrone.setFlag((byte) 1);
        }
    }

    private void gazUp() {
        if (mIsWaveOut == false) {
            if (mDebug) {
                Log.d("test", "gazUp");
            } else {
                mMiniDrone.setGaz((byte) 50);
            }
        }
    }

    private void gazDown() {
        if (mIsWaveIn == false) {
            if (mDebug) {
                Log.d("test", "gazDown");
            } else {
                mMiniDrone.setGaz((byte) -50);
            }
        }
    }

    private void yawLeft() {
        if (mDebug) {
            Log.d("Yaw", "yawLeft");
            RxBusManager.getInstance().post(UPDATE_YAW_EVENT, "yawLeft");
        } else {
            mMiniDrone.setYaw((byte) -50);
        }
    }

    private void yawRight() {
        if (mDebug) {
            Log.d("Yaw", "yawRight");
            RxBusManager.getInstance().post(UPDATE_YAW_EVENT, "yawRight");
        } else {
            mMiniDrone.setYaw((byte) 50);
        }
    }

    private void resetPitchRoll() {
        if (mDebug) {
            Log.d("Reset", STATUS_RESET);
            if (!STATUS_RESET.equals(mPitchRollStatus)) {
                RxBusManager.getInstance().post(UPDATE_PITCH_EVENT, STATUS_RESET);
                RxBusManager.getInstance().post(UPDATE_ROLL_EVENT, STATUS_RESET);
                mPitchRollStatus = STATUS_RESET;
            }
        } else {
            mMiniDrone.setPitch((byte) 0);
            mMiniDrone.setFlag((byte) 0);
            mMiniDrone.setRoll((byte) 0);
            mMiniDrone.setFlag((byte) 0);
        }
    }

    private void resetWaveStatus() {
        mIsWaveIn = false;
        mIsWaveOut = false;
    }

    private void resetGaz() {
        if (mDebug) {
            Log.d("test", "resetGaz");
        } else {
            mMiniDrone.setGaz((byte) 0);
        }
    }

    private void resetYaw() {
        if (mDebug) {
            Log.d("Reset", "resetYaw");
            RxBusManager.getInstance().post(UPDATE_YAW_EVENT, "resetYaw");
        } else {
            mMiniDrone.setYaw((byte) 0);
        }
    }
}
