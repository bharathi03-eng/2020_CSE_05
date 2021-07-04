package pragma.embd.androidbasedassitanceseniorcitizens;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class RegisterNumberScreenActivity extends Activity {

    EditText et_phone_number;
    Button btn_update;

    private static final int MY_PERMISSIONS_REQUEST_NETWORK_PROVIDER =1 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registernumberscreen);

        et_phone_number = (EditText) findViewById(R.id.et_phone_number);
        btn_update = (Button) findViewById(R.id.btn_update);

        et_phone_number.setText(Constants.str_phoneno);

        requestForPermissions();

        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (et_phone_number.getText().toString().trim().equals("") ||
                        et_phone_number.getText().toString().trim().length() != 10) {
                    et_phone_number.setError("Enter 10 digit Phone No");
                }
                else{

                    Constants.str_phoneno = et_phone_number.getText().toString().trim();

                    Toast.makeText(getApplicationContext(), "Senior Citizen number updated successfully",
                            Toast.LENGTH_SHORT).show();

                    finish();
                    Intent mainscreen = new Intent(getApplicationContext(), MainScreenActivity.class);
                    startActivity(mainscreen);
                }
            }
        });
    }

    void requestForPermissions(){

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(getApplicationContext(), "in first if",
                    Toast.LENGTH_LONG).show();
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.SEND_SMS)) {

                Toast.makeText(getApplicationContext(), "in second if",
                        Toast.LENGTH_LONG).show();
            } else {

                // permission is already granted
            /*    Toast.makeText(getApplicationContext(), "in else",
                        Toast.LENGTH_LONG).show();*/
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS},
                        MY_PERMISSIONS_REQUEST_NETWORK_PROVIDER);
            }


        } else {
           /* mlocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0,
                    0, mlocListener);*/

        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        Intent mainscreen = new Intent(getApplicationContext(), MainScreenActivity.class);
        startActivity(mainscreen);
    }
}
