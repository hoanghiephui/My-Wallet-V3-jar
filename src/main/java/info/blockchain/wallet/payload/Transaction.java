package info.blockchain.wallet.payload;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import info.blockchain.wallet.multiaddr.MultiAddrFactory;

public class Transaction	{

    private String strData = null;
    private JSONObject objX = null;
    private HashMap<String,Long> fromLabelValuePair = null;
    private HashMap<String,Long> toLabelValuePair = null;

    public class xPut {
    	public long value;
    	public String addr;
    	public String addr_tag;
    }

	private long height = -1L;
	private String hash = null;
	private long time = -1L;
	private long result = 0L;
	private long fee = 0L;
	private String relayed_by = null;

    private ArrayList<xPut> inputs = null;
    private ArrayList<xPut> outputs = null;
    private HashMap<String,Long> totalValues = null;
    private HashMap<String,Long> inputValues = null;
    private HashMap<String,Long> outputValues = null;

    public Transaction(JSONObject jsonObj) {
    	objX = jsonObj;
    	inputs = new ArrayList<xPut>();
    	outputs = new ArrayList<xPut>();
    	totalValues = new HashMap<String,Long>();
    	inputValues = new HashMap<String,Long>();
    	outputValues = new HashMap<String,Long>();

		parse();
    }

    public String getHash() {
    	return hash;
    }

    public long getResult() {
    	return result;
    }

    public long getFee() {
    	return fee;
    }

    public long getHeight() {
    	return height;
    }

    public long getTime() {
    	return time;
    }

    public String getRelayedBy() {
    	return relayed_by;
    }

    public ArrayList<xPut> getInputs() {
    	return inputs;
    }

    public ArrayList<xPut> getOutputs() {
    	return outputs;
    }

    public HashMap<String,Long> getTotalValues() {
    	return totalValues;
    }

    public HashMap<String,Long> getInputValues() {
    	return inputValues;
    }

    public HashMap<String,Long> getOutputValues() {
    	return outputValues;
    }

    public void parse()	{
    	
    	
        try {
    		JSONObject tx = objX;
    		if(tx != null)	{
//    			Log.d(TAG, "Object OK");
    			if(tx.has("block_height"))	{
        			height = tx.getLong("block_height");
    			}
    			hash = tx.getString("hash");
    			time = tx.getLong("time");
    			relayed_by = tx.getString("relayed_by");
    			
    			long total_input = 0L;
    			long total_output = 0L;

    			long our_xput_value = 0L;

    			if(tx.has("inputs"))	{
        			JSONArray _inputs = tx.getJSONArray("inputs");
        			for(int j = 0; j < _inputs.length(); j++)	{
            			JSONObject _input = _inputs.getJSONObject(j);
            			JSONObject prev_out = _input.getJSONObject("prev_out");
            			boolean our_xput = false;
            			if(prev_out != null)	{
                			xPut input = new xPut();
                			if(prev_out.has("xpub"))	{
                				our_xput = true;
                			}
                			else if(prev_out.has("addr"))	{
                                input.addr = prev_out.getString("addr");

                                if(PayloadFactory.getInstance().get().containsLegacyAddress(input.addr))  {
                    				our_xput = true;
                                }
                			}

                			if(prev_out.has("addr_tag"))	{
                    			input.addr_tag = prev_out.getString("addr_tag");
                			}
                			if(prev_out.has("value"))	{
                    			input.value = prev_out.getLong("value");
                    			total_input += input.value;
                    			
                    			if(our_xput)	{
                    				our_xput_value -= input.value;
                    			}
                    			
                    			if(totalValues.get(input.addr) != null)	{
                    				totalValues.put(input.addr, totalValues.get(input.addr) - input.value);
                    			}
                    			else	{
                    				totalValues.put(input.addr, 0L - input.value);
                    			}
                    			
                    			if(inputValues.get(input.addr) != null)	{
                    				inputValues.put(input.addr, inputValues.get(input.addr) - input.value);
                    			}
                    			else	{
                    				inputValues.put(input.addr, 0L - input.value);
                    			}

                			}
                			inputs.add(input);
            			}
        			}
    			}

    			if(tx.has("out"))	{
        			JSONArray _outputs = tx.getJSONArray("out");
        			for(int j = 0; j < _outputs.length(); j++)	{
            			JSONObject _output = _outputs.getJSONObject(j);
            			boolean our_xput = false;
            			if(_output != null)	{
                			xPut output = new xPut();
                			if(_output.has("xpub"))	{
                				our_xput = true;
                			}
                			else if(_output.has("addr"))	{
                                output.addr = _output.getString("addr");

                                if(PayloadFactory.getInstance().get().containsLegacyAddress(output.addr))  {
                    				our_xput = true;
                                }
                			}

                			if(_output.has("addr_tag"))	{
                    			output.addr_tag = _output.getString("addr_tag");
                			}
                			if(_output.has("value"))	{
                    			output.value = _output.getLong("value");
                    			total_output += output.value;

                    			if(our_xput)	{
                    				our_xput_value += output.value;
                    			}

                    			if(totalValues.get(output.addr) != null)	{
                    				totalValues.put(output.addr, totalValues.get(output.addr) + output.value);
                    			}
                    			else	{
                    				totalValues.put(output.addr, output.value);
                    			}
                    			
                    			if(outputValues.get(output.addr) != null)	{
                    				outputValues.put(output.addr, outputValues.get(output.addr) - output.value);
                    			}
                    			else	{
                    				outputValues.put(output.addr, 0L - output.value);
                    			}

                			}
                			outputs.add(output);
            			}
        			}
    			}
    			
    			fee = Math.abs(total_input - total_output);
				result = our_xput_value;
    		}
    	} catch (JSONException je) {
    		je.printStackTrace();
    	}

    }

