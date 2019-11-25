package com.vipassistant.mobile.demo;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

	private EditText num1, num2, operation;
	private TextView result;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		/* Stuff that comes default in MainActivity when initializing an Android Project */
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_main);
//		Toolbar toolbar = findViewById(R.id.toolbar);
//		setSupportActionBar(toolbar);
//
//		FloatingActionButton fab = findViewById(R.id.fab);
//		fab.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View view) {
//				Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//						.setAction("Action", null).show();
//			}
//		});

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		this.num1 = findViewById(R.id.num1);
		this.num2 = findViewById(R.id.num2);
		this.operation = findViewById(R.id.operation);
		this.result = findViewById(R.id.result);
	}

	public void calculateOnClick(View v) {
		Double a = Double.parseDouble(this.num1.getText().toString());
		Double b = Double.parseDouble(this.num2.getText().toString());
		String op = this.operation.getText().toString();

		this.num1.setText("");
		this.num2.setText("");
		this.operation.setText("");

		switch (op) {
			case "+":
				this.result.setText(String.format("Result = %.2f", a + b));
				break;
			case "-":
				this.result.setText(String.format("Result = %.2f", a - b));
				break;
			case "*":
				this.result.setText(String.format("Result = %.2f", a * b));
				break;
			case "/":
				this.result.setText(String.format("Result = %.2f", a / b));
				break;
			default:
				this.result.setText("Please only enter one of the following ops: +, -, *, /");
				break;
		}
	}

}
