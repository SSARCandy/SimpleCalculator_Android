package com.app.ssarcandy.simplecal;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MyActivity extends Activity {

    char lastBinaryOp;
    double total;
    boolean shiftDown = false;

    private TextView tbDisplay;
    private TextView tbDisplayEquation;
    private Button[] btnInput;
    public void initCompoment() {
        tbDisplayEquation = (TextView) findViewById(R.id.tbDisplayEquation);
        tbDisplay = (TextView) findViewById(R.id.tbDisplay);
        btnInput = new Button[]{
                (Button) findViewById(R.id.btnEqual),
                (Button) findViewById(R.id.btn0),
                (Button) findViewById(R.id.btn1),
                (Button) findViewById(R.id.btn2),
                (Button) findViewById(R.id.btn3),
                (Button) findViewById(R.id.btn4),
                (Button) findViewById(R.id.btn5),
                (Button) findViewById(R.id.btn6),
                (Button) findViewById(R.id.btn7),
                (Button) findViewById(R.id.btn8),
                (Button) findViewById(R.id.btn9),
                (Button) findViewById(R.id.btnAdd),
                (Button) findViewById(R.id.btnDivide),
                (Button) findViewById(R.id.btnMinus),
                (Button) findViewById(R.id.btnMul),
                (Button) findViewById(R.id.btnPeriod),
                (Button) findViewById(R.id.btnClear),
                (Button) findViewById(R.id.btnBackspace)
        };
        for(Button b : btnInput)
            b.setOnClickListener(btnClick);
    }

    public enum State
    {
        ZERO_FIRST,  //第一個運算元為零的狀態
        ZERO_INTERMED,  //第二個運算元為零的狀態
        OPERAND_FIRST_NODEC,  //第一個運算元無小數點的狀態
        OPERAND_FIRST_DEC,  //第一個運算元有小數點的狀態
        OPERAND_INTERMED_NODEC, //第二個運算元無小數點的狀態
        OPERAND_INTERMED_DEC, //第二個運算元有小數點的狀態
        AFTER_EQUALS, //輸入等號後的狀態
        OPERATOR  //處理運算子的狀態
    }
    State state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        initCompoment();
        initializeState();
    }

    View.OnClickListener btnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Button b = (Button) v;
//            double input = Double.parseDouble(tbDisplay.getText().toString());
//            if (isNumeric == false)
//                initializeState();

