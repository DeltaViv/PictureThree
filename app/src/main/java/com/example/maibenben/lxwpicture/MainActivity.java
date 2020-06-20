package com.example.maibenben.lxwpicture;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity

        implements NavigationView.OnNavigationItemSelectedListener {
    private ImageView oldimageView;
    private Uri imageUri;
    public static final int TAKE_PHOTO = 1;
    public static final int GET_PHOTO = 2;
    public static final int CUT_IMAGE = 3;
    public String OUTPATH = "1.jpg";
    final int Requset_Photo = 3000;
    final int Requset_Vedio = 3001;
    public Uri uri;
    String path = "";

    private SeekBar sb_alpha;
    private SeekBar sb_rad;
    private SeekBar sb_green;
    private SeekBar sb_blue;

    // 原图
    private Bitmap bitmap;

    private Canvas canvas;
    private Bitmap newBitmap; // 空白的新图
    private Paint paint;

    private final int SB_MAX = 255;

    private Button rgnBtn;
    private Button cutBtn;
    private Button paiBtn;

    private FrameLayout includeLayout;

    private String inpath;


    // 涂鸦
    private GraffitiView handWrite = null;
    private Button clear = null;
    private BitmapUtils bitmapUtils = null;

    // 保存
    private UsualDialogger dialog2 = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            }
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog2 = UsualDialogger.Builder(MainActivity.this)
                        .setTitle("图片名称")
                        .setMessage("")
                        .setOnConfirmClickListener("确定", new UsualDialogger.onConfirmClickListener() {

                            @Override
                            public void onClick(View view) {
                                String name = String.valueOf(dialog2.tvMessage.getText())+".jpg";
                                try {
                                    saveMyBitmap(bitmap.copy(Bitmap.Config.ARGB_8888, true), name);
                                    Toast.makeText(MainActivity.this, "图片"+name+"保存成功", Toast.LENGTH_SHORT).show();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                if (dialog2 != null) {
                                    dialog2.dismiss();
                                }
                            }
                        })
                        .setOnCancelClickListener("取消", new UsualDialogger.onCancelClickListener() {
                            @Override
                            public void onClick(View view) {
                                Toast.makeText(MainActivity.this, "取消", Toast.LENGTH_SHORT).show();
                                if (dialog2 != null) {
                                    dialog2.dismiss();
                                }
                            }


                        })
                        .build()
                        .shown();
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // 控件获取
        oldimageView = (ImageView) findViewById(R.id.oldimageView);
        initView();
        // 工具栏按钮
        rgnBtn = (Button) findViewById(R.id.rgnBtn);
        includeLayout = (FrameLayout) findViewById(R.id.includeLayout);
        includeLayout.setVisibility(View.INVISIBLE);
        cutBtn = (Button)findViewById(R.id.cutBtn);
        // 色彩按钮监听
        rgnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getBaseContext(), "打开色彩编辑", Toast.LENGTH_SHORT).show();
                if (includeLayout.getVisibility() == View.INVISIBLE){
                    includeLayout.setVisibility(View.VISIBLE);
                }else{
                    includeLayout.setVisibility(View.INVISIBLE);
                }
                initEdit_Picture();
            }
        });

        cutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipImage();
            }
        });

        // 涂鸦
        bitmapUtils = new BitmapUtils();
        handWrite = (GraffitiView) findViewById(R.id.handwriteview);
        handWrite.setstyle(10);
        handWrite.setColor(Color.RED);
        handWrite.setVisibility(View.INVISIBLE);
        paiBtn = (Button) findViewById(R.id.paiBtn);
        paiBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(oldimageView.getVisibility() == View.VISIBLE){
                    oldimageView.setVisibility(View.INVISIBLE);
                    handWrite.setVisibility(View.VISIBLE);
                }else{
                    oldimageView.setVisibility(View.VISIBLE);
                    handWrite.setVisibility(View.INVISIBLE);
                }
                handWrite.clear(bitmapUtils.scaleBitmap(bitmap.copy(Bitmap.Config.ARGB_8888, true), (float) 0.4));
            }
        });

    }

    private void initView(){
        // 绘图控件
        sb_alpha = (SeekBar) findViewById(R.id.sb_alpha);
        sb_rad = (SeekBar) findViewById(R.id.sb_rad);
        sb_green = (SeekBar) findViewById(R.id.sb_green);
        sb_blue = (SeekBar) findViewById(R.id.sb_blue);

        /**
         * 设置拖动条最大值
         */
        sb_alpha.setMax(SB_MAX);
        sb_rad.setMax(SB_MAX);
        sb_green.setMax(SB_MAX);
        sb_blue.setMax(SB_MAX);

        sb_alpha.setOnSeekBarChangeListener(seekBarChangeListener);
        sb_rad.setOnSeekBarChangeListener(seekBarChangeListener);
        sb_green.setOnSeekBarChangeListener(seekBarChangeListener);
        sb_blue.setOnSeekBarChangeListener(seekBarChangeListener);

    }

    private void initEdit_Picture(){
        /**
         * 绘制图片的基本流程
         *  1.获取原图(已经完成)
         *  2.创建一张空白的图片
         *  3.指定一张图片
         *  4.显示图片：获取画好的空白图片
         */
        /**
         * 2.创建一张空白的图片
         * 必须要给一张空白的图片，然后操作图片；
         *   参数一：空白图片的宽度，就拿原始图片的宽度
         *   参数二：空白图片的高度，就拿原始图片的高度
         *   参数三：空白图片的配置信息，就拿原始图片的配置信息
         */
        newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());

        // 拿一块新的画布，把空白图片和画布关联
        canvas = new Canvas(newBitmap);
        // 设置画布的背景
        canvas.drawColor(Color.WHITE);
        // 拿一只新的画笔
        paint = new Paint();

        /**
         * 3.在指定的图片上操作，必须要指定好在哪张图片上操作；
         *    参数一：原始的 之前的 bitmap
         *    参数二：注意：Matrix集结了非常复杂的高等数学运算，操作图片的 图片缩放/图片旋转/图片平移/等等 都是Matrix来计算运算的
         *    参数三：画笔🖌️
         */
        canvas.drawBitmap(bitmap, new Matrix(), paint);

        /**
         * 4.操作图片完成✅后，需要获取结果，操作后的结果就是空白图片
         */
        oldimageView.setImageBitmap(newBitmap);
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_gallery) {
            Toast.makeText(getBaseContext(), "cannot use", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Intent.ACTION_PICK, null);
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
            startActivityForResult(intent, GET_PHOTO);
        } else if (id == R.id.nav_camera) {
            if(CameraCanUseUtils.isCameraCanUse()) {
                //摄像头可用
                selectPicFromCamera();
            } else{
                //摄像头不可用
                Toast.makeText(getBaseContext(),"请打开摄像头权限以及存储权限", Toast.LENGTH_LONG);
                //跳转至app设置
                getAppDetailSettingIntent();
            }
        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }



    //
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    try {
                        bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    oldimageView.setImageBitmap(bitmap);
                    handWrite.clear(bitmapUtils.scaleBitmap(bitmap.copy(Bitmap.Config.ARGB_8888, true), (float) 0.4));
                }
                break;
            case GET_PHOTO:
                // 从相册返回的数据
                if (data != null) {
                    // 得到图片的全路径
                    uri = data.getData();
                    try {
                        bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    oldimageView.setImageURI(uri);
                    handWrite.clear(bitmapUtils.scaleBitmap(bitmap.copy(Bitmap.Config.ARGB_8888, true), (float) 0.4));
                }
                break;
            case CUT_IMAGE:
                String path = ClipImageActivity.ClipOptions.createFromBundle(data).getOutputPath();
//                Toast.makeText(getBaseContext(), path, Toast.LENGTH_LONG).show();
                if (path != null) {
                    Bitmap bitmap = BitmapFactory.decodeFile(path);
                    oldimageView.setImageBitmap(bitmap);
                }
                break;
            default:
                break;
        }
    }

    private void selectPicFromCamera() {
        // 用来存放摄像头拍下的图片，把图片命名为output_image.jpg
        File outputImage = new File(Environment.getExternalStorageDirectory(), "output_image.jpg");
        try {
            if (outputImage.exists()) {
                outputImage.delete();
            }
            outputImage.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (Build.VERSION.SDK_INT >= 24) {
            imageUri = FileProvider.getUriForFile(MainActivity.this, "com.example.maibenben.lxwpicture.fileprovider", outputImage);
        } else {
            //将File对象转换成URI对象，这个URI对象标识着output_image.jpg这张图片的本地真是路径
            imageUri = Uri.fromFile(outputImage);
        }
        uri = imageUri;
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, TAKE_PHOTO);
    }

    private static int count = 0;
    private static String preTime = "";

    /** 生成新的名称 */
    public static String NewName()
    {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String date = formatter.format(new Date());

        DateFormat formatter2 = new SimpleDateFormat("HH.mm.ss");
        String time = formatter2.format(new Date());

        if (!preTime.equals(time))
        {
            count = 1;
            preTime = time;
        }
        else count++;

        String fileName = date + "_" + time + "_" + count;
        return fileName;
    }


    //跳转app设置
    private void getAppDetailSettingIntent() {
        Intent localIntent = new Intent();
        if (Build.VERSION.SDK_INT >= 9) {
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", getBaseContext().getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName", getBaseContext().getPackageName());
        }
        startActivity(localIntent);
    }

    // edit
    /**
     * 定义SeekBar监听
     */
    private final SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        float alpha = 1; // （0 ～ 1 就是变化的过程） 0就是透明  0.5就是半透明  1就是完全显示
        float rad = 0;
        float green = 0;
        float blue = 0;
        /**
         * 拖动条 拖动过程中，拖动的改变
         *
         * @param seekBar
         * @param progress
         * @param fromUser
         */
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            switch (seekBar.getId()) {
                case R.id.sb_alpha:
                    alpha = 1-(float) (SB_MAX - progress) / SB_MAX;
                    Log.d("edit", "progress:" + progress + " alpha:" + alpha);
                    break;
                case R.id.sb_rad:
                    rad = 1-(float)(SB_MAX - progress) / SB_MAX;
                    break;
                case R.id.sb_green:
                    green = 1-(float)(SB_MAX - progress) / SB_MAX;
                    break;
                case R.id.sb_blue:
                    blue = 1-(float) (SB_MAX - progress) / SB_MAX;
                    break;
                default:
                    break;

            }
            /**
             * ColorMatrix 做了一系列复杂的颜色值ragb运算
             * 颜色值ragb运算，颜色矩阵是 5*4 的float数组
             */
            ColorMatrix colorMatrix = new ColorMatrix();
            /**
             * 以下这些值，是颜色矩阵的内部运算是需要这样的值，是这样排列的
             * 0, 0, 0, 0, 0,  // 红色
             * 0, 0, 0, 0, 0,  // 绿色
             * 0, 0, 0, 0, 0,  // 蓝色
             * 0, 0, 0, 0, 0,  // 透明的
             *
             * 以下这些值是让图片正常显示，是原图的效果 （0 ～ 1 就是变化的过程）
             * 1, 0, 0, 0, 0,  // 红色
             * 0, 1, 0, 0, 0,  // 绿色
             * 0, 0, 1, 0, 0,  // 蓝色
             * 0, 0, 0, 1, 0,  // 透明的
             */
            colorMatrix.set(new float[]{
                    rad, 0, 0, 0, 0,  // 红色
                    0, green, 0, 0, 0,  // 绿色
                    0, 0, blue, 0, 0,  // 蓝色
                    0, 0, 0, alpha, 0,  // 透明的
            });
            //颜色矩阵-颜色过滤器，需要ColorMatrix
            ColorFilter colorFilter = new ColorMatrixColorFilter(colorMatrix);
            //给画笔设置颜色过滤器，就可以绘制好图片的颜色
            paint.setColorFilter(colorFilter);
            //由于画笔,修改了颜色过滤器-颜色矩阵的颜色运算，所以需要 用画笔画图片操作
            canvas.drawBitmap(bitmap, new Matrix(), paint);
            //4.操作图片完成后，需要获取结果，操作后的结果就是空白图片
            oldimageView.setImageBitmap(newBitmap);
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };

    // 剪裁
    private void ClipImage(){
        ClipImageActivity.prepare()
                .aspectX(3).aspectY(2)
                .inputPath(String.valueOf(uri)).outputPath(OUTPATH)
                .startForResult(this, CUT_IMAGE);
    }

    private String getPath(Context context, Uri uri) {
        String path = null;
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor == null) {
            return null;
        }
        if (cursor.moveToFirst()) {
            try {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        cursor.close();
        return path;
    }

    public boolean saveMyBitmap(Bitmap bmp, String bitName) throws IOException {

        boolean flag = false;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
        {

            // 获得存储卡的路径
            String sdpath = Environment.getExternalStorageDirectory() + "/";
            String mSavePath = sdpath + "lzrc/imgs";

            File f = new File(mSavePath, bitName);
            if (!f.exists()) {
                f.getParentFile().mkdirs();
                f.createNewFile();
            }
            FileOutputStream fOut = null;
            try {
                fOut = new FileOutputStream(f);
                bmp.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                flag = true;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                fOut.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return flag;
    }


}
