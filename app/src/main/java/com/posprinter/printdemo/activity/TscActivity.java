package com.posprinter.printdemo.activity;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
//import androidx.coordinatorlayout.widget.CoordinatorLayout;

//import com.google.android.material.snackbar.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;


import com.posprinter.printdemo.R;
import com.posprinter.printdemo.utils.StringUtils;

import net.posprinter.posprinterface.ProcessData;
import net.posprinter.posprinterface.UiExecute;
import net.posprinter.utils.BitmapToByteData;
import net.posprinter.utils.DataForSendToPrinterTSC;

import java.util.ArrayList;
import java.util.List;

public class TscActivity extends AppCompatActivity {


    Button btcontent,
            bttsctext,
            bttscbarcode,
            bttscread,
            btTest,
            bttscpic;
  //  CoordinatorLayout container;

    RelativeLayout relativeLayout;
    ImageView imageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tsc);

        initView();


        clk();
    }



    private void initView() {
        btcontent= (Button) findViewById(R.id.content);
        bttsctext= (Button) findViewById(R.id.tsctext);
        bttscbarcode= (Button) findViewById(R.id.tscbarcode);
        bttscpic= (Button) findViewById(R.id.tscpic);
        relativeLayout= (RelativeLayout) findViewById(R.id.rlimage);
        imageView= (ImageView) findViewById(R.id.image);
       // container = (CoordinatorLayout) findViewById(R.id.activity_tsc);
        btTest= (Button) findViewById(R.id.test);
    }


    public void clk(){
        findViewById(R.id.content).setOnClickListener(v -> {
            printContent();
        });
        findViewById(R.id.tsctext).setOnClickListener(v -> {
            printText();
        });
        findViewById(R.id.tscbarcode).setOnClickListener(v -> {
            printBarcode();
        });
        findViewById(R.id.tscpic).setOnClickListener(v -> {
            printPic();
        });
        findViewById(R.id.test).setOnClickListener(v -> {
            test();
        });
    }

/*
self-check
 */
    private void test() {
        MainActivity.binder.writeDataByYouself(new UiExecute() {
            @Override
            public void onsucess() {
                showSnackbar("successed");
            }

            @Override
            public void onfailed() {
                showSnackbar("failed");

            }
        }, new ProcessData() {
            @Override
            public List<byte[]> processDataBeforeSend() {
                List<byte[]> list=new ArrayList<byte[]>();
                list.add(DataForSendToPrinterTSC.selfTest());
                list.add(DataForSendToPrinterTSC.print(1));
                return list;
            }
        });
    }

    /*
    print image
     */
    private void printPic() {
        Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent,0);
    }



    /*
    print the text ,line and barcode
     */
    private void printContent() {
        MainActivity.binder.writeDataByYouself(new UiExecute() {
            @Override
            public void onsucess() {
                showSnackbar("print ok !");

            }

            @Override
            public void onfailed() {
                showSnackbar("print not ok !");

            }
        }, new ProcessData() {
            @Override
            public List<byte[]> processDataBeforeSend() {

                ArrayList<byte[]> list=new ArrayList<byte[]>();
                //default is gbk,if you don't set the charset
                DataForSendToPrinterTSC.setCharsetName("gbk");
                byte[] data= DataForSendToPrinterTSC.sizeBymm(60,30);
                list.add(data);
                //set the gap
                list.add(DataForSendToPrinterTSC.gapBymm(0,0));
                // clear the cache
                list.add(DataForSendToPrinterTSC.cls());
                //barcode command，parama：int x: x print start point；int y:y print start point；
                //string font，text font type ；int rotation，angle of rotation ；
                //int x_multiplication，Font x directional magnification
                //int y_multiplication,Font y directional magnification
                //string content，print cont
                byte[] data1 = DataForSendToPrinterTSC
                        .text(10, 10, "1", 0, 1, 1,
                                "abc123");
                list.add(data1);
                //print line,int x;int y;int width，width of the line ;int height,height of the line
                list.add(DataForSendToPrinterTSC.bar(20,
                        40, 200, 3));
                //print barcode
                list.add(DataForSendToPrinterTSC.barCode(
                        60, 50, "128", 100, 1, 0, 2, 2,
                        "abcdef12345"));
                //print
                list.add(DataForSendToPrinterTSC.print(1));
                showSnackbar("content");

                return list;
            }
        });

    }
    /*
    print text
     */
    private void printText(){
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

                //if the data is complicated,please do not use the method
                //The data processing of the above sending method is done in the worker thread and does not block the UI thread
                byte[] data0=DataForSendToPrinterTSC.sizeBydot(480, 240);
                byte[] data1=DataForSendToPrinterTSC.cls();

                byte[] data2=DataForSendToPrinterTSC.text(10, 10, "1", 0, 2, 2, "123456" );
                byte[] data3=DataForSendToPrinterTSC.print(1);
                byte[] data= StringUtils.byteMerger(StringUtils.byteMerger
                        (StringUtils.byteMerger(data0, data1), data2), data3);
                List<byte[]> l =new ArrayList<byte[]>();
                l.add(data);
                return l;
            }
        });
    }

    /*
    print barcode
     */
    private void printBarcode(){
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
                ArrayList<byte[]> list=new ArrayList<byte[]>();
                //first you have to set the width and heigt ,
                // you can also use dot or inch as a unit method, specific conversion reference programming manual
                list.add(DataForSendToPrinterTSC.sizeBymm(60,30));
                //set the gap
                list.add(DataForSendToPrinterTSC.gapBymm(0,0));
                //clear cach
                list.add(DataForSendToPrinterTSC.cls());
                //print barcode
                list.add(DataForSendToPrinterTSC.barCode(60,50,"128",100,1,0,2,2,"abcdef12345"));
                //print
                list.add(DataForSendToPrinterTSC.print(1));

                return list;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("test",requestCode+"  "+resultCode);
        if (requestCode==0&&resultCode==RESULT_OK){
            try{
                Uri imagepath=data.getData();
                ContentResolver resolver = getContentResolver();
                Bitmap b= MediaStore.Images.Media.getBitmap(resolver,imagepath);
                imageView.setImageBitmap(b);
                printpicCode(b);


            }catch (Exception e){
                e.printStackTrace();
                Log.e("pic",e.toString());
            }
        }

    }
    /*
    print bitmap
     */
    private void printpicCode(final Bitmap b) {
        if (b==null){showSnackbar("b is null");}else {
            MainActivity.binder.writeDataByYouself(new UiExecute() {
                @Override
                public void onsucess() {
                    relativeLayout.setVisibility(View.VISIBLE);
                    imageView.setImageBitmap(b);

                }

                @Override
                public void onfailed() {

                }
            }, new ProcessData() {
                @Override
                public List<byte[]> processDataBeforeSend() {

                    ArrayList<byte[]> list=new ArrayList<byte[]>();
                    list.add(DataForSendToPrinterTSC.cls());

                    list.add(DataForSendToPrinterTSC.sizeBymm(76,40));
                    list.add(DataForSendToPrinterTSC.gapBymm(2, 0));
                    list.add(DataForSendToPrinterTSC.cls());
                    list.add(DataForSendToPrinterTSC.bitmap(0,0,0,b, BitmapToByteData.BmpType.Dithering));

                    list.add(DataForSendToPrinterTSC.print(1));

                    return list;
                }
            });

        }

    }


    /**
     * show the message
     * @param showstring content
     */
    private void showSnackbar(String showstring){

        Toast.makeText(getApplicationContext(), showstring, Toast.LENGTH_LONG);
    }
}
