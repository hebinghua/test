package com.demo.paymentlauncher;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int REQ_IMAGE = 1001;
    private ImageView imagePreview;
    private TextView imageHint;
    private Button payButton;
    private Uri selectedImageUri;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(buildContent());
    }

    private View buildContent() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.rgb(247,248,250));
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(24),dp(36),dp(24),dp(32));
        scroll.addView(root,new ScrollView.LayoutParams(-1,-1));

        TextView title=new TextView(this); title.setText("QRIS Payment Launcher"); title.setTextSize(26); title.setTextColor(Color.rgb(20,20,20)); title.setTypeface(null,1); root.addView(title);
        TextView sub=new TextView(this); sub.setText("选择二维码图片，通过系统分享协议直达钱包"); sub.setTextSize(15); sub.setTextColor(Color.rgb(100,104,110)); LinearLayout.LayoutParams slp=new LinearLayout.LayoutParams(-1,-2); slp.topMargin=dp(8); root.addView(sub,slp);

        LinearLayout card=new LinearLayout(this); card.setOrientation(LinearLayout.VERTICAL); card.setPadding(dp(16),dp(16),dp(16),dp(16)); card.setBackground(roundRect(Color.WHITE,18)); LinearLayout.LayoutParams clp=new LinearLayout.LayoutParams(-1,-2); clp.topMargin=dp(26); root.addView(card,clp);
        imagePreview=new ImageView(this); imagePreview.setScaleType(ImageView.ScaleType.CENTER_CROP); imagePreview.setBackground(roundRect(Color.rgb(238,240,243),14)); card.addView(imagePreview,new LinearLayout.LayoutParams(-1,dp(260)));
        imageHint=new TextView(this); imageHint.setText("尚未选择图片"); imageHint.setGravity(Gravity.CENTER); imageHint.setTextColor(Color.rgb(125,130,136)); imageHint.setTextSize(15); card.addView(imageHint,new LinearLayout.LayoutParams(-1,dp(44)));
        Button upload=makePrimaryButton("上传图片"); upload.setOnClickListener(v->pickImage()); LinearLayout.LayoutParams ulp=new LinearLayout.LayoutParams(-1,dp(52)); ulp.topMargin=dp(10); card.addView(upload,ulp);
        payButton=makePrimaryButton("支付"); payButton.setEnabled(false); payButton.setAlpha(.45f); payButton.setOnClickListener(v->showWalletBottomSheet()); LinearLayout.LayoutParams plp=new LinearLayout.LayoutParams(-1,dp(56)); plp.topMargin=dp(18); root.addView(payButton,plp);

        TextView note=new TextView(this); note.setText("V3：ACTION_SEND image/* + EXTRA_STREAM + ClipData\n直接把当前图片 URI 授权给钱包的图片接收 Activity"); note.setTextSize(13); note.setTextColor(Color.rgb(110,114,120)); note.setGravity(Gravity.CENTER); LinearLayout.LayoutParams nlp=new LinearLayout.LayoutParams(-1,-2); nlp.topMargin=dp(16); root.addView(note,nlp);
        return scroll;
    }

    private Button makePrimaryButton(String text){ Button b=new Button(this); b.setText(text); b.setAllCaps(false); b.setTextSize(16); b.setTextColor(Color.WHITE); b.setBackground(roundRect(Color.rgb(24,24,24),14)); return b; }

    private void pickImage(){
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(i,REQ_IMAGE);
    }

    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==REQ_IMAGE&&resultCode==RESULT_OK&&data!=null&&data.getData()!=null){
            selectedImageUri=data.getData();
            try { getContentResolver().takePersistableUriPermission(selectedImageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION); } catch(Exception ignored){}
            imagePreview.setImageURI(selectedImageUri);
            imageHint.setText("图片已选择");
            payButton.setEnabled(true);
            payButton.setAlpha(1f);
        }
    }

    private void showWalletBottomSheet(){
        final Dialog d=new Dialog(this);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LinearLayout sheet=new LinearLayout(this);
        sheet.setOrientation(LinearLayout.VERTICAL);
        sheet.setPadding(dp(18),dp(10),dp(18),dp(18));
        sheet.setBackground(topRoundRect(Color.rgb(30,30,32),24));

        View handle=new View(this); handle.setBackground(roundRect(Color.rgb(105,105,110),6));
        LinearLayout.LayoutParams hlp=new LinearLayout.LayoutParams(dp(42),dp(4)); hlp.gravity=Gravity.CENTER_HORIZONTAL; hlp.bottomMargin=dp(14); sheet.addView(handle,hlp);
        TextView title=new TextView(this); title.setText("Pay with"); title.setTextSize(20); title.setTypeface(null,1); title.setTextColor(Color.WHITE); title.setPadding(dp(6),0,0,dp(8)); sheet.addView(title,new LinearLayout.LayoutParams(-1,-2));

        addWalletRow(sheet,d,"GoPay","com.gojek.gopay");
        addWalletRow(sheet,d,"DANA","id.dana");
        addWalletRow(sheet,d,"OVO","ovo.id");
        addWalletRow(sheet,d,"ShopeePay","com.shopeepay.id");

        d.setContentView(sheet); d.show();
        Window w=d.getWindow();
        if(w!=null){
            w.setBackgroundDrawableResource(android.R.color.transparent);
            w.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            WindowManager.LayoutParams lp=w.getAttributes();
            lp.width=WindowManager.LayoutParams.MATCH_PARENT; lp.height=WindowManager.LayoutParams.WRAP_CONTENT; lp.gravity=Gravity.BOTTOM; lp.dimAmount=.42f; w.setAttributes(lp);
        }
    }

    private void addWalletRow(LinearLayout parent, Dialog dialog, String name, String pkg){
        LinearLayout row=new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.CENTER_VERTICAL); row.setPadding(dp(6),0,dp(8),0);
        ImageView icon=new ImageView(this); icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        try { icon.setImageDrawable(getPackageManager().getApplicationIcon(pkg)); } catch(Exception e){ icon.setBackground(roundRect(Color.rgb(245,245,245),22)); }
        LinearLayout.LayoutParams ilp=new LinearLayout.LayoutParams(dp(46),dp(46)); ilp.rightMargin=dp(16); row.addView(icon,ilp);
        TextView label=new TextView(this); label.setText(name); label.setTextSize(18); label.setTextColor(Color.WHITE); label.setGravity(Gravity.CENTER_VERTICAL); row.addView(label,new LinearLayout.LayoutParams(0,dp(68),1f));
        row.setOnClickListener(v->{ dialog.dismiss(); shareImageToWallet(name,pkg); });
        parent.addView(row,new LinearLayout.LayoutParams(-1,dp(68)));
        View divider=new View(this); divider.setBackgroundColor(Color.rgb(58,58,61)); LinearLayout.LayoutParams dlp=new LinearLayout.LayoutParams(-1,dp(1)); dlp.leftMargin=dp(68); parent.addView(divider,dlp);
    }

    private Intent buildShareIntent(String pkg){
        Intent send=new Intent(Intent.ACTION_SEND);
        send.setType("image/*");
        send.putExtra(Intent.EXTRA_STREAM, selectedImageUri);
        send.setClipData(ClipData.newRawUri("QRIS image", selectedImageUri));
        send.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        send.setPackage(pkg);
        return send;
    }

    private void shareImageToWallet(String name,String pkg){
        if(selectedImageUri==null){ Toast.makeText(this,"请先选择图片",Toast.LENGTH_SHORT).show(); return; }
        PackageManager pm=getPackageManager();
        try { pm.getApplicationInfo(pkg,0); } catch(Exception e){ Toast.makeText(this,name+" 未安装",Toast.LENGTH_SHORT).show(); return; }

        Intent send=buildShareIntent(pkg);
        List<ResolveInfo> matches=pm.queryIntentActivities(send, PackageManager.MATCH_DEFAULT_ONLY);
        if(matches!=null && !matches.isEmpty()){
            ResolveInfo best=pickBestShareReceiver(matches,name);
            if(best!=null && best.activityInfo!=null){
                send.setComponent(new ComponentName(best.activityInfo.packageName,best.activityInfo.name));
                try { startActivity(send); return; } catch(Exception ignored){}
            }
            try { startActivity(buildShareIntent(pkg)); return; } catch(Exception ignored){}
        }

        // Some versions register image opening instead of ACTION_SEND. Try ACTION_VIEW with the same URI grant.
        Intent view=new Intent(Intent.ACTION_VIEW);
        view.setDataAndType(selectedImageUri,"image/*");
        view.setClipData(ClipData.newRawUri("QRIS image",selectedImageUri));
        view.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        view.setPackage(pkg);
        if(view.resolveActivity(pm)!=null){ try { startActivity(view); return; } catch(Exception ignored){} }

        Toast.makeText(this,name+" 当前版本没有暴露图片分享接收入口",Toast.LENGTH_LONG).show();
    }

    private ResolveInfo pickBestShareReceiver(List<ResolveInfo> list,String walletName){
        ResolveInfo best=null; int bestScore=Integer.MIN_VALUE;
        for(ResolveInfo r:list){
            if(r.activityInfo==null || !r.activityInfo.exported) continue;
            String cls=r.activityInfo.name==null?"":r.activityInfo.name.toLowerCase(Locale.US);
            String label="";
            try { CharSequence cs=r.loadLabel(getPackageManager()); if(cs!=null) label=cs.toString().toLowerCase(Locale.US); } catch(Exception ignored){}
            String s=cls+" "+label;
            int score=0;
            if(s.contains("qris")) score+=100;
            if(s.contains("scan")) score+=80;
            if(s.contains("scanner")) score+=80;
            if(s.contains("qr")) score+=60;
            if(s.contains("share")) score+=50;
            if(s.contains("image")) score+=35;
            if(s.contains("gallery")) score+=30;
            if(s.contains("pay")) score+=20;
            if(label.contains(walletName.toLowerCase(Locale.US))) score+=5;
            if(score>bestScore){ bestScore=score; best=r; }
        }
        return best;
    }

    private GradientDrawable roundRect(int color,int radius){ GradientDrawable d=new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius)); return d; }
    private GradientDrawable topRoundRect(int color,int radius){ GradientDrawable d=new GradientDrawable(); d.setColor(color); float r=dp(radius); d.setCornerRadii(new float[]{r,r,r,r,0,0,0,0}); return d; }
    private int dp(int v){ return Math.round(v*getResources().getDisplayMetrics().density); }
}
