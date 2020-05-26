package com.example.cryptocurrencyconverter;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.text.DecimalFormat;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

public class MainActivity extends AppCompatActivity {
    static double bitcoinToDollarRatio = 9750.79;
    double dogecoinToDollarRatio = 0.002794;
    //Used internet to hardcode the exchange rates

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //To extract the the conversion rate, we will use the new file
        //process.execute();
    }

    /** Called when the user taps the "Refresh Exchange Rate" */
    public void refreshRate(View view){
        //refreshes the currency rate and shows the accurate resulting currency value
        System.out.println(bitcoinToDollarRatio);
        //APIHandler.apiCall();
        System.out.println(bitcoinToDollarRatio);
        EditText enteredNumber = (EditText) findViewById(R.id.enterNumber);
        EditText returnNumber = (EditText) findViewById(R.id.resultNumber);
        RadioGroup enteredGroup = (RadioGroup) findViewById(R.id.enterButtons);
        RadioGroup resultGroup = (RadioGroup) findViewById(R.id.resultButton);
        String enteredString = enteredNumber.getText().toString();
        String returnString = returnNumber.getText().toString();
        // If no input number is provided, display the string "Invalid Value"
        if (enteredString.matches("") && returnString.matches("")) {
            Toast.makeText(this, "Invalid Value", Toast.LENGTH_SHORT).show();
            return;
        }
        double returnVal = 0;
        // if input number is typed in the "from input box"
        if (!enteredString.equals("")) {
            double enteredDouble = Double.parseDouble(enteredString);
            if (enteredGroup.getCheckedRadioButtonId() == R.id.enterDOGE) {
                if (resultGroup.getCheckedRadioButtonId() == R.id.resultDOGE) {
                    returnVal = enteredDouble;
                } else if (resultGroup.getCheckedRadioButtonId() == R.id.resultUSD) {
                    returnVal = enteredDouble * dogecoinToDollarRatio;
                } else if (resultGroup.getCheckedRadioButtonId() == R.id.resultBTC) {
                    returnVal = enteredDouble * dogecoinToDollarRatio / bitcoinToDollarRatio;
                } else {
                    Toast.makeText(this, "Invalid Value", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else if (enteredGroup.getCheckedRadioButtonId() == R.id.enterUSD) {
                if (resultGroup.getCheckedRadioButtonId() == R.id.resultDOGE) {
                    returnVal = enteredDouble / dogecoinToDollarRatio;
                } else if (resultGroup.getCheckedRadioButtonId() == R.id.resultUSD) {
                    returnVal = enteredDouble;
                } else if (resultGroup.getCheckedRadioButtonId() == R.id.resultBTC) {
                    returnVal = enteredDouble / bitcoinToDollarRatio;
                } else {
                    Toast.makeText(this, "Invalid Value", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else if (enteredGroup.getCheckedRadioButtonId() == R.id.enterBTC) {
                if (resultGroup.getCheckedRadioButtonId() == R.id.resultDOGE) {
                    returnVal = enteredDouble * bitcoinToDollarRatio / dogecoinToDollarRatio;
                } else if (resultGroup.getCheckedRadioButtonId() == R.id.resultUSD) {
                    returnVal = enteredDouble * bitcoinToDollarRatio;
                } else if (resultGroup.getCheckedRadioButtonId() == R.id.resultBTC) {
                    returnVal = enteredDouble;
                } else {
                    Toast.makeText(this, "Invalid Value", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                Toast.makeText(this, "Invalid Value", Toast.LENGTH_SHORT).show();
                return;
            }
            //We added decimal to cover exhance rate conversion between small numbers till 10 decimal digits
            DecimalFormat df = new DecimalFormat("0.0000000000");
              //Now we will set the text to the returning value
            returnNumber.setText(df.format(returnVal));
        } else if (!returnString.equals("")) {
            double returnDouble = Double.parseDouble(returnString);
            if (resultGroup.getCheckedRadioButtonId() == R.id.resultDOGE) {
                if (enteredGroup.getCheckedRadioButtonId() == R.id.enterDOGE) {
                    returnVal = returnDouble;
                } else if (enteredGroup.getCheckedRadioButtonId() == R.id.enterUSD) {
                    returnVal = returnDouble * dogecoinToDollarRatio;
                } else if (enteredGroup.getCheckedRadioButtonId() == R.id.enterBTC) {
                    returnVal = returnDouble * dogecoinToDollarRatio / bitcoinToDollarRatio;
                } else {
                    Toast.makeText(this, "Invalid Value", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else if (resultGroup.getCheckedRadioButtonId() == R.id.resultUSD) {
                if (enteredGroup.getCheckedRadioButtonId() == R.id.enterDOGE) {
                    returnVal = returnDouble / dogecoinToDollarRatio;
                } else if (enteredGroup.getCheckedRadioButtonId() == R.id.enterUSD) {
                    returnVal = returnDouble;
                } else if (enteredGroup.getCheckedRadioButtonId() == R.id.enterBTC) {
                    returnVal = returnDouble / bitcoinToDollarRatio;
                } else {
                    Toast.makeText(this, "Invalid Value", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else if (resultGroup.getCheckedRadioButtonId() == R.id.resultBTC) {
                if (enteredGroup.getCheckedRadioButtonId() == R.id.enterDOGE) {
                    returnVal = returnDouble * bitcoinToDollarRatio / dogecoinToDollarRatio;
                } else if (enteredGroup.getCheckedRadioButtonId() == R.id.enterUSD) {
                    returnVal = returnDouble * bitcoinToDollarRatio;
                } else if (enteredGroup.getCheckedRadioButtonId() == R.id.enterBTC) {
                    returnVal = returnDouble;
                } else {
                    Toast.makeText(this, "Invalid Value", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                Toast.makeText(this, "Invalid Value", Toast.LENGTH_SHORT).show();
                return;
            }
            //We added decimal to cover exhance rate conversion between small numbers till 10 decimal digits
            DecimalFormat df = new DecimalFormat("0.0000000000");
            //Now we will set the text to the returning value
            enteredNumber.setText(df.format(returnVal));
        }
    }
}
