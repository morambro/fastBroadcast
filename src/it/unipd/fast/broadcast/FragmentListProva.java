package it.unipd.fast.broadcast;


import it.unipd.fast.broadcast.MainActivity.OnPeerSelectedCallback;

import java.util.ArrayList;
import java.util.List;

import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class FragmentListProva extends ListFragment {
	protected final String TAG = "it.unipd.fast.broadcast";

	private List<WifiP2pDevice> peers;
	private OnPeerSelectedCallback callback;
	
	public FragmentListProva(List<WifiP2pDevice> peers,OnPeerSelectedCallback callback) {
		this.peers = peers;
		if(this.peers == null) this.peers = new ArrayList<WifiP2pDevice>();
		this.callback = callback;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		this.setListAdapter(new ArrayAdapter<WifiP2pDevice>(getActivity(),android.R.layout.simple_list_item_1,peers));
		this.getListView().setOnItemClickListener(new OnItemClickListener() {
			
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				WifiP2pDevice selected_device = (WifiP2pDevice)parent.getItemAtPosition(position);
				if(selected_device != null){
					Log.d(TAG, this.getClass().getSimpleName()+": "+selected_device.deviceAddress);
					callback.doConnection(selected_device);
				}
			}
			
		});
		
	}

	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.list_fragment_layout, container, false);
	}


	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Log.i("FragmentList", "Item clicked: " + id);
	}

}
