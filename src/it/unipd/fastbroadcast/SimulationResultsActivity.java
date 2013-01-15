package it.unipd.fastbroadcast;

import it.unipd.testbase.R;
import it.unipd.vanets.framework.helper.LogPrinter;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class SimulationResultsActivity extends Activity {
	
	public static boolean isOpened = false;
	
	private TextView results;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		isOpened = true;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_results);
		
		results  = (TextView)this.findViewById(R.id.textView1);
		results.setText(LogPrinter.getInstance().getResults());
	}
	
	protected void onDestroy() {
		LogPrinter.getInstance().reset();
		super.onDestroy();
		isOpened = false;
	}
}
