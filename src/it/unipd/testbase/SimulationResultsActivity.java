package it.unipd.testbase;

import it.unipd.testbase.helper.LogPrinter;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class SimulationResultsActivity extends Activity {
	
	private TextView results;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_results);
		
		results  = (TextView)this.findViewById(R.id.textView1);
		results.setText(LogPrinter.getInstance().getResults());
	}
}
