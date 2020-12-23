package org.imtiezfares.deeplyfe.tpvig;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class MainActivity extends AppCompatActivity implements ValueEventListener {

    private EditText text,key,result;
    private TextView txt_msg;
    private EditText ed_msg;
    private RadioButton rd_write,rd_pause;
    private CheckBox encrypt,decrypt;
    private Button exe,read,write,send,copy;
    static final int max = 100;

    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance("https://tpvig-3e69b-default-rtdb.firebaseio.com/");
    DatabaseReference mRootReference= firebaseDatabase.getReference();
    DatabaseReference mHeadingReference = mRootReference.child("heading");
    DatabaseReference mFontColorReference = mRootReference.child("fontcolor");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txt_msg=(TextView)findViewById(R.id.txt_msg);
        text = (EditText)findViewById(R.id.text) ;
        key = (EditText)findViewById(R.id.keyphrase) ;
        ed_msg = (EditText)findViewById(R.id.ed_msg) ;
        result = (EditText)findViewById(R.id.answer);
        encrypt = (CheckBox)findViewById(R.id.encrypt);
        decrypt = (CheckBox)findViewById(R.id.decrypt);
        exe = (Button)findViewById(R.id.runButton);
        read = (Button)findViewById(R.id.btn_read);
        write = (Button)findViewById(R.id.btn_write);
        send = (Button)findViewById(R.id.btn_send);
        //copy = (Button)findViewById(R.id.btn_copy);
        rd_pause = (RadioButton)findViewById(R.id.rb_red);
        rd_write = (RadioButton)findViewById(R.id.rb_bleu);




        read.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    FileInputStream fileIn=openFileInput("TpVeg.txt");
                    InputStreamReader InputRead= new InputStreamReader(fileIn);
                    char[] inputBuffer= new char[max];
                    String s="";
                    int charRead;

                    while ((charRead=InputRead.read(inputBuffer))>0) {
                        String readstring=String.copyValueOf(inputBuffer,0,charRead);
                        s +=readstring;
                    }
                    InputRead.close();
                    Toast.makeText(getBaseContext(), s,Toast.LENGTH_LONG).show();
                    text.setText(s);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        write.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    FileOutputStream fileout;
                    fileout = openFileOutput("TpVeg.txt", MODE_PRIVATE);
                    OutputStreamWriter outputWriter=new OutputStreamWriter(fileout);
                    outputWriter.write(result.getText().toString());
                    outputWriter.close();

                    Toast.makeText(getBaseContext(), "Enregistrer",
                            Toast.LENGTH_SHORT).show();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public void submitHeading(View view){
        String heading = ed_msg.getText().toString();
        mHeadingReference.setValue(heading);
        ed_msg.setText("");
    }

    public void onRadioButtonClicked(View view){
        switch (view.getId()){
            case R.id.rb_red:
                mFontColorReference.setValue("red");
                break;
            case R.id.rb_bleu:
                mFontColorReference.setValue("blue");
                break;
        }
    }

    public void encryptOrDecryptUsingKeyphraseOnClick(View view) {

        String decalage;

        if (view.getId() == R.id.runButton) {

            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }

            decalage = null;
            String textString = this.text.getText().toString();
            String keyString = this.key.getText().toString();

            if (!InputTest(textString, keyString)) {
                if (encrypt.isChecked()) {
                    decalage = this.encrypt(textString, keyString);
                }
                if (decrypt.isChecked()) {
                    decalage = this.decryptAlgorithm(textString, keyString);
                }
                this.result.setText(decalage);
            }
        }
    } 


    private String encrypt(String text, String key) {

        key = key.toUpperCase();
        StringBuilder sb = new StringBuilder(127);

        for (int i = 0, j = 0; i < text.length(); i++) {

            char toUpper= text.toUpperCase().charAt(i);
            char textChar = text.charAt(i);

            if (Character.isAlphabetic(textChar)) {
                if (Character.isUpperCase(textChar)) {
                    sb.append((char)((toUpper+ key.charAt(j)) % 26 + 65));
                    ++j;
                    j %= key.length();
                } else {
                    sb.append(Character.toLowerCase((char)((toUpper+ key.charAt(j)) % 26 + 65)));
                    ++j;
                    j %= key.length();
                }
            } else {
                sb.append(textChar);
            }
        }
        return sb.toString();
    }


    private String decryptAlgorithm(String text, String key) {

        key = key.toUpperCase();
        StringBuilder sb = new StringBuilder(127);

        for (int i = 0, j = 0; i < text.length(); i++) {

            char toUpper= text.toUpperCase().charAt(i);
            char textChar = text.charAt(i);

            if (Character.isAlphabetic(textChar)) {
                if (Character.isUpperCase(textChar)) {
                    sb.append((char)((toUpper- key.charAt(j) + 26 ) % 26 + 65));
                    ++j;
                    j %= key.length();
                } else {
                    sb.append(Character.toLowerCase((char)((toUpper- key.charAt(j) + 26) % 26 + 65)));
                    ++j;
                    j %= key.length();
                }
            } else {
                sb.append(textChar);
            }
        }
        return sb.toString();
    }


    private boolean InputTest(String text, String key) {

        boolean a = false;

        if (!text.matches(".*[a-zA-Z]+.*")) {
            this.text.setError("text vide");
            a = true;
        }

        if (null == key || key.isEmpty()) {
            this.key.setError("key vide");
            a = true;
        }

        
        boolean s = this.keyTest(key);
        if (!s) {
            this.key.setError("key inse");
            a = true;
        }
        return a;
    }



    private boolean keyTest(String key) {

        boolean s = true;

        for(int z = 0; z < key.length(); ++z) {
            char c = key.charAt(z);
            if (!Character.isAlphabetic(c)) {
                s = false;
                break;
            }
        }
        return s;
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        if (dataSnapshot.getValue(String.class)!=null){
            String key = dataSnapshot.getKey();
            if (key.equals("heading")){
                String heading = dataSnapshot.getValue(String.class);
                txt_msg.setText(heading);
            }else if(key.equals("fontcolor")) {
                String color = dataSnapshot.getValue(String.class);
                if (color.equals("red")){
                    rd_write.setChecked(true);
                    rd_pause.setChecked(false);
                }else if (color.equals("blue")){
                    rd_write.setChecked(false);
                    rd_pause.setChecked(true);
                }
            }
        }
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        mHeadingReference.addValueEventListener(this);
        mFontColorReference.addValueEventListener(this);
    }
}
