package com.comp3111.pacekeeper;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import com.comp3111.pedometer.Pedometer;
import com.comp3111.pedometer.SpeedAdjuster;
import com.comp3111.pedometer.StatisticsInfo;
import com.comp3111.swipeview.SwipeDismissTouchListener;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.smp.soundtouchandroid.SoundTouchPlayable;

import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.AssetFileDescriptor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MusicActivity extends Activity {
	
	// For ViewPager
	private ViewPager mPager;
    private List<View> listViews;
    private ViewGroup leftPanel, centerPanel, rightPanel;
    private int currIndex = 1;	// start off from middle page
    private int ivCursorWidth;
    private int tabWidth, screenW, screenH, offsetX;
    private ImageView ivCursor, ivAlbumArt;
    int finalHeight, actionBarHeight;
    
    // For things inside ViewPager
	String fullPathToAudioFile = Environment.getExternalStorageDirectory().toString() + "/test.mp3";
	TextView gForce, pedoSteps, av_duration, left_steps, left_miles, left_stepsPerMin, left_milesPerHour, left_calories;
	double time_axis;
	Pedometer pedo;
	StatisticsInfo stinfo = new StatisticsInfo(68);	// for 68kg

	//class-accessible Player
	SoundTouchPlayable stp;
	float tempoValue = 1.0f;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.activity_pedo_viewpager);
		InitViewPager();
		InitImageView();	// for page cursor and album art resizing
        mPager.setCurrentItem(1);
        InitPostView();		// for setting up view's position dynamically
	}
	
	private void InitPostView(){
        //find actionbar size
        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)){
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
        }

		LinearLayout collpased_player_layout = (LinearLayout)findViewById(R.id.pedo_vp_collpase_player_layout);
		LinearLayout collpase_player_layout_placeholder = (LinearLayout)findViewById(R.id.pedo_vp_collpase_player_layout_placeholder);
		
		// set height of player
		collpase_player_layout_placeholder.getLayoutParams().height = (int) ((screenH - actionBarHeight) * 0.4);
		collpased_player_layout.getLayoutParams().height = (int) ((screenH - actionBarHeight) * 0.4);
		collpased_player_layout.bringToFront();
		collpased_player_layout.invalidate();
        // collapsed music player action
		collpased_player_layout.setOnTouchListener(new SwipeDismissTouchListener(
			collpased_player_layout,
			actionBarHeight,
			null,
			new SwipeDismissTouchListener.OnDismissCallback(){
				@Override
				public void onDismiss(View view, Object token) {
					// TODO Auto-generated method stub
					// should be problem of swipeView
					//view.setY(screenH - actionBarHeight - view.getHeight());
					//view.requestLayout();
				}
			}
		));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.calibrate, menu);
		return true;
	}
	
	// Methods for ViewPager
    public class MyOnClickListener implements View.OnClickListener {
        private int index = 0;

        public MyOnClickListener(int i) {
            index = i;
        }

        @Override
        public void onClick(View v) {
            mPager.setCurrentItem(index);
        }
    };
    
    private void InitViewPager() {
        mPager = (ViewPager) findViewById(R.id.pedo_vp_vPager);
        listViews = new ArrayList<View>();
        LayoutInflater mInflater = getLayoutInflater();
        leftPanel = (ViewGroup) mInflater.inflate(R.layout.activity_pedo_left, null);
        centerPanel = (ViewGroup) mInflater.inflate(R.layout.activity_pedo, null);
        rightPanel = (ViewGroup) mInflater.inflate(R.layout.activity_goal, null);
        listViews.add(leftPanel);
        listViews.add(centerPanel);
        listViews.add(rightPanel);
        InitViewsListener(leftPanel, centerPanel, rightPanel);	// for pages actions
        mPager.setAdapter(new MyPagerAdapter(listViews));
        mPager.setOnPageChangeListener(new MyOnPageChangeListener());
        mPager.setCurrentItem(0);
    }
    
    private void InitViewsListener(ViewGroup leftPanel, final ViewGroup centerPanel, ViewGroup rightPanel) {
    	// rename left cells
    	TextView cell_text = (TextView)leftPanel.findViewById(R.id.pedo_left_cell2).findViewById(R.id.pedo_left_block_desc);
    	cell_text.setText("miles travelled");
    	cell_text = (TextView)leftPanel.findViewById(R.id.pedo_left_cell3).findViewById(R.id.pedo_left_block_desc);
    	cell_text.setText("steps/min");
    	cell_text = (TextView)leftPanel.findViewById(R.id.pedo_left_cell4).findViewById(R.id.pedo_left_block_desc);
    	cell_text.setText("miles/hour");
    	cell_text = (TextView)leftPanel.findViewById(R.id.pedo_left_cell5).findViewById(R.id.pedo_left_block_desc);
    	cell_text.setText("calories");
    	left_steps = (TextView)leftPanel.findViewById(R.id.pedo_left_cell1).findViewById(R.id.pedo_left_block_number);
    	left_miles = (TextView)leftPanel.findViewById(R.id.pedo_left_cell2).findViewById(R.id.pedo_left_block_number);
    	left_stepsPerMin = (TextView)leftPanel.findViewById(R.id.pedo_left_cell3).findViewById(R.id.pedo_left_block_number);
    	left_milesPerHour = (TextView)leftPanel.findViewById(R.id.pedo_left_cell4).findViewById(R.id.pedo_left_block_number);
    	left_calories = (TextView)leftPanel.findViewById(R.id.pedo_left_cell5).findViewById(R.id.pedo_left_block_number);
		// center page controls
		gForce=(TextView)centerPanel.findViewById(R.id.tw_pedo_gf);
		pedoSteps=(TextView)centerPanel.findViewById(R.id.tw_pedo_steps);
		av_duration=(TextView)centerPanel.findViewById(R.id.tw_pedo_av_duration);
		final Button btn_ru  = (Button)centerPanel.findViewById(R.id.mus_btn_rampup);
		final Button btn_rn  = (Button)centerPanel.findViewById(R.id.mus_btn_rampnormal);
		final Button btn_rd  = (Button)centerPanel.findViewById(R.id.mus_btn_rampdown);
		gForce.setText(Environment.getExternalStorageDirectory().toString());
		// center page graph
		// init example series data
		final GraphViewSeries exampleSeries = new GraphViewSeries(new GraphViewData[] {
			        new GraphViewData(0, 0.0d)
			});		
		final GraphView graphView = new LineGraphView(
				this // context
				, "" // heading
		);
		graphView.addSeries(exampleSeries); // data
		graphView.setScrollable(true);		// for appending
		// optional - set view port; size matched with data stored, start at y-value 0
		graphView.setViewPort(0, 49);
		graphView.getGraphViewStyle().setTextSize(getResources().getDimension(R.dimen.chart_text)); // set text size
		graphView.getGraphViewStyle().setGridColor(Color.WHITE);
		graphView.getGraphViewStyle().setHorizontalLabelsColor(Color.WHITE);
		graphView.getGraphViewStyle().setVerticalLabelsColor(Color.WHITE);
		
		// fire up the chart
		LinearLayout layout = (LinearLayout)centerPanel.findViewById(R.id.chart_container);
		layout.addView(graphView);
		// init time_axis
		time_axis = 0;		

        // init Pedometer, a new thread is preserved for graph updating action
		// it is running at 100ms = 10Hz
        pedo = new Pedometer(MusicActivity.this, 100, 10){
        	// for immediate actions
        	public void onSensorChangedCallback(float g){
        		gForce.setText("G-Force: " + g);
        	}
        	// for lengthier thread action
        	public void PedoThreadCallback(int st, float threshold, float s_duration){
        		// set left page values
        		stinfo.setTotalSteps(st);
        		stinfo.addTime(0.1);	// 0.1 * 10 = 1 sec
        		left_steps.setText(""+st);
        		left_miles.setText(""+stinfo.getDistanceTravelled());	// side effect : changed miles field
        		left_stepsPerMin.setText(""+roundOneDecimal(stinfo.getStepPerTime()));
        		left_milesPerHour.setText(""+roundOneDecimal(stinfo.getDistancePerTime()));
        		left_calories.setText(""+roundOneDecimal(stinfo.getCaloriesBurn()));

        		// graph 
				time_axis += 1d;
				exampleSeries.appendData(new GraphViewData(time_axis, pForce), true, 50 );
				pedoSteps.setText("Steps taken: " + st+"; Th: " + threshold);
				av_duration.setText("Av. Step Duration: "+s_duration);
				switch(SpeedAdjuster.react(pedo, stp)){
					case	SpeedAdjuster.SA_FAST:
						btn_ru.setVisibility(View.VISIBLE);
						btn_rn.setVisibility(View.INVISIBLE);
						btn_rd.setVisibility(View.INVISIBLE);
						break;
					case	SpeedAdjuster.SA_NORMAL:
						btn_ru.setVisibility(View.INVISIBLE);
						btn_rn.setVisibility(View.VISIBLE);
						btn_rd.setVisibility(View.INVISIBLE);
						break;
					case	SpeedAdjuster.SA_SLOW:
						btn_ru.setVisibility(View.INVISIBLE);
						btn_rn.setVisibility(View.INVISIBLE);
						btn_rd.setVisibility(View.VISIBLE);
						break;
				}
        	}
        };
        RelativeLayout btn_pp  =(RelativeLayout)centerPanel.findViewById(R.id.mus_btn_trigger);
        btn_pp.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(stp.isPaused()){
					/*
					//the last two parameters are speed of playback and pitch in semi-tones.
					try {
						// use temporarily - for internal testing
						AssetFileDescriptor assetFd = getAssets().openFd("test.mp3");
						stp = SoundTouchPlayable.createSoundTouchPlayable(assetFd , 0, 1.0f, 0.0f);
						//stp = SoundTouchPlayable.createSoundTouchPlayable(fullPathToAudioFile , 0, 1.0f, 0.0f);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}*/
					stp.play();
			        pedo.startSensor();
				}else{
					stp.pause();
					pedo.stopSensor();
				}
			}        	
        });
		//the last two parameters are speed of playback and pitch in semi-tones.
		try {
			// use temporarily - for internal testing
			AssetFileDescriptor assetFd = getAssets().openFd("test.mp3");
			stp = SoundTouchPlayable.createSoundTouchPlayable(assetFd , 0, 1.0f, 0.0f);
			//stp = SoundTouchPlayable.createSoundTouchPlayable(fullPathToAudioFile , 0, 1.0f, 0.0f);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public class MyPagerAdapter extends PagerAdapter {
        public List<View> mListViews;

        public MyPagerAdapter(List<View> mListViews) {
            this.mListViews = mListViews;
        }

        @Override
        public void destroyItem(View arg0, int arg1, Object arg2) {
            ((ViewPager) arg0).removeView(mListViews.get(arg1));
        }

        @Override
        public int getCount() {
            return mListViews.size();
        }

        @Override
        public Object instantiateItem(View arg0, int arg1) {
            ((ViewPager) arg0).addView(mListViews.get(arg1), 0);
            return mListViews.get(arg1);
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == (arg1);
        }
        
        @Override
        public Parcelable saveState() {
            return null;
        }
    }
    
    private void InitImageView() {
    	// cursor part for correct indicator width
    	ivCursor = (ImageView) findViewById(R.id.pedo_vp_cursor);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        screenW = dm.widthPixels;
        screenH = dm.heightPixels;
        ivCursorWidth = BitmapFactory.decodeResource(getResources(), R.drawable.viewpager_tab).getWidth();
        tabWidth = screenW / listViews.size();
        ivCursor.getLayoutParams().width = tabWidth;
        ivCursorWidth = tabWidth;
        offsetX = (tabWidth - ivCursorWidth) / 2;
        // album art part; to resize after knowing the actual image height
        ivAlbumArt = (ImageView) findViewById(R.id.pedo_vp_collapse_albumart);
        ViewTreeObserver vto = ivAlbumArt.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                finalHeight = ivAlbumArt.getMeasuredHeight();
                ivAlbumArt.getLayoutParams().width = finalHeight;
                return true;
            }
        });
    }
    
    public class MyOnPageChangeListener implements OnPageChangeListener {
        @Override
        public void onPageSelected(int arg0) {
        	Animation animation;
            // initially, current index is same as translate-to index
            if(arg0 == currIndex){
                animation = new TranslateAnimation(tabWidth * currIndex + offsetX, tabWidth * arg0 + offsetX, -12, 0);
            }else{
                animation = new TranslateAnimation(tabWidth * currIndex + offsetX, tabWidth * arg0 + offsetX, 0, 0);              	
            }
            currIndex = arg0;
            animation.setFillAfter(true);
            animation.setDuration(350);
            ivCursor.startAnimation(animation);
        }

		@Override
		public void onPageScrollStateChanged(int arg0) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			// TODO Auto-generated method stub
			
		}
    }
    
    double roundOneDecimal(double d) { 
        DecimalFormat twoDForm = new DecimalFormat("#.#"); 
        return Double.valueOf(twoDForm.format(d));
  }  

}