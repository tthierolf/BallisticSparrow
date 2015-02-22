package blindmanballistics.ballisticsparrow;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;


public class MainActivity extends ActionBarActivity {

    private Button mUpButton, mDownButton, mLeftButton, mRightButton, mFireButton;
    private boolean mBarrelMoving = false, mTurretMoving = false;
    private Switch mSafety;
    private NumberPicker mPowerLevelPicker;
    private CommunicationManager mCommunicationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

    private void init(){
        mCommunicationManager = new CommunicationManager(this);
        mUpButton = (Button)findViewById(R.id.up_button);
        mDownButton = (Button)findViewById(R.id.down_button);
        mLeftButton = (Button)findViewById(R.id.left_button);
        mRightButton = (Button)findViewById(R.id.right_button);
        mFireButton = (Button)findViewById(R.id.fire_button);
        mSafety = (Switch)findViewById(R.id.safety_switch);
        mPowerLevelPicker = (NumberPicker)findViewById(R.id.power_level_picker);

        mPowerLevelPicker.setMaxValue(6);
        mPowerLevelPicker.setMinValue(1);

        mUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCommunicationManager.incrementRaiseBarrel();
            }
        });

        mUpButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mCommunicationManager.raiseBarrel();
                mBarrelMoving = true;
                return true;
            }
        });

        mUpButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == android.view.MotionEvent.ACTION_UP && mBarrelMoving) {
                    mCommunicationManager.stopBarrel();
                    mBarrelMoving = false;
                }
                return false;
            }
        });

        mDownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCommunicationManager.incrementLowerBarrel();
            }
        });

        mDownButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mCommunicationManager.lowerBarrel();
                mBarrelMoving = true;
                return true;
            }
        });

        mDownButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == android.view.MotionEvent.ACTION_UP && mBarrelMoving) {
                    mCommunicationManager.stopBarrel();
                    mBarrelMoving = false;
                }
                return false;
            }
        });

        mLeftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCommunicationManager.incrementRotateLeft();
            }
        });

        mLeftButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mCommunicationManager.rotateLeft();
                mTurretMoving = true;
                return true;
            }
        });

        mLeftButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == android.view.MotionEvent.ACTION_UP && mTurretMoving) {
                    mCommunicationManager.stopRotation();
                    mTurretMoving = false;
                }
                return false;
            }
        });

        mRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCommunicationManager.incrementRotateRight();
            }
        });

        mRightButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mCommunicationManager.rotateRight();
                mTurretMoving = true;
                return true;
            }
        });

        mRightButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == android.view.MotionEvent.ACTION_UP && mTurretMoving) {
                    mCommunicationManager.stopRotation();
                    mTurretMoving = false;
                }
                return false;
            }
        });

        mFireButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mSafety.isChecked()){
                    mCommunicationManager.fire(mPowerLevelPicker.getValue());
                }
            }
        });
    }
}
