package fyi.jackson.drew.noaaimages.service;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;

public class NoaaWallpaperService extends WallpaperService {

    @Override
    public Engine onCreateEngine() {
        return new NoaaWallpaperEngine();
    }

    private class NoaaWallpaperEngine extends WallpaperService.Engine {
        private static final String TAG = "NoaaWallpaperEngine";

        private final int frameDuration = 20;

        private SurfaceHolder holder;

        private boolean visible;
        private Handler handler;
        private Runnable drawRunnable;
        private Target target;
        private Bitmap earthBitmap = null;
        private String imageUrl = "https://cdn.star.nesdis.noaa.gov/GOES16/ABI/FD/GEOCOLOR/20180350245_GOES16-ABI-FD-GEOCOLOR-1808x1808.jpg";

        private int attempts = 0;
        private int maxAttempts = 10;

        private int screenWidth = 0, screenHeight = 0;

        public NoaaWallpaperEngine() {
            handler = new Handler();
            drawRunnable = new Runnable() {
                @Override
                public void run() {
                    draw();
                }
            };

            target = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    attempts = 0;
                    Log.d(TAG, "onBitmapLoaded: Bitmap loaded");
                    earthBitmap = bitmap;
                    handler.post(drawRunnable);
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                    Log.d(TAG, "onBitmapFailed: Bitmap failed to load: attempt: " + attempts);
                    attempts++;
                    if (attempts < maxAttempts) {
                        Picasso.with(getApplicationContext()).load(imageUrl).into(target);
                    }
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                    Log.d(TAG, "onPrepareLoad: Bitmap preparing to load: attempt " + attempts);

                }
            };
            Picasso.with(getApplicationContext()).load(imageUrl).into(target);
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            this.holder = surfaceHolder;

            handler.post(drawRunnable);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            this.visible = visible;
            if (visible) {
                handler.post(drawRunnable);
            } else {
                handler.removeCallbacks(drawRunnable);
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            screenHeight = height;
            screenWidth = width;
        }

        private void draw() {
            if (visible) {
                Log.d(TAG, "draw: Drawing...");
                final Canvas canvas = holder.lockCanvas();
                canvas.save();
                Paint paint = new Paint();
                paint.setColor(Color.BLACK);
                canvas.drawRect(0, 0, screenWidth, screenHeight, paint);
                if (earthBitmap != null) {
                    //earthBitmap = Bitmap.createScaledBitmap(earthBitmap, screenWidth, screenWidth, false);
                    int left = (screenWidth == 0 ? 0 : (screenWidth - earthBitmap.getWidth()) / 2);
                    int top = (screenHeight == 0 ? 0 : (screenHeight - earthBitmap.getHeight()) / 2);
                    Log.d(TAG, "draw: Drawing bitmap at x: " + left + ", y: " + top);
                    canvas.drawBitmap(earthBitmap, left, top, null);
                    int logoBlockSize = (int) (0.5 * earthBitmap.getWidth() * (1 - Math.cos(Math.PI / 4)));
                    Log.d(TAG, "draw: logoBlockSize: " + logoBlockSize);
                    paint.setColor(Color.BLACK);
                    canvas.drawRect(left,
                            top + earthBitmap.getHeight() - logoBlockSize,
                            left + logoBlockSize,
                            top + earthBitmap.getHeight(),
                            paint);

                } else {
                    Log.d(TAG, "draw: Bitmap null.");
                }
                holder.unlockCanvasAndPost(canvas);
            }
        }

    }
}
