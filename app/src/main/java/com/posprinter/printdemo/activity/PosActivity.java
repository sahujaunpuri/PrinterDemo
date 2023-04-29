package com.posprinter.printdemo.activity;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.snackbar.Snackbar;
import com.posprinter.printdemo.R;
import com.posprinter.printdemo.utils.StringUtils;
import com.zxy.tiny.Tiny;
import com.zxy.tiny.callback.BitmapCallback;

import net.posprinter.posprinterface.ProcessData;
import net.posprinter.posprinterface.UiExecute;
import net.posprinter.utils.BitmapToByteData;
import net.posprinter.utils.DataForSendToPrinterPos80;
import net.posprinter.utils.PosPrinterDev;

import java.util.ArrayList;
import java.util.List;


public class PosActivity extends AppCompatActivity {

    Button btText,btBarCode,btImage,btQRcode,checklink;
    CoordinatorLayout container;
    ImageView imageView;
    EditText text;
    RelativeLayout rl;
    Receiver netReciever;
    TextView tip;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pos);


        netReciever=new Receiver();
        registerReceiver(netReciever,new IntentFilter(MainActivity.DISCONNECT));

        initview();

        if (MainActivity.ISCONNECT){
            clk();
        }else {
            showSnackbar(getString(R.string.con_has_discon));
        }
        //init the tiny (use to compress the bitmap).
        Tiny.getInstance().init(getApplication());
    }

    private void initview(){
        container= (CoordinatorLayout) findViewById(R.id.activity_pos);
        btText= (Button) findViewById(R.id.btText);
        btBarCode= (Button) findViewById(R.id.btbarcode);
        btImage= (Button) findViewById(R.id.btpic);
        btQRcode= (Button) findViewById(R.id.qrcode);
        imageView= (ImageView) findViewById(R.id.image);
        rl= (RelativeLayout) findViewById(R.id.rl);
        text= (EditText) findViewById(R.id.text);
        checklink= (Button) findViewById(R.id.checklink);
        tip= (TextView) findViewById(R.id.tv_net_disconnect);

    }

    public void clk(){
        findViewById(R.id.btText).setOnClickListener(v -> {
            printText();
        });
        findViewById(R.id.btbarcode).setOnClickListener(v -> {
            printBarcode();
        });
        findViewById(R.id.qrcode).setOnClickListener(v -> {
            printQRcode();
        });
        findViewById(R.id.btpic).setOnClickListener(v -> {
            printPIC();
        });
        findViewById(R.id.checklink).setOnClickListener(v -> {
            checklink();
        });
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(netReciever);
    }

    /*
     print text
     */
    private void printText(){

        MainActivity.binder.writeDataByYouself(
                new UiExecute() {
                    @Override
                    public void onsucess() {

                    }

                    @Override
                    public void onfailed() {

                    }
                }, new ProcessData() {
                    @Override
                    public List<byte[]> processDataBeforeSend() {

                        List<byte[]> list=new ArrayList<byte[]>();
                        //creat a text ,and make it to byte[],
                        String str=text.getText().toString();
                        if (str.equals(null)||str.equals("")){
                            showSnackbar(getString(R.string.text_for));
                        }else {
                            //initialize the printer
//                            list.add( DataForSendToPrinterPos58.initializePrinter());
                            list.add(DataForSendToPrinterPos80.initializePrinter());
                            byte[] data1= StringUtils.strTobytes(str);
                            list.add(data1);
                            //should add the command of print and feed line,because print only when one line is complete, not one line, no print
                            list.add(DataForSendToPrinterPos80.printAndFeedLine());
                            //cut pager
                            list.add(DataForSendToPrinterPos80.selectCutPagerModerAndCutPager(66,1));
                            return list;
                        }
                        return null;
                    }
                });

    }

    /*
    print barcode
	before you print the barcode ,you should set attribute of the barcode,for example :width ,height ,HRI.
     */
    private void printBarcode(){
        MainActivity.binder.writeDataByYouself(new UiExecute() {
            @Override
            public void onsucess() {
                showSnackbar("01234567890");
            }

            @Override
            public void onfailed() {

            }
        }, new ProcessData() {
            @Override
            public List<byte[]> processDataBeforeSend() {
                List<byte[]>list=new ArrayList<byte[]>();
                //initialize the printer
                list.add(DataForSendToPrinterPos80.initializePrinter());
                //select alignment
                list.add(DataForSendToPrinterPos80.selectAlignment(1));
                //select HRI position
                list.add(DataForSendToPrinterPos80.selectHRICharacterPrintPosition(02));
                //set the width
                list.add(DataForSendToPrinterPos80.setBarcodeWidth(3));
                //set the height ,usually 162
                list.add(DataForSendToPrinterPos80.setBarcodeHeight(162));
                //print barcode ,attention,there are two method for barcode.
                //different barcode type,please refer to the programming manual
                //UPC-A
                list.add(DataForSendToPrinterPos80.printBarcode(69,10,"B123456789"));

                list.add(DataForSendToPrinterPos80.printAndFeedLine());

                return list;
            }
        });
    }
    /*
    print the barcode ,also need to set the attribute before print
    Some of the necessary settings, you need to refer to the example of the programming manual,
     and then call the corresponding instructions and methods
     */
    private void printQRcode(){
        MainActivity.binder.writeDataByYouself(new UiExecute() {
            @Override
            public void onsucess() {
            }
            @Override
            public void onfailed() {
            }
        }, new ProcessData() {
            @Override
            public List<byte[]> processDataBeforeSend() {
                List<byte[]> list=new ArrayList<byte[]>();
                //initialize the printer
                list.add(DataForSendToPrinterPos80.initializePrinter());
                //select alignment
                list.add(DataForSendToPrinterPos80.selectAlignment(1));

                //set the size
                list.add(DataForSendToPrinterPos80.SetsTheSizeOfTheQRCodeSymbolModule(3));
                //set the error correction level
                list.add(DataForSendToPrinterPos80.SetsTheErrorCorrectionLevelForQRCodeSymbol(48));
                //store symbol data in the QRcode symbol storage area
                list.add(DataForSendToPrinterPos80.StoresSymbolDataInTheQRCodeSymbolStorageArea(
                        "Welcome to Printer Technology to create advantages Quality to win in the future"
                ));

                //Prints The QRCode Symbol Data In The Symbol Storage Area
                list.add(DataForSendToPrinterPos80.PrintsTheQRCodeSymbolDataInTheSymbolStorageArea());
                //print
                list.add(DataForSendToPrinterPos80.printAndFeedLine());
                //or else you could use the simple encapsulation method
                //but different ，Call the step method above，the storage data din't clean up ,
                //call PrintsTheQRCodeSymbolDataInTheSymbolStorageArea，print，you don't have set the content of qrcode again
                //Equivalent to resetting the qrcode contents in the cache every time

                //list.add(DataForSendToPrinterPos80.printQRcode(3, 48, "www.xprint.net"));

                return list;
            }
        });

    }


    private void printPIC(){
        Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent,0);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("test",requestCode+"  "+resultCode);
        if (requestCode==0&&resultCode==RESULT_OK){
            Log.e("test","test2");

            try{
                Uri imagepath=data.getData();
                ContentResolver resolver = getContentResolver();
                Bitmap b=MediaStore.Images.Media.getBitmap(resolver,imagepath);
                b1=convertGreyImg(b);
                Message message=new Message();
                message.what=1;
                handler.handleMessage(message);

                //compress the bitmap
                Tiny.BitmapCompressOptions options = new Tiny.BitmapCompressOptions();
                Tiny.getInstance().source(b1).asBitmap().withOptions(options).compress(new BitmapCallback() {
                    @Override
                    public void callback(boolean isSuccess, Bitmap bitmap) {
                        if (isSuccess){
//                            Toast.makeText(PosActivity.this,"bitmap: "+bitmap.getByteCount(),Toast.LENGTH_LONG).show();
                            b2=bitmap;
//                            b2=resizeImage(b1,380,false);
                            Message message=new Message();
                            message.what=2;
                            handler.handleMessage(message);
                        }


                    }
                });
//                b2=resizeImage(b1,576,386,false);
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    /*
    let the printer print bitmap
     */
    private Bitmap b1;//grey-scale bitmap
    private  Bitmap b2;//compress bitmap
    private void printpicCode(final Bitmap printBmp){


        MainActivity.binder.writeDataByYouself(new UiExecute() {
            @Override
            public void onsucess() {

            }

            @Override
            public void onfailed() {
                showSnackbar("failed");
            }
        }, new ProcessData() {
            @Override
            public List<byte[]> processDataBeforeSend() {
                List<byte[]> list=new ArrayList<byte[]>();
                list.add(DataForSendToPrinterPos80.initializePrinter());
                list.add(DataForSendToPrinterPos80.printRasterBmp(
                        0,printBmp, BitmapToByteData.BmpType.Threshold, BitmapToByteData.AlignType.Left,576));
//                list.add(DataForSendToPrinterPos80.printAndFeedForward(3));
                list.add(DataForSendToPrinterPos80.selectCutPagerModerAndCutPager(66,1));
                return list;
            }
        });




    }
/*
print the bitmap ,the connection is USB
 */
    private void printUSBbitamp(final Bitmap printBmp){

        int height=printBmp.getHeight();
        // if height > 200 cut the bitmap
        if (height>200){

            MainActivity.binder.writeDataByYouself(new UiExecute() {
                @Override
                public void onsucess() {

                }

                @Override
                public void onfailed() {

                }
            }, new ProcessData() {
                @Override
                public List<byte[]> processDataBeforeSend() {
                    List<byte[]> list=new ArrayList<byte[]>();
                    list.add(DataForSendToPrinterPos80.initializePrinter());
                    List<Bitmap> bitmaplist=new ArrayList<>();
                    bitmaplist=cutBitmap(200,printBmp);//cut bitmap
                    if(bitmaplist.size()!=0){
                        for (int i=0;i<bitmaplist.size();i++){
                            list.add(DataForSendToPrinterPos80.printRasterBmp(0,bitmaplist.get(i),BitmapToByteData.BmpType.Threshold,BitmapToByteData.AlignType.Center,576));
                        }
                    }
                    list.add(DataForSendToPrinterPos80.selectCutPagerModerAndCutPager(66,1));
                    return list;
                }
            });
        }else {
            MainActivity.binder.writeDataByYouself(new UiExecute() {
                @Override
                public void onsucess() {

                }

                @Override
                public void onfailed() {

                }
            }, new ProcessData() {
                @Override
                public List<byte[]> processDataBeforeSend() {
                    List<byte[]> list=new ArrayList<byte[]>();
                    list.add(DataForSendToPrinterPos80.initializePrinter());
                    list.add(DataForSendToPrinterPos80.printRasterBmp(
                            0,printBmp, BitmapToByteData.BmpType.Threshold, BitmapToByteData.AlignType.Center,576));
                    list.add(DataForSendToPrinterPos80.selectCutPagerModerAndCutPager(66,1));
                    return list;
                }
            });
        }

    }
    /*
    Cut picture method, equal height cutting
     */
    private List<Bitmap> cutBitmap(int h,Bitmap bitmap){
        int width=bitmap.getWidth();
        int height=bitmap.getHeight();
        boolean full=height%h==0;
        int n=height%h==0?height/h:(height/h)+1;
        Bitmap b;
        List<Bitmap> bitmaps=new ArrayList<>();
        for (int i=0;i<n;i++){
            if (full){
                b=Bitmap.createBitmap(bitmap,0,i*h,width,h);
            }else {
                if (i==n-1){
                    b=Bitmap.createBitmap(bitmap,0,i*h,width,height-i*h);
                }else {
                    b=Bitmap.createBitmap(bitmap,0,i*h,width,h);
                }
            }

            bitmaps.add(b);
        }

        return bitmaps;
    }




    /**
     * show the message
     * @param showstring content
     */
    private void showSnackbar(String showstring){
        Snackbar.make(container, showstring,Snackbar.LENGTH_LONG)
                .setActionTextColor(getResources().getColor(R.color.button_unable)).show();
    }

   public Handler handler=new Handler(){
       @Override
       public void handleMessage(Message msg) {
           super.handleMessage(msg);
           switch (msg.what){
               case 1:
                   rl.setVisibility(View.VISIBLE);
                   tip.setVisibility(View.GONE);
                   imageView.setImageBitmap(b1);
                   break;
               case 2:
                   //usb connection need special deal with
                   if (PosPrinterDev.PortType.USB!=MainActivity.portType){
                       printpicCode(b2);
                   }else {
                       showSnackbar("bimap  "+b2.getWidth()+"  height: "+b2.getHeight());
                       b2=resizeImage(b2,576,false);
                       printUSBbitamp(b2);

                   }



                   tip.setVisibility(View.GONE);
                   break;
               case 3://disconnect
                   btText.setEnabled(false);
                   btBarCode.setEnabled(false);
                   btQRcode.setEnabled(false);
                   btImage.setEnabled(false);
                   tip.setVisibility(View.VISIBLE);
                   break;
               case 4:
                   tip.setVisibility(View.VISIBLE);
                   break;


           }

       }
   };

    /**
     convert grey image
     * @param img  bitmap
     * @return  data
     */
    public Bitmap convertGreyImg(Bitmap img) {
        int width = img.getWidth();
        int height = img.getHeight();

        int[] pixels = new int[width * height];

        img.getPixels(pixels, 0, width, 0, 0, width, height);


        //The arithmetic average of a grayscale image; a threshold
        double redSum=0,greenSum=0,blueSun=0;
        double total=width*height;

        for(int i = 0; i < height; i++)  {
            for(int j = 0; j < width; j++) {
                int grey = pixels[width * i + j];

                int red = ((grey  & 0x00FF0000 ) >> 16);
                int green = ((grey & 0x0000FF00) >> 8);
                int blue = (grey & 0x000000FF);



                redSum+=red;
                greenSum+=green;
                blueSun+=blue;


            }
        }
        int m=(int) (redSum/total);

        //Conversion monochrome diagram
        for(int i = 0; i < height; i++)  {
            for(int j = 0; j < width; j++) {
                int grey = pixels[width * i + j];

                int alpha1 = 0xFF << 24;
                int red = ((grey  & 0x00FF0000 ) >> 16);
                int green = ((grey & 0x0000FF00) >> 8);
                int blue = (grey & 0x000000FF);


                if (red>=m) {
                    red=green=blue=255;
                }else{
                    red=green=blue=0;
                }
                grey = alpha1 | (red << 16) | (green << 8) | blue;
                pixels[width*i+j]=grey;


            }
        }
        Bitmap mBitmap=Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        mBitmap.setPixels(pixels, 0, width, 0, 0, width, height);



        return mBitmap;
    }


    /*
        use the Matrix compress the bitmap
	 *   */
    public static Bitmap resizeImage(Bitmap bitmap, int w,boolean ischecked)
    {

        Bitmap BitmapOrg = bitmap;
        Bitmap resizedBitmap = null;
        int width = BitmapOrg.getWidth();
        int height = BitmapOrg.getHeight();
        if (width<=w) {
            return bitmap;
        }
        if (!ischecked) {
            int newWidth = w;
            int newHeight = height*w/width;

            float scaleWidth = ((float) newWidth) / width;
            float scaleHeight = ((float) newHeight) / height;

            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleHeight);
            // if you want to rotate the Bitmap
            // matrix.postRotate(45);
            resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width,
                    height, matrix, true);
        }else {
            resizedBitmap=Bitmap.createBitmap(BitmapOrg, 0, 0, w, height);
        }

        return resizedBitmap;
    }

    /*
     check link
     */
    private void checklink(){
        MainActivity.binder.checkLinkedState(new UiExecute() {
            @Override
            public void onsucess() {

            }

            @Override
            public void onfailed() {
                showSnackbar("disconnect ");
                Message message =new Message();
                message.what=3;
                handler.handleMessage(message);

            }
        });
    }





/*
BroadcastReceiver of disconnect
 */
    private class Receiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            if (action.equals(MainActivity.DISCONNECT)){
                Message message=new Message();
                message.what=4;
                handler.handleMessage(message);
            }
        }
    }
}
