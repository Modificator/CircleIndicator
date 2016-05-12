package cn.modificator.sample;

import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cn.modificator.circleindicator.CircleIndicator;

public class MainActivity extends AppCompatActivity {
    ViewPager mViewPager;
    CircleIndicator mTabView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mViewPager = (ViewPager) findViewById(R.id.mViewPager);
        mTabView = (CircleIndicator) findViewById(R.id.mTabView);

        mViewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return 4;
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
//                super.destroyItem(container, position, object);
                container.removeView((View) object);
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                TextView textView = new TextView(container.getContext());
                textView.setText("page " + position);
                container.addView(textView);
                return textView;
            }
        });
        mTabView.setViewPager(mViewPager);

        ((CircleIndicator) findViewById(R.id.indicator1)).setViewPager(mViewPager);
        ((CircleIndicator) findViewById(R.id.indicator2)).setViewPager(mViewPager);
        ((CircleIndicator) findViewById(R.id.indicator3)).setViewPager(mViewPager);
        ((CircleIndicator) findViewById(R.id.indicator4)).setViewPager(mViewPager);
    }

}
