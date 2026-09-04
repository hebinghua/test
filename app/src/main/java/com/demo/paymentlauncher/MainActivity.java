package com.demo.paymentlauncher;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
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
        TextView sub=new TextView(this); sub.setText("选择一张二维码/支付图片，然后选择钱包 App"); sub.setTextSize(15); sub.setTextColor(Color.rgb(100,104,110)); LinearLayout.LayoutParams slp=new LinearLayout.LayoutParams(-1,-2); slp.topMargin=dp(8); root.addView(sub,slp);

        LinearLayout card=new LinearLayout(this); card.setOrientation(LinearLayout.VERTICAL); card.setPadding(dp(16),dp(16),dp(16),dp(16)); card.setBackground(roundRect(Color.WHITE,18)); LinearLayout.LayoutParams clp=new LinearLayout.LayoutParams(-1,-2); clp.topMargin=dp(26); root.addView(card,clp);
        imagePreview=new ImageView(this); imagePreview.setScaleType(ImageView.ScaleType.CENTER_CROP); imagePreview.setBackground(roundRect(Color.rgb(238,240,243),14)); card.addView(imagePreview,new LinearLayout.LayoutParams(-1,dp(260)));
        imageHint=new TextView(this); imageHint.setText("尚未选择图片"); imageHint.setGravity(Gravity.CENTER); imageHint.setTextColor(Color.rgb(125,130,136)); imageHint.setTextSize(15); card.addView(imageHint,new LinearLayout.LayoutParams(-1,dp(44)));
        Button upload=makePrimaryButton("上传图片"); upload.setOnClickListener(v->pickImage()); LinearLayout.LayoutParams ulp=new LinearLayout.LayoutParams(-1,dp(52)); ulp.topMargin=dp(10); card.addView(upload,ulp);
        payButton=makePrimaryButton("支付"); payButton.setEnabled(false); payButton.setAlpha(.45f); payButton.setOnClickListener(v->showWalletDialog()); LinearLayout.LayoutParams plp=new LinearLayout.LayoutParams(-1,dp(56)); plp.topMargin=dp(18); root.addView(payButton,plp);
        TextView note=new TextView(this); note.setText("Demo 仅负责拉起指定钱包 App，不执行真实支付。\nGoPay / ShopeePay / OVO / DANA"); note.setTextSize(13); note.setTextColor(Color.rgb(110,114,120)); note.setGravity(Gravity.CENTER); LinearLayout.LayoutParams nlp=new LinearLayout.LayoutParams(-1,-2); nlp.topMargin=dp(16); root.addView(note,nlp);
        return scroll;
    }

    private Button makePrimaryButton(String text){ Button b=new Button(this); b.setText(text); b.setAllCaps(false); b.setTextSize(16); b.setTextColor(Color.WHITE); b.setBackground(roundRect(Color.rgb(24,24,24),14)); return b; }
    private Button makeWalletButton(String name){ Button b=new Button(this); b.setText(name); b.setAllCaps(false); b.setTextSize(17); b.setTextColor(Color.rgb(25,25,25)); b.setGravity(Gravity.CENTER_VERTICAL|Gravity.START); b.setPadding(dp(20),0,dp(20),0); b.setBackground(roundRect(Color.rgb(245,246,248),14)); return b; }
    private void pickImage(){ Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT); i.addCategory(Intent.CATEGORY_OPENABLE); i.setType("image/*"); startActivityForResult(i,REQ_IMAGE); }
    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){ super.onActivityResult(requestCode,resultCode,data); if(requestCode==REQ_IMAGE&&resultCode==RESULT_OK&&data!=null&&data.getData()!=null){ selectedImageUri=data.getData(); imagePreview.setImageURI(selectedImageUri); imageHint.setText("图片已选择"); payButton.setEnabled(true); payButton.setAlpha(1f); } }
    private void showWalletDialog(){ final Dialog d=new Dialog(this); d.requestWindowFeature(Window.FEATURE_NO_TITLE); LinearLayout p=new LinearLayout(this); p.setOrientation(LinearLayout.VERTICAL); p.setPadding(dp(22),dp(22),dp(22),dp(22)); p.setBackground(roundRect(Color.WHITE,24)); TextView t=new TextView(this); t.setText("选择支付 App"); t.setTextSize(21); t.setTypeface(null,1); t.setTextColor(Color.rgb(20,20,20)); p.addView(t); TextView desc=new TextView(this); desc.setText("仅显示支持的指定应用"); desc.setTextSize(14); desc.setTextColor(Color.rgb(110,114,120)); LinearLayout.LayoutParams dl=new LinearLayout.LayoutParams(-1,-2); dl.topMargin=dp(6); dl.bottomMargin=dp(14); p.addView(desc,dl); addWallet(p,d,"GoPay","com.gojek.gopay"); addWallet(p,d,"ShopeePay","com.shopeepay.id"); addWallet(p,d,"OVO","ovo.id"); addWallet(p,d,"DANA","id.dana"); d.setContentView(p); d.show(); Window w=d.getWindow(); if(w!=null){ w.setBackgroundDrawableResource(android.R.color.transparent); WindowManager.LayoutParams lp=new WindowManager.LayoutParams(); lp.copyFrom(w.getAttributes()); lp.width=(int)(getResources().getDisplayMetrics().widthPixels*.90f); lp.height=WindowManager.LayoutParams.WRAP_CONTENT; lp.gravity=Gravity.CENTER; w.setAttributes(lp); } }
    private void addWallet(LinearLayout p,Dialog d,String name,String pkg){ Button b=makeWalletButton(name); b.setOnClickListener(v->{d.dismiss();launchPackage(name,pkg);}); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(58)); lp.topMargin=dp(10); p.addView(b,lp); }
    private void launchPackage(String name,String pkg){ PackageManager pm=getPackageManager(); Intent launch=pm.getLaunchIntentForPackage(pkg); if(launch!=null){launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);startActivity(launch);} else Toast.makeText(this,name+" 未安装",Toast.LENGTH_SHORT).show(); }
    private GradientDrawable roundRect(int color,int radius){ GradientDrawable d=new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius)); return d; }
    private int dp(int v){ return Math.round(v*getResources().getDisplayMetrics().density); }
}