//            string sBC = ((Button)e.Source).Content.ToString();  //取出按鈕上的字串
            switch (b.getId())
            {
                case R.id.btn0:
                case R.id.btn1:
                case R.id.btn2:
                case R.id.btn3:
                case R.id.btn4:
                case R.id.btn5:
                case R.id.btn6:
                case R.id.btn7:
                case R.id.btn8:
                case R.id.btn9:
                    handleDigit(Integer.parseInt(b.getText().toString()));
                    break;
                case R.id.btnAdd:
                case R.id.btnMinus:
                case R.id.btnMul:
                case R.id.btnDivide:
                //case "%":
                    handleBinaryOp(b.getText().charAt(0));
                    break;
                case R.id.btnClear:
                    handleClear();
                    break;
                case R.id.btnEqual:
                    handleEquals();
                    break;
                case R.id.btnPeriod:
                    handleDecimal();
                    break;
                case R.id.btnBackspace:
                    handleBackspace();
                    break;
    /*            case "x^y":
                    handleBinaryOp('^');
                    break;
                case "±":
                    handleNegate();
                    break;
                case "CE":
                    handleCE();
                    break;
                case "√":
                    handleSqrt();
                    break;
            */
            }
        }
    };

    private void handleClear()
    {
        initializeState();
    }

    //處理退位鍵的輸入
    private void handleBackspace()
    {
        if (state != State.AFTER_EQUALS) {
            int length = tbDisplay.getText().length();
            if (length > 2) {
                if(tbDisplay.getText().charAt(length-1) != '.')
                    tbDisplay.setText(tbDisplay.getText().toString().substring(0, length - 1) );
                else
                    tbDisplay.setText(tbDisplay.getText().toString().substring(0, length - 2) + ".");
            }
            else
            {
                tbDisplay.setText("0.");
                if (state == State.OPERAND_INTERMED_NODEC || state == State.OPERAND_INTERMED_DEC || state == State.ZERO_INTERMED)
                    state = State.OPERATOR;
                if (state == State.OPERAND_FIRST_DEC || state == State.OPERAND_FIRST_NODEC || state == State.AFTER_EQUALS)
                    state = State.ZERO_FIRST;
            }
        }
    }
    //處理小數點的輸入
    private void handleDecimal()
    {
        switch (state)
        {
            case ZERO_FIRST:
                state = State.OPERAND_FIRST_DEC;
                tbDisplay.setText("0.");
                break;

            case OPERAND_FIRST_NODEC:
                state = State.OPERAND_FIRST_DEC;
                break;

            case OPERAND_INTERMED_NODEC:
                state = State.OPERAND_INTERMED_DEC;
                break;

            case AFTER_EQUALS:
                state = State.OPERAND_FIRST_DEC;
                tbDisplay.setText("0.");
                break;

            case OPERATOR:
                state = State.OPERAND_INTERMED_DEC;
                tbDisplay.setText("0.");
                break;

            case ZERO_INTERMED:
                state = State.OPERAND_INTERMED_DEC;
                break;
        }
    }
    //處理等號的輸入
    private void handleEquals()
    {
        switch (state)
        {
            case OPERAND_INTERMED_NODEC:
            case OPERAND_INTERMED_DEC:
            case ZERO_INTERMED:
                updateTotal();
                if (tbDisplay.getText().charAt(tbDisplay.getText().length() - 1) != '.')
                {
                    state = State.OPERAND_FIRST_DEC;
                    break;
                }
                state = State.OPERAND_FIRST_NODEC;
                break;

            default:
                return;
        }
        state = State.AFTER_EQUALS;
        tbDisplayEquation.setText("");
    }
    //處理數字的輸入
    private void handleDigit(int i)
    {
        if (tbDisplay.getText().length() > 8 && state != State.OPERATOR)
            return;
        switch (state)
        {
            case ZERO_FIRST:
                if (i > 0)
                {
                    tbDisplay.setText(String.valueOf(i) + ".");
                    state = State.OPERAND_FIRST_NODEC;
                }
                break;

            case OPERAND_FIRST_NODEC:
                tbDisplay.setText(tbDisplay.getText().toString().substring(0, tbDisplay.getText().length() - 1) + String.valueOf(i) + ".");
                break;

            case OPERAND_FIRST_DEC:
                tbDisplay.setText(tbDisplay.getText() + String.valueOf(i));
                break;

            case OPERAND_INTERMED_NODEC:
                tbDisplay.setText(tbDisplay.getText().toString().substring(0, tbDisplay.getText().length() - 1) + String.valueOf(i) + ".");
                break;

            case OPERAND_INTERMED_DEC:
                tbDisplay.setText(tbDisplay.getText() + String.valueOf(i));
                break;

            case AFTER_EQUALS:
                if (i != 0)
                {
                    state = State.OPERAND_FIRST_NODEC;
                    tbDisplay.setText(String.valueOf(i) + ".");
                    break;
                }
                state = State.ZERO_FIRST;
                tbDisplay.setText("0.");
                break;

            case OPERATOR:
                if (i != 0)
                {
                    state = State.OPERAND_INTERMED_NODEC;
                    tbDisplay.setText(String.valueOf(i) + ".");
                    break;
                }
                state = State.ZERO_INTERMED;
                tbDisplay.setText("0.");
                break;
        }
    }
    //處理運算子輸入
    private void handleBinaryOp(char op)
    {
        switch (state)
        {
            case OPERAND_FIRST_NODEC:
            case OPERAND_FIRST_DEC:
            case AFTER_EQUALS:
                state = State.OPERATOR;
                total = Double.parseDouble(tbDisplay.getText().toString());
                break;

            case OPERAND_INTERMED_NODEC:
            case OPERAND_INTERMED_DEC:
                state = State.OPERATOR;
                updateTotal();
                break;
        }
        tbDisplayEquation.setText(String.valueOf(total) + " " + String.valueOf(op));
        lastBinaryOp = op;
    }

    private void updateTotal()
    {
        boolean exception = false;
        double num = Double.parseDouble(tbDisplay.getText().toString());
        //         MessageBox.Show(num.ToString());
        switch (lastBinaryOp)
        {
            case '*':
                total *= num;
                break;

            case '+':
                total += num;
                break;

            case '-':
                total -= num;
                break;

            case '/':
                if (num == 0)
                {
                    exception = true;
           //         tbDisplay.setText("不要除以零 QAQ");
                }
                else
                    total /= num;
                break;

            case '%':
                total %= num;
                break;

            case '^':
                total = Math.pow(total, num);
                break;
        }
        if (!exception)
        {
            tbDisplay.setText(String.valueOf(total));
            if(total == (int) total)
                tbDisplay.setText(String.valueOf(total).substring(0, tbDisplay.getText().length()-2));

       //     if (tbDisplay.getText().length() > 8)
        //        tbDisplay.setText(total.ToString("E");

            if (tbDisplay.getText().toString().indexOf('.') == -1)  //如果計算結果沒有小數點，就在最後一位補上。
            {
                tbDisplay.setText(tbDisplay.getText() + ".");
            }
        }

    }

    //初始設定
    private void initializeState()
    {
        state = State.ZERO_FIRST;  //初始狀態:第一個運算元為零的狀態
        total = 0.0;  //初始計算值(實數)
        tbDisplay.setText("0.");  //預設顯示「0.」
        tbDisplayEquation.setText("");
    }


















    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
