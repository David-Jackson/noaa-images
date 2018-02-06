package fyi.jackson.drew.noaaimages;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.transition.TransitionManager;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";

    ImageView imageView;
    int screenHeight, screenWidth;
    boolean fullscreen = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Display display = getWindowManager().getDefaultDisplay();
        screenHeight = display.getHeight();
        screenWidth = display.getWidth();

        imageView = findViewById(R.id.iv);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    TransitionManager.beginDelayedTransition((ViewGroup) view.getRootView());
                }
                int currentWidth = view.getWidth();
                double desiredWidth = (fullscreen ? screenWidth : Math.hypot(screenHeight, screenWidth));
                float scale = (float) desiredWidth / currentWidth;
                view.animate()
                        .setDuration(300)
                        .scaleX(scale)
                        .scaleY(scale)
                        .start();
                fullscreen = !fullscreen;
            }
        });

        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                openListActivity();
                return true;
            }
        });

        String imageUrl = "https://cdn.star.nesdis.noaa.gov/GOES16/ABI/FD/GEOCOLOR/1808x1808.jpg";

        Picasso.with(this)
                .load(imageUrl)
                .into(imageView);
    }

    public void openListActivity() {
        Intent intent = new Intent(this, ListActivity.class);
        startActivity(intent);
    }
}
