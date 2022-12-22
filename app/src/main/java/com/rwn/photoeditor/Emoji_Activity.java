package com.rwn.photoeditor;


import static com.rwn.photoeditor.R.id.*;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.rwn.photoeditor.R.id;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Emoji_Activity extends AppCompatActivity {


    private static final String TAG = Image_Display_Activity.class.getSimpleName();
    private String userInputValue="";

    float vH=0,vW=0;
    float Esize = 105;

    boolean flg=false;

    EmojiconEditText emojiconEditText;

    {
        emojiconEditText = (EmojiconEditText) this.findViewById(emojicon_edit_text);
    }

    ImageView emojiImageView;
    ImageView submitButton;
    EmojIconActions emojIcon;
    View root_view;


    DrawingView dv;
    private Paint mPaint;

    Bitmap textBit = Image_Display_Activity.bm;

    public Emoji_Activity() {
     }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emoji_);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        root_view = findViewById(root_layout);

        emojiImageView = (ImageView)findViewById(emoji_btn);
        submitButton = (ImageView)findViewById(submit_btn);
        emojiconEditText.setEmojiconSize(28);
        emojIcon = new EmojIconActions(this, root_view, emojiconEditText, emojiImageView) {
            @Override
            public void onKeyboardOpen() {

            }

            @Override
            public void onKeyboardClose() {

            }

            @Override
            public void setUseSystemEmoji(boolean b) {

            }
        };
        emojIcon.getClass(R.mipmap.ic_launcher,R.mipmap.ic_launcher);
        emojIcon.setUseSystemEmoji(true);
        emojIcon.ShowEmojIcon();
        emojIcon.setUseSystemEmoji(new KeyboardListenIer() {
            @Override
            public void onKeyboardOpen() {
                Log.e(TAG,"Keyboard Opened!");
            }

            @Override
            protected void onKeyboardClose() {
                Log.e(TAG,"Keyboard Closed!");
            }
        });


        float targetW = getIntent().getExtras().getFloat("width");
        float targetH = getIntent().getExtras().getFloat("height");



        {vH = targetH*(0.89f);
            vW = (targetH*(0.89f) / ((Image_Display_Activity.bm).getHeight())) * ((Image_Display_Activity.bm).getWidth());}
        if(vW>targetW){vW = targetW;vH=(targetW/((Image_Display_Activity.bm).getWidth()))*((Image_Display_Activity.bm).getHeight());}

        dv = new DrawingView(this);
        dv.setBackground(new BitmapDrawable(getResources(),textBit));
        dv.setLayoutParams(new ViewGroup.LayoutParams((int)vW,(int)vH));
        ((RelativeLayout)findViewById(view_drawing_pad)).addView(dv);

        final ImageView saveButton = (ImageView) findViewById(saveIcon);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    saveImage();
                }catch (Exception e){e.printStackTrace();}
            }
        });


        final SeekBar sizeBar = (SeekBar)findViewById(id.sizeBar);
        sizeBar.setProgress(50);
        sizeBar.setEnabled(false);
        sizeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                Esize = progress*(2.1f);
                dv.invalidate();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                saveBitmap();
                dv.invalidate();
                dv.setBackground(new BitmapDrawable(getResources(),textBit));
                userInputValue = emojiconEditText.getText().toString();
                emojIcon.closeEmojIcon();
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(root_view.getWindowToken(),0);
                emojiconEditText.setText("");
                flg=true;
                if(userInputValue!=""&&!userInputValue.isEmpty()) {
                    sizeBar.setEnabled(true);
                }else{sizeBar.setEnabled(false);}
                dv.invalidate();
            }
        });

        Button saveChangesButton = (Button)findViewById(id.saveButton);
        saveChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveBitmap();
                Image_Display_Activity.bm = textBit;
                (Image_Display_Activity.imageDisplay).setImageBitmap();
                Image_Display_Activity.iHeight = textBit.getHeight();
                Toast.makeText(getApplicationContext(),"Changes Applied",Toast.LENGTH_SHORT).show();
            }
        });
        Button clearEmojisButton = (Button)findViewById(clearEmojiButton);
        clearEmojisButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                (dv.mCanvas).drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                userInputValue="";
                dv.invalidate();
            }
        });

        ImageView cancelIcon = (ImageView)findViewById(id.cancelIcon);
        cancelIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = getBaseContext().getPackageManager()
                        .getLaunchIntentForPackage( getBaseContext().getPackageName() );
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });

    }

    public class DrawingView extends View{


        private Bitmap mBitmap;
        private Canvas mCanvas;
        //private Path mPath;
        private Paint mBitmapPaint;
        Context context;
        float xPos=0,yPos=0;

        public DrawingView(Context c){
            super(c);
            context = c;
            mBitmapPaint = new Paint(Paint.DITHER_FLAG);
            mPaint = new Paint();

            mPaint.setColor(Color.RED);
            mPaint.setStyle(Paint.Style.FILL);

            mPaint.setTextSize(Esize);
        }
        @Override
        protected void onSizeChanged(int w,int h,int oldw,int oldh){
            super.onSizeChanged((int)vW,(int)vH,oldw,oldh);
            mBitmap = Bitmap.createBitmap((int)vW,(int)vH, Bitmap.Config.ARGB_8888);

            mCanvas = new Canvas(mBitmap);
            Rect textRect = new Rect();
            xPos = (mCanvas.getWidth()/2)-2;
            yPos = (int)((mCanvas.getHeight()/2)-((mPaint.descent()+mPaint.ascent())/2));
        }

        @Override
        protected void onDraw(Canvas canvas){
            super.onDraw(canvas);

            canvas.drawBitmap(mBitmap,0,0,mBitmapPaint);
            mPaint.setTextSize(Esize);
            if(flg) {
                canvas.drawText(userInputValue, xPos, yPos, mPaint);
            }

        }
        private float mX,mY;
        private static final float TOUCH_TOLERANCE = 4;

        private void touch_start(float x,float y){

            mX = x;
            mY = y;
            xPos=x;
            yPos=y;
        }
        private void touch_up(){

        }
        private void touch_move(float x,float y){
            float dx = Math.abs(x-mX);
            float dy = Math.abs(y-mY);
            if(dx>=TOUCH_TOLERANCE || dy>=TOUCH_TOLERANCE){

                mX=x;
                mY=y;
                xPos=x;
                yPos=y;
            }
        }
        @Override
        public boolean onTouchEvent(MotionEvent event){
            float x = event.getX();
            float y = event.getY();

            switch(event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    touch_start(x,y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    touch_move(x,y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    touch_up();
                    invalidate();
                    break;
            }

            return true;
        }


    }

    private void saveBitmap(){
        Bitmap bitmap = Bitmap.createBitmap(dv.getWidth(),dv.getHeight(),Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bitmap);
        dv.draw(c);
        textBit = bitmap;
    }

    private void saveImage()throws Exception{
        saveBitmap();
        Image_Display_Activity.bm = textBit;
        (Image_Display_Activity.imageDisplay).setImageBitmap();
        FileOutputStream fOut = null;

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "PNG_"+timeStamp+"_";
        File file2 = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File file = File.createTempFile(imageFileName,".png",file2);

        try{
            fOut = new FileOutputStream(file);
        }catch (Exception e){e.printStackTrace();}
        (Image_Display_Activity.bm).compress(Bitmap.CompressFormat.PNG,100,fOut);
        try{
            fOut.flush();
        }catch (Exception e){e.printStackTrace();}
        try{fOut.close();}catch (IOException e){e.printStackTrace();}
        try{
            MediaStore.Images.Media.insertImage(getContentResolver(),file.getAbsolutePath(),file.getName(),file.getName());}
        catch (FileNotFoundException e){e.printStackTrace();}

        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri cUri = Uri.fromFile(file);
        mediaScanIntent.setData(cUri);
        this.sendBroadcast(mediaScanIntent);
        Toast.makeText(getApplicationContext(),"Image Saved to Pictures",Toast.LENGTH_SHORT).show();

        dv.invalidate();
    }

}