    public HashMap<String,Long> getFromLabelValuePair(String txDirection) {

        fromLabelValuePair = new HashMap<String,Long>();

        if (txDirection.equals(MultiAddrFactory.RECEIVED)) {//only 1 addr for receive
            fromLabelValuePair.put(getInputs().get(0).addr, getInputs().get(0).value);

        } else
            for (Transaction.xPut ip : getInputs()) {
                if (MultiAddrFactory.getInstance().isOwnHDAddress(ip.addr)) {

                    HashMap<String, String> xpub = MultiAddrFactory.getInstance().getAddress2Xpub();
                    Map<String, Integer> xpubAcc = PayloadFactory.getInstance().get().getXpub2Account();
                    int accIndex = xpubAcc.get(xpub.get(ip.addr));

                    List<Account> acc = PayloadFactory.getInstance().get().getHdWallet().getAccounts();
                    String accountLabel = acc.get(accIndex).getLabel();
                    if (acc != null) {
                        if (fromLabelValuePair.containsKey(acc.get(accIndex).getLabel())) {
                            long prevAmount = fromLabelValuePair.get(accountLabel)+acc.get(accIndex).amount;
                            fromLabelValuePair.put(accountLabel, prevAmount);
                        }else{
                            fromLabelValuePair.put(acc.get(accIndex).getLabel(), ip.value);
                        }
                    } else
                        fromLabelValuePair.put(ip.addr, ip.value);
                } else {
                    fromLabelValuePair.put(ip.addr, ip.value);
                }
            }

        return fromLabelValuePair;
    }

    public HashMap<String,Long> getToLabelValuePair(String txDirection, double amount) {

        toLabelValuePair = new HashMap<String,Long>();

        for (Transaction.xPut ip : getOutputs()) {
            if (MultiAddrFactory.getInstance().isOwnHDAddress(ip.addr)) {

                if (txDirection.equals(MultiAddrFactory.SENT))
                    continue;//change addr
                if (txDirection.equals(MultiAddrFactory.MOVED) && amount != (double) ip.value)
                    continue;//change addr

                HashMap<String, String> xpub = MultiAddrFactory.getInstance().getAddress2Xpub();
                Map<String, Integer> xpubAcc = PayloadFactory.getInstance().get().getXpub2Account();
                int accIndex = 0;
                if(!xpubAcc.isEmpty())accIndex = xpubAcc.get(xpub.get(ip.addr));

                List<Account> acc = PayloadFactory.getInstance().get().getHdWallet().getAccounts();
                String accountLabel = acc.get(accIndex).getLabel();
                if (acc != null) {
                    if(fromLabelValuePair.containsKey(acc.get(accIndex).getLabel()))continue;

                    if (toLabelValuePair.containsKey(acc.get(accIndex).getLabel())) {
                        long prevAmount = toLabelValuePair.get(accountLabel)+acc.get(accIndex).amount;
                        toLabelValuePair.put(accountLabel, prevAmount);
                    }else{
                        toLabelValuePair.put(acc.get(accIndex).getLabel(), ip.value);
                    }
                } else {
                    if(!fromLabelValuePair.containsKey(ip.addr))
                        toLabelValuePair.put(ip.addr, ip.value);
                }

            } else {
                if(!fromLabelValuePair.containsKey(ip.addr))
                    toLabelValuePair.put(ip.addr, ip.value);
            }
        }

        return toLabelValuePair;
    }
}