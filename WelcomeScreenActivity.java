package pragma.embd.androidbasedassitanceseniorcitizens;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Window;

public class WelcomeScreenActivity extends Activity {

    Cursor cursor = null;
    String ans = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.welcome_screen);



        Thread timer = new Thread() {
            @Override
            public void run() {
                try {

                    sleep(3000);

                } catch (InterruptedException e) {
                    // Toast.makeText(getApplicationContext(), "err : " +
                    // e.getMessage(), Toast.LENGTH_SHORT).show();
                } finally {

                    finish();
                    Intent mainScreen = new Intent(getApplicationContext(), LoginScreenActivity.class);
                    startActivity(mainScreen);


                }
            }
        };
        timer.start();

    }





    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();
        finish();
    }

}